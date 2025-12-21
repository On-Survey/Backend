package OneQ.OnSurvey.domain.survey.model.export;

public record SurveyExportFile(
        byte[] bytes,
        String filename,
        String contentType
) {}
