package OneQ.OnSurvey.global.common.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public final class RedisUtils {

    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redisson;

    private static RedissonClient staticRedisson;
    private static StringRedisTemplate staticRedisTemplate;

    @PostConstruct
    public void init() {
        staticRedisson = this.redisson;
        staticRedisTemplate = this.redisTemplate;
    }

    private static RLock getLock(String lockKey) {
        return staticRedisson.getLock(lockKey);
    }

    /**
     * 락 획득 후 실행할 로직을 인자로 받아 분산락을 이용하여 실행하는 유틸리티 메서드
     * @param lockKey 분산락 설정을 위한 키
     * @param waitTIme 분산락 획득 대기시간 (단위: 초)
     * @param leaseTime 분산락 최대 점유시간 (단위: 초)
     * @param action 분산락 획득 후 실행할 로직
     * @return {@code action}의 실행 결과
     * @throws RedisException 락 획득 실패 시 예외
     * @throws InterruptedException 락 획득 대기 중 인터럽트 발생 시 예외
     */
    public static <R> R executeWithLock(
        String lockKey, long waitTIme, long leaseTime, Supplier<R> action
    ) throws InterruptedException, RedisException {
        RLock lock = getLock(lockKey);
        boolean available = lock.tryLock(waitTIme, leaseTime, TimeUnit.SECONDS);

        if (!available) {
            throw new RedisException("락 획득 실패");
        }

        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 저장된 값을 조회하는 유틸리티 메서드
     * @param key 조회할 키 (keyPrefix + id 형태로 사용)
     * @return String
     */
    public static String getValue(String key) {
        return staticRedisTemplate.opsForValue().get(key);
    }

    /**
     * 저장된 값을 int로 조회하는 유틸리티 메서드
     * @param key 조회할 키 (keyPrefix + id 형태로 사용)
     * @return int, 존재하지 않으면 0 반환
     */
    public static int getIntValue(String key) {
        String value = staticRedisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    /**
     * 저장된 값을 long으로 조회하는 유틸리티 메서드
     * @param key 조회할 키 (keyPrefix + id 형태로 사용)
     * @return long, 존재하지 않으면 0 반환
     */
    public static long getLongValue(String key) {
        String value = staticRedisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    /**
     * 값을 저장하는 유틸리티 메서드
     * @param key 저장할 키 (keyPrefix + id 형태로 사용)
     * @param value 저장할 값
     * @param ttl TTL, 예: {@code Duration.ofSeconds(60)} - 60초 동안 유효
     */
    public static void setValue(String key, String value, Duration ttl) {
        staticRedisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 키가 존재하지 않을 때에만 값을 저장하는 유틸리티 메서드
     * @param key 저장할 키 (keyPrefix + id 형태로 사용)
     * @param value 저장할 값
     * @param ttl TTL, 예: {@code Duration.ofSeconds(60)} - 60초 동안 유효
     * @return 값이 저장됨 : true
     * <p> 이미 키가 존재하여 저장되지 않음 : false
     */
    public static Boolean setValueIfAbsent(String key, String value, Duration ttl) {
        return staticRedisTemplate.opsForValue().setIfAbsent(key, value, ttl);
    }

    /**
     * 값을 1 증가시키는 유틸리티 메서드
     * @param key 조회할 키 (keyPrefix + id 형태로 사용)
     * @return 증가된 값
     */
    public static Long incrementValue(String key) {
        return staticRedisTemplate.opsForValue().increment(key);
    }

    /**
     * 값을 증가시키는 유틸리티 메서드
     * @param key 조회할 키 (keyPrefix + id 형태로 사용)
     * @param delta 증가시킬 값
     * @return 증가된 값
     */
    public static Long incrementValue(String key, long delta) {
        return staticRedisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 값을 1 감소시키는 유틸리티 메서드
     * @param key 조회할 키 (keyPrefix + id 형태로 사용)
     * @return 감소된 값
     */
    public static Long decrementValue(String key) {
        return staticRedisTemplate.opsForValue().decrement(key);
    }

    /**
     * 값을 감소시키는 유틸리티 메서드
     * @param key 조회할 키 (keyPrefix + id 형태로 사용)
     * @param delta 감소시킬 값
     * @return 감소된 값
     */
    public static Long decrementValue(String key, long delta) {
        return staticRedisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * 키를 삭제하는 유틸리티 메서드
     * @param keyList 삭제할 키 리스트 (keyPrefix + id 형태로 사용)
     */
    public static void deleteKeys(List<String> keyList) {
        if (keyList != null && !keyList.isEmpty()) {
            staticRedisTemplate.delete(keyList);
        }
    }

    /**
     * Sorted Set의 score 범위 내 요소 개수를 조회하는 유틸리티 메서드
     * @param key 조회할 키 (keyPrefix + id 형태로 사용)
     * @param min 조회할 범위(score)의 최소값
     * @param max 조회할 범위(score)의 최대값
     * @return score 범위 내 요소 개수, 존재하지 않으면 0 반환
     */
    public static long getZSetCount(String key, long min, long max) {
        Long count = staticRedisTemplate.opsForZSet().count(key, min, max);
        return count != null ? count : 0L;
    }

    /**
     * Sorted Set에서 특정 요소의 score를 조회하는 유틸리티 메서드
     * @param key 조회할 키 (keyPrefix + id 형태로 사용)
     * @param value 조회할 요소 값
     * @return 요소의 score, 존재하지 않으면 null 반환
     */
    public static Double getZSetScore(String key, String value) {
        return staticRedisTemplate.opsForZSet().score(key, value);
    }

    /**
     * Sorted Set에 요소를 추가하거나 갱신하는 유틸리티 메서드
     * @param key 요소를 추가/갱신할 키 (keyPrefix + id 형태로 사용)
     * @param value Sorted Set에 추가/갱신할 값
     * @param score Sorted Set에 추가/갱신할 값의 score
     */
    public static void addToZSet(String key, String value, long score) {
        staticRedisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * Sorted Set에 요소를 추가하는 유틸리티 메서드 (값을 갱신하지는 않음)
     * @param key - 요소를 추가할 키 (keyPrefix + id 형태로 사용)
     * @param value - Sorted Set에 추가할 값
     * @param score - Sorted Set에 추가할 값의 score
     */
    public static void addToZSetIfAbsent(String key, String value, long score) {
        staticRedisTemplate.opsForZSet().addIfAbsent(key, value, score);
    }

    /**
     * Sorted Set에서 특정 요소를 제거하는 유틸리티 메서드
     * @param key 삭제할 키 (keyPrefix + id 형태로 사용)
     * @param value 삭제할 요소 값
     */
    public static void removeFromZSet(String key, String value) {
        staticRedisTemplate.opsForZSet().remove(key, value);
    }

    /**
     * Sorted Set에서 score 범위 내 요소를 제거하는 유틸리티 메서드
     * @param key 삭제할 키 (keyPrefix + id 형태로 사용)
     * @param min 삭제할 범위(score)의 최소값
     * @param max 삭제할 범위(score)의 최대값
     */
    public static void rangeRemoveFromZSet(String key, long min, long max) {
        staticRedisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }
}
