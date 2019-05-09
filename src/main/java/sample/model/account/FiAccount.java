package sample.model.account;

import java.util.List;

import javax.persistence.*;

import lombok.*;
import sample.ValidationException;
import sample.context.orm.*;
import sample.model.constraints.*;

/**
 * the financial institution account in an account.
 * <p>Use it by an account activity.
 * low: The minimum columns with this sample.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@NamedQuery(name = "FiAccount.load", query = "from FiAccount a where a.accountId=?1 and a.category=?2 and a.currency=?3")
public class FiAccount extends JpaActiveRecord<FiAccount> {

    private static final long serialVersionUID = 1L;

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

    public static FiAccount load(final JpaRepository rep, String accountId, String category, String currency) {
        List<FiAccount> list = rep.tmpl().find("FiAccount.load", accountId, category, currency);
        if (list.isEmpty()) {
            throw new ValidationException("error.Entity.load");
        }
        return list.get(0);
    }
}
