package OneQ.OnSurvey.domain.survey.service.export;

import OneQ.OnSurvey.domain.survey.model.export.SurveyExportFile;

public interface SurveyExport {
    SurveyExportFile exportCsv(Long surveyId);
}
