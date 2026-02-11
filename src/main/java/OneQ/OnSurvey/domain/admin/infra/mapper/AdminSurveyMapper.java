package OneQ.OnSurvey.domain.admin.infra.mapper;

import OneQ.OnSurvey.domain.admin.domain.model.survey.AdminSurveyListView;
import OneQ.OnSurvey.domain.admin.domain.model.survey.AdminSurveyStatus;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveyQuestion;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveyScreening;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveySection;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveySingleViewInfo;
import OneQ.OnSurvey.domain.question.model.dto.SectionDto;
import OneQ.OnSurvey.domain.question.model.dto.type.ChoiceDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DateDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.model.dto.type.RatingDto;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningViewData;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyDetailData;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyListView;

import java.util.List;
import java.util.stream.Collectors;

public final class AdminSurveyMapper {

    public static List<SurveyStatus> toSurveyStatus(AdminSurveyStatus status) {
        if (status == null) {
            return List.of(
                SurveyStatus.WRITING,
                SurveyStatus.ONGOING, SurveyStatus.CLOSED, SurveyStatus.REFUNDED, SurveyStatus.REVIEW
            );
        }

        return AdminSurveyStatus.WRITING.equals(status)
            ? List.of(SurveyStatus.WRITING)
            : List.of(SurveyStatus.ONGOING, SurveyStatus.CLOSED, SurveyStatus.REFUNDED, SurveyStatus.REVIEW);
    }

    public static AdminSurveyListView toAdminSurveyListView(SurveyListView surveyListView) {
        if (surveyListView == null) return null;

        return new AdminSurveyListView(
            surveyListView.getSurveyId(),
            surveyListView.getTitle(),
            "WRITING".equals(surveyListView.getStatus().name()) ? AdminSurveyStatus.WRITING : AdminSurveyStatus.NON_WRITING,
            surveyListView.getCreator(),
            surveyListView.getCreatedAt()
        );
    }

    public static SurveySingleViewInfo toSurveySingleViewInfo(SurveyDetailData surveyDetailData) {
        if (surveyDetailData == null) return null;

        return new SurveySingleViewInfo(
            surveyDetailData.getSurveyId(),
            surveyDetailData.getTitle(),
            surveyDetailData.getDescription(),
            surveyDetailData.getDeadline() != null ? surveyDetailData.getDeadline().toLocalDate() : null,
            surveyDetailData.getAges().stream().map(Enum::name).collect(Collectors.toSet()),
            surveyDetailData.getGender() != null ? surveyDetailData.getGender().name() : null,
            surveyDetailData.getResidence() != null ? surveyDetailData.getResidence().name() : null,
            surveyDetailData.getInterests().stream().map(Enum::name).collect(Collectors.toSet()),
            surveyDetailData.getDueCount()
        );
    }

    public static SurveyQuestion toSurveyQuestion(DefaultQuestionDto questionDto) {
        if (questionDto == null) return null;

        SurveyQuestion.SurveyQuestionBuilder question = SurveyQuestion.builder()
            .questionId(questionDto.getQuestionId())
            .questionType(questionDto.getQuestionType())
            .title(questionDto.getTitle())
            .description(questionDto.getDescription())
            .isRequired(questionDto.getIsRequired())
            .questionOrder(questionDto.getQuestionOrder())
            .section(questionDto.getSection());

        return switch (questionDto.getQuestionType()) {
            case "CHOICE" -> {
                ChoiceDto choice = (ChoiceDto) questionDto;
                yield question.choiceProperty(
                    new SurveyQuestion.ChoiceProp(
                        choice.getMaxChoice(),
                        choice.getHasCustomInput(),
                        choice.getHasNoneOption(),
                        choice.getIsSectionDecidable(),
                        choice.getOptions().stream()
                            .map(optionDto -> new SurveyQuestion.ChoiceProp.Option(
                                optionDto.getContent(),
                                optionDto.getNextSection()
                            ))
                            .collect(Collectors.toSet()
                        )
                    )
                ).build();
            }
            case "RATING" -> {
                RatingDto rating = (RatingDto) questionDto;
                yield question.ratingProperty(
                    new SurveyQuestion.RatingProp(
                        rating.getMinValue(),
                        rating.getMaxValue(),
                        rating.getRate()
                    )
                ).build();
            }
            case "DATE" -> {
                DateDto date = (DateDto) questionDto;
                yield question.dateProperty(
                    new SurveyQuestion.DateProp(
                        date.getDate() != null ? date.getDate().toLocalDate() : null
                    )
                ).build();
            }
            default -> question.build();
        };
    }

    public static SurveyScreening toSurveyScreening(ScreeningViewData screeningViewData) {
        if (screeningViewData == null) return null;

        return new SurveyScreening(
            screeningViewData.getScreeningId(),
            screeningViewData.getContent(),
            screeningViewData.getAnswer().toString()
        );
    }

    public static SurveySection toSurveySection(SectionDto sectionDto) {
        if (sectionDto == null) return null;

        return new SurveySection(
            sectionDto.sectionId(),
            sectionDto.title(),
            sectionDto.description(),
            sectionDto.order(),
            sectionDto.nextSection()
        );
    }
}
