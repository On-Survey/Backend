package OneQ.OnSurvey.domain.survey.service.export;

import OneQ.OnSurvey.domain.survey.model.export.SurveyAnswerProjection;
import OneQ.OnSurvey.domain.survey.model.export.SurveyExportFile;
import OneQ.OnSurvey.domain.survey.model.export.SurveyMemberProjection;
import OneQ.OnSurvey.domain.survey.model.export.SurveyQuestionHeader;
import OneQ.OnSurvey.domain.survey.repository.export.SurveyExportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyExportService implements SurveyExport {

    private final SurveyExportRepository surveyExportRepository;

    @Override
    @Transactional(readOnly = true)
    public SurveyExportFile exportCsv(Long surveyId) {
        List<SurveyQuestionHeader> headers = surveyExportRepository.findQuestionHeaders(surveyId);
        List<SurveyMemberProjection> members = surveyExportRepository.findMembersWhoAnswered(surveyId);
        List<SurveyAnswerProjection> answers = surveyExportRepository.findAnswers(surveyId);

        // memberId -> (questionId -> content)
        Map<Long, Map<Long, String>> answerMap = new HashMap<>();
        for (SurveyAnswerProjection a : answers) {
            answerMap.computeIfAbsent(a.getMemberId(), k -> new HashMap<>())
                    .put(a.getQuestionId(), a.getContent());
        }

        StringBuilder sb = new StringBuilder();

        // 1) 헤더
        List<String> headerCols = new ArrayList<>();
        headerCols.add("age");
        headerCols.add("gender");
        headerCols.add("residence");
        for (SurveyQuestionHeader h : headers) {
            headerCols.add("Q" + nvlInt(h.getOrderNo()) + ". " + nvl(h.getTitle()));
        }
        sb.append(String.join(",", escapeCsv(headerCols))).append("\n");

        // 2) 로우: member 1명 = 1행
        for (SurveyMemberProjection m : members) {
            List<String> row = new ArrayList<>();

            Integer age = toAge(m.getBirthDay());
            row.add(age == null ? "" : String.valueOf(age));
            row.add(toKoreanGender(m.getGender()));
            row.add(nvl(m.getResidence()));

            Map<Long, String> memberAnswers = answerMap.getOrDefault(m.getMemberId(), Map.of());
            for (SurveyQuestionHeader h : headers) {
                row.add(nvl(memberAnswers.get(h.getQuestionId())));
            }

            sb.append(String.join(",", escapeCsv(row))).append("\n");
        }

        // 3) UTF-8 BOM (엑셀 한글 깨짐 방지)
        byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, out, 0, bom.length);
        System.arraycopy(body, 0, out, bom.length, body.length);

        String filename = "survey-" + surveyId + "-export.csv";
        return new SurveyExportFile(out, filename, "text/csv; charset=UTF-8");
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
     * birthDay(String) -> age(Integer)
     * - "2003-02-22", "20030222", "2003/02/22" 등 최대한 대응
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

    private String toKoreanGender(String genderRaw) {
        if (genderRaw == null) return "";
        String g = genderRaw.trim().toUpperCase();
        return switch (g) {
            case "MALE", "M", "MAN" -> "남";
            case "FEMALE", "F", "WOMAN" -> "여";
            default -> genderRaw;
        };
    }
}
