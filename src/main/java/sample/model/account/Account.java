package sample.model.account;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sample.context.DomainEntity;
import sample.context.ValidationException;
import sample.context.orm.OrmRepository;
import sample.model.constraints.AccountId;
import sample.model.constraints.Email;
import sample.model.constraints.Name;

/**
 * Account.
 * low: The minimum columns with this sample.
 */
@Entity
@Data
public class Account implements DomainEntity {

    @Id
    @AccountId
    private String id;
    @Name
    private String name;
    @Email
    private String mail;
    @NotNull
    @Enumerated
    private AccountStatusType statusType;

    public static Account load(final OrmRepository rep, String id) {
        return rep.load(Account.class, id);
    }

    public static Account loadActive(final OrmRepository rep, String id) {
        Account acc = load(rep, id);
        if (acc.getStatusType().inacitve()) {
            throw new ValidationException("error.Account.loadActive");
        }
        return acc;
    }

    public static enum AccountStatusType {
        NORMAL,
        WITHDRAWAL;

        public boolean inacitve() {
            return this == WITHDRAWAL;
        }
    }

}
