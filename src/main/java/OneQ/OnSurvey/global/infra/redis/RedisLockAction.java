package OneQ.OnSurvey.global.infra.redis;

import org.redisson.api.RLock;
import org.redisson.client.RedisException;

import java.util.function.Supplier;

public interface RedisLockAction {

    RLock getLock(String lockKey);

    <R> R executeWithLock(String lockKey, long waitTIme, Supplier<R> action) throws InterruptedException, RedisException;

    <R> R executeNewTransactionAfterLock(String lockKey, long waitTime, Supplier<R> action) throws InterruptedException, RedisException;
}
