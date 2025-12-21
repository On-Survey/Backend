package OneQ.OnSurvey.domain.survey.service.export;

import OneQ.OnSurvey.domain.survey.model.export.SurveyAnswerProjection;
import OneQ.OnSurvey.domain.survey.model.export.SurveyExportFile;
import OneQ.OnSurvey.domain.survey.model.export.SurveyMemberProjection;
import OneQ.OnSurvey.domain.survey.model.export.SurveyQuestionHeader;
import OneQ.OnSurvey.domain.survey.repository.export.SurveyExportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyExportService implements SurveyExport {

    private final SurveyExportRepository surveyExportRepository;

    @Override
    @Transactional(readOnly = true)
    public SurveyExportFile exportCsv(Long surveyId) {
        log.info("[SurveyExport] CSV export start. surveyId={}", surveyId);

        try {
            List<SurveyQuestionHeader> headers = surveyExportRepository.findQuestionHeaders(surveyId);
            List<SurveyMemberProjection> members = surveyExportRepository.findMembersWhoAnswered(surveyId);
            List<SurveyAnswerProjection> answers = surveyExportRepository.findAnswers(surveyId);

            log.info("[SurveyExport] fetched. surveyId={}, questions={}, members={}, answers={}",
                    surveyId, headers.size(), members.size(), answers.size());

            Map<Long, Map<Long, String>> answerMap = new HashMap<>();
            for (SurveyAnswerProjection a : answers) {
                answerMap.computeIfAbsent(a.getMemberId(), k -> new HashMap<>())
                        .put(a.getQuestionId(), a.getContent());
            }

            StringBuilder sb = new StringBuilder();

            // header
            List<String> headerCols = new ArrayList<>();
            headerCols.add("age");
            headerCols.add("gender");
            headerCols.add("residence");
            for (SurveyQuestionHeader h : headers) {
                headerCols.add("Q" + nvlInt(h.getOrderNo()) + ". " + nvl(h.getTitle()));
            }
            sb.append(String.join(",", escapeCsv(headerCols))).append("\n");

            // rows
            for (SurveyMemberProjection m : members) {
                List<String> row = new ArrayList<>();

                Integer age = toAge(m.getBirthDay());
                row.add(age == null ? "" : String.valueOf(age));
                row.add(m.getGender());
                row.add(nvl(m.getResidence()));

                Map<Long, String> memberAnswers = answerMap.getOrDefault(m.getMemberId(), Map.of());
                for (SurveyQuestionHeader h : headers) {
                    row.add(nvl(memberAnswers.get(h.getQuestionId())));
                }

                sb.append(String.join(",", escapeCsv(row))).append("\n");
            }

            // UTF-8 BOM
            byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
            byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
            byte[] out = new byte[bom.length + body.length];
            System.arraycopy(bom, 0, out, 0, bom.length);
            System.arraycopy(body, 0, out, bom.length, body.length);

            String filename = "survey-" + surveyId + "-export.csv";
            log.info("[SurveyExport] CSV export success. surveyId={}, bytes={}, filename={}",
                    surveyId, out.length, filename);

            return new SurveyExportFile(out, filename, "text/csv; charset=UTF-8");
        } catch (Exception e) {
            log.error("[SurveyExport] CSV export failed. surveyId={}", surveyId, e);
            throw e;
        }
    }

    private String nvl(String s) { return s == null ? "" : s; }
    private String nvlInt(Integer i) { return i == null ? "" : String.valueOf(i); }

    private List<String> escapeCsv(List<String> vals) {
        List<String> out = new ArrayList<>(vals.size());
        for (String v : vals) out.add(escapeCsvOne(v));
        return out;
    }

    private String escapeCsvOne(String s) {
        if (s == null) return "";
        String v = s.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\n") || v.contains("\r") || v.contains("\"")) {
            return "\"" + v + "\"";
        }
        return v;
    }

    /**
     * birthDay 기반 age 계산
     */
    private Integer toAge(String birthDay) {
        if (birthDay == null || birthDay.isBlank()) return null;

        String digits = birthDay.replaceAll("[^0-9]", "");
        if (digits.length() < 4) return null;

        int year;
        int month = 1;
        int day = 1;

        try {
            year = Integer.parseInt(digits.substring(0, 4));
            if (digits.length() >= 6) month = Integer.parseInt(digits.substring(4, 6));
            if (digits.length() >= 8) day = Integer.parseInt(digits.substring(6, 8));

            LocalDate dob = LocalDate.of(year, month, day);
            return Period.between(dob, LocalDate.now()).getYears();
        } catch (Exception e) {
            return null;
        }
    }
}
