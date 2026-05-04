package OneQ.OnSurvey.domain.admin.application;

import OneQ.OnSurvey.domain.admin.domain.port.out.MemberPort;
import OneQ.OnSurvey.domain.admin.domain.port.out.SurveyPort;
import OneQ.OnSurvey.domain.admin.domain.repository.AdminRepository;
import OneQ.OnSurvey.domain.survey.model.export.SurveyExportFile;
import OneQ.OnSurvey.domain.survey.service.export.SurveyExport;
import OneQ.OnSurvey.global.promotion.port.out.PromotionGrantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminFacadeExportTest {

    @Mock AdminRepository adminRepository;
    @Mock MemberPort memberPort;
    @Mock SurveyPort surveyPort;
    @Mock PasswordEncoder passwordEncoder;
    @Mock PromotionGrantRepository promotionGrantRepository;
    @Mock SurveyExport surveyExport;

    @InjectMocks AdminFacade adminFacade;

    @Test
    void exportSurveyCsv_delegatesToSurveyExport() {
        byte[] csvBytes = "id,answer\n1,yes".getBytes();
        SurveyExportFile expected = new SurveyExportFile(csvBytes, "survey_20260504.csv", "text/csv; charset=UTF-8");
        when(surveyExport.exportCsv(42L, 0L)).thenReturn(expected);

        SurveyExportFile result = adminFacade.exportSurveyCsv(42L);

        assertThat(result.filename()).isEqualTo("survey_20260504.csv");
        assertThat(result.bytes()).isEqualTo(csvBytes);
        verify(surveyExport).exportCsv(42L, 0L);
    }
}
