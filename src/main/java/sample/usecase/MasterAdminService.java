package sample.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import sample.context.Timestamper;
import sample.context.audit.AuditHandler;
import sample.context.orm.JpaRepository.DefaultRepository;

/**
 * The use case processing for the master domain in the organization.
 */
@Service
public class MasterAdminService {
    private final DefaultRepository rep;
    @SuppressWarnings("unused")
    private final PlatformTransactionManager txm;
    private final AuditHandler audit;

    public MasterAdminService(
            DefaultRepository rep,
            PlatformTransactionManager txm,
            AuditHandler audit) {
        this.rep = rep;
        this.txm = txm;
        this.audit = audit;
    }

    public void processDay() {
        audit.audit("Forward day.", () -> {
            Timestamper time = rep.dh().time();
            time.daySet(time.dayPlus(1));
        });
    }

}
