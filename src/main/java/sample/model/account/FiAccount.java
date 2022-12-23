package sample.model.account;

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
 * 口座に紐づく金融機関口座を表現します。
 * <p>
 * 口座を相手方とする入出金で利用します。
 * low: サンプルなので支店や名称、名義といった本来必須な情報をかなり省略しています。(通常は全銀仕様を踏襲します)
 * 
 * @author jkazama
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FiAccount extends JpaActiveRecord<FiAccount> {

    private static final long serialVersionUID = 1L;

    /** ID */
    @Id
    @GeneratedValue
    private Long id;
    /** 口座ID */
    @AccountId
    private String accountId;
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

    public static FiAccount load(final JpaRepository rep, String accountId, String category, String currency) {
        var jpql = """
                FROM FiAccount a
                WHERE a.accountId=?1 AND a.category=?2 AND a.currency=?3
                """;
        return rep.tmpl().load(jpql, accountId, category, currency);
    }
}
