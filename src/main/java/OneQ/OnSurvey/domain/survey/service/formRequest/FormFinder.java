package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.survey.model.formRequest.FormListResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormRequestResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationEmailQuotaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FormFinder {
    FormListResponse getAllUnregisteredRequests();

    /**
     * 폼 요청 목록 조회 (페이지네이션, 검색, 필터링)
     * @param email 신청자 이메일 (검색)
     * @param isRegistered 등록 상태 필터 (null: 전체, true: 등록완료, false: 미등록)
     * @param pageable page 페이지 번호 (0부터 시작), size 페이지 크기
     * @return 페이지네이션된 폼 요청 목록
     */
    Page<FormRequestResponse> getFormRequests(String email, Boolean isRegistered, Pageable pageable);
    FormValidationEmailQuotaResponse getEmailQuota(Long userKey);
}
