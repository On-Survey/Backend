package OneQ.OnSurvey.domain.survey.service.command;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.MemberErrorCode;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.question.service.QuestionQueryService;
import OneQ.OnSurvey.domain.discount.entity.DiscountCode;
import OneQ.OnSurvey.domain.discount.service.DiscountCodeQueryService;
import OneQ.OnSurvey.global.promotion.application.PromotionTierResolver;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.entity.Screening;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyOwnerChangeDto;
import OneQ.OnSurvey.domain.survey.model.request.FreeSurveyFormRequest;
import OneQ.OnSurvey.domain.survey.model.request.SurveyFormCreateRequest;
import OneQ.OnSurvey.domain.survey.model.request.SurveyFormRequest;
import OneQ.OnSurvey.domain.survey.model.response.InterestResponse;
import OneQ.OnSurvey.domain.survey.model.response.ScreeningResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;
import OneQ.OnSurvey.domain.survey.repository.SurveyRepository;
import OneQ.OnSurvey.domain.survey.repository.screening.ScreeningRepository;
import OneQ.OnSurvey.domain.survey.repository.surveyInfo.SurveyInfoRepository;
import OneQ.OnSurvey.domain.survey.service.SurveyGlobalStatsService;
import OneQ.OnSurvey.domain.survey.service.refund.SurveyRefundPolicy;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.common.exception.ErrorCode;
import OneQ.OnSurvey.global.common.util.AuthorizationUtils;
import OneQ.OnSurvey.global.infra.discord.notifier.AlertNotifier;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveySubmittedAlert;
import OneQ.OnSurvey.global.infra.redis.RedisAgent;
import OneQ.OnSurvey.global.infra.transaction.AfterCommitExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static OneQ.OnSurvey.domain.survey.model.SurveyStatus.REFUNDED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SurveyCommandService implements SurveyCommand {

    private final SurveyRepository surveyRepository;
    private final ScreeningRepository screeningRepository;
    private final SurveyInfoRepository surveyInfoRepository;
    private final MemberRepository memberRepository;
    private final SurveyRefundPolicy surveyRefundPolicy;
    private final SurveyGlobalStatsService surveyGlobalStatsService;
    private final RedisAgent redisAgent;
    private final QuestionQueryService questionQueryService;
    private final PromotionTierResolver promotionTierResolver;
    private final DiscountCodeQueryService discountCodeQueryService;

    private final AlertNotifier alertNotifier;
    private final AfterCommitExecutor afterCommitExecutor;

    @Value("${redis.survey-key-prefix.potential-count}")
    private String potentialKey;
    @Value("${redis.survey-key-prefix.completed-count}")
    private String completedKey;
    @Value("${redis.survey-key-prefix.due-count}")
    private String dueCountKey;
    @Value("${redis.survey-key-prefix.creator-userkey}")
    private String creatorKey;

    @Override
    public SurveyFormResponse upsertSurvey(Long memberId, Long surveyId, SurveyFormCreateRequest request){

        Survey survey;
        if (surveyId == null) {
            survey = Survey.of(
                    memberId,
                    request.title(),
                    request.description()
            );
            survey = surveyRepository.save(survey);
            log.info("[SURVEY:COMMAND:upsertSurvey] 설문 생성 완료 - surveyId={}", survey.getId());
        } else {
            survey = surveyRepository.getSurveyById(surveyId)
                    .orElseThrow(() -> {
                        log.warn("[SURVEY:COMMAND:upsertSurvey] 설문이 존재하지 않음 - surveyId={}, memberId={}", surveyId, memberId);
                        throw new CustomException(SurveyErrorCode.SURVEY_NOT_FOUND);
                    });

            if (AuthorizationUtils.validateOwnershipOrAdmin(survey.getMemberId(), memberId)) {
                log.warn("[SURVEY:COMMAND:upsertSurvey] 설문 수정 권한 없음 - surveyId={}, memberId={}", surveyId, memberId);
                throw new CustomException(SurveyErrorCode.SURVEY_FORBIDDEN);
            }
            if (survey.getTitle().equals(request.title())
                && survey.getDescription().equals(request.description())
            ) {
                return SurveyFormResponse.fromEntity(survey);
            }

            survey.updateSurvey(
                    request.title(),
                    request.description(),
                    survey.getDeadline(),
                    survey.getTotalCoin()
            );
            survey = surveyRepository.save(survey);
            log.info("[SURVEY:COMMAND:upsertSurvey] 설문 수정 완료 - surveyId={}", surveyId);
        }

        return SurveyFormResponse.fromEntity(survey);
    }

    @Override
    public SurveyFormResponse submitSurvey(Long userKey, Long surveyId, SurveyFormRequest request) {
        Survey survey = getSurvey(surveyId);
        Member member = validateMember(userKey);

        Set<AgeRange> ages = (request.ages() == null) ? Set.of() : new HashSet<>(request.ages());

        // 할인 코드 저장 (존재 여부만 확인 후 ID 기록)
        Long discountCodeId = null;
        if (request.discountCode() != null && !request.discountCode().isBlank()) {
            DiscountCode discountCode = discountCodeQueryService.getByCode(request.discountCode());
            discountCodeId = discountCode.getId();
            log.info("[SurveySubmit] 할인 코드 저장 - surveyId={}, org={}", surveyId, discountCode.getOrganizationName());
        }

        survey.updateSurvey(survey.getTitle(), survey.getDescription(), request.deadline(), request.totalCoin());

        int questionCount = questionQueryService.countQuestionsBySurveyId(surveyId);
        int resolvedPromotionAmount = promotionTierResolver.resolveAmountByQuestionCount(questionCount);

        SurveyInfo info = upsertSurveyInfo(
                surveyId,
                request.dueCount(),
                request.gender(),
                ages,
                request.residence(),
                request.genderPrice(),
                request.agePrice(),
                request.residencePrice(),
                request.dueCountPrice(),
                resolvedPromotionAmount,
                discountCodeId,
                true
        );

        member.decreaseCoin(request.totalCoin());
        log.info("[SurveySubmit] 설문 제출 완료 - surveyId={}", surveyId);
        return finalizeSubmit(userKey, surveyId, survey, info, request.dueCount(), request.deadline(), request.totalCoin());
    }


    @Override
    public SurveyFormResponse submitFreeSurvey(Long userKey, Long surveyId, FreeSurveyFormRequest request) {
        Survey survey = getSurvey(surveyId);
        validateMember(userKey);

        survey.markFree();
        survey.updateSurvey(survey.getTitle(), survey.getDescription(), request.deadline(), 0);

        SurveyInfo info = upsertSurveyInfo(
                surveyId,
                100,
                Gender.ALL,
                Set.of(AgeRange.ALL),
                Residence.ALL,
                0, 0, 0, 0,
                0,
                null,
                false
        );

        log.info("[SurveySubmit] FREE 설문 제출 완료 - surveyId={}", surveyId);
        return finalizeSubmit(userKey, surveyId, survey, info, 100, request.deadline(), 0);
    }

    @Override
    public ScreeningResponse upsertScreening(Long surveyId, String content, Boolean answer) {
        Screening screening = screeningRepository.getScreeningBySurveyId(surveyId);
        if (content == null || content.strip().isBlank() || answer == null) {
            if (screening != null) {
                screeningRepository.delete(screening);
            }
            return ScreeningResponse.builder().build();
        } else {
            if (screening == null) {
                screening = Screening.of(surveyId, content, answer);
            } else {
                screening.updateScreening(content, answer);
            }
            screening = screeningRepository.save(screening);

            return ScreeningResponse.builder()
                .screeningId(screening.getId())
                .surveyId(screening.getSurveyId())
                .content(screening.getContent())
                .answer(screening.getAnswer())
                .build();
        }
    }

    @Override
    public InterestResponse upsertInterest(Long surveyId, Set<Interest> interestSet) {
        Survey survey = surveyRepository.getSurveyById(surveyId).orElseThrow(
            () -> new CustomException(ErrorCode.INVALID_REQUEST)
        );
        survey.updateInterests(interestSet);

        survey = surveyRepository.save(survey);

        return InterestResponse.builder()
            .interests(survey.getInterests())
            .build();
    }

    @Override
    public Boolean refundSurvey(Long userKey, Long surveyId) {
        Survey survey = surveyRepository.getSurveyById(surveyId)
                .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_NOT_FOUND));

        SurveyInfo surveyInfo = surveyInfoRepository.findBySurveyId(surveyId)
                .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_INFO_NOT_FOUND));

        if (!surveyInfo.isRefundable()) {
            log.warn("[SurveyRefund] 이미 환불 불가 상태 - surveyId={}", surveyId);
            throw new CustomException(SurveyErrorCode.SURVEY_NOT_REFUNDABLE);
        }

        int refundAmount = surveyRefundPolicy.calculateRefundAmount(survey, surveyInfo);

        if (refundAmount <= 0) {
            log.warn("[SurveyRefund] 환불 가능 금액이 0 이하 - surveyId={}, refundAmount={}",
                    surveyId, refundAmount);
            throw new CustomException(SurveyErrorCode.SURVEY_NOT_REFUNDABLE);
        }

        Member member = memberRepository.findMemberByUserKey(userKey)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.increaseCoin(refundAmount);
        log.info("[SurveyRefund] 코인 환불 완료 - userKey={}, surveyId={}, refundedCoin={}, memberCoinAfter={}",
                userKey, surveyId, survey.getTotalCoin(), member.getCoin());

        survey.updateSurveyStatus(REFUNDED);
        surveyInfo.markNonRefundable();

        return true;
    }

    @Override
    public boolean sendSurveyHeartbeat(Long surveyId, Long userKey) {
        String potentialKey = this.potentialKey + surveyId;
        String memberValue = String.valueOf(userKey);

        if (redisAgent.getZSetScore(potentialKey, memberValue) == null) {
            return false;
        }
        // 잠재 응답자 목록에 현재 시간을 score로 사용자 갱신
        redisAgent.addToZSet(potentialKey, memberValue, System.currentTimeMillis());
        return true;
    }

    @Override
    public void updateSurveyOwner(SurveyOwnerChangeDto changeDto) {
        Survey survey = surveyRepository.getSurveyById(changeDto.surveyId())
                .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_NOT_FOUND));

        survey.changeOwner(changeDto.newMemberId());
        surveyRepository.save(survey);

        log.info("[SURVEY:COMMAND:updateSurveyOwner] 설문 소유자 변경 완료 - surveyId: {}, newMemberId: {}",
            changeDto.surveyId(), changeDto.newMemberId());
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 0 * * *") // 매 자정마다 실행
    public void closeDueSurveys() {
        List<Long> dueSurveyIdList = surveyRepository.closeDueSurveys();
        deleteSurveyRuntimeCache(dueSurveyIdList);
    }

    private Survey getSurvey(Long surveyId) {
        return surveyRepository.getSurveyById(surveyId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
    }

    private Member validateMember(Long userKey) {
        return memberRepository.findMemberByUserKey(userKey)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private SurveyInfo upsertSurveyInfo(
            Long surveyId,
            Integer dueCount,
            Gender gender,
            Set<AgeRange> ages,
            Residence residence,
            Integer genderPrice,
            Integer agePrice,
            Integer residencePrice,
            Integer dueCountPrice,
            Integer promotionAmount,
            Long discountCodeId,
            boolean refundable
    ) {
        SurveyInfo info = surveyInfoRepository.findBySurveyId(surveyId)
                .orElseGet(() -> SurveyInfo.createSurveyInfo(
                        surveyId, dueCount, gender, ages, residence,
                        genderPrice, agePrice, residencePrice, dueCountPrice, promotionAmount, discountCodeId
                ));

        info.updateSurveyInfo(dueCount, gender, ages, residence, genderPrice, agePrice, residencePrice, dueCountPrice, promotionAmount, discountCodeId);

        if (!refundable) info.markNonRefundable();

        return info;
    }

    private SurveyFormResponse finalizeSubmit(
            Long userKey,
            Long surveyId,
            Survey survey,
            SurveyInfo info,
            Integer dueCount,
            LocalDateTime deadline,
            Integer totalCoin
    ) {
        survey.submitSurvey();

        surveyRepository.save(survey);
        surveyInfoRepository.save(info);
        surveyGlobalStatsService.addDueCount(info.getDueCount());
        if (survey.getIsFree()) {
            screeningRepository.deleteBySurveyId(surveyId);
        }

        applySurveyRuntimeCache(surveyId, userKey, dueCount, deadline);

        SurveySubmittedAlert alert = new SurveySubmittedAlert(
                userKey, surveyId, survey.getTitle(), totalCoin, info.getDueCount(),
                survey.getDeadline(), survey.getIsFree(), info.getGender(), info.getAges().stream().toList()
        );
        afterCommitExecutor.run(() -> alertNotifier.sendSurveySubmittedAsync(alert));

        return SurveyFormResponse.fromEntity(survey);
    }

    private void applySurveyRuntimeCache(Long surveyId, Long userKey, Integer dueCount, LocalDateTime deadline) {
        Duration ttl = Duration.between(LocalDateTime.now(), deadline);
        if (ttl.isNegative() || ttl.isZero()) {
            log.warn("[SURVEY:COMMAND] 이미 지난 날짜가 마감기한으로 설정  - surveyId: {}, deadline: {}", surveyId, deadline);
            throw new CustomException(SurveyErrorCode.SURVEY_INCORRECT_STATUS);
        }

        redisAgent.setValue(this.dueCountKey + surveyId, String.valueOf(dueCount), ttl);
        redisAgent.setValue(this.completedKey + surveyId, "0", ttl);
        redisAgent.addToZSet(this.potentialKey + surveyId, String.valueOf(userKey), System.currentTimeMillis());
        redisAgent.setValue(this.creatorKey + surveyId, String.valueOf(userKey), ttl);
    }

    private void deleteSurveyRuntimeCache(List<Long> surveyIdList) {
        List<String> keysToDelete = surveyIdList.stream()
            .flatMap(id -> Stream.of(
                this.dueCountKey + id,
                this.completedKey + id,
                this.potentialKey + id,
                this.creatorKey + id
            ))
            .toList();

        redisAgent.deleteKeys(keysToDelete);
    }
}
