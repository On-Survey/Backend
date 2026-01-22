package OneQ.OnSurvey.domain.participation.model.dto;

public record ParticipationStatus (
    boolean isScreenRequired,
    boolean isScreened,
    boolean isSurveyResponded
) {

    public static ParticipationStatus defaultStatus(boolean hasScreening) {
        return new ParticipationStatus(hasScreening, false, false);
    }

    public static ParticipationStatus generateStatus(
        Long screeningId, Boolean eIsScreened, Boolean eIsResponded
    ) {
        // 스크리닝 퀴즈 존재 여부
        boolean hasScreening = screeningId != null;
        // 스크리닝 퀴즈가 있으나, 이에 대한 응답이 없는 케이스
        boolean isScreenRequired = hasScreening && eIsScreened == null;
        // 스크리닝 퀴즈가 있고, 그 응답이 오답인 케이스
        boolean isScreened = hasScreening && Boolean.TRUE.equals(eIsScreened);
        // 응답이 완료된 케이스
        boolean isSurveyResponded = Boolean.TRUE.equals(eIsResponded);

        return new ParticipationStatus(isScreenRequired, isScreened, isSurveyResponded);
    }
}
