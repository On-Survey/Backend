package OneQ.OnSurvey.global.infra.toss.service;

import OneQ.OnSurvey.global.infra.toss.PromotionGrant;
import OneQ.OnSurvey.global.infra.toss.repository.PromotionGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PromotionGrantTxService {

    private final PromotionGrantRepository repo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPending(PromotionGrant g, String execKey) {
        if (execKey != null) g.withExecKey(execKey);
        g.pending();
        repo.save(g);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(PromotionGrant g) {
        g.success();
        repo.save(g);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFail(PromotionGrant g) {
        g.fail();
        repo.save(g);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveExecKey(PromotionGrant g, String execKey) {
        g.withExecKey(execKey);
        repo.save(g);
    }
}
