package sample.model.master;

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
 * The settlement financial institution of the service company.
 * low: It is a sample, a branch and a name, and considerably originally omit
 * required information.
 */
@Entity
@Data
public class SelfFiAccount implements DomainEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Category
    private String category;
    @Currency
    private String currency;
    @IdStr
    private String fiCode;
    @AccountId
    private String fiAccountId;

    public static SelfFiAccount load(final OrmRepository rep, String category, String currency) {
        var jpql = """
                FROM SelfFiAccount a
                WHERE a.category=?1 AND a.currency=?2
                """;
        return rep.tmpl().load(jpql, category, currency);
    }

}
