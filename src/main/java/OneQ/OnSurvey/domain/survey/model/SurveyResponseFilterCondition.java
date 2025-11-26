package OneQ.OnSurvey.domain.survey.model;

import java.util.List;

public record SurveyResponseFilterCondition(
        List<AgeRange> ages,
        List<Gender> genders,
        List<Residence> residences
) {

    public boolean isEmpty() {
        return (ages == null || ages.isEmpty())
                && (genders == null || genders.isEmpty())
                && (residences == null || residences.isEmpty());
    }

    /**
     * 전체 조건을 제외한 필터 조건을 반환합니다.
     */
    public SurveyResponseFilterCondition normalize() {
        List<AgeRange> normalizedAges = (ages == null) ? null
                : ages.stream()
                .filter(age -> age != AgeRange.ALL)
                .toList();

        List<Gender> normalizedGenders = (genders == null) ? null
                : genders.stream()
                .filter(g -> g != Gender.ALL)
                .toList();

        List<Residence> normalizedResidences = (residences == null) ? null
                : residences.stream()
                .filter(r -> r != Residence.ALL)
                .toList();

        return new SurveyResponseFilterCondition(
                normalizedAges,
                normalizedGenders,
                normalizedResidences
        );
    }
}
