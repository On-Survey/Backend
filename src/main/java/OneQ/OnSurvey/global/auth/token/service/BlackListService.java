package OneQ.OnSurvey.global.auth.token.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class BlackListService {
    private final StringRedisTemplate redisTemplate;

    private String key(String tokenIdOrHash) {
        return "bl:jwt:" + tokenIdOrHash;
    }

    public void blacklist(String tokenIdOrHash, long ttlSeconds) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = key(tokenIdOrHash);
        ops.set(key, "1", Duration.ofSeconds(ttlSeconds));
    }

    public boolean isBlacklisted(String tokenIdOrHash) {
        return redisTemplate.hasKey(key(tokenIdOrHash));
    }
}
