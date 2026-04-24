package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.GridOptionDto;
import OneQ.OnSurvey.domain.question.model.dto.GridOptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.OptionDto;
import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.type.ChoiceDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.model.dto.type.GridDto;
import OneQ.OnSurvey.domain.question.model.dto.type.RatingDto;
import OneQ.OnSurvey.domain.question.model.dto.type.TimeDto;
import OneQ.OnSurvey.domain.question.service.QuestionCommand;
import OneQ.OnSurvey.domain.survey.model.formRequest.ConversionDto;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationPostResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationResponse;
import OneQ.OnSurvey.domain.survey.model.request.SurveyFormCreateRequest;
import OneQ.OnSurvey.domain.survey.service.command.SurveyCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FormConverter {

    private final SurveyCommand surveyCommand;
    private final QuestionCommand questionCommand;

    public Long createSurveyFromConversionResult(ConversionDto dto, Long memberId) {
        // 1. 최초 설문 제목, 설명 생성
        SurveyFormCreateRequest surveyRequest = new SurveyFormCreateRequest(
            dto.title(), dto.description()
        );
        Long surveyId = surveyCommand.upsertSurvey(memberId, null, surveyRequest).surveyId();

        // 2. 설문 섹션 생성
        if (dto.sections() != null && !dto.sections().isEmpty()) {
            questionCommand.upsertSections(surveyId, dto.sections());
        }

        // 3. 문항 생성
        Map<Integer, QuestionUpsertDto.UpsertInfo> upsertInfoList = dto.questions().stream()
            .map(q -> {
                QuestionType type = QuestionType.valueOf(q.getQuestionType());
                QuestionUpsertDto.UpsertInfo.UpsertInfoBuilder builder = QuestionUpsertDto.UpsertInfo.builder()
                    .questionId(null)
                    .title(q.getTitle())
                    .description(q.getDescription())
                    .questionOrder(q.getQuestionOrder())
                    .section(q.getSection())
                    .isRequired(q.getIsRequired())
                    .questionType(type)
                    .imageUrl(q.getImageUrl());

                return switch(type) {
                    case CHOICE -> {
                        ChoiceDto choiceDto = (ChoiceDto) q;
                        yield builder
                            .maxChoice(choiceDto.getMaxChoice())
                            .hasNoneOption(choiceDto.getHasNoneOption())
                            .hasCustomInput(choiceDto.getHasCustomInput())
                            .isSectionDecidable(choiceDto.getIsSectionDecidable())
                            .options(choiceDto.getOptions())
                            .build();
                    }
                    case RATING -> {
                        RatingDto ratingDto = (RatingDto) q;
                        yield builder
                            .minValue(ratingDto.getMinValue())
                            .maxValue(ratingDto.getMaxValue())
                            .rate(ratingDto.getRate())
                            .build();
                    }
                    case GRID -> {
                        GridDto gridDto = (GridDto) q;
                        yield builder
                            .isCheckbox(gridDto.getIsCheckbox())
                            .isChoiceMixed(gridDto.getIsChoiceMixed())
                            .isChoiceDistinct(gridDto.getIsChoiceDistinct())
                            .gridOptions(gridDto.getGridOptions())
                            .build();
                    }
                    case TIME -> {
                        TimeDto timeDto = (TimeDto) q;
                        yield builder.isInterval(timeDto.getIsInterval()).build();
                    }
                    default -> builder.build();
                };
            })
            .collect(Collectors.toMap(QuestionUpsertDto.UpsertInfo::getQuestionOrder, Function.identity()));

        if (!upsertInfoList.isEmpty()) {
            QuestionUpsertDto upsertDto = QuestionUpsertDto.builder()
                .surveyId(surveyId)
                .upsertInfoList(upsertInfoList.values().stream().toList())
                .build();
            // 4. 전체 문항 저장
            QuestionUpsertDto savedQuestions = questionCommand.upsertQuestionList(upsertDto);

            // 5. CHOICE, GRID 문항 옵션 저장
            List<OptionUpsertDto> optionUpsertDtoList = new ArrayList<>();
            List<GridOptionUpsertDto> gridOptionUpsertDtoList = new ArrayList<>();
            List<QuestionUpsertDto.UpsertInfo> savedInfoList = savedQuestions.getUpsertInfoList();
            savedInfoList.stream()
                .filter(info -> info.getQuestionType().isChoice() || info.getQuestionType().isGrid())
                .filter(info -> upsertInfoList.get(info.getQuestionOrder()) != null)
                .forEach(info -> {
                    QuestionUpsertDto.UpsertInfo originalInfo = upsertInfoList.get(info.getQuestionOrder());
                    if (originalInfo.getQuestionType().isChoice() && originalInfo.getOptions() != null) {
                        optionUpsertDtoList.add(OptionUpsertDto.builder()
                            .questionId(info.getQuestionId())
                            .optionInfoList(originalInfo.getOptions().stream()
                                .map(opt -> OptionDto.builder()
                                    .questionId(info.getQuestionId())
                                    .content(opt.getContent())
                                    .nextSection(opt.getNextSection())
                                    .imageUrl(opt.getImageUrl())
                                    .build()
                                )
                                .toList())
                            .build());
                    } else if (originalInfo.getQuestionType().isGrid() && originalInfo.getGridOptions() != null) {
                        gridOptionUpsertDtoList.add(GridOptionUpsertDto.builder()
                            .questionId(info.getQuestionId())
                            .gridOptionInfoList(originalInfo.getGridOptions().stream()
                                .map(opt -> GridOptionDto.builder()
                                    .questionId(info.getQuestionId())
                                    .isRow(opt.getIsRow())
                                    .content(opt.getContent())
                                    .order(opt.getOrder())
                                    .build()
                                )
                                .toList())
                            .build());
                    }
                });
            if (!optionUpsertDtoList.isEmpty()) {
                questionCommand.upsertChoiceOptionList(optionUpsertDtoList);
            }
            if (!gridOptionUpsertDtoList.isEmpty()) {
                questionCommand.upsertGridOptionList(gridOptionUpsertDtoList);
            }
        }
        return surveyId;
    }

    public FormValidationResponse toResponse(FormValidationPostResponse dto) {
        if (dto == null || dto.results() == null || dto.results().isEmpty()) {
            return null;
        }

        List<FormValidationResponse.Result> results = dto.results().stream()
            .map(this::mapToResult)
            .toList();

        return new FormValidationResponse(results, dto.emailSent());
    }

    private FormValidationResponse.Result mapToResult(FormValidationPostResponse.Result r) {
        if (r.isSuccess()) {
            return FormValidationResponse.success(
                r.url(),
                r.counts().total(),
                r.counts().convertible(),
                r.counts().inconvertible(),
                mapInconvertible(r.inconvertibleDetails()),
                mapConvertible(r.convertibleDetails())
            );
        }

        // 설문 변환에 실패한 경우 (status == "FAIL")
        return FormValidationResponse.fail(
            r.url(),
            r.message()
        );
    }

    private List<FormValidationResponse.Inconvertible> mapInconvertible(
        List<FormValidationPostResponse.Inconvertible> details
    ) {
        if (details == null || details.isEmpty()) return List.of();

        return details.stream()
            .map(i -> new FormValidationResponse.Inconvertible(i.title(), i.type(), i.reason(), i.section(), i.order()))
            .toList();
    }

    private List<FormValidationResponse.Convertible> mapConvertible(
        FormValidationPostResponse.Convertible details
    ) {
        if (details == null) return List.of();

        List<DefaultQuestionDto> questionDtoList = details.questions() != null ? details.questions() : List.of();

        // 섹션이 없는 경우 (섹션 구분이 없는 설문)
        if (details.sections() == null || details.sections().isEmpty()) {
            return List.of(new FormValidationResponse.Convertible(
                details.title(), details.description(), 1, 0, questionDtoList
            ));
        }

        // 문항들을 섹션별로 그룹화
        Map<Integer, List<DefaultQuestionDto>> sectionOrderQuestionMap = questionDtoList.stream()
            .collect(Collectors.groupingBy(DefaultQuestionDto::getSection, Collectors.toList()));

        // 섹션 순회하며 변환
        return details.sections().stream()
            .map(c -> new FormValidationResponse.Convertible(
                c.title(), c.description(), c.order(), c.nextSection(),
                sectionOrderQuestionMap.getOrDefault(c.order(), List.of())
            ))
            .toList();
    }
}
