package OneQ.OnSurvey.global.infra.redis;

import java.time.Duration;
import java.util.List;

public interface RedisCacheAction {

    String getValue(String key);

    int getIntValue(String key);

    long getLongValue(String key);

    void setValue(String key, String value, Duration ttl);

    Boolean setValueIfAbsent(String key, String value, Duration ttl);

    Long incrementValue(String key);

    Long incrementValue(String key, long delta);

    Long decrementValue(String key);

    Long decrementValue(String key, long delta);

    void deleteKeys(List<String> keyList);

    long getZSetCount(String key, long min, long max);

    Double getZSetScore(String key, String value);

    void addToZSet(String key, String value, long score);

    void addToZSetIfAbsent(String key, String value, long score);

    void removeFromZSet(String key, String value);

    void rangeRemoveFromZSet(String key, long min, long max);
}
