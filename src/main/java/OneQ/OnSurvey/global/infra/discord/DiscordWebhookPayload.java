package OneQ.OnSurvey.global.infra.discord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscordWebhookPayload {
    private List<Embed> embeds;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Embed {
        private String title;
        private String description;
    }
}
