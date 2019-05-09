package sample.model.master;

import java.util.List;

import javax.persistence.*;

import lombok.*;
import sample.context.orm.*;
import sample.model.constraints.*;

/**
 * The settlement financial institution of the service company.
 * low: It is a sample, a branch and a name, and considerably originally omit required information.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@NamedQuery(name = "SelfFiAccount.load", query = "from SelfFiAccount a where a.category=?1 and a.currency=?2")
public class SelfFiAccount extends JpaActiveRecord<SelfFiAccount> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;
    @Category
    private String category;
    @Currency
    private String currency;
    /** financial institution code */
    @IdStr
    private String fiCode;
    /** financial institution account ID */
    @AccountId
    private String fiAccountId;

    public static SelfFiAccount load(final JpaRepository rep, String category, String currency) {
        List<SelfFiAccount> list = rep.tmpl().find("SelfFiAccount.load", category, currency);
        if (list.isEmpty()) {
            throw new IllegalStateException("SelfFiAccount has not been registered. [" + category + ": " + currency + "]");
        }
        return list.get(0);
    }

}
