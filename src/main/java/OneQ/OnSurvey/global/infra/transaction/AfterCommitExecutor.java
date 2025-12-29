package OneQ.OnSurvey.global.infra.transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
public class AfterCommitExecutor {

    public void run(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        task.run();
                    } catch (Exception e) {
                        log.error("[AfterCommitExecutor] afterCommit task failed", e);
                    }
                }
            });
        } else {
            try {
                task.run();
            } catch (Exception e) {
                log.error("[AfterCommitExecutor] non-tx task failed", e);
            }
        }
    }
}
