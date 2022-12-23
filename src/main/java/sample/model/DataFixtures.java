package sample.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.annotation.PostConstruct;
import sample.ActionStatusType;
import sample.context.Timestamper;
import sample.context.actor.Actor;
import sample.context.orm.JpaRepository;
import sample.context.orm.TxTemplate;
import sample.context.uid.IdGenerator;
import sample.model.account.Account;
import sample.model.account.Account.AccountStatusType;
import sample.model.account.FiAccount;
import sample.model.asset.CashBalance;
import sample.model.asset.CashInOut;
import sample.model.asset.Cashflow;
import sample.model.asset.Cashflow.CashflowType;
import sample.model.asset.Cashflow.RegCashflow;
import sample.model.asset.Remarks;
import sample.model.master.SelfFiAccount;
import sample.util.TimePoint;

/**
 * データ生成用のサポートコンポーネント。
 * <p>
 * テストや開発時の簡易マスタデータ生成を目的としているため本番での利用は想定していません。
 * low: 実際の開発では開発/テスト環境のみ有効となるよう細かなプロファイル指定が必要となります。
 * 
 * @author jkazama
 */
@Component
public class DataFixtures {

    private final JpaRepository rep;
    private final PlatformTransactionManager txm;
    private final Timestamper time;
    private final IdGenerator uid;

    public DataFixtures(
            JpaRepository rep,
            PlatformTransactionManager txm,
            Timestamper time,
            IdGenerator uid) {
        this.rep = rep;
        this.txm = txm;
        this.time = time;
        this.uid = uid;
    }

    @PostConstruct
    public void initialize() {
        TxTemplate.of(txm).tx(() -> {
            initializeInTx();
            return null;
        });
    }

    public void initializeInTx() {
        String ccy = "JPY";
        LocalDate baseDay = LocalDate.of(2014, 11, 18);
        time.daySet(baseDay);

        // 自社金融機関
        selfFiAcc(Remarks.CashOut, ccy).save(rep);

        // 口座: sample
        String idSample = "sample";
        acc(idSample).save(rep);
        fiAcc(idSample, Remarks.CashOut, ccy).save(rep);
        cb(idSample, baseDay, ccy, "1000000").save(rep);
    }

    // account

    /** 口座の簡易生成 */
    public Account acc(String id) {
        return new Account(id, id, "hoge@example.com", AccountStatusType.NORMAL);
    }

    /** 口座に紐付く金融機関口座の簡易生成 */
    public FiAccount fiAcc(String accountId, String category, String currency) {
        return new FiAccount(null, accountId, category, currency, category + "-" + currency, "FI" + accountId);
    }

    // asset

    /** 口座残高の簡易生成 */
    public CashBalance cb(String accountId, LocalDate baseDay, String currency, String amount) {
        return new CashBalance(null, accountId, baseDay, currency, new BigDecimal(amount), LocalDateTime.now());
    }

    /** キャッシュフローの簡易生成 */
    public Cashflow cf(String accountId, String amount, LocalDate eventDay, LocalDate valueDay) {
        return cfReg(accountId, amount, valueDay).create(TimePoint.of(eventDay), Actor.Anonymous.id());
    }

    /** キャッシュフロー登録パラメタの簡易生成 */
    public RegCashflow cfReg(String accountId, String amount, LocalDate valueDay) {
        return RegCashflow.builder()
                .accountId(accountId)
                .currency("JPY")
                .amount(new BigDecimal(amount))
                .cashflowType(CashflowType.CashIn)
                .remark("cashIn")
                .valueDay(valueDay)
                .build();
    }

    /** 振込入出金依頼の簡易生成。 [発生日(T+1)/受渡日(T+3)] */
    public CashInOut cio(String accountId, String absAmount, boolean withdrawal) {
        TimePoint now = time.tp();
        var cb = new CashInOut();
        cb.setId(uid.generate(CashInOut.class.getSimpleName()));
        cb.setAccountId(accountId);
        cb.setCurrency("JPY");
        cb.setAbsAmount(new BigDecimal(absAmount));
        cb.setWithdrawal(withdrawal);
        cb.setRequestDay(now.getDay());
        cb.setRequestDate(now.getDate());
        cb.setEventDay(now.getDay().plusDays(1));
        cb.setValueDay(now.getDay().plusDays(3));
        cb.setTargetFiCode("tFiCode");
        cb.setTargetFiAccountId("tFiAccId");
        cb.setSelfFiCode("sFiCode");
        cb.setSelfFiAccountId("sFiAccId");
        cb.setStatusType(ActionStatusType.UNPROCESSED);
        cb.setUpdateActor("dummy");
        cb.setUpdateDate(now.getDate());
        cb.setCashflowId(null);
        return cb;
    }

    // master

    /** 自社金融機関口座の簡易生成 */
    public SelfFiAccount selfFiAcc(String category, String currency) {
        return new SelfFiAccount(null, category, currency, category + "-" + currency, "xxxxxx");
    }

}
