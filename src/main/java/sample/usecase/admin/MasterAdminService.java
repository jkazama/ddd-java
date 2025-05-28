package sample.usecase.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import sample.context.Timestamper;
import sample.context.audit.AuditHandler;
import sample.context.orm.OrmRepository;

/**
 * The use case processing for the master domain in the organization.
 */
@Service
@RequiredArgsConstructor
public class MasterAdminService {
    private final OrmRepository rep;
    @SuppressWarnings("unused")
    private final PlatformTransactionManager txm;
    private final AuditHandler audit;

    public void processDay() {
        audit.audit("Forward day.", () -> {
            Timestamper time = rep.dh().time();
            time.daySet(time.dayPlus(1));
        });
    }

}
