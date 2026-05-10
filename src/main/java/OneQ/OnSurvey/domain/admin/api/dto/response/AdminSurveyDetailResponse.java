package OneQ.OnSurvey.domain.admin.api.dto.response;

import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveySingleViewInfo;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveyQuestion;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveyScreening;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveySection;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record AdminSurveyDetailResponse(
    SurveyInformationDto information,
    List<QuestionDto> questions,
    ScreeningDto screening,
    List<SectionDto> sections
) {

    public static AdminSurveyDetailResponse from(
        SurveySingleViewInfo infoVo,
        List<SurveyQuestion> questionVos,
        SurveyScreening screeningVo,
        List<SurveySection> sectionVos
    ) {
        return new AdminSurveyDetailResponse(
            SurveyInformationDto.from(infoVo),

            questionVos.stream()
                .map(QuestionDto::from)
                .toList(),

            ScreeningDto.from(screeningVo),

            sectionVos.stream()
                .map(SectionDto::from)
                .toList()
        );
    }

    public record SurveyInformationDto(
        Long surveyId,
        String title,
        String description,
        String deadline,
        Set<String> ages,
        String gender,
        Set<String> residences,
        Set<String> interests,
        Integer dueCount
    ) {
        public static SurveyInformationDto from(SurveySingleViewInfo vo) {
            if (vo == null) return null;
            return new SurveyInformationDto(
                vo.surveyId(),
                vo.title(),
                vo.description(),
                vo.deadline() != null ? vo.deadline().toString() : null,
                vo.ages(),
                vo.gender(),
                vo.residences(),
                vo.interests(),
                vo.dueCount()
            );
        }
    }

    public record QuestionDto(
        Long questionId,
        String questionType,
        String title,
        String description,
        Boolean isRequired,
        Integer questionOrder,
        Integer section,
        String imageUrl,
        ChoicePropDto choiceProperty,
        RatingPropDto ratingProperty,
        DatePropDto dateProperty,
        GridPropDto gridProperty,
        TimePropDto timeProperty
    ) {
        public static QuestionDto from(SurveyQuestion vo) {
            if (vo == null) return null;
            return new QuestionDto(
                vo.questionId(),
                vo.questionType(),
                vo.title(),
                vo.description(),
                vo.isRequired(),
                vo.questionOrder(),
                vo.section(),
                vo.imageUrl(),
                ChoicePropDto.from(vo.choiceProperty()),
                RatingPropDto.from(vo.ratingProperty()),
                DatePropDto.from(vo.dateProperty()),
                GridPropDto.from(vo.gridProperty()),
                TimePropDto.from(vo.timeProperty())
            );
        }

        public record ChoicePropDto(
            Integer maxChoice,
            Boolean hasCustomInput,
            Boolean hasNoneOption,
            Boolean isSectionDecidable,
            Set<OptionDto> options
        ) {
            public static ChoicePropDto from(SurveyQuestion.ChoiceProp vo) {
                if (vo == null) return null;
                Set<OptionDto> optionDtos = vo.options() != null
                    ? vo.options().stream().map(OptionDto::from).collect(Collectors.toSet())
                    : Set.of();
                return new ChoicePropDto(vo.maxChoice(), vo.hasCustomInput(), vo.hasNoneOption(), vo.isSectionDecidable(), optionDtos);
            }

            public record OptionDto(Long optionId, String content, Integer nextSection, String imageUrl) {
                public static OptionDto from(SurveyQuestion.ChoiceProp.Option vo) {
                    if (vo == null) return null;
                    return new OptionDto(vo.optionId(), vo.content(), vo.nextSection(), vo.imageUrl());
                }
            }
        }

        public record RatingPropDto(String minValue, String maxValue, Integer rate) {
            public static RatingPropDto from(SurveyQuestion.RatingProp vo) {
                if (vo == null) return null;
                return new RatingPropDto(vo.minValue(), vo.maxValue(), vo.rate());
            }
        }

        public record DatePropDto(LocalDate defaultDate) {
            public static DatePropDto from(SurveyQuestion.DateProp vo) {
                if (vo == null) return null;
                return new DatePropDto(vo.defaultDate());
            }
        }

        public record TimePropDto(Boolean isInterval) {
            public static TimePropDto from(SurveyQuestion.TimeProp vo) {
                if (vo == null) return null;
                return new TimePropDto(vo.isInterval());
            }
        }

        public record GridPropDto(
            Boolean isCheckbox,
            Boolean isChoiceMixed,
            Boolean isChoiceDistinct,
            Set<GridOptionDto> gridOptions
        ) {
            public static GridPropDto from(SurveyQuestion.GridProp vo) {
                if (vo == null) return null;
                Set<GridOptionDto> optionDtos = vo.gridOptions() != null
                    ? vo.gridOptions().stream().map(GridOptionDto::from).collect(Collectors.toSet())
                    : Set.of();
                return new GridPropDto(vo.isCheckbox(), vo.isChoiceMixed(), vo.isChoiceDistinct(), optionDtos);
            }

            public record GridOptionDto(Long gridOptionId, Boolean isRow, String content, Integer order) {
                public static GridOptionDto from(SurveyQuestion.GridProp.GridOption vo) {
                    if (vo == null) return null;
                    return new GridOptionDto(vo.gridOptionId(), vo.isRow(), vo.content(), vo.order());
                }
            }
        }
    }

    public record ScreeningDto(
        Long screeningId,
        String content,
        String answer
    ) {
        public static ScreeningDto from(SurveyScreening vo) {
            if (vo == null) return null;
            return new ScreeningDto(
                vo.screeningId(),
                vo.content(),
                vo.answer()
            );
        }
    }

    public record SectionDto(
        Long sectionId,
        String title,
        String description,
        Integer order,
        Integer nextSection
    ) {
        public static SectionDto from(SurveySection vo) {
            if (vo == null) return null;
            return new SectionDto(
                vo.sectionId(),
                vo.title(),
                vo.description(),
                vo.order(),
                vo.nextSection()
            );
        }
    }
}