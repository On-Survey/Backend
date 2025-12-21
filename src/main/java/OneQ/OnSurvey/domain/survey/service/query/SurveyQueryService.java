package OneQ.OnSurvey.domain.survey.service.query;

import OneQ.OnSurvey.domain.member.dto.MemberSegmentation;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.entity.Screening;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.dto.SurveySegmentation;
import OneQ.OnSurvey.domain.survey.model.response.*;
import OneQ.OnSurvey.domain.survey.repository.SurveyRepository;
import OneQ.OnSurvey.domain.survey.repository.screening.ScreeningRepository;
import OneQ.OnSurvey.domain.survey.repository.surveyInfo.SurveyInfoRepository;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public SurveyParticipationResponse.SliceSurveyData getParticipationSurveyList(
        Long lastSurveyId, Pageable pageable, SurveyStatus status, Long memberId
    ) {
        log.info("[SURVEY:QUERY:getParticipationSurveyList] 본인 제작 제외 스크리닝, 관심사, 마감기한 기반 설문 조회 - "
            + "lastSurveyId: {}, size: {}, status: {}, memberId: {}",
            lastSurveyId, pageable.getPageSize(), status.name(), memberId
        );

        List<Long> excludedIdList = responseRepository.getExcludedSurveyIdList(memberId, true);

        Slice<Survey> recommendedList = surveyRepository.getSurveyListByFilters(
            lastSurveyId, null, pageable,
            status, memberId, excludedIdList, Collections.emptyList()
        );
        log.info("[SURVEY:QUERY:getParticipationSurveyList] 추천 설문 조회 결과 - recommended: {}", recommendedList);

        return new SurveyParticipationResponse.SliceSurveyData(
            recommendedList.stream().map(SurveyParticipationResponse::fromEntity).toList(), recommendedList.hasNext()
        );
    }

    @Override
    public SurveyParticipationResponse.SliceSurveyData getParticipationSurveyList(
        Long lastSurveyId, LocalDateTime lastDeadline, Pageable pageable, SurveyStatus status, Long memberId
    ) {
        log.info("[SURVEY:QUERY:getParticipationSurveyList] 본인 제작 제외 마감기한 기반 설문 조회 - "
            + "lastSurveyId: {}, lastDateTime: {}, size: {}, status: {}, memberId: {}",
            lastSurveyId, lastDeadline, pageable.getPageSize(), status.name(), memberId
        );

        List<Long> excludedIdList = responseRepository.getExcludedSurveyIdList(memberId, true);
        Slice<Survey> impendingList = surveyRepository.getSurveyListByFilters(
            lastSurveyId, lastDeadline, pageable,
            status, memberId, excludedIdList, Set.of()
        );
        log.info("[SURVEY:QUERY:getParticipationSurveyList] 마감임박 설문 조회 결과 - impending: {}", impendingList);

        return new SurveyParticipationResponse.SliceSurveyData(
            impendingList.stream().map(SurveyParticipationResponse::fromEntity).toList(), impendingList.hasNext()
        );
    }

    @Override
    public ParticipationScreeningResponse getScreeningList(
        Long lastSurveyId, Pageable pageable, Long memberId
    ) {
        log.info("[SURVEY:QUERY:getScreeningList] 본인 제작 제외 관심사 기반 설문의 스크리닝 문항 조회 - "
            + "lastSurveyId: {}, size: {}, memberId: {}",
            lastSurveyId, pageable.getPageSize(), memberId
        );

        List<Long> excludedIdList = responseRepository.getExcludedSurveyIdList(memberId, false);
        Set<Interest> interestSet = memberRepository.findMemberInterestsById(memberId).getInterests();
        log.info("[SURVEY:QUERY:getScreeningList] 사용자 관심사 - memberId: {}, interests: {}, excludedIdList: {}", memberId, interestSet, excludedIdList);

        Slice<Survey> surveyList = surveyRepository.getSurveyListByFilters(
            lastSurveyId, null, pageable,
            SurveyStatus.ONGOING, memberId, excludedIdList, interestSet
        );
        List<Long> idList = surveyList.stream().map(Survey::getId).toList();
        log.info("[SURVEY:QUERY:getScreeningList] 스크리닝을 조회할 설문 IDs: {}", idList);

        List<Screening> screeningList = List.of();
        if (!idList.isEmpty()) {
            screeningList = screeningRepository.getScreeningListBySurveyIdList(idList);
        }

        return ParticipationScreeningResponse.builder()
            .data(screeningList.stream().map(ParticipationScreeningResponse::fromEntity).toList())
            .hasNext(surveyList.hasNext())
            .build();
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

        if (!survey.getMemberId().equals(memberId)) {
            log.warn("[SURVEY:QUERY:VALIDATE] 접근 권한 없음 - surveyId: {}, memberId: {}, surveyMemberId: {}",
                    surveyId, memberId, survey.getMemberId());
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (!survey.getStatus().equals(status)) {
            log.warn("[SURVEY:QUERY:VALIDATE] 설문 상태 불일치 - surveyId: {}, memberId: {}, expectedStatus: {}, actualStatus: {}",
                    surveyId, memberId, status, survey.getStatus());
        }
    }

    @Override
    public Survey getSurveyById(Long surveyId) {
        return surveyRepository.getSurveyById(surveyId)
                .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_NOT_FOUND));
    }

    @Override
    public boolean checkValidSegmentation(Long surveyId, Long userKey) {
        SurveySegmentation surveySegmentation = surveyInfoRepository.findSegmentationBySurveyId(surveyId);
        MemberSegmentation memberSegmentation = memberRepository.findMemberSegmentByUserKey(userKey);

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
}
