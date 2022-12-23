package sample.model.master;

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
import sample.model.constraints.Category;
import sample.model.constraints.Currency;
import sample.model.constraints.IdStr;

/**
 * サービス事業者の決済金融機関を表現します。
 * low: サンプルなので支店や名称、名義といったなど本来必須な情報をかなり省略しています。(通常は全銀仕様を踏襲します)
 * 
 * @author jkazama
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SelfFiAccount extends JpaActiveRecord<SelfFiAccount> {
    private static final long serialVersionUID = 1L;

    /** ID */
    @Id
    @GeneratedValue
    private Long id;
    /** 利用用途カテゴリ */
    @Category
    private String category;
    /** 通貨 */
    @Currency
    private String currency;
    /** 金融機関コード */
    @IdStr
    private String fiCode;
    /** 金融機関口座ID */
    @AccountId
    private String fiAccountId;

    public static SelfFiAccount load(final JpaRepository rep, String category, String currency) {
        var jpql = """
                FROM SelfFiAccount a
                WHERE a.category=?1 AND a.currency=?2
                """;
        return rep.tmpl().load(jpql, category, currency);
    }

}
