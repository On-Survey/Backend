package OneQ.OnSurvey.global.promotion.application;

import OneQ.OnSurvey.global.infra.toss.common.dto.promotion.ExecutionResultResponse;

public interface PromotionUseCase {

    /**
     * 토스 포인트 지급 실행 / 결과 검증
     * - SUCCESS : 그대로 반환
     * - FAILED  : CustomException throw
     * - PENDING : confirmWaitMs 타임아웃 내 최종 상태가 안 나오면 그대로 PENDING 반환
     */
    ExecutionResultResponse issueAndConfirm(long userKey, long surveyId);
}
