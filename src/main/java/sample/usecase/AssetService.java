package sample.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import sample.context.lock.IdLockHandler.LockType;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.RegCashOut;

/**
 * 資産ドメインに対する顧客ユースケース処理。
 * 
 * @author jkazama
 */
@Service
public class AssetService extends ServiceSupport {

    /**
     * 未処理の振込依頼情報を検索します。
     * low: 参照系は口座ロックが必要無いケースであれば@Transactionalでも十分
     * low: CashInOutは情報過多ですがアプリケーション層では公開対象を特定しにくい事もあり、
     * UI層に最終判断を委ねています。
     */
    public List<CashInOut> findUnprocessedCashOut() {
        final String accId = actor().getId();
        return tx(accId, LockType.READ, () -> CashInOut.findUnprocessed(rep(), accId));
    }

    /**
     * 振込出金依頼をします。
     * low: 公開リスクがあるためUI層には必要以上の情報を返さない事を意識します。
     * low: 監査ログの記録は状態を変えうる更新系ユースケースでのみ行います。
     * low: ロールバック発生時にメールが飛ばないようにトランザクション境界線を明確に分離します。
     * @return 振込出金依頼ID
     */
    public String withdraw(final RegCashOut p) {
        return audit().audit("振込出金依頼をします", () -> {
            p.setAccountId(actor().getId()); // 顧客側はログイン利用者で強制上書き
            // low: 口座IDロック(WRITE)とトランザクションをかけて振込処理
            CashInOut cio = tx(actor().getId(), LockType.WRITE, () -> CashInOut.withdraw(rep(), p));
            // low: トランザクション確定後に出金依頼を受付した事をメール通知します。
            mail().sendWithdrawal(cio);
            return cio.getId();
        });
    }

}
