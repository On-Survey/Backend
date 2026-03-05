package OneQ.OnSurvey.domain.survey.entity;

import OneQ.OnSurvey.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

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

    @Column(nullable = false)
    private Integer questionCount;

    @Column(nullable = false)
    private Integer targetResponseCount;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(nullable = false, length = 100)
    private String requesterEmail;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "is_registered", nullable = false)
    @ColumnDefault("FALSE")
    @Builder.Default
    private Boolean isRegistered = false;

    @Column(name = "registered_survey_id")
    private Long registeredSurveyId;

    public static FormRequest createRequest(
            String formLink,
            Integer questionCount,
            Integer targetResponseCount,
            LocalDate deadline,
            String requesterEmail,
            Integer price
    ) {
        return FormRequest.builder()
                .formLink(formLink)
                .questionCount(questionCount)
                .targetResponseCount(targetResponseCount)
                .deadline(deadline)
                .requesterEmail(requesterEmail)
                .price(price)
                .isRegistered(false)
                .build();
    }

    public void markAsRegistered(Long surveyId) {
        this.isRegistered = true;
        this.registeredSurveyId = surveyId;
    }
}