package OneQ.OnSurvey.global.auth.token;

import OneQ.OnSurvey.global.common.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TokenStore {

    public boolean acquireLock(String key, Duration ttl) {
        Boolean ok = RedisUtils.setValueIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(ok);
    }
    public void releaseLock(String key) {
        RedisUtils.deleteKeys(List.of(key));
    }
}
