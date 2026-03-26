package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.OptionDto;
import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.SectionDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.service.QuestionCommand;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormConversionResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationPostResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationResponse;
import OneQ.OnSurvey.domain.survey.model.request.SurveyFormCreateRequest;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;
import OneQ.OnSurvey.domain.survey.service.command.SurveyCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FormConverter {

    private final SurveyCommand surveyCommand;
    private final QuestionCommand questionCommand;

    public Long createSurveyFromConversionResult(FormConversionResponse.Result result, Long memberId) {
        FormConversionResponse.Survey survey = result.survey();

        // 1. 설문 생성
        SurveyFormCreateRequest surveyRequest = new SurveyFormCreateRequest(
            survey.title(),
            survey.description()
        );
        SurveyFormResponse surveyResponse = surveyCommand.upsertSurvey(memberId, null, surveyRequest);
        Long surveyId = surveyResponse.surveyId();

        // 2. 섹션 생성
        if (survey.sections() != null && !survey.sections().isEmpty()) {
            List<SectionDto> sectionDtoList = survey.sections().stream()
                .map(section -> new SectionDto(
                    null,
                    section.title(),
                    section.description(),
                    section.order(),
                    section.nextSectionOrder() != null ? section.nextSectionOrder() : 0
                ))
                .toList();

            questionCommand.upsertSections(surveyId, sectionDtoList);
        }

        // 3. 문항 생성
        List<QuestionUpsertDto.UpsertInfo> questionUpsertInfoList = new ArrayList<>();
        AtomicInteger questionOrder = new AtomicInteger(0);

        if (survey.sections() != null) {
            for (FormConversionResponse.Section section : survey.sections()) {
                if (section.questions() != null) {
                    for (FormConversionResponse.Question question : section.questions()) {
                        QuestionType questionType = mapQuestionType(question.type());
                        if (questionType == null) {
                            continue;
                        }

                        QuestionUpsertDto.UpsertInfo.UpsertInfoBuilder builder = QuestionUpsertDto.UpsertInfo.builder()
                            .questionId(null)
                            .title(question.title())
                            .description(question.description())
                            .isRequired(question.required())
                            .questionType(questionType)
                            .questionOrder(questionOrder.getAndIncrement())
                            .imageUrl(question.imageUrl())
                            .section(section.order());

                        // Choice 타입인 경우 옵션 설정
                        if (questionType == QuestionType.CHOICE && question.options() != null) {
                            boolean hasOtherOption = question.options().stream()
                                .anyMatch(FormConversionResponse.Option::isOther);
                            boolean isSectionDecidable = question.options().stream()
                                .anyMatch(opt -> opt.goToSectionOrder() != null);
                            int maxChoice = switch (question.type().toUpperCase()) {
                                case "CHECKBOX" -> question.maxChoice() != null ? question.maxChoice() : question.options().size(); // 체크박스는 여러 개 선택 가능
                                case "DROPDOWN", "MULTIPLE_CHOICE" -> 1; // 드롭다운, 객관식은 하나만 선택 가능
                                default -> 1; // 기본적으로 하나만 선택 가능하도록 설정
                            };

                            builder.maxChoice(maxChoice)
                                .hasNoneOption(false)
                                .hasCustomInput(hasOtherOption)
                                .isSectionDecidable(isSectionDecidable)
                                .options(question.options().stream()
                                    .filter(opt -> !opt.isOther())
                                    .map(opt -> OptionDto.builder()
                                        .content(opt.text())
                                        .nextSection(opt.goToSectionOrder())
                                        .imageUrl(opt.imageUrl())
                                        .build())
                                    .toList());
                        }

                        questionUpsertInfoList.add(builder.build());
                    }
                }
            }
        }

        if (!questionUpsertInfoList.isEmpty()) {
            QuestionUpsertDto questionUpsertDto = QuestionUpsertDto.builder()
                .surveyId(surveyId)
                .upsertInfoList(questionUpsertInfoList)
                .build();

            QuestionUpsertDto savedQuestions = questionCommand.upsertQuestionList(questionUpsertDto);

            // 4. Choice 문항의 옵션 저장
            List<OptionUpsertDto> optionUpsertDtoList = new ArrayList<>();
            List<QuestionUpsertDto.UpsertInfo> savedInfoList = savedQuestions.getUpsertInfoList();
            Map<Integer, QuestionUpsertDto.UpsertInfo> originalInfoMap = questionUpsertInfoList.stream()
                .collect(java.util.stream.Collectors.toMap(QuestionUpsertDto.UpsertInfo::getQuestionOrder, Function.identity()));

            for (QuestionUpsertDto.UpsertInfo savedInfo : savedInfoList) {
                QuestionUpsertDto.UpsertInfo originalInfo = originalInfoMap.get(savedInfo.getQuestionOrder());

                if (savedInfo.getQuestionType() == QuestionType.CHOICE && originalInfo.getOptions() != null) {
                    List<OptionDto> options = originalInfo.getOptions().stream()
                        .map(opt -> OptionDto.builder()
                            .questionId(savedInfo.getQuestionId())
                            .content(opt.getContent())
                            .nextSection(opt.getNextSection())
                            .imageUrl(opt.getImageUrl())
                            .build())
                        .toList();

                    optionUpsertDtoList.add(OptionUpsertDto.builder()
                        .questionId(savedInfo.getQuestionId())
                        .optionInfoList(options)
                        .build());
                }
            }

            if (!optionUpsertDtoList.isEmpty()) {
                questionCommand.upsertChoiceOptionList(optionUpsertDtoList);
            }
        }

        return surveyId;
    }

    public FormValidationResponse toResponse(FormValidationPostResponse dto) {
        if (dto == null || dto.results() == null) {
            return null;
        }

        List<FormValidationResponse.Result> results = dto.results().stream()
            .map(this::mapToResult)
            .toList();

        return new FormValidationResponse(results);
    }

    private FormValidationResponse.Result mapToResult(FormValidationPostResponse.Result r) {
        List<FormValidationResponse.Inconvertible> inconvertibles = mapInconvertible(r.inconvertibleDetails());

        if (r.isSuccess()) {
            return FormValidationResponse.success(
                r.url(),
                r.counts().total(),
                r.counts().convertible(),
                r.counts().unconvertible(),
                inconvertibles,
                mapConvertible(r.convertibleDetails())
            );
        }

        // 실패하거나 convertibleDetails가 없는 경우
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
            .map(u -> new FormValidationResponse.Inconvertible(u.title(), u.type(), u.reason()))
            .toList();
    }

    private List<FormValidationResponse.Convertible> mapConvertible(
        FormValidationPostResponse.Convertible details
    ) {
        if (details == null || details.sections().isEmpty()) return List.of();

        // 문항들을 섹션별로 그룹화
        Map<Integer, List<DefaultQuestionDto>> sectionOrderQuestionMap = details.questions().stream()
            .collect(Collectors.groupingBy(DefaultQuestionDto::getSection, Collectors.toList()));

        // 섹션 순회하며 변환
        return details.sections().stream()
            .map(c -> new FormValidationResponse.Convertible(
                c.title(), c.description(), c.order(), c.nextSection(),
                sectionOrderQuestionMap.getOrDefault(c.order(), List.of())
            ))
            .toList();
    }

    private QuestionType mapQuestionType(String googleFormType) {
        if (googleFormType == null) {
            return null;
        }

        return switch (googleFormType.toUpperCase()) {
            case "MULTIPLE_CHOICE", "CHECKBOX" -> QuestionType.CHOICE;
            case "SHORT_ANSWER", "SHORT" -> QuestionType.SHORT;
            case "PARAGRAPH", "LONG" -> QuestionType.LONG;
            case "LINEAR_SCALE", "SCALE" -> QuestionType.RATING;
            case "DATE" -> QuestionType.DATE;
            case "NUMBER" -> QuestionType.NUMBER;
            case "IMAGE" -> QuestionType.IMAGE;
            case "TITLE_DESCRIPTION" -> QuestionType.TITLE;
            default -> null;
        };
    }
}
