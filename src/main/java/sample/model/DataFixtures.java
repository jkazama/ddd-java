package sample.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import sample.ActionStatusType;
import sample.context.Timestamper;
import sample.context.actor.Actor;
import sample.context.orm.*;
import sample.context.uid.IdGenerator;
import sample.model.account.*;
import sample.model.account.Account.AccountStatusType;
import sample.model.asset.*;
import sample.model.asset.Cashflow.*;
import sample.model.master.SelfFiAccount;
import sample.util.TimePoint;

/**
 * A support component for the data generation.
 * <p>It is aimed for master data generation at the time of a test and the development,
 * Please do not use it in the production.
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
        String baseDay = "20141118";
        time.daySet(baseDay);

        selfFiAcc(Remarks.CashOut, ccy).save(rep);

        // Account: sample
        String idSample = "sample";
        acc(idSample).save(rep);
        fiAcc(idSample, Remarks.CashOut, ccy).save(rep);
        cb(idSample, baseDay, ccy, "1000000").save(rep);
    }

    // account

    public Account acc(String id) {
        return new Account(id, id, "hoge@example.com", AccountStatusType.NORMAL);
    }

    public FiAccount fiAcc(String accountId, String category, String currency) {
        return new FiAccount(null, accountId, category, currency, category + "-" + currency, "FI" + accountId);
    }

    // asset

    public CashBalance cb(String accountId, String baseDay, String currency, String amount) {
        return new CashBalance(null, accountId, baseDay, currency, new BigDecimal(amount), new Date());
    }

    public Cashflow cf(String accountId, String amount, String eventDay, String valueDay) {
        return cfReg(accountId, amount, valueDay).create(TimePoint.by(eventDay), Actor.Anonymous.getId());
    }

    public RegCashflow cfReg(String accountId, String amount, String valueDay) {
        return new RegCashflow(accountId, "JPY", new BigDecimal(amount), CashflowType.CashIn, "cashIn", null, valueDay);
    }

    // eventDay(T+1) / valueDay(T+3)
    public CashInOut cio(String accountId, String absAmount, boolean withdrawal) {
        return new CashInOut(uid.generate(CashInOut.class.getSimpleName()), accountId, "JPY",
                new BigDecimal(absAmount), withdrawal, time.tp(), time.dayPlus(1), time.dayPlus(3), "tFiCode",
                "tFiAccId",
                "sFiCode", "sFiAccId", ActionStatusType.UNPROCESSED, "dummy", time.date(), null);
    }

    // master

    public SelfFiAccount selfFiAcc(String category, String currency) {
        return new SelfFiAccount(null, category, currency, category + "-" + currency, "xxxxxx");
    }

}
