package OneQ.OnSurvey.global.infra.discord;

import OneQ.OnSurvey.global.common.util.JwtDecodeUtils;
import OneQ.OnSurvey.global.infra.discord.client.DiscordWebhookClient;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.PaymentCompletedAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveyConversionAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveySubmittedAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.TossAccessTokenAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DiscordAlarmService {

    private final DiscordWebhookClient discordClient;

    private static final String APP_PKG_PREFIX = "OneQ.Onsurvey";
    private static final int APP_FRAMES = 12;
    private static final int OTHER_FRAMES = 5;
    private static final int MAX_EMBED_DESC = 1800;
    private static final String TRUNC_SUFFIX = "\n... (truncated)";

    @Value("${discord.enabled:false}")
    private boolean enabled;

    @Value("${discord.error-alert-url:}")
    private String errorWebhookUrl;

    @Value("${discord.payment-alert-url:}")
    private String paymentWebhookUrl;

    @Value("${discord.survey-alert-url:}")
    private String surveyWebhookUrl;

    @Value("${discord.test-toss-auth-url:}")
    private String tossAuthTestWebhookUrl;

    @Value("${discord.survey-conversion-alert-url:}")
    private String surveyConversionWebhookUrl;

    public void sendErrorAlert(Throwable e, String method, String path, String query) {
        if (!enabled || errorWebhookUrl == null || errorWebhookUrl.isBlank()) return;

        String title = "🚨 미등록 예외 발생";
        String desc  = buildDescription(e, method, path, query);

        post(errorWebhookUrl, title, desc);
    }

    public void sendPaymentCompletedAlert(PaymentCompletedAlert completedAlert) {
        if (!enabled) return;

        String url = (paymentWebhookUrl != null && !paymentWebhookUrl.isBlank())
                ? paymentWebhookUrl
                : errorWebhookUrl;
        if (url == null || url.isBlank()) return;

        String title = "✅ 결제 완료 · 코인 지급";
        String desc =
                "• userKey: `" + completedAlert.userKey() + "`\n" +
                        "• orderId: `" + safe(completedAlert.orderId()) + "`\n" +
                        "• amount: `" + completedAlert.amount() + "` (KRW==COIN)\n" +
                        "• paidAt: `" + safe(completedAlert.paidAt()) + "`\n" +
                        "• paymentPurpose: `" + safe(completedAlert.paymentPurpose().name()) + "`\n" +
                        "• newBalance: `" + completedAlert.newBalance() + "`\n";

        post(url, title, desc);
    }

    public void sendSurveySubmittedAlert(SurveySubmittedAlert a) {
        if (!enabled) return;

        String url = (surveyWebhookUrl != null && !surveyWebhookUrl.isBlank())
                ? surveyWebhookUrl
                : errorWebhookUrl;
        if (url == null || url.isBlank()) return;

        String title = "📝 설문 제출 완료";
        String desc =
                "• userKey: `" + a.userKey() + "`\n" +
                        "• surveyId: `" + a.surveyId() + "`\n" +
                        "• title: `" + safe(a.title()) + "`\n" +
                        "• totalCoin: `" + a.totalCoin() + "`\n" +
                        "• dueCount: `" + a.dueCount() + "`\n" +
                        "• deadline: `" + a.deadline() + "`\n" +
                        "• isFree: `" + a.isFree() + "`\n" +
                        "• gender: `" + a.gender() + "`\n" +
                        "• ages: `" + a.ages() + "`\n";

        post(url, title, desc);
    }

    public void sendTossAccessTokenAlert(TossAccessTokenAlert a) {
        if (!enabled) return;

        String url = (tossAuthTestWebhookUrl != null && !tossAuthTestWebhookUrl.isBlank())
                ? tossAuthTestWebhookUrl
                : errorWebhookUrl;
        if (url == null || url.isBlank()) return;

        Map<String, Object> value = JwtDecodeUtils.decodePayload(a.accessToken());

        String title = "🔔 Toss 비정상 AccessToken 테스트 알림";
        String desc = value.get("error") != null ? "Toss IAP AccessToken 이 비어있습니다. \n" :
            "Toss IAP AccessToken 갱신이 비정상적으로 이루어지고 있습니다. \n" +
            " ERROR: `" + safe(a.errorCode()) + "` - `" + safe(a.errorReason()) + "`\n" +
            "  • accessToken: `" + maskKey(safe(a.accessToken())) + "`\n" +
            "  • String exp: `" + value.get("exp") + "`\n" +
            "  • String iss: `" + value.get("iss") + "`\n" +
            "  • String scope: `" + value.get("scope") + "`\n";

        post(url, title, desc);
    }

    public void sendSurveyConversionAlert(SurveyConversionAlert alert) {
        if (!enabled) return;

        String url = (surveyConversionWebhookUrl != null && !surveyConversionWebhookUrl.isBlank())
                ? surveyConversionWebhookUrl
                : errorWebhookUrl;
        if (url == null || url.isBlank()) return;

        String title;
        StringBuilder desc = new StringBuilder();
        if (alert.isSuccess()) {
            title = "📊 설문 변환 성공";
            desc.append("* 설문 변환 시도: ").append(alert.totalCount()).append('\n')
                .append("* 설문 변환 성공: ").append(alert.successCount()).append('\n')
                .append("* 설문 변환 상세 :\n");
            for (SurveyConversionAlert.SurveyDetails d : alert.details()) {

                desc.append("\u3000[\n")
                    .append("\u3000 URL: ").append(safe(d.url())).append('\n')
                    .append("\u3000 tittle: ").append(safe(d.title())).append('\n')
                    .append("\u3000 surveyId: ").append(d.surveyId()).append('\n')
                    .append("\u3000 memberId: ").append(d.memberId()).append('\n')
                    .append("\u3000 questionCount: ").append(d.questionCount()).append('\n');
                if (!d.unsupportedList().isEmpty()) {
                    desc.append("  * 변환 실패 질문:\n");
                    for (SurveyConversionAlert.SurveyDetails.UnsupportedQuestion q : d.unsupportedList()) {
                        desc.append("    * order: ").append(q.order())
                            .append(", type: ").append(safe(q.type()))
                            .append(", reason: ").append(safe(q.reason())).append('\n');
                    }
                }
                desc.append("\u3000 ],\n");
            }
        } else {
            title = "⚠️ 설문 전환 실패";
            desc.append("* 설문 변환 시도: ").append(alert.totalCount()).append('\n')
                .append("* 설문 변환 성공: ").append(alert.successCount()).append('\n')
                .append("* error: ").append(safe(alert.error())).append("`\n");
        }

        post(url, title, desc.toString());
    }


    private String maskKey(String key) {
        return JwtDecodeUtils.maskToken(key);
    }

    private void post(String url, String title, String desc) {
        DiscordWebhookPayload payload =
                new DiscordWebhookPayload(List.of(new DiscordWebhookPayload.Embed(title, desc)));

        discordClient.post(url, payload).subscribe();
    }

    private String buildDescription(Throwable e, String method, String path, String query) {
        StringBuilder header = new StringBuilder();

        if (method != null || path != null) {
            header.append("• Endpoint: `").append(nullToEmpty(method)).append(" ").append(nullToEmpty(path)).append("`\n");
        }
        if (query != null && !query.isBlank()) {
            header.append("• Query: `").append(safe(query)).append("`\n");
        }
        header.append("• Type: `").append(e.getClass().getName()).append("`\n");
        header.append("• Message: `").append(safe(e.getMessage())).append("`\n\n");

        String stack = formatStack(rootCause(e));

        int overhead = header.length() + 6;
        int budget = Math.max(0, MAX_EMBED_DESC - overhead);
        boolean truncated = false;

        if (stack.length() > budget) {
            int sliceBudget = Math.max(0, budget - TRUNC_SUFFIX.length());
            stack = stack.substring(0, sliceBudget);
            truncated = true;
        }

        StringBuilder desc = new StringBuilder(header)
                .append("```").append(stack).append("```");
        if (truncated) desc.append(TRUNC_SUFFIX);

        return desc.toString();
    }

    private Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur;
    }

    private String formatStack(Throwable root) {
        StackTraceElement[] frames = root.getStackTrace();

        List<StackTraceElement> app = new ArrayList<>();
        List<StackTraceElement> others = new ArrayList<>();

        for (StackTraceElement f : frames) {
            if (f.getClassName().startsWith(APP_PKG_PREFIX)) {
                if (app.size() < APP_FRAMES) app.add(f);
            } else {
                if (others.size() < OTHER_FRAMES) others.add(f);
            }
            if (app.size() >= APP_FRAMES && others.size() >= OTHER_FRAMES) break;
        }

        StringBuilder sb = new StringBuilder();
        for (StackTraceElement f : app) sb.append("at ").append(f).append("\n");
        if (!others.isEmpty()) {
            sb.append("--- non-app frames ---\n");
            for (StackTraceElement f : others) sb.append("at ").append(f).append("\n");
        }

        int shown = app.size() + others.size();
        int remaining = Math.max(0, frames.length - shown);
        if (remaining > 0) sb.append("... (+").append(remaining).append(" more)");
        return sb.toString();
    }

    private String safe(String s) { return s == null ? "" : s; }
    private String nullToEmpty(String s) { return s == null ? "" : s; }
}
