package sample.model.asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import sample.ActionStatusType;
import sample.context.DomainEntity;
import sample.context.DomainHelper;
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
@Table("CASHFLOW")
@Builder
public record Cashflow(
        @Id String id,
        @AccountId String accountId,
        @Currency String currency,
        @Amount BigDecimal amount,
        @NotNull CashflowType cashflowType,
        @Category String remark,
        @ISODate LocalDate eventDay,
        @ISODateTime LocalDateTime eventDate,
        @ISODate LocalDate valueDay,
        @NotNull ActionStatusType statusType,
        @AccountId String updateActor,
        @ISODateTime LocalDateTime updateDate) implements DomainEntity {

    public CashflowBuilder copyBuilder() {
        return Cashflow.builder()
                .id(this.id)
                .accountId(this.accountId)
                .currency(this.currency)
                .amount(this.amount)
                .cashflowType(this.cashflowType)
                .remark(this.remark)
                .eventDay(this.eventDay)
                .eventDate(this.eventDate)
                .valueDay(this.valueDay)
                .statusType(this.statusType)
                .updateActor(this.updateActor)
                .updateDate(this.updateDate);
    }

    /** Make cashflow processed and reflect it to the balance. */
    public Cashflow realize(final OrmRepository rep) {
        TimePoint now = rep.dh().time().tp();
        Validator.validate(v -> {
            v.verify(canRealize(rep), AssetErrorKeys.CF_REALIZE_DAY);
            v.verify(statusType.isUnprocessing(), DomainErrorKeys.STATUS_PROCESSING);
        });

        Cashflow updatedCashflow = rep.update(this.copyBuilder()
                .statusType(ActionStatusType.PROCESSED)
                .updateActor(rep.dh().actor().id())
                .updateDate(now.date())
                .build());
        CashBalance.getOrNew(rep, accountId, currency).add(rep, amount);
        return updatedCashflow;
    }

    /**
     * Mark error status.
     * low: Actually, Take error reasons in an argument and maintain it.
     */
    public Cashflow error(final OrmRepository rep) {
        Validator.validate(v -> {
            v.verify(statusType.isUnprocessed(), DomainErrorKeys.STATUS_PROCESSING);
        });

        return rep.update(this.copyBuilder()
                .statusType(ActionStatusType.ERROR)
                .updateActor(rep.dh().actor().id())
                .updateDate(rep.dh().time().date())
                .build());
    }

    public boolean canRealize(final OrmRepository rep) {
        return rep.dh().time().tp().afterEqualsDay(valueDay);
    }

    public static Cashflow load(final OrmRepository rep, String id) {
        return rep.load(Cashflow.class, id);
    }

    public static List<Cashflow> findUnrealize(
            final OrmRepository rep, String accountId, String currency, LocalDate valueDay) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        return rep.tmpl().find(Cashflow.class, criteria -> criteria
                .and("accountId").is(accountId)
                .and("currency").is(currency)
                .and("valueDay").lessThanOrEquals(valueDay)
                .and("statusType").in(ActionStatusType.UNPROCESSED_TYPES), sort);
    }

    public static List<Cashflow> findDoRealize(final OrmRepository rep, LocalDate valueDay) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        return rep.tmpl().find(Cashflow.class, criteria -> criteria
                .and("valueDay").is(valueDay)
                .and("statusType").in(ActionStatusType.UNPROCESSED_TYPES), sort);
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
        Cashflow cf = rep.save(p.create(rep.dh()));
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

        public Cashflow create(final DomainHelper dh) {
            var now = dh.time().tp();
            var id = dh.uid().generate(Cashflow.class.getSimpleName());
            var eventDate = eventDay == null ? now : new TimePoint(eventDay, now.date());
            var updActor = dh.actor().id();
            return Cashflow.builder()
                    .id(id)
                    .accountId(accountId)
                    .currency(currency)
                    .amount(amount)
                    .cashflowType(cashflowType)
                    .remark(remark)
                    .eventDay(eventDate.day())
                    .eventDate(eventDate.date())
                    .valueDay(valueDay)
                    .statusType(ActionStatusType.UNPROCESSED)
                    .updateActor(updActor)
                    .updateDate(now.date())
                    .build();
        }
    }

}
