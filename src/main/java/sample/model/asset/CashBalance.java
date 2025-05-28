package sample.model.asset;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
import sample.context.DomainEntity;
import sample.context.orm.OrmRepository;
import sample.model.constraints.AccountId;
import sample.model.constraints.Amount;
import sample.model.constraints.Currency;
import sample.model.constraints.ISODate;
import sample.model.constraints.ISODateTime;
import sample.util.Calculator;
import sample.util.TimePoint;

/**
 * The account balance.
 */
@Table("CASH_BALANCE")
@Builder
public record CashBalance(
        @Id String id,
        @AccountId String accountId,
        @ISODate LocalDate baseDay,
        @Currency String currency,
        @Amount BigDecimal amount,
        @ISODateTime LocalDateTime updateDate) implements DomainEntity {

    public CashBalanceBuilder copyBuilder() {
        return CashBalance.builder()
                .id(this.id)
                .accountId(this.accountId)
                .baseDay(this.baseDay)
                .currency(this.currency)
                .amount(this.amount)
                .updateDate(this.updateDate);
    }

    /**
     * low: Use Currency here, but the real number of the currency figures and
     * fraction processing definition
     * are managed with DB or the configuration file.
     */
    public CashBalance add(final OrmRepository rep, BigDecimal addAmount) {
        int scale = java.util.Currency.getInstance(currency).getDefaultFractionDigits();
        RoundingMode mode = RoundingMode.DOWN;
        BigDecimal newAmount = Calculator.init(amount).scale(scale, mode).add(addAmount).decimal();
        return rep.update(this.copyBuilder()
                .amount(newAmount)
                .updateDate(rep.dh().time().date())
                .build());
    }

    /**
     * Acquire the balance of the designated account.
     * (when I do not exist, acquire it after carrying forward preservation)
     * low: The appropriate consideration and examination of multiple currencies are
     * omitted.
     */
    public static CashBalance getOrNew(final OrmRepository rep, String accountId, String currency) {
        LocalDate baseDay = rep.dh().time().day();
        List<CashBalance> list = rep.tmpl().find(CashBalance.class, criteria -> criteria
                .and("accountId").is(accountId)
                .and("currency").is(currency)
                .and("baseDay").is(baseDay));

        if (list.isEmpty()) {
            return create(rep, accountId, currency);
        } else {
            return list.stream()
                    .max(Comparator.comparing(CashBalance::baseDay))
                    .orElse(list.get(0));
        }
    }

    private static CashBalance create(final OrmRepository rep, String accountId, String currency) {
        var id = rep.dh().uid().generate(CashBalance.class.getSimpleName());
        TimePoint now = rep.dh().time().tp();
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "baseDay"));
        List<CashBalance> list = rep.tmpl().find(CashBalance.class, criteria -> criteria
                .and("accountId").is(accountId)
                .and("currency").is(currency), pageable)
                .getContent();

        if (list.isEmpty()) {
            return rep.save(CashBalance.builder()
                    .id(id)
                    .accountId(accountId)
                    .baseDay(now.day())
                    .currency(currency)
                    .amount(BigDecimal.ZERO)
                    .updateDate(now.date())
                    .build());
        } else { // roll over
            var prev = list.get(0);
            return rep.save(CashBalance.builder()
                    .id(id)
                    .accountId(accountId)
                    .baseDay(now.day())
                    .currency(currency)
                    .amount(prev.amount)
                    .updateDate(now.date())
                    .build());
        }
    }

}
