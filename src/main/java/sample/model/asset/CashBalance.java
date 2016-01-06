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
 * 口座残高を表現します。
 * 
 * @author jkazama
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

    /** ID */
    @Id
    @GeneratedValue
    private Long id;
    /** 口座ID */
    @AccountId
    private String accountId;
    /** 基準日 */
    @Day
    private String baseDay;
    /** 通貨 */
    @Currency
    private String currency;
    /** 金額 */
    @Amount
    private BigDecimal amount;
    /** 更新日 */
    @NotNull
    private Date updateDate;

    /**
     * 残高へ指定した金額を反映します。
     * low ここではCurrencyを使っていますが、実際の通貨桁数や端数処理定義はDBや設定ファイル等で管理されます。
     */
    public CashBalance add(final JpaRepository rep, BigDecimal addAmount) {
        int scale = java.util.Currency.getInstance(currency).getDefaultFractionDigits();
        RoundingMode mode = RoundingMode.DOWN;
        setAmount(Calculator.init(amount).scale(scale, mode).add(addAmount).decimal());
        return update(rep);
    }

    /**
     * 指定口座の残高を取得します。(存在しない時は繰越保存後に取得します)
     * low: 複数通貨の適切な考慮や細かい審査は本筋でないので割愛。
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
        } else { // 残高繰越
            CashBalance prev = list.get(0);
            return new CashBalance(null, accountId, now.getDay(), currency, prev.getAmount(), now.getDate()).save(rep);
        }
    }

}
