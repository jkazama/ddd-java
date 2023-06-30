package sample.model.account;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import sample.context.DomainEntity;
import sample.context.orm.OrmRepository;
import sample.model.constraints.AccountId;
import sample.model.constraints.Category;
import sample.model.constraints.Currency;
import sample.model.constraints.IdStr;

/**
 * the financial institution account in an account.
 * <p>
 * Use it by an account activity.
 * low: The minimum columns with this sample.
 */
@Entity
@Data
public class FiAccount implements DomainEntity {
    @Id
    @GeneratedValue
    private Long id;
    @AccountId
    private String accountId;
    @Category
    private String category;
    @Currency
    private String currency;
    @IdStr
    private String fiCode;
    @AccountId
    private String fiAccountId;

    public static FiAccount load(final OrmRepository rep, String accountId, String category, String currency) {
        var jpql = """
                FROM FiAccount a
                WHERE a.accountId=?1 AND a.category=?2 AND a.currency=?3
                """;
        return rep.tmpl().load(jpql, accountId, category, currency);
    }
}
