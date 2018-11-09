package sample.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import sample.context.Timestamper;
import sample.context.audit.AuditHandler;
import sample.context.orm.JpaRepository.DefaultRepository;

/**
 * サービスマスタドメインに対する社内ユースケース処理。
 * 
 * @author jkazama
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

    /**
     * 営業日を進めます。
     * low: 実際はスレッドセーフの考慮やDB連携含めて、色々とちゃんと作らないとダメです。
     */
    public void processDay() {
        audit.audit("営業日を進める", () -> {
            Timestamper time = rep.dh().time();
            time.daySet(time.dayPlus(1));
        });
    }

}
