package sample.model.asset;

import java.math.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.*;
import sample.context.orm.*;
import sample.model.constraints.*;
import sample.model.constraints.Currency;
import sample.util.*;

/**
 * The account balance.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@NamedQueries({
        @NamedQuery(name = "CashBalance.findAcc", query = "from CashBalance c where c.accountId=?1 and c.currency=?2 order by c.baseDay desc"),
        @NamedQuery(name = "CashBalance.findAccWithDay", query = "from CashBalance c where c.accountId=?1 and c.currency=?2 and c.baseDay=?3 order by c.baseDay desc") })
public class CashBalance extends JpaActiveRecord<CashBalance> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;
    @AccountId
    private String accountId;
    @Day
    private String baseDay;
    @Currency
    private String currency;
    @Amount
    private BigDecimal amount;
    @NotNull
    private Date updateDate;

    /**
     * low: Use Currency here, but the real number of the currency figures and fraction processing definition
     *  are managed with DB or the configuration file.
     */
    public CashBalance add(final JpaRepository rep, BigDecimal addAmount) {
        int scale = java.util.Currency.getInstance(currency).getDefaultFractionDigits();
        RoundingMode mode = RoundingMode.DOWN;
        setAmount(Calculator.init(amount).scale(scale, mode).add(addAmount).decimal());
        return update(rep);
    }

    /**
     * Acquire the balance of the designated account.
     * (when I do not exist, acquire it after carrying forward preservation)
     * low: The appropriate consideration and examination of plural currencies are omitted.
     */
    public static CashBalance getOrNew(final JpaRepository rep, String accountId, String currency) {
        String baseDay = rep.dh().time().day();
        List<CashBalance> list = rep.tmpl().find("CashBalance.findAccWithDay", accountId, currency, baseDay);
        if (list.isEmpty()) {
            return create(rep, accountId, currency);
        } else {
            return list.get(0);
        }
    }

    private static CashBalance create(final JpaRepository rep, String accountId, String currency) {
        TimePoint now = rep.dh().time().tp();
        List<CashBalance> list = rep.tmpl().find("CashBalance.findAcc", accountId, currency);
        if (list.isEmpty()) {
            return new CashBalance(null, accountId, now.getDay(), currency, BigDecimal.ZERO, now.getDate()).save(rep);
        } else {    // roll over
            CashBalance prev = list.get(0);
            return new CashBalance(null, accountId, now.getDay(), currency, prev.getAmount(), now.getDate()).save(rep);
        }
    }

}
