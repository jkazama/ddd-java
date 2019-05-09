package sample.model.asset;

import java.math.BigDecimal;
import java.util.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import lombok.*;
import sample.ActionStatusType;
import sample.context.*;
import sample.context.orm.*;
import sample.model.account.FiAccount;
import sample.model.asset.Cashflow.CashflowType;
import sample.model.asset.Cashflow.RegCashflow;
import sample.model.constraints.*;
import sample.model.constraints.Currency;
import sample.model.master.SelfFiAccount;
import sample.util.*;

/**
 * Cashflow action to ask for a transfer account activity.
 * low: It is a sample, a branch and a name, and considerably originally omit required information.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@NamedQueries({
        @NamedQuery(name = "CashInOut.findUnprocessed", query = "from CashInOut c where c.eventDay=?1 and c.statusType in ?2 order by c.id"),
        @NamedQuery(name = "CashInOut.findAccUnprocessed1", query = "from CashInOut c where c.accountId=?1 and c.currency=?2 and c.withdrawal=?3 and c.statusType in ?4 order by c.id"),
        @NamedQuery(name = "CashInOut.findAccUnprocessed2", query = "from CashInOut c where c.accountId=?1 and c.statusType in ?2 order by c.updateDate desc") })
public class CashInOut extends JpaActiveRecord<CashInOut> {

    private static final long serialVersionUID = 1L;

    @Id
    @IdStr
    private String id;
    @AccountId
    private String accountId;
    @Currency
    private String currency;
    @AbsAmount
    private BigDecimal absAmount;
    private boolean withdrawal;
    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "day", column = @Column(name = "request_day")),
            @AttributeOverride(name = "date", column = @Column(name = "request_date")) })
    private TimePoint requestDate;
    @Day
    private String eventDay;
    @Day
    private String valueDay;
    @IdStr
    private String targetFiCode;
    @AccountId
    private String targetFiAccountId;
    @IdStr
    private String selfFiCode;
    @AccountId
    private String selfFiAccountId;
    @NotNull
    @Enumerated(EnumType.STRING)
    private ActionStatusType statusType;
    @AccountId
    private String updateActor;
    @NotNull
    private Date updateDate;
    /** Set only with a processed status. */
    private Long cashflowId;

    /**
     * Processed status.
     * <p>Processed CashInOut and generate Cashflow.
     */
    public CashInOut process(final JpaRepository rep) {
        TimePoint now = rep.dh().time().tp();
        Validator v = validator();
        v.verify(statusType.isUnprocessed(), "error.ActionStatusType.unprocessing");
        v.verify(now.afterEqualsDay(eventDay), "error.CashInOut.afterEqualsDay");

        setStatusType(ActionStatusType.PROCESSED);
        setUpdateActor(rep.dh().actor().getId());
        setUpdateDate(now.getDate());
        setCashflowId(Cashflow.register(rep, regCf()).getId());
        return update(rep);
    }

    private RegCashflow regCf() {
        BigDecimal amount = withdrawal ? absAmount.negate() : absAmount;
        CashflowType cashflowType = withdrawal ? CashflowType.CashOut : CashflowType.CashIn;
        String remark = withdrawal ? Remarks.CashOut : Remarks.CashIn;
        return new RegCashflow(accountId, currency, amount, cashflowType, remark, eventDay, valueDay);
    }

    /** Cancelled status. */
    public CashInOut cancel(final JpaRepository rep) {
        TimePoint now = rep.dh().time().tp();
        Validator v = validator();
        v.verify(statusType.isUnprocessing(), "error.ActionStatusType.unprocessing");
        v.verify(now.beforeDay(eventDay), "error.CashInOut.beforeEqualsDay");

        setStatusType(ActionStatusType.CANCELLED);
        setUpdateActor(rep.dh().actor().getId());
        setUpdateDate(now.getDate());
        return update(rep);
    }

    /**
     * Mark error status.
     * low: Actually, Take error reasons in an argument and maintain it.
     */
    public CashInOut error(final JpaRepository rep) {
        validator().verify(statusType.isUnprocessed(), "error.ActionStatusType.unprocessing");

        setStatusType(ActionStatusType.ERROR);
        setUpdateActor(rep.dh().actor().getId());
        setUpdateDate(rep.dh().time().date());
        return update(rep);
    }

    public static CashInOut load(final JpaRepository rep, String id) {
        return rep.load(CashInOut.class, id);
    }

    /** low: criteria implementation example */
    public static List<CashInOut> find(final JpaRepository rep, final FindCashInOut p) {
        // low: check during a period of from/to if usual
        JpaCriteria<CashInOut> criteria = rep.criteria(CashInOut.class);
        criteria.equal("currency", p.getCurrency());
        criteria.in("statusType", p.getStatusTypes());
        criteria.between("updateDate", DateUtils.date(p.getUpdFromDay()),
                DateUtils.dateTo(p.getUpdToDay()));
        return rep.tmpl().find(criteria.sortDesc("updateDate").result());
    }

    public static List<CashInOut> findUnprocessed(final JpaRepository rep) {
        return rep.tmpl().find("CashInOut.findUnprocessed", rep.dh().time().day(), ActionStatusType.unprocessedTypes);
    }
    
    public static List<CashInOut> findUnprocessed(final JpaRepository rep, String accountId, String currency,
            boolean withdrawal) {
        return rep.tmpl().find("CashInOut.findAccUnprocessed1", accountId, currency, withdrawal,
                ActionStatusType.unprocessedTypes);
    }

    public static List<CashInOut> findUnprocessed(final JpaRepository rep, String accountId) {
        return rep.tmpl().find("CashInOut.findAccUnprocessed2", accountId, ActionStatusType.unprocessedTypes);
    }

    public static CashInOut withdraw(final JpaRepository rep, final RegCashOut p) {
        DomainHelper dh = rep.dh();
        TimePoint now = dh.time().tp();
        // low: It is often managed DB or properties.
        String eventDay = now.getDay();
        // low: T+N calculation that we consider the holiday of each financial institution / currency.
        String valueDay = dh.time().dayPlus(3);

        Validator v = new Validator();
        v.verifyField(0 < p.getAbsAmount().signum(), "absAmount", "error.domain.AbsAmount.zero");
        boolean canWithdraw = Asset.by(p.getAccountId()).canWithdraw(rep, p.getCurrency(), p.getAbsAmount(), valueDay);
        v.verifyField(canWithdraw, "absAmount", "error.CashInOut.withdrawAmount");

        String uid = dh.uid().generate(CashInOut.class.getSimpleName());
        FiAccount acc = FiAccount.load(rep, p.getAccountId(), Remarks.CashOut, p.getCurrency());
        SelfFiAccount selfAcc = SelfFiAccount.load(rep, Remarks.CashOut, p.getCurrency());
        String updateActor = dh.actor().getId();
        return p.create(now, uid, eventDay, valueDay, acc, selfAcc, updateActor).save(rep);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindCashInOut implements Dto {
        private static final long serialVersionUID = 1L;
        @CurrencyEmpty
        private String currency;
        private ActionStatusType[] statusTypes;
        @Day
        private String updFromDay;
        @Day
        private String updToDay;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegCashOut implements Dto {
        private static final long serialVersionUID = 1L;
        @AccountId
        private String accountId;
        @Currency
        private String currency;
        @AbsAmount
        private BigDecimal absAmount;

        public CashInOut create(final TimePoint now, String id, String eventDay, String valueDay, final FiAccount acc,
                final SelfFiAccount selfAcc, String updActor) {
            return new CashInOut(id, accountId, currency, absAmount, true, now, eventDay, valueDay, acc.getFiCode(),
                    acc.getFiAccountId(), selfAcc.getFiCode(), selfAcc.getFiAccountId(), ActionStatusType.UNPROCESSED,
                    updActor, now.getDate(), null);
        }
    }

}
