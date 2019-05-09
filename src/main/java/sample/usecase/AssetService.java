package sample.usecase;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import sample.context.actor.Actor;
import sample.context.audit.AuditHandler;
import sample.context.lock.IdLockHandler;
import sample.context.orm.JpaRepository.DefaultRepository;
import sample.context.orm.TxTemplate;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.RegCashOut;
import sample.usecase.event.AppMailEvent;
import sample.usecase.event.AppMailEvent.AppMailType;

/**
 * The customer use case processing for the asset domain.
 */
@Service
public class AssetService {

    private final DefaultRepository rep;
    private final PlatformTransactionManager txm;
    private final AuditHandler audit;
    private final IdLockHandler idLock;
    private final ApplicationEventPublisher event;

    public AssetService(
            DefaultRepository rep,
            PlatformTransactionManager txm,
            AuditHandler audit,
            IdLockHandler idLock,
            ApplicationEventPublisher event) {
        this.rep = rep;
        this.txm = txm;
        this.audit = audit;
        this.idLock = idLock;
        this.event = event;
    }

    public List<CashInOut> findUnprocessedCashOut() {
        final String accId = actor().getId();
        return TxTemplate.of(txm).readOnly().readIdLock(idLock, accId).tx(
                () -> CashInOut.findUnprocessed(rep, accId));
    }

    private Actor actor() {
        return rep.dh().actor();
    }

    public String withdraw(final RegCashOut p) {
        return audit.audit("Requesting a withdrawal", () -> {
            p.setAccountId(actor().getId()); // The customer side overwrites in login users forcibly
            // low: Take account ID lock (WRITE) and transaction and handle transfer
            CashInOut cio = TxTemplate.of(txm).writeIdLock(idLock, actor().getId()).tx(
                    () -> CashInOut.withdraw(rep, p));
            // low: this service e-mail it and notify user.
            this.event.publishEvent(AppMailEvent.of(AppMailType.FinishRequestWithdraw, cio));
            return cio.getId();
        });
    }

}
