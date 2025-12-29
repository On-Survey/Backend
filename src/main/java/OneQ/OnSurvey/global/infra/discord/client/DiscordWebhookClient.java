package OneQ.OnSurvey.global.infra.discord.client;

import OneQ.OnSurvey.global.infra.discord.DiscordWebhookPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class DiscordWebhookClient {

    private final WebClient webClient;

    public Mono<Void> post(String url, DiscordWebhookPayload payload) {
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
                        .filter(ex -> ex instanceof WebClientResponseException.TooManyRequests))
                .onErrorResume(ex -> Mono.empty())
                .then();
    }
}
