package sample.model.asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import sample.ActionStatusType;
import sample.context.DomainEntity;
import sample.context.DomainHelper;
import sample.context.Dto;
import sample.context.orm.JpqlBuilder;
import sample.context.orm.OrmRepository;
import sample.model.DomainErrorKeys;
import sample.model.account.FiAccount;
import sample.model.asset.Cashflow.CashflowType;
import sample.model.asset.Cashflow.RegCashflow;
import sample.model.constraints.AbsAmount;
import sample.model.constraints.AccountId;
import sample.model.constraints.Currency;
import sample.model.constraints.CurrencyEmpty;
import sample.model.constraints.ISODate;
import sample.model.constraints.ISODateTime;
import sample.model.constraints.IdStr;
import sample.model.master.SelfFiAccount;
import sample.util.TimePoint;
import sample.util.Validator;

/**
 * Cashflow action to ask for a transfer account activity.
 * low: It is a sample, a branch and a name, and considerably originally omit
 * required information.
 */
@Entity
@Data
public class CashInOut implements DomainEntity {
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
    @ISODate
    private LocalDate requestDay;
    @ISODateTime
    private LocalDateTime requestDate;
    @ISODate
    private LocalDate eventDay;
    @ISODate
    private LocalDate valueDay;
    @IdStr
    private String targetFiCode;
    @AccountId
    private String targetFiAccountId;
    @IdStr
    private String selfFiCode;
    @AccountId
    private String selfFiAccountId;
    @NotNull
    @Enumerated
    private ActionStatusType statusType;
    @AccountId
    private String updateActor;
    @ISODateTime
    private LocalDateTime updateDate;
    /** Set only with a processed status. */
    private Long cashflowId;

    /**
     * Processed status.
     * <p>
     * Processed CashInOut and generate Cashflow.
     */
    public CashInOut process(final OrmRepository rep) {
        TimePoint now = rep.dh().time().tp();
        Validator.validate(v -> {
            v.verify(statusType.isUnprocessed(), DomainErrorKeys.STATUS_PROCESSING);
            v.verify(now.afterEqualsDay(eventDay), AssetErrorKeys.CIO_EVENT_DAY_AFTER_EQUALS_DAY);
        });
        this.setStatusType(ActionStatusType.PROCESSED);
        this.setUpdateActor(rep.dh().actor().id());
        this.setUpdateDate(now.getDate());
        this.setCashflowId(Cashflow.register(rep, regCf()).getId());
        return rep.update(this);
    }

    private RegCashflow regCf() {
        BigDecimal amount = withdrawal ? absAmount.negate() : absAmount;
        CashflowType cashflowType = withdrawal ? CashflowType.CASH_OUT : CashflowType.CASH_IN;
        String remark = withdrawal ? Remarks.CASH_OUT : Remarks.CASH_IN;
        return RegCashflow.builder()
                .accountId(accountId)
                .currency(currency)
                .amount(amount)
                .cashflowType(cashflowType)
                .remark(remark)
                .eventDay(eventDay)
                .valueDay(valueDay)
                .build();
    }

    /** Cancelled status. */
    public CashInOut cancel(final OrmRepository rep) {
        TimePoint now = rep.dh().time().tp();
        Validator.validate(v -> {
            v.verify(statusType.isUnprocessing(), DomainErrorKeys.STATUS_PROCESSING);
            v.verify(now.beforeDay(eventDay), AssetErrorKeys.CIO_EVENT_DAY_BEFORE_EQUALS_DAY);
        });
        this.setStatusType(ActionStatusType.CANCELLED);
        this.setUpdateActor(rep.dh().actor().id());
        this.setUpdateDate(now.getDate());
        return rep.update(this);
    }

    /**
     * Mark error status.
     * low: Actually, Take error reasons in an argument and maintain it.
     */
    public CashInOut error(final OrmRepository rep) {
        Validator.validate(v -> {
            v.verify(statusType.isUnprocessed(), DomainErrorKeys.STATUS_PROCESSING);
        });

        this.setStatusType(ActionStatusType.ERROR);
        this.setUpdateActor(rep.dh().actor().id());
        this.setUpdateDate(rep.dh().time().date());
        return rep.update(this);
    }

    public static CashInOut load(final OrmRepository rep, String id) {
        return rep.load(CashInOut.class, id);
    }

    /** low: JpqlBuilder implementation example */
    public static List<CashInOut> find(final OrmRepository rep, final FindCashInOut p) {
        var jpql = JpqlBuilder.of("FROM CashInOut cio")
                .equal("cio.currency", p.currency())
                .in("cio.statusType", p.statusTypes())
                .between("cio.eventDay", p.updFromDay(), p.updToDay())
                .orderBy("cio.updateDate DESC");
        return rep.tmpl().find(jpql.build(), jpql.args());
    }

    @Builder
    public static record FindCashInOut(
            @CurrencyEmpty String currency,
            Set<ActionStatusType> statusTypes,
            @ISODate LocalDate updFromDay,
            @ISODate LocalDate updToDay) implements Dto {

        @AssertTrue(message = DomainErrorKeys.BEFORE_EQUALS_DAY)
        public boolean isUpdFromDay() {
            if (this.updFromDay == null || this.updToDay == null) {
                return true;
            }
            return this.updFromDay.isBefore(this.updToDay)
                    || this.updFromDay.isEqual(this.updToDay);
        }
    }

    public static List<CashInOut> findUnprocessed(final OrmRepository rep) {
        var jpql = """
                FROM CashInOut cio
                WHERE cio.eventDay=?1 AND cio.statusType IN ?2
                ORDER BY cio.id
                """;
        return rep.tmpl().find(jpql, rep.dh().time().day(), ActionStatusType.UNPROCESSED_TYPES);
    }

    public static List<CashInOut> findUnprocessed(
            final OrmRepository rep, String accountId, String currency, boolean withdrawal) {
        var jpql = """
                FROM CashInOut cio
                WHERE cio.accountId=?1 AND cio.currency=?2
                 AND cio.withdrawal=?3 AND cio.statusType IN ?4
                 ORDER BY cio.id
                """;
        return rep.tmpl().find(
                jpql, accountId, currency, withdrawal, ActionStatusType.UNPROCESSED_TYPES);
    }

    public static List<CashInOut> findUnprocessed(final OrmRepository rep, String accountId) {
        var jpql = """
                FROM CashInOut cio
                WHERE cio.accountId=?1 AND cio.statusType IN ?2
                ORDER BY cio.updateDate DESC
                """;
        return rep.tmpl().find(
                jpql, accountId, ActionStatusType.UNPROCESSED_TYPES);
    }

    public static CashInOut withdraw(final OrmRepository rep, final RegCashOut p) {
        DomainHelper dh = rep.dh();
        TimePoint now = dh.time().tp();
        // low: It is often managed DB or properties.
        LocalDate eventDay = now.getDay();
        // low: T+N calculation that we consider the holiday of each financial
        // institution / currency.
        LocalDate valueDay = dh.time().dayPlus(3);

        Validator.validate(v -> {
            v.verifyField(dh.actor().id().equals(p.accountId()),
                    "accountId", DomainErrorKeys.ENTITY_NOT_FOUND);
            v.verifyField(0 < p.absAmount().signum(), "absAmount", "error.domain.AbsAmount.zero");
            boolean canWithdraw = Asset.of(p.accountId())
                    .canWithdraw(rep, p.currency(), p.absAmount(), valueDay);
            v.verifyField(canWithdraw, "absAmount", AssetErrorKeys.CIO_WITHDRAWAL_AMOUNT);
        });
        String uid = dh.uid().generate(CashInOut.class.getSimpleName());
        var acc = FiAccount.load(rep, p.accountId(), Remarks.CASH_OUT, p.currency());
        var selfAcc = SelfFiAccount.load(rep, Remarks.CASH_OUT, p.currency());
        String updateActor = dh.actor().id();
        return rep.save(p.create(now, uid, eventDay, valueDay, acc, selfAcc, updateActor));
    }

    @Builder
    public static record RegCashOut(
            @AccountId String accountId,
            @Currency String currency,
            @AbsAmount BigDecimal absAmount) implements Dto {

        public CashInOut create(
                final TimePoint now, String id, LocalDate eventDay, LocalDate valueDay,
                final FiAccount acc, final SelfFiAccount selfAcc, String updActor) {
            var m = new CashInOut();
            m.setId(id);
            m.setAccountId(accountId);
            m.setCurrency(currency);
            m.setAbsAmount(absAmount);
            m.setWithdrawal(true);
            m.setRequestDay(now.getDay());
            m.setRequestDate(now.getDate());
            m.setEventDay(eventDay);
            m.setValueDay(valueDay);
            m.setTargetFiCode(acc.getFiCode());
            m.setTargetFiAccountId(acc.getFiAccountId());
            m.setSelfFiCode(selfAcc.getFiCode());
            m.setSelfFiAccountId(selfAcc.getFiAccountId());
            m.setStatusType(ActionStatusType.UNPROCESSED);
            m.setUpdateActor(updActor);
            m.setUpdateDate(now.getDate());
            m.setCashflowId(null);
            return m;
        }
    }

}
