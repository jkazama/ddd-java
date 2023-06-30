package sample.model.asset;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
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
@Entity
@Data
public class CashBalance implements DomainEntity {

    @Id
    @GeneratedValue
    private Long id;
    @AccountId
    private String accountId;
    @ISODate
    private LocalDate baseDay;
    @Currency
    private String currency;
    @Amount
    private BigDecimal amount;
    @ISODateTime
    private LocalDateTime updateDate;

    /**
     * low: Use Currency here, but the real number of the currency figures and
     * fraction processing definition
     * are managed with DB or the configuration file.
     */
    public CashBalance add(final OrmRepository rep, BigDecimal addAmount) {
        int scale = java.util.Currency.getInstance(currency).getDefaultFractionDigits();
        RoundingMode mode = RoundingMode.DOWN;
        setAmount(Calculator.init(amount).scale(scale, mode).add(addAmount).decimal());
        return rep.update(this);
    }

    /**
     * Acquire the balance of the designated account.
     * (when I do not exist, acquire it after carrying forward preservation)
     * low: The appropriate consideration and examination of multiple currencies are
     * omitted.
     */
    public static CashBalance getOrNew(final OrmRepository rep, String accountId, String currency) {
        LocalDate baseDay = rep.dh().time().day();
        var jpql = """
                FROM CashBalance cb
                WHERE cb.accountId=?1 AND cb.currency=?2 AND cb.baseDay=?3
                ORDER BY cb.baseDay DESC
                """;
        List<CashBalance> list = rep.tmpl().find(jpql, accountId, currency, baseDay);
        if (list.isEmpty()) {
            return create(rep, accountId, currency);
        } else {
            return list.get(0);
        }
    }

    private static CashBalance create(final OrmRepository rep, String accountId, String currency) {
        TimePoint now = rep.dh().time().tp();
        var jpql = """
                FROM CashBalance cb
                WHERE cb.accountId=?1 AND cb.currency=?2
                ORDER BY cb.baseDay DESC
                """;
        List<CashBalance> list = rep.tmpl().find(jpql, accountId, currency);
        if (list.isEmpty()) {
            var m = new CashBalance();
            m.setAccountId(accountId);
            m.setBaseDay(now.getDay());
            m.setCurrency(currency);
            m.setAmount(BigDecimal.ZERO);
            m.setUpdateDate(now.getDate());
            return rep.save(m);
        } else { // roll over
            var prev = list.get(0);
            var m = new CashBalance();
            m.setAccountId(accountId);
            m.setBaseDay(now.getDay());
            m.setCurrency(currency);
            m.setAmount(prev.getAmount());
            m.setUpdateDate(now.getDate());
            return rep.save(m);
        }
    }

}
