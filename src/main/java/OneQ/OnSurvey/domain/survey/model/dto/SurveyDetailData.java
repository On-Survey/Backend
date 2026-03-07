package OneQ.OnSurvey.domain.survey.model.dto;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@Getter @ToString
@NoArgsConstructor
public class SurveyDetailData {

    // 설문 기본 정보
    private Long surveyId;
    private String title;
    private String description;
    private LocalDateTime deadline;

    // 설문 상세 정보
    private Integer dueCount;
    private Set<AgeRange> ages;
    private Gender gender;
    private Residence residence;
    private Set<Interest> interests;
}
