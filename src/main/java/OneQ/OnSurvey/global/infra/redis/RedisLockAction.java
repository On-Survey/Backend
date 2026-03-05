package OneQ.OnSurvey.global.infra.redis;

import org.redisson.api.RLock;
import org.redisson.client.RedisException;

import java.util.function.Supplier;

public interface RedisLockAction {

    RLock getLock(String lockKey);

    <R> R executeWithLock(String lockKey, long waitTIme, long leaseTime, Supplier<R> action) throws InterruptedException, RedisException;
}
