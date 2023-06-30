package sample.model.asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import sample.ActionStatusType;
import sample.context.DomainEntity;
import sample.context.Dto;
import sample.context.orm.OrmRepository;
import sample.model.DomainErrorKeys;
import sample.model.constraints.AccountId;
import sample.model.constraints.Amount;
import sample.model.constraints.Category;
import sample.model.constraints.Currency;
import sample.model.constraints.ISODate;
import sample.model.constraints.ISODateEmpty;
import sample.model.constraints.ISODateTime;
import sample.util.TimePoint;
import sample.util.Validator;

/**
 * Account activity cashflow.
 * The cashflow is account activity information of the decision state generated
 * by a cashflow action
 * such as the transfer (without the request cancellation).
 * low: The minimum columns with this sample
 */
@Entity
@Data
public class Cashflow implements DomainEntity {

    @Id
    @GeneratedValue
    private Long id;
    @AccountId
    private String accountId;
    @Currency
    private String currency;
    @Amount
    private BigDecimal amount;
    @NotNull
    @Enumerated
    private CashflowType cashflowType;
    @Category
    private String remark;
    @ISODate
    private LocalDate eventDay;
    @ISODateTime
    private LocalDateTime eventDate;
    @ISODate
    private LocalDate valueDay;
    @NotNull
    @Enumerated
    private ActionStatusType statusType;
    @AccountId
    private String updateActor;
    @ISODateTime
    private LocalDateTime updateDate;

    /** Make cashflow processed and reflect it to the balance. */
    public Cashflow realize(final OrmRepository rep) {
        TimePoint now = rep.dh().time().tp();
        Validator.validate(v -> {
            v.verify(canRealize(rep), AssetErrorKeys.CF_REALIZE_DAY);
            v.verify(statusType.isUnprocessing(), DomainErrorKeys.STATUS_PROCESSING);
        });

        this.setStatusType(ActionStatusType.PROCESSED);
        this.setUpdateActor(rep.dh().actor().id());
        this.setUpdateDate(now.getDate());
        rep.update(this);
        CashBalance.getOrNew(rep, accountId, currency).add(rep, amount);
        return this;
    }

    /**
     * Mark error status.
     * low: Actually, Take error reasons in an argument and maintain it.
     */
    public Cashflow error(final OrmRepository rep) {
        Validator.validate(v -> {
            v.verify(statusType.isUnprocessed(), DomainErrorKeys.STATUS_PROCESSING);
        });

        this.setStatusType(ActionStatusType.ERROR);
        this.setUpdateActor(rep.dh().actor().id());
        this.setUpdateDate(rep.dh().time().date());
        return rep.update(this);
    }

    public boolean canRealize(final OrmRepository rep) {
        return rep.dh().time().tp().afterEqualsDay(valueDay);
    }

    public static Cashflow load(final OrmRepository rep, Long id) {
        return rep.load(Cashflow.class, id);
    }

    public static List<Cashflow> findUnrealize(
            final OrmRepository rep, String accountId, String currency, LocalDate valueDay) {
        var jpql = """
                FROM Cashflow c
                WHERE c.accountId=?1 AND c.currency=?2
                 AND c.valueDay<=?3 AND c.statusType IN ?4
                ORDER BY c.id
                """;
        return rep.tmpl().find(jpql,
                accountId, currency, valueDay, ActionStatusType.UNPROCESSED_TYPES);
    }

    public static List<Cashflow> findDoRealize(final OrmRepository rep, LocalDate valueDay) {
        var jpql = """
                FROM Cashflow c
                WHERE c.valueDay=?1 AND c.statusType in ?2
                ORDER BY c.id
                """;
        return rep.tmpl().find(jpql, valueDay, ActionStatusType.UNPROCESSED_TYPES);
    }

    /**
     * Register cashflow.
     * <p>
     * Reached a value day, just reflect it to the balance.
     */
    public static Cashflow register(final OrmRepository rep, final RegCashflow p) {
        TimePoint now = rep.dh().time().tp();
        Validator.validate(v -> {
            v.checkField(now.beforeEqualsDay(p.valueDay()),
                    "valueDay", "error.Cashflow.beforeEqualsDay");
        });
        Cashflow cf = rep.save(p.create(now, rep.dh().actor().id()));
        return cf.canRealize(rep) ? cf.realize(rep) : cf;
    }

    /** Cashflow types. */
    public static enum CashflowType {
        CASH_IN,
        CASH_OUT,
        CASH_TRANSFER_IN,
        CASH_TRANSFER_OUT
    }

    @Builder
    public static record RegCashflow(
            @AccountId String accountId,
            @Currency String currency,
            @Amount BigDecimal amount,
            @NotNull CashflowType cashflowType,
            @Category String remark,
            @ISODateEmpty LocalDate eventDay,
            @ISODate LocalDate valueDay) implements Dto {

        public Cashflow create(final TimePoint now, String updActor) {
            var eventDate = eventDay == null ? now : new TimePoint(eventDay, now.getDate());
            var m = new Cashflow();
            m.setAccountId(accountId);
            m.setCurrency(currency);
            m.setAmount(amount);
            m.setCashflowType(cashflowType);
            m.setRemark(remark);
            m.setEventDay(eventDate.getDay());
            m.setEventDate(eventDate.getDate());
            m.setValueDay(valueDay);
            m.setStatusType(ActionStatusType.UNPROCESSED);
            m.setUpdateActor(updActor);
            m.setUpdateDate(now.getDate());
            return m;
        }
    }

}
