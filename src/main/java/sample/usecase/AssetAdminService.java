package sample.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sample.context.audit.AuditHandler;
import sample.context.lock.IdLockHandler;
import sample.context.lock.IdLockHandler.LockType;
import sample.context.orm.JpaRepository.DefaultRepository;
import sample.context.orm.TxTemplate;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.FindCashInOut;
import sample.model.asset.Cashflow;

/**
 * 資産ドメインに対する社内ユースケース処理。
 * 
 * @author jkazama
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssetAdminService {

    private final DefaultRepository rep;
    private final PlatformTransactionManager txm;
    private final AuditHandler audit;
    private final IdLockHandler idLock;

    /**
     * 振込入出金依頼を検索します。
     * low: 口座横断的なので割り切りでREADロックはかけません。
     */
    public List<CashInOut> findCashInOut(final FindCashInOut p) {
        return TxTemplate.of(txm).readOnly().tx(() -> {
            return CashInOut.find(rep, p);
        });
    }

    /**
     * 振込出金依頼を締めます。
     */
    public void closingCashOut() {
        audit.audit("振込出金依頼の締め処理をする", () -> {
            TxTemplate.of(txm).tx(() -> {
                closingCashOutInTx();
            });
        });
    }

    private void closingCashOutInTx() {
        // low: 以降の処理は口座単位でfilter束ねしてから実行する方が望ましい。
        // low: 大量件数の処理が必要な時はそのままやるとヒープが死ぬため、idソートでページング分割して差分実行していく。
        CashInOut.findUnprocessed(rep).forEach(cio -> {
            // low: TX内のロックが適切に動くかはIdLockHandlerの実装次第。
            // 調整が難しいようなら大人しく営業停止時間(IdLock必要な処理のみ非活性化されている状態)を作って、
            // ロック無しで一気に処理してしまう方がシンプル。
            idLock.call(cio.getAccountId(), LockType.Write, () -> {
                try {
                    cio.process(rep);
                    // low: SQLの発行担保。扱う情報に相互依存が無く、セッションキャッシュはリークしがちなので都度消しておく。
                    rep.flushAndClear();
                } catch (Exception e) {
                    log.error("[" + cio.getId() + "] 振込出金依頼の締め処理に失敗しました。", e);
                    try {
                        cio.error(rep);
                        rep.flush();
                    } catch (Exception ex) {
                        // low: 2重障害(恐らくDB起因)なのでloggerのみの記載に留める
                    }
                }
            });
        });
    }

    /**
     * キャッシュフローを実現します。
     * <p>
     * 受渡日を迎えたキャッシュフローを残高に反映します。
     */
    public void realizeCashflow() {
        audit.audit("キャッシュフローを実現する", () -> {
            TxTemplate.of(txm).tx(() -> realizeCashflowInTx());
        });
    }

    private void realizeCashflowInTx() {
        // low: 日回し後の実行を想定
        var day = rep.dh().time().day();
        for (final Cashflow cf : Cashflow.findDoRealize(rep, day)) {
            idLock.call(cf.getAccountId(), LockType.Write, () -> {
                try {
                    cf.realize(rep);
                    rep.flushAndClear();
                } catch (Exception e) {
                    log.error("[" + cf.getId() + "] キャッシュフローの実現に失敗しました。", e);
                    try {
                        cf.error(rep);
                        rep.flush();
                    } catch (Exception ex) {
                    }
                }
                return null;
            });
        }
    }

}
