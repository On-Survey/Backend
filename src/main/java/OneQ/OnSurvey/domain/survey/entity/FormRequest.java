package OneQ.OnSurvey.domain.survey.entity;

import OneQ.OnSurvey.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class FormRequest extends BaseEntity {

    @Id
    @Column(name = "form_request_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String formLink;

    @Column(nullable = false, length = 100)
    private String requesterEmail;

    @Column
    private Integer questionCount;

    @Column
    private Integer targetResponseCount;

    @Column
    private Integer price;

    @Column(name = "is_registered", nullable = false)
    @ColumnDefault("FALSE")
    @Builder.Default
    private Boolean isRegistered = false;

    @Column(name = "registered_survey_id")
    private Long registeredSurveyId;

    public static FormRequest createRequest(String formLink, String requesterEmail) {
        return FormRequest.builder()
                .formLink(formLink)
                .requesterEmail(requesterEmail)
                .isRegistered(false)
                .build();
    }

    public void markAsRegistered(Long surveyId, Integer questionCount) {
        this.isRegistered = true;
        this.registeredSurveyId = surveyId;
        this.questionCount = questionCount;
    }
}