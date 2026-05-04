package OneQ.OnSurvey.domain.admin.api;

import OneQ.OnSurvey.domain.admin.application.AdminFacade;
import OneQ.OnSurvey.domain.survey.model.export.SurveyExportFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerExportTest {

    @Mock AdminFacade adminFacade;
    @InjectMocks AdminController adminController;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    void exportSurveyCsv_returns200WithAttachmentHeader() throws Exception {
        byte[] csvBytes = "﻿age,gender\n30,MALE\n".getBytes();
        SurveyExportFile file = new SurveyExportFile(csvBytes, "my_survey_20260504.csv", "text/csv; charset=UTF-8");
        when(adminFacade.exportSurveyCsv(7L)).thenReturn(file);

        mockMvc.perform(get("/v1/admin/surveys/7/export"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("my_survey_20260504.csv")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().bytes(csvBytes));
    }
}
