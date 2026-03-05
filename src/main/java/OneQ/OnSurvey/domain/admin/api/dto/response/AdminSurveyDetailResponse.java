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
        String residence,
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
                vo.residence(),
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
        DatePropDto dateProperty
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
                DatePropDto.from(vo.dateProperty())
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

            public record OptionDto(String content, Integer nextSection, String imageUrl) {
                public static OptionDto from(SurveyQuestion.ChoiceProp.Option vo) {
                    if (vo == null) return null;
                    return new OptionDto(vo.content(), vo.nextSection(), vo.imageUrl());
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