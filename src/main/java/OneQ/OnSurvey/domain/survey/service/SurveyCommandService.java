package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.MemberErrorCode;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.entity.Screening;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.response.InterestResponse;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.request.SurveyFormRequest;
import OneQ.OnSurvey.domain.survey.model.response.ScreeningResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;
import OneQ.OnSurvey.domain.survey.repository.SurveyInfoRepository;
import OneQ.OnSurvey.domain.survey.repository.SurveyRepository;
import OneQ.OnSurvey.domain.survey.repository.screening.ScreeningRepository;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

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

    @Override
    public SurveyFormResponse upsertSurvey(Long memberId, Long surveyId, SurveyFormRequest request){

        Survey survey;
        log.info("surveyId: {}", surveyId);
        if (surveyId == null) {
            survey = Survey.of(
                    memberId,
                    request.title(),
                    request.description(),
                    request.deadline(),
                    request.totalCoin()
            );
            survey = surveyRepository.save(survey);

            SurveyInfo info = SurveyInfo.createSurveyInfo(
                    survey.getId(),
                    request.dueCount(),
                    request.gender(),
                    request.age(),
                    request.residence(),
                    request.genderPrice(),
                    request.agePrice(),
                    request.residencePrice(),
                    request.dueCountPrice()
            );
            surveyInfoRepository.save(info);

            log.info("[SurveyUpsert] 설문 생성 완료 - surveyId={}", survey.getId());
        } else {
            survey = surveyRepository.getSurveyById(surveyId)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));

            survey.updateSurvey(
                    request.title(),
                    request.description(),
                    request.deadline(),
                    request.totalCoin()
            );

            SurveyInfo info = surveyInfoRepository.findBySurveyId(surveyId)
                    .orElseGet(() -> {
                        SurveyInfo newInfo = SurveyInfo.createSurveyInfo(
                                surveyId,
                                request.dueCount(),
                                request.gender(),
                                request.age(),
                                request.residence(),
                                request.genderPrice(),
                                request.agePrice(),
                                request.residencePrice(),
                                request.dueCountPrice()
                        );
                        return surveyInfoRepository.save(newInfo);
                    });

            info.updateSurveyInfo(
                    request.dueCount(),
                    request.gender(),
                    request.age(),
                    request.residence(),
                    request.genderPrice(),
                    request.agePrice(),
                    request.residencePrice(),
                    request.dueCountPrice()
            );

            log.info("[SurveyUpsert] 설문 수정 완료 - surveyId={}", surveyId);
        }

        survey = surveyRepository.save(survey);
        return SurveyFormResponse.fromEntity(survey);
    }

    @Override
    public SurveyFormResponse submitSurvey(Long surveyId) {
        Survey survey = surveyRepository.getSurveyById(surveyId).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
        survey.submitSurvey();

        surveyRepository.save(survey);

        return SurveyFormResponse.fromEntity(survey);
    }

    @Override
    public Boolean deleteById(Long surveyId) {
        surveyRepository.deleteById(surveyId);
        return true;
    }

    @Override
    public ScreeningResponse upsertScreening(Long screeningId, Long surveyId, String content, Boolean answer) {
        Screening screening;
        if (screeningId == null) {
            screening = Screening.of(surveyId, content, answer);
            screening = screeningRepository.save(screening);
        } else {
            screening = screeningRepository.getScreeningBySurveyId(surveyId);
            screening.updateScreening(content, answer);

            screeningRepository.save(screening);
        }
        return ScreeningResponse.builder()
            .screeningId(screening.getId())
            .surveyId(screening.getSurveyId())
            .content(screening.getContent())
            .answer(answer)
            .build();
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

        Member member = memberRepository.findMemberByUserKey(userKey)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.increaseCoin(survey.getTotalCoin());
        log.info("[SurveyRefund] 코인 환불 완료 - userKey={}, surveyId={}, refundedCoin={}, memberCoinAfter={}",
                userKey, surveyId, survey.getTotalCoin(), member.getCoin());

        survey.updateSurveyStatus(REFUNDED);
        surveyInfo.markNonRefundable();

        return true;
    }
}
