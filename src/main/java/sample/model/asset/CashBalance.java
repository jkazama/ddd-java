package sample.model.asset;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sample.context.orm.JpaActiveRecord;
import sample.context.orm.JpaRepository;
import sample.model.constraints.AccountId;
import sample.model.constraints.Amount;
import sample.model.constraints.Currency;
import sample.model.constraints.ISODate;
import sample.model.constraints.ISODateTime;
import sample.util.Calculator;
import sample.util.TimePoint;

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
    @ISODate
    private LocalDate baseDay;
    /** 通貨 */
    @Currency
    private String currency;
    /** 金額 */
    @Amount
    private BigDecimal amount;
    /** 更新日 */
    @ISODateTime
    private LocalDateTime updateDate;

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

    private static CashBalance create(final JpaRepository rep, String accountId, String currency) {
        TimePoint now = rep.dh().time().tp();
        var jpql = """
                FROM CashBalance cb
                WHERE cb.accountId=?1 AND cb.currency=?2
                ORDER BY cb.baseDay DESC
                """;
        List<CashBalance> list = rep.tmpl().find(jpql, accountId, currency);
        if (list.isEmpty()) {
            return new CashBalance(null, accountId, now.getDay(), currency, BigDecimal.ZERO, now.getDate()).save(rep);
        } else { // 残高繰越
            var prev = list.get(0);
            return new CashBalance(null, accountId, now.getDay(), currency, prev.getAmount(), now.getDate()).save(rep);
        }
    }

}
