package OneQ.OnSurvey.global.infra.redis;

import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.common.exception.ErrorCode;
import OneQ.OnSurvey.global.infra.transaction.TransactionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/*
 Lettuce, Embedded Redis 등 다른 환경별 다른 Redis 클라이언트로 변경 시에 새로운 구현체를 구현하면 됨
 */
@Slf4j
@Component("RedisAgent")
@RequiredArgsConstructor
public class RedisAgent implements RedisLockAction, RedisCacheAction {

    private final RedissonClient redisson;
    private final StringRedisTemplate redisTemplate;
    private final TransactionHandler transactionhandler;

    /**
     * 락 획득을 위한 RLock 객체를 반환
     * @param lockKey 분산락 설정을 위한 키
     * @return RLock
     */
    public RLock getLock(String lockKey) {
        return redisson.getLock(lockKey);
    }

    /**
     * 락 획득 후 실행할 로직을 인자로 받아 분산락을 이용하여 실행
     * @param lockKey   분산락 설정을 위한 키
     * @param waitTime  분산락 획득 대기시간 (단위: 초)
     * @param leaseTime 분산락 최대 점유시간 (단위: 초)
     * @param action    분산락 획득 후 실행할 로직
     * @return {@code action}의 실행 결과
     * @throws RedisException       락 획득 실패 시 예외
     * @throws InterruptedException 락 획득 대기 중 인터럽트 발생 시 예외
     */
    public <R> R executeWithLock(
        String lockKey, long waitTime, long leaseTime, Supplier<R> action
    ) throws InterruptedException, RedisException {
        RLock lock = getLock(lockKey);
        boolean available = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);

        if (!available) {
            throw new RedisException("락 획득 실패");
        }

        try {
            return action.get();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 락 획득 후 실행할 트랜잭션 로직을 인자로 받아 분산락을 이용하여 실행
     * <p> 락 획득 후 DB에 접근하여 값을 수정하는 로직을 실행해야 할 때 사용,
     * <br> 강력한 일관성이 필요한 데이터에 대해서는 단순 조회 로직에도 사용해야 함
     * @param lockKey   분산락 설정을 위한 키
     * @param waitTime  분산락 획득 대기시간 (단위: 초)
     * @param leaseTime 분산락 최대 점유시간 (단위: 초)
     * @param action    분산락 획득 후 실행할 로직
     * @return {@code action}의 실행 결과
     * @throws RedisException       락 획득 실패 시 예외
     * @throws InterruptedException 락 획득 대기 중 인터럽트 발생 시 예외
     */
    public <R> R executeNewTransactionAfterLock(
        String lockKey, long waitTime, long leaseTime, Supplier<R> action
    ) throws InterruptedException, RedisException {
        RLock lock = getLock(lockKey);
        boolean available = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);

        if (!available) {
            throw new RedisException("락 획득 실패");
        }

        try {
            return transactionhandler.runInTransaction(action);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 저장된 값을 조회
     *
     * @param key 조회할 키 (keyPrefix + id 형태로 사용)
     * @return String
     */
    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 저장된 값을 int로 조회
     *
     * @param key 조회할 키 (keyPrefix + id 형태로 사용)
     * @return int, 존재하지 않으면 0 반환
     */
    public int getIntValue(String key) {
        String value = redisTemplate.opsForValue().get(key);
        try {
            return value != null ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException e) {
            log.warn("[RedisAgent] 캐시값 Integer 파싱 실패 - key: {}, value: {}", key, value);
            throw new CustomException(ErrorCode.SERVER_UNTRACKED_ERROR);
        }
    }

    /**
     * 저장된 값을 long으로 조회
     *
     * @param key 조회할 키 (keyPrefix + id 형태로 사용)
     * @return long, 존재하지 않으면 0 반환
     */
    public long getLongValue(String key) {
        String value = redisTemplate.opsForValue().get(key);
        try {
            return value != null ? Long.parseLong(value) : 0L;
        } catch (NumberFormatException e) {
            log.warn("[RedisAgent] 캐시값 Long 파싱 실패 - key: {}, value: {}", key, value);
            throw new CustomException(ErrorCode.SERVER_UNTRACKED_ERROR);
        }
    }

    /**
     * 값을 저장
     *
     * @param key   저장할 키 (keyPrefix + id 형태로 사용)
     * @param value 저장할 값
     * @param ttl   TTL, 예: {@code Duration.ofSeconds(60)} - 60초 동안 유효
     */
    public void setValue(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 키가 존재하지 않을 때에만 값을 저장
     *
     * @param key   저장할 키 (keyPrefix + id 형태로 사용)
     * @param value 저장할 값
     * @param ttl   TTL, 예: {@code Duration.ofSeconds(60)} - 60초 동안 유효
     * @return 값이 저장됨 : true
     * <p> 이미 키가 존재하여 저장되지 않음 : false
     */
    public Boolean setValueIfAbsent(String key, String value, Duration ttl) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
    }

    /**
     * 값을 1 증가
     *
     * @param key 조회할 키 (keyPrefix + id 형태로 사용)
     * @return 증가된 값
     */
    public Long incrementValue(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 값을 증가
     *
     * @param key   조회할 키 (keyPrefix + id 형태로 사용)
     * @param delta 증가시킬 값
     * @return 증가된 값
     */
    public Long incrementValue(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 값을 1 감소
     *
     * @param key 조회할 키 (keyPrefix + id 형태로 사용)
     * @return 감소된 값
     */
    public Long decrementValue(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 값을 감소
     *
     * @param key   조회할 키 (keyPrefix + id 형태로 사용)
     * @param delta 감소시킬 값
     * @return 감소된 값
     */
    public Long decrementValue(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * 키를 삭제
     *
     * @param keyList 삭제할 키 리스트 (keyPrefix + id 형태로 사용)
     */
    public void deleteKeys(List<String> keyList) {
        if (keyList != null && !keyList.isEmpty()) {
            redisTemplate.delete(keyList);
        }
    }

    /**
     * Sorted Set의 score 범위 내 요소 개수를 조회
     *
     * @param key 조회할 키 (keyPrefix + id 형태로 사용)
     * @param min 조회할 범위(score)의 최소값
     * @param max 조회할 범위(score)의 최대값
     * @return score 범위 내 요소 개수, 존재하지 않으면 0 반환
     */
    public long getZSetCount(String key, long min, long max) {
        Long count = redisTemplate.opsForZSet().count(key, min, max);
        return count != null ? count : 0L;
    }

    /**
     * Sorted Set에서 특정 요소의 score를 조회
     *
     * @param key   조회할 키 (keyPrefix + id 형태로 사용)
     * @param value 조회할 요소 값
     * @return 요소의 score, 존재하지 않으면 null 반환
     */
    public Double getZSetScore(String key, String value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * Sorted Set에 요소를 추가하거나 갱신
     *
     * @param key   요소를 추가/갱신할 키 (keyPrefix + id 형태로 사용)
     * @param value Sorted Set에 추가/갱신할 값
     * @param score Sorted Set에 추가/갱신할 값의 score
     */
    public void addToZSet(String key, String value, long score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * Sorted Set에 요소를 추가 (값을 갱신하지는 않음)
     *
     * @param key   - 요소를 추가할 키 (keyPrefix + id 형태로 사용)
     * @param value - Sorted Set에 추가할 값
     * @param score - Sorted Set에 추가할 값의 score
     */
    public void addToZSetIfAbsent(String key, String value, long score) {
        redisTemplate.opsForZSet().addIfAbsent(key, value, score);
    }

    /**
     * Sorted Set에서 특정 요소를 제거
     *
     * @param key   삭제할 키 (keyPrefix + id 형태로 사용)
     * @param value 삭제할 요소 값
     */
    public void removeFromZSet(String key, String value) {
        redisTemplate.opsForZSet().remove(key, value);
    }

    /**
     * Sorted Set에서 score 범위 내 요소를 제거
     *
     * @param key 삭제할 키 (keyPrefix + id 형태로 사용)
     * @param min 삭제할 범위(score)의 최소값
     * @param max 삭제할 범위(score)의 최대값
     */
    public void rangeRemoveFromZSet(String key, long min, long max) {
        redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }
}
