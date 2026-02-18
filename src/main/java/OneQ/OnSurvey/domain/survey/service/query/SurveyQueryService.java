package OneQ.OnSurvey.domain.survey.service.query;

import OneQ.OnSurvey.domain.member.dto.MemberSegmentation;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.participation.model.dto.ParticipationStatus;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import OneQ.OnSurvey.domain.question.model.dto.SectionDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.repository.section.SectionRepository;
import OneQ.OnSurvey.domain.question.service.QuestionQueryService;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningIntroData;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningViewData;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyDetailData;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyListView;
import OneQ.OnSurvey.domain.survey.model.dto.SurveySearchQuery;
import OneQ.OnSurvey.domain.survey.model.dto.SurveySegmentation;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyWithEligibility;
import OneQ.OnSurvey.domain.survey.model.response.*;
import OneQ.OnSurvey.domain.survey.repository.SurveyRepository;
import OneQ.OnSurvey.domain.survey.repository.screening.ScreeningRepository;
import OneQ.OnSurvey.domain.survey.repository.surveyInfo.SurveyInfoRepository;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.common.exception.ErrorCode;
import OneQ.OnSurvey.global.common.util.AuthorizationUtils;
import OneQ.OnSurvey.global.common.util.RedisUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static OneQ.OnSurvey.domain.survey.model.SurveyStatus.ONGOING;
import static OneQ.OnSurvey.domain.survey.model.SurveyStatus.REFUNDED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyQueryService implements SurveyQuery {

    private final SurveyRepository surveyRepository;
    private final SurveyInfoRepository surveyInfoRepository;
    private final ScreeningRepository screeningRepository;
    private final ResponseRepository responseRepository;
    private final MemberRepository memberRepository;
    private final SectionRepository sectionRepository;

    private final QuestionQueryService questionQueryService;

    @Value("${redis.survey-key-prefix.potential-count}")
    private String potentialKey;

    @Value("${redis.survey-key-prefix.completed-count}")
    private String completedKey;

    @Value("${redis.survey-key-prefix.due-count}")
    private String dueCountKey;

    @Value("${redis.survey-key-prefix.creator-userkey}")
    private String creatorKey;

    @Value("${redis.survey-potential-expiration-seconds}")
    private Integer potentialTimeout;

    private Duration potentialDuration;
    @PostConstruct
    public void init() {
        potentialDuration = Duration.ofSeconds(this.potentialTimeout);
    }

    @Override
    public SurveyManagementDetailResponse getSurvey(Long surveyId) {
        Survey survey = surveyRepository.getSurveyById(surveyId).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));

        return new SurveyManagementDetailResponse(survey.getId(), survey.getMemberId(), survey.getStatus());
    }

    @Override
    public List<SurveyManagementResponse.SurveyInformation> getSurveyListByMemberId(Long memberId) {
        List<Survey> surveyList = surveyRepository.getSurveyListByMemberId(memberId);

        List<Long> surveyIds = surveyList.stream()
                .map(Survey::getId)
                .toList();

        List<SurveyInfo> surveyInfos = surveyInfoRepository.findBySurveyIdIn(surveyIds);

        Map<Long, SurveyInfo> infoMap = surveyInfos.stream()
                .collect(Collectors.toMap(SurveyInfo::getSurveyId, Function.identity()));

        return surveyList.stream()
                .map(survey -> {
                    SurveyInfo info = infoMap.getOrDefault(survey.getId(), null);
                    return info == null
                        ? SurveyManagementResponse.fromEntity(survey)
                        : SurveyManagementResponse.fromEntity(survey, info);
                })
                .toList();
    }

    @Override
    public SurveyParticipationResponse getParticipationSurveySlice(
        Long lastSurveyId, Pageable pageable, SurveyStatus status, Long memberId, Long userKey
    ) {
        log.info("[SURVEY:QUERY:getParticipationSurveySlice] 제작자 제외 필터에 따른 설문 조회 - "
            + "lastSurveyId: {}, size: {}, status: {}, creator: {}",
            lastSurveyId, pageable.getPageSize(), status.name(), userKey
        );

        List<Long> excludedIdList = responseRepository.getExcludedSurveyIdList(memberId, true);
        MemberSegmentation memberSegmentation = memberRepository.findMemberSegmentByUserKey(userKey);
        log.info("[SURVEY:QUERY:getParticipationSurveySlice] 사용자 세그멘테이션 - userKey: {}, memberSegmentation: {}, excludedIdList: {}",
            userKey, memberSegmentation, excludedIdList);

        Slice<SurveyWithEligibility> surveySlice = surveyRepository.getSurveyListWithEligibility(
            lastSurveyId, null, pageable, status, memberId, excludedIdList, memberSegmentation
        );

        return SurveyParticipationResponse.builder()
            .surveys(surveySlice.stream().map(SurveyParticipationResponse::from).toList())
            .hasNext(surveySlice.hasNext())
            .build();
    }

    @Override
    public SurveyParticipationResponse.SliceSurveyData getParticipationSurveyList(
        Long lastSurveyId, Pageable pageable, SurveyStatus status, Long memberId, Long userKey
    ) {
        log.info("[SURVEY:QUERY:getParticipationSurveyList] 본인 제작 제외 설문 조회 - "
            + "lastSurveyId: {}, size: {}, status: {}, userKey: {}",
            lastSurveyId, pageable.getPageSize(), status.name(), userKey
        );

        List<Long> excludedIdList = responseRepository.getExcludedSurveyIdList(memberId, true);
        MemberSegmentation memberSegmentation = memberRepository.findMemberSegmentByUserKey(userKey);
        log.info("[SURVEY:QUERY:getParticipationSurveyList] 사용자 세그멘테이션 - userKey: {}, memberSegmentation: {}, excludedIdList: {}",
            userKey, memberSegmentation, excludedIdList);

        Slice<SurveyWithEligibility> recommendedList = surveyRepository.getSurveyListWithEligibility(
            lastSurveyId, null, pageable, status, memberId, excludedIdList, memberSegmentation
        );
        log.info("[SURVEY:QUERY:getParticipationSurveyList] 추천 설문 조회 결과 - recommended: {}", recommendedList);

        return new SurveyParticipationResponse.SliceSurveyData(
            recommendedList.stream().map(SurveyParticipationResponse::from).toList(), recommendedList.hasNext()
        );
    }

    @Override
    public SurveyParticipationResponse.SliceSurveyData getParticipationSurveyList(
        Long lastSurveyId, LocalDateTime lastDeadline, Pageable pageable, SurveyStatus status, Long memberId, Long userKey
    ) {
        log.info("[SURVEY:QUERY:getParticipationSurveyList] 본인 제작 제외 마감기한 기반 설문 조회 - "
            + "lastSurveyId: {}, lastDateTime: {}, size: {}, status: {}, userKey: {}",
            lastSurveyId, lastDeadline, pageable.getPageSize(), status.name(), userKey
        );

        List<Long> excludedIdList = responseRepository.getExcludedSurveyIdList(memberId, true);
        MemberSegmentation memberSegmentation = memberRepository.findMemberSegmentByUserKey(userKey);
        log.info("[SURVEY:QUERY:getParticipationSurveyList] 사용자 세그멘테이션 - userKey: {}, memberSegmentation: {}, excludedIdList: {}",
            userKey, memberSegmentation, excludedIdList);

        Slice<SurveyWithEligibility> impendingList = surveyRepository.getSurveyListWithEligibility(
            lastSurveyId, lastDeadline, pageable, status, memberId, excludedIdList, memberSegmentation
        );
        log.info("[SURVEY:QUERY:getParticipationSurveyList] 마감임박 설문 조회 결과 - impending: {}", impendingList);

        return new SurveyParticipationResponse.SliceSurveyData(
            impendingList.stream().map(SurveyParticipationResponse::from).toList(), impendingList.hasNext()
        );
    }

    @Override
    public ParticipationScreeningListResponse getScreeningList(
        Long lastSurveyId, Pageable pageable, Long memberId, Long userKey
    ) {
        log.info("[SURVEY:QUERY:getScreeningList] 본인 제작 제외 스크리닝 문항 조회 - "
            + "lastSurveyId: {}, size: {}, userKey: {}",
            lastSurveyId, pageable.getPageSize(), userKey
        );

        Slice<ScreeningIntroData> screeningSlice = screeningRepository.getScreeningSliceByFilters(
            lastSurveyId, pageable, SurveyStatus.ONGOING, memberId
        );

        return ParticipationScreeningListResponse.builder()
            .data(screeningSlice.getContent())
            .hasNext(screeningSlice.hasNext())
            .build();
    }

    @Override
    public ParticipationScreeningSingleResponse getScreeningSingleResponse(Long screeningId) {
        log.info("[SURVEY:QUERY:getScreeningSingleResponse] 단일 스크리닝 퀴즈 조회 - screeningId: {}", screeningId);

        ScreeningIntroData screening = screeningRepository.getScreeningIntroDataByScreeningId(screeningId);

        return new ParticipationScreeningSingleResponse(screening);
    }

    @Override
    public ParticipationInfoResponse getParticipationInfo(Long surveyId, Long userKey, Long memberId) {
        log.info("[SURVEY:QUERY:getParticipationInfo] 설문 기본정보 조회 - surveyId: {}", surveyId);

        if (checkValidSegmentation(surveyId, userKey) && AuthorizationUtils.validateOwnershipOrAdmin(surveyId, userKey)) {
            log.warn("[SURVEY:QUERY] 세그먼트 불일치로 인한 설문 응답 불가 - surveyId: {}, userKey: {}", surveyId, userKey);
            throw new CustomException(SurveyErrorCode.SURVEY_WRONG_SEGMENTATION);
        }

        Survey survey = surveyRepository.getSurveyById(surveyId)
            .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_NOT_FOUND));

        if (!isSurveyAccessible(survey.getStatus())) {
            log.warn("[SURVEY:QUERY] 마감된 설문 참여 불가 - surveyId: {}, status: {}", surveyId, survey.getStatus());
            throw new CustomException(SurveyErrorCode.SURVEY_INCORRECT_STATUS);
        }

        int completedCount = RedisUtils.getIntValue(this.completedKey + surveyId);
        ParticipationStatus participationStatus = surveyRepository.getParticipationStatus(surveyId, memberId);
        if (participationStatus.isScreenRequired()) {
            log.warn("[SURVEY:QUERY] 스크리닝 퀴즈 응답이 필요합니다. - surveyId: {}, memberId: {}", surveyId, memberId);
        }
        if (participationStatus.isScreened()) {
            log.warn("[SURVEY:QUERY] 스크리닝 퀴즈에 의해 필터링되었습니다. - surveyId: {}, memberId: {}", surveyId, memberId);
        }
        if (participationStatus.isSurveyResponded()) {
            log.warn("[SURVEY:QUERY] 이미 참여한 설문입니다. - surveyId: {}, memberId: {}", surveyId, memberId);
        }

        return ParticipationInfoResponse.from(survey, completedCount, participationStatus);
    }

    @Override
    public ParticipationQuestionResponse getParticipationQuestionInfo(Long surveyId, Integer sectionOrder, Long userKey) {
        log.info("[SURVEY:QUERY] 설문 문항정보 조회 - surveyId: {}, userKey: {}", surveyId, userKey);

        if (AuthorizationUtils.validateNonOwnershipOrAdmin(userKey, RedisUtils.getLongValue(this.creatorKey + surveyId))) {
            log.warn("[SURVEY:QUERY] 설문 제작자는 참여 불가 - surveyId: {}, userKey: {}", surveyId, userKey);
            throw new CustomException(SurveyErrorCode.SURVEY_PARTICIPATION_OWN_SURVEY);
        }

        SurveyStatus status = surveyRepository.getSurveyStatusById(surveyId);
        if (!isSurveyAccessible(status)) {
            log.warn("[SURVEY:QUERY] 마감된 설문 참여 불가 - surveyId: {}, status: {}", surveyId, status);
            throw new CustomException(SurveyErrorCode.SURVEY_INCORRECT_STATUS);
        }

        if (!isActivationAvailable(surveyId, userKey)) {
            log.warn("[SURVEY:QUERY] 일시적 설문 참여 불가 - surveyId: {}, userKey: {}", surveyId, userKey);
            throw new CustomException(SurveyErrorCode.SURVEY_PARTICIPATION_TEMP_EXCEEDED);
        }

        List<DefaultQuestionDto> questionDtoList = sectionOrder != null
            ? questionQueryService.getQuestionDtoListBySurveyIdAndSection(surveyId, sectionOrder)
            : questionQueryService.getQuestionDtoListBySurveyId(surveyId);
        SectionDto section = sectionRepository.findSectionDtoBySurveyIdAndOrder(surveyId, sectionOrder != null ? sectionOrder : 1);

        return section != null
            ? ParticipationQuestionResponse.of(
                section.title(), section.description(), section.order(), section.nextSection(), questionDtoList
            )
            : ParticipationQuestionResponse.of(questionDtoList);
    }

    @Override
    public MySurveyListResponse getMySurveys(Long memberId) {
        List<Survey> surveys = surveyRepository.getSurveyListByMemberId(memberId);

        List<MySurveyItemResponse> ongoing = new ArrayList<>();
        List<MySurveyItemResponse> refunded = new ArrayList<>();

        for (Survey survey : surveys) {
            MySurveyItemResponse item = new MySurveyItemResponse(
                    survey.getId(),
                    survey.getTitle(),
                    survey.getStatus(),
                    survey.getTotalCoin() != null ? survey.getTotalCoin() : 0,
                    survey.getCreatedAt().toLocalDate(),
                    survey.getDeadline()
            );

            if (survey.getStatus() == REFUNDED) {
                refunded.add(item);
            } else if (survey.getStatus() == ONGOING || survey.getStatus() == SurveyStatus.CLOSED) {
                ongoing.add(item);
            }
        }

        Comparator<MySurveyItemResponse> byDateDesc =
                Comparator.comparing(MySurveyItemResponse::createdDate).reversed();

        ongoing.sort(byDateDesc);
        refunded.sort(byDateDesc);

        int totalCount = ongoing.size();
        int refundedCount = refunded.size();

        return new MySurveyListResponse(
                totalCount,
                refundedCount,
                ongoing,
                refunded
        );
    }

    @Override
    public SurveyDetailResponse getMySurveyDetail(Long memberId, Long surveyId) {
        Survey survey = surveyRepository.getSurveyById(surveyId)
                .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_NOT_FOUND));

        log.info("[getMySurveyDetail] survey found: id={}, memberId={}, status={}",
                survey.getId(), survey.getMemberId(), survey.getStatus());

        SurveyInfo info = surveyInfoRepository.findBySurveyId(survey.getId())
                .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_INFO_NOT_FOUND));

        return SurveyDetailResponse.from(survey, info);
    }

    @Override
    public void validateSurveyRequest(Long surveyId, Long memberId, SurveyStatus status) {
        Survey survey = surveyRepository.getSurveyById(surveyId)
                .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_NOT_FOUND));

        if (AuthorizationUtils.validateNonOwnershipOrAdmin(survey.getMemberId(), memberId)) {
            log.warn("[SURVEY:QUERY:VALIDATE] 접근 권한 없음 - surveyId: {}, memberId: {}, surveyMemberId: {}",
                    surveyId, memberId, survey.getMemberId());
            throw new CustomException(SurveyErrorCode.SURVEY_FORBIDDEN);
        }

        if (!survey.getStatus().equals(status)) {
            log.warn("[SURVEY:QUERY:VALIDATE] 설문 상태 불일치 - surveyId: {}, memberId: {}, expectedStatus: {}, actualStatus: {}",
                    surveyId, memberId, status, survey.getStatus());
            throw new CustomException(SurveyErrorCode.SURVEY_INCORRECT_STATUS);
        }
    }

    /**
     * 설문 접근 가능 여부 판단
     *
     * @return 설문상태 == 진행중 : true
     * <p> 설문상태 != 진행중 : false
     */
    private boolean isSurveyAccessible(SurveyStatus status) {
        return ONGOING.equals(status);
    }

    /**
     * 활성 참여자 등록 및 등록가능 여부 판단
     * @return 등록가능: true
     * <p> 등록불가능: false
     */
    private boolean isActivationAvailable(Long surveyId, Long userKey) {
        log.info("[SURVEY:QUERY] 활성 참여자 등록 및 등록가능 여부 판단 - surveyId: {}, userKey: {}", surveyId, userKey);

        final String potentialKey = this.potentialKey + surveyId;
        final String memberValue = String.valueOf(userKey);

        Integer dueCount = RedisUtils.getIntValue(this.dueCountKey + surveyId);
        /* dueCount가 설정되어 있지 않을 경우 0으로 반환되므로 이를 설정해줄 필요가 있음. (임의로 시작된 설문 등에 대한 방어코드) */
        if (dueCount == 0) {
            dueCount = initialDueCount(surveyId);
        }

        boolean result;
        try {
            Integer finalDueCount = dueCount;
            result = RedisUtils.executeWithLock("lock:survey:" + surveyId, 5, 10, () -> {
                Double existingScore = RedisUtils.getZSetScore(potentialKey, memberValue);

                // 새로운 참여자인 경우
                if (existingScore == null) {
                    long activePotentialCount = RedisUtils.getZSetCount(
                        potentialKey,
                        System.currentTimeMillis() - potentialDuration.toMillis(),
                        Long.MAX_VALUE
                    );
                    int completedCount = RedisUtils.getIntValue(this.completedKey + surveyId);

                    if (activePotentialCount + 1 + completedCount > finalDueCount) {
                        return false;
                    }

                    // Sorted Set에 현재 시간을 score로 사용자 추가
                    RedisUtils.addToZSet(potentialKey, memberValue, System.currentTimeMillis());
                } else {
                    // 기존 참여자 - score 갱신
                    RedisUtils.addToZSet(potentialKey, memberValue, System.currentTimeMillis());
                }
                return true;
            });
        } catch (RedisException e) {
            log.warn("[SURVEY:QUERY] 설문 참여자 등록을 위한 락 획득 실패 - surveyId: {}, userKey: {}", surveyId, userKey);
            throw new CustomException(SurveyErrorCode.SURVEY_PARTICIPATION_TEMP_EXCEEDED);
        } catch (InterruptedException e) {
            log.error("[SURVEY:QUERY] 설문 참여자 등록 락 획득 중 에러가 발생했습니다.");
            Thread.currentThread().interrupt();
            throw new CustomException(SurveyErrorCode.SURVEY_PARTICIPATION_TEMP_EXCEEDED);
        } finally {
            try {
                RedisUtils.rangeRemoveFromZSet(potentialKey, 0, System.currentTimeMillis() - potentialDuration.toMillis());
            } catch (Exception ignore) {
                log.warn("[SURVEY:QUERY] 만료된 참여자 정리 중 오류 발생", ignore);
            }
        }

        return result;
    }

    private Integer initialDueCount(Long surveyId) {
        try {
             return RedisUtils.executeWithLock("lock:survey:" + surveyId, 3, 6, () -> {
                int dueCount = RedisUtils.getIntValue(this.dueCountKey + surveyId);

                // 다른 스레드에서 값이 설정된 경우, 재조회하지 않고 그대로 값 반환하도록 더블체크
                if (dueCount > 0) {
                    return dueCount;
                }

                return setDueCount(surveyId);
            });
        } catch (RedisException e) {
            log.warn("[SURVEY:QUERY] 설문 참여자 등록을 위한 락 획득 실패 - surveyId: {}", surveyId);
            throw new CustomException(SurveyErrorCode.SURVEY_PARTICIPATION_TEMP_EXCEEDED);
        } catch (InterruptedException e) {
            log.error("[SURVEY:QUERY] 설문 참여가능 인원 초기화 락 획득 중 에러가 발생했습니다.");
            Thread.currentThread().interrupt();
            throw new CustomException(SurveyErrorCode.SURVEY_PARTICIPATION_TEMP_EXCEEDED);
        }
    }

    private Integer setDueCount(Long surveyId) {
        SurveyInfo surveyInfo = surveyInfoRepository.findBySurveyId(surveyId)
            .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_INFO_NOT_FOUND));
        Survey survey = surveyRepository.getSurveyById(surveyId)
            .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_NOT_FOUND));

        Duration duration = Duration.between(
            LocalDateTime.now(), survey.getDeadline());

        RedisUtils.setValue(this.dueCountKey + surveyId, String.valueOf(surveyInfo.getDueCount()), duration);
        return surveyInfo.getDueCount();
    }

    @Override
    public boolean checkValidSegmentation(Long surveyId, Long userKey) {
        SurveySegmentation surveySegmentation = surveyInfoRepository.findSegmentationBySurveyId(surveyId);
        MemberSegmentation memberSegmentation = memberRepository.findMemberSegmentByUserKey(userKey);

        if (surveySegmentation == null || memberSegmentation == null) {
            log.warn("[SURVEY:QUERY:checkValidSegmentation] 세그멘테이션 정보 없음 - surveyId: {}, userKey: {}",
                surveyId, userKey);
            throw new CustomException(SurveyErrorCode.SURVEY_WRONG_SEGMENTATION);
        }

        log.info("{}", !(checkAgeSegmentation(surveySegmentation.getAges(), memberSegmentation.convertBirthDayIntoAgeRange())
            && checkGenderSegmentation(surveySegmentation.getGender(), memberSegmentation.getGender())));
        return !(checkAgeSegmentation(surveySegmentation.getAges(), memberSegmentation.convertBirthDayIntoAgeRange())
            && checkGenderSegmentation(surveySegmentation.getGender(), memberSegmentation.getGender()));
            // || checkResidenceSegmentation(surveySegmentation.residence(), memberSegmentation.residence());
            // || checkInterestSegmentation(surveySegmentation.interests, memberSegmentation.interests);
    }

    private boolean checkAgeSegmentation(Set<AgeRange> surveyAges, AgeRange memberAge) {
        return surveyAges.contains(AgeRange.ALL) || surveyAges.contains(memberAge);
    }

    private boolean checkGenderSegmentation(Gender surveyGender, Gender memberGender) {
        return Gender.ALL.equals(surveyGender) || surveyGender.equals(memberGender);
    }

    private boolean checkResidenceSegmentation(Residence surveyResidence, Residence memberResidence) {
        return Residence.ALL.equals(surveyResidence) || surveyResidence.equals(memberResidence);
    }

    private boolean checkInterestSegmentation(Set<Interest> surveyInterests, Set<Interest> memberInterests) {
        return surveyInterests.stream().anyMatch(memberInterests::contains);
    }

    @Override
    public Survey getSurveyById(Long surveyId) {
        return surveyRepository.getSurveyById(surveyId)
            .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_NOT_FOUND));
    }

    // 외부 PORT
    @Override
    public Page<SurveyListView> getPagedSurveyListViewByQuery(Pageable pageable, SurveySearchQuery query) {
        return surveyRepository.getPagedSurveyListViewByQuery(pageable, query);
    }

    @Override
    public SurveyDetailData getSurveyDetailById(Long surveyId) {
        return surveyRepository.getSurveyDetailDataById(surveyId);
    }

    @Override
    public ScreeningViewData getScreeningIntroBySurveyId(Long surveyId) {
        return screeningRepository.getScreeningIntroBySurveyId(surveyId);
    }

    @Override
    public List<SectionDto> getSectionDtoListBySurveyId(Long surveyId) {
        return sectionRepository.findAllSectionDtoBySurveyId(surveyId);
    }
}
