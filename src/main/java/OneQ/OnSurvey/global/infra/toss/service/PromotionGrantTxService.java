package OneQ.OnSurvey.global.infra.toss.service;

import OneQ.OnSurvey.global.infra.toss.PromotionGrant;
import OneQ.OnSurvey.global.infra.toss.repository.PromotionGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class PromotionGrantTxService {

    private final PromotionGrantRepository repo;

    /** 낙관락 충돌 시 짧게 재시도 */
    private <T> T retryOptimistic(Supplier<T> work) {
        int tries = 0;
        long[] waits = {50, 100, 150};
        while (true) {
            try {
                return work.get();
            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                if (tries >= waits.length) throw e;
                try { Thread.sleep(waits[tries++]); } catch (InterruptedException ignored) {}
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPending(Long grantId, String execKey) {
        retryOptimistic(() -> {
            PromotionGrant g = repo.findById(grantId).orElseThrow();
            if (execKey != null) g.withExecKey(execKey);
            g.pending();
            repo.saveAndFlush(g);
            return null;
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(Long grantId) {
        retryOptimistic(() -> {
            PromotionGrant g = repo.findById(grantId).orElseThrow();
            g.success();
            repo.saveAndFlush(g);
            return null;
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFail(Long grantId) {
        retryOptimistic(() -> {
            PromotionGrant g = repo.findById(grantId).orElseThrow();
            g.fail();
            repo.saveAndFlush(g);
            return null;
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveExecKey(Long grantId, String execKey) {
        retryOptimistic(() -> {
            PromotionGrant g = repo.findById(grantId).orElseThrow();
            g.withExecKey(execKey);
            repo.saveAndFlush(g);
            return null;
        });
    }
}
