package OneQ.OnSurvey.domain.survey.service.export;

import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.export.SurveyAnswerProjection;
import OneQ.OnSurvey.domain.survey.model.export.SurveyExportFile;
import OneQ.OnSurvey.domain.survey.model.export.SurveyMemberProjection;
import OneQ.OnSurvey.domain.survey.model.export.SurveyQuestionHeader;
import OneQ.OnSurvey.domain.survey.repository.export.SurveyExportRepository;
import OneQ.OnSurvey.domain.survey.repository.surveyInfo.SurveyInfoRepository;
import OneQ.OnSurvey.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import static OneQ.OnSurvey.domain.survey.SurveyErrorCode.SURVEY_FORBIDDEN;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyExportService implements SurveyExport {

    private final SurveyExportRepository surveyExportRepository;
    private final SurveyInfoRepository surveyInfoRepository;

    @Override
    public SurveyExportFile exportCsvForAdmin(Long surveyId) {
        log.info("[SurveyExport] CSV export start (admin). surveyId={}", surveyId);
        return buildCsv(surveyId);
    }

    @Override
    public SurveyExportFile exportCsv(Long surveyId, Long requesterMemberId) {
        log.info("[SurveyExport] CSV export start. surveyId={}", surveyId);

        if (!surveyExportRepository.existsOwnedSurvey(surveyId, requesterMemberId)) {
            throw new CustomException(SURVEY_FORBIDDEN);
        }

        return buildCsv(surveyId);
    }

    private SurveyExportFile buildCsv(Long surveyId) {
        try {
            SurveyInfo surveyInfo = surveyInfoRepository.findBySurveyId(surveyId)
                    .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_INFO_NOT_FOUND));
            boolean includeGender = shouldIncludeGender(surveyInfo);
            boolean includeAge = shouldIncludeAge(surveyInfo);

            List<SurveyQuestionHeader> headers = surveyExportRepository.findQuestionHeaders(surveyId);
            List<SurveyMemberProjection> members = surveyExportRepository.findMembersWhoAnswered(surveyId);
            List<SurveyAnswerProjection> answers = surveyExportRepository.findAnswers(surveyId);

            log.info("[SurveyExport] fetched. surveyId={}, questions={}, members={}, answers={}",
                    surveyId, headers.size(), members.size(), answers.size());

            Map<Long, Map<Long, Map<Integer, Set<String>>>> answerMap = buildAnswerMap(answers);

            StringBuilder sb = new StringBuilder();
            sb.append(String.join(",", escapeCsv(buildHeaderColumns(headers, includeAge, includeGender)))).append("\n");
            for (SurveyMemberProjection m : members) {
                sb.append(String.join(",", escapeCsv(buildDataRow(m, headers, answerMap, includeAge, includeGender)))).append("\n");
            }

            byte[] csv = addBom(sb.toString().getBytes(StandardCharsets.UTF_8));
            String title = surveyExportRepository.findSurveyTitle(surveyId);
            String date = LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
            String filename = sanitizeFileName(title == null ? "survey" : title) + "_" + date + ".csv";

            log.info("[SurveyExport] CSV export success. surveyId={}, bytes={}, filename={}", surveyId, csv.length, filename);
            return new SurveyExportFile(csv, filename, "text/csv; charset=UTF-8");
        } catch (Exception e) {
            log.error("[SurveyExport] CSV export failed. surveyId={}", surveyId, e);
            throw e;
        }
    }

    private Map<Long, Map<Long, Map<Integer, Set<String>>>> buildAnswerMap(List<SurveyAnswerProjection> answers) {
        Map<Long, Map<Long, Map<Integer, Set<String>>>> map = new HashMap<>();
        for (SurveyAnswerProjection a : answers) {
            map.computeIfAbsent(a.getMemberId(), k -> new HashMap<>())
               .computeIfAbsent(a.getQuestionId(), k -> new HashMap<>())
               .computeIfAbsent(a.getGridRowOrder(), k -> new TreeSet<>())
               .add(a.getContent());
        }
        return map;
    }

    private List<String> buildHeaderColumns(List<SurveyQuestionHeader> headers, boolean includeAge, boolean includeGender) {
        List<String> cols = new ArrayList<>();
        if (includeAge) cols.add("age");
        if (includeGender) cols.add("gender");
        cols.add("residence");
        for (SurveyQuestionHeader h : headers) {
            int orderLabel = h.getOrderNo() == null ? 0 : h.getOrderNo() + 1;
            cols.add("Q" + orderLabel + ". " + nvl(h.getTitle()) + gridNvl(h.getRowContent()));
        }
        return cols;
    }

    private List<String> buildDataRow(SurveyMemberProjection m, List<SurveyQuestionHeader> headers,
                                      Map<Long, Map<Long, Map<Integer, Set<String>>>> answerMap,
                                      boolean includeAge, boolean includeGender) {
        List<String> row = new ArrayList<>();
        if (includeAge) {
            Integer age = toAge(m.getBirthDay());
            row.add(age == null ? "" : String.valueOf(age));
        }
        if (includeGender) {
            row.add(nvl(m.getGender()));
        }
        row.add(nvl(m.getResidence()));

        Map<Long, Map<Integer, Set<String>>> memberAnswers = answerMap.getOrDefault(m.getMemberId(), Map.of());
        for (SurveyQuestionHeader h : headers) {
            row.add(nvl(String.join(",", memberAnswers
                    .getOrDefault(h.getQuestionId(), Map.of())
                    .getOrDefault(h.getRowOrder(), Set.of()))));
        }
        return row;
    }

    private byte[] addBom(byte[] body) {
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] out = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, out, 0, bom.length);
        System.arraycopy(body, 0, out, bom.length, body.length);
        return out;
    }

    private boolean shouldIncludeGender(SurveyInfo info) {
        if (info == null) return false;
        if (info.getGender() == null) return false;
        return info.getGender() != Gender.ALL;
    }

    private boolean shouldIncludeAge(SurveyInfo info) {
        if (info == null) return false;
        Set<AgeRange> ages = info.getAges();
        if (ages == null || ages.isEmpty()) return false;
        return !ages.contains(AgeRange.ALL);
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private String gridNvl(String s) {
        return s == null ? "" : " [" + s + "]";
    }

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

    private Integer toAge(String birthDay) {
        if (birthDay == null || birthDay.isBlank()) return null;
        String digits = birthDay.replaceAll("[^0-9]", "");
        if (digits.length() < 4) return null;
        try {
            int year = Integer.parseInt(digits.substring(0, 4));
            return LocalDate.now().getYear() - year + 1;
        } catch (Exception e) {
            return null;
        }
    }

    private String sanitizeFileName(String raw) {
        String s = raw.trim();
        s = s.replaceAll("[\\\\/:*?\"<>|\\r\\n\\t]", " ");
        s = s.replaceAll("\\s+", " ").trim();
        if (s.length() > 50) s = s.substring(0, 50).trim();
        return s.isBlank() ? "survey" : s;
    }
}
