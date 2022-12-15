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
 * 資産ドメインに対する顧客ユースケース処理。
 * 
 * @author jkazama
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

    /**
     * 未処理の振込依頼情報を検索します。
     * low: 参照系は口座ロックが必要無いケースであれば@Transactionalでも十分
     * low: CashInOutは情報過多ですがアプリケーション層では公開対象を特定しにくい事もあり、
     * UI層に最終判断を委ねています。
     */
    public List<CashInOut> findUnprocessedCashOut() {
        final String accId = actor().id();
        return TxTemplate.of(txm).readOnly().readIdLock(idLock, accId).tx(() -> {
            return CashInOut.findUnprocessed(rep, accId);
        });
    }

    /** 利用者を返します。 */
    private Actor actor() {
        return rep.dh().actor();
    }

    /**
     * 振込出金依頼をします。
     * low: 公開リスクがあるためUI層には必要以上の情報を返さない事を意識します。
     * low: 監査ログの記録は状態を変えうる更新系ユースケースでのみ行います。
     * low: ロールバック発生時にメールが飛ばないようにトランザクション境界線を明確に分離します。
     * 
     * @return 振込出金依頼ID
     */
    public String withdraw(final RegCashOut p) {
        return audit.audit("振込出金依頼をします", () -> {
            // low: 口座IDロック(WRITE)とトランザクションをかけて振込処理
            CashInOut cio = TxTemplate.of(txm).writeIdLock(idLock, actor().id()).tx(() -> {
                return CashInOut.withdraw(rep, p);
            });
            // low: トランザクション確定後に出金依頼を受付した事をメール通知します。
            this.event.publishEvent(AppMailEvent.of(AppMailType.FinishRequestWithdraw, cio));
            return cio.getId();
        });
    }

}
