package sample.model.asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import sample.ActionStatusType;
import sample.context.DomainEntity;
import sample.context.DomainHelper;
import sample.context.Dto;
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
@Table("CASH_IN_OUT")
@Builder
public record CashInOut(
        @Id @IdStr String id,
        @AccountId String accountId,
        @Currency String currency,
        @AbsAmount BigDecimal absAmount,
        boolean withdrawal,
        @ISODate LocalDate requestDay,
        @ISODateTime LocalDateTime requestDate,
        @ISODate LocalDate eventDay,
        @ISODate LocalDate valueDay,
        @IdStr String targetFiCode,
        @AccountId String targetFiAccountId,
        @IdStr String selfFiCode,
        @AccountId String selfFiAccountId,
        @NotNull ActionStatusType statusType,
        @AccountId String updateActor,
        @ISODateTime LocalDateTime updateDate,
        /** Set only with a processed status. */
        String cashflowId) implements DomainEntity {

    public CashInOutBuilder copyBuilder() {
        return CashInOut.builder()
                .id(this.id)
                .accountId(this.accountId)
                .currency(this.currency)
                .absAmount(this.absAmount)
                .withdrawal(this.withdrawal)
                .requestDay(this.requestDay)
                .requestDate(this.requestDate)
                .eventDay(this.eventDay)
                .valueDay(this.valueDay)
                .targetFiCode(this.targetFiCode)
                .targetFiAccountId(this.targetFiAccountId)
                .selfFiCode(this.selfFiCode)
                .selfFiAccountId(this.selfFiAccountId)
                .statusType(this.statusType)
                .updateActor(this.updateActor)
                .updateDate(this.updateDate)
                .cashflowId(this.cashflowId);
    }

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

        return rep.update(this.copyBuilder()
                .statusType(ActionStatusType.PROCESSED)
                .updateActor(rep.dh().actor().id())
                .updateDate(now.date())
                .cashflowId(Cashflow.register(rep, regCf()).id())
                .build());
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

        return rep.update(this.copyBuilder()
                .statusType(ActionStatusType.CANCELLED)
                .updateActor(rep.dh().actor().id())
                .updateDate(now.date())
                .build());
    }

    /**
     * Mark error status.
     * low: Actually, Take error reasons in an argument and maintain it.
     */
    public CashInOut error(final OrmRepository rep) {
        Validator.validate(v -> {
            v.verify(statusType.isUnprocessed(), DomainErrorKeys.STATUS_PROCESSING);
        });

        return rep.update(this.copyBuilder()
                .statusType(ActionStatusType.ERROR)
                .updateActor(rep.dh().actor().id())
                .updateDate(rep.dh().time().date())
                .build());
    }

    public static CashInOut load(final OrmRepository rep, String id) {
        return rep.load(CashInOut.class, id);
    }

    /** Criteria API implementation example */
    public static List<CashInOut> find(final OrmRepository rep, final FindCashInOut p) {
        Sort sort = Sort.by(Sort.Direction.DESC, "updateDate");
        return rep.tmpl().find(CashInOut.class, criteria -> {
            var c = criteria;
            if (p.currency() != null && !p.currency().isEmpty()) {
                c = c.and("currency").is(p.currency());
            }
            if (p.statusTypes() != null && !p.statusTypes().isEmpty()) {
                c = c.and("statusType").in(p.statusTypes());
            }
            if (p.updFromDay() != null) {
                c = c.and("eventDay").greaterThanOrEquals(p.updFromDay());
            }
            if (p.updToDay() != null) {
                c = c.and("eventDay").lessThanOrEquals(p.updToDay());
            }
            return c;
        }, sort);
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
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        return rep.tmpl().find(CashInOut.class, criteria -> criteria
                .and("eventDay").is(rep.dh().time().day())
                .and("statusType").in(ActionStatusType.UNPROCESSED_TYPES), sort);
    }

    public static List<CashInOut> findUnprocessed(
            final OrmRepository rep, String accountId, String currency, boolean withdrawal) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        return rep.tmpl().find(CashInOut.class, criteria -> criteria
                .and("accountId").is(accountId)
                .and("currency").is(currency)
                .and("withdrawal").is(withdrawal)
                .and("statusType").in(ActionStatusType.UNPROCESSED_TYPES), sort);
    }

    public static List<CashInOut> findUnprocessed(final OrmRepository rep, String accountId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "updateDate");
        return rep.tmpl().find(CashInOut.class, criteria -> criteria
                .and("accountId").is(accountId)
                .and("statusType").in(ActionStatusType.UNPROCESSED_TYPES), sort);
    }

    public static CashInOut withdraw(final OrmRepository rep, final RegCashOut p) {
        DomainHelper dh = rep.dh();
        TimePoint now = dh.time().tp();
        // low: It is often managed DB or properties.
        LocalDate eventDay = now.day();
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
            return CashInOut.builder()
                    .id(id)
                    .accountId(accountId)
                    .currency(currency)
                    .absAmount(absAmount)
                    .withdrawal(true)
                    .requestDay(now.day())
                    .requestDate(now.date())
                    .eventDay(eventDay)
                    .valueDay(valueDay)
                    .targetFiCode(acc.fiCode())
                    .targetFiAccountId(acc.fiAccountId())
                    .selfFiCode(selfAcc.fiCode())
                    .selfFiAccountId(selfAcc.fiAccountId())
                    .statusType(ActionStatusType.UNPROCESSED)
                    .updateActor(updActor)
                    .updateDate(now.date())
                    .cashflowId(null)
                    .build();
        }
    }

}
