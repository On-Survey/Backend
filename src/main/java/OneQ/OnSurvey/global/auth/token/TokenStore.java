package OneQ.OnSurvey.global.auth.token;

import OneQ.OnSurvey.global.infra.redis.RedisAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TokenStore {

    private final RedisAgent redisAgent;

    public boolean acquireLock(String key, Duration ttl) {
        Boolean ok = redisAgent.setValueIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(ok);
    }
    public void releaseLock(String key) {
        redisAgent.deleteKeys(List.of(key));
    }
}
