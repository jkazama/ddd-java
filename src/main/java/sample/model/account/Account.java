package sample.model.account;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.*;
import sample.ValidationException;
import sample.context.orm.*;
import sample.model.constraints.*;

/**
 * Account.
 * low: The minimum columns with this sample.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Account extends JpaActiveRecord<Account> {

    private static final long serialVersionUID = 1L;

    @Id
    @AccountId
    private String id;
    @Name
    private String name;
    @Email
    private String mail;
    @Enumerated(EnumType.STRING)
    @NotNull
    private AccountStatusType statusType;

    public static Account load(final JpaRepository rep, String id) {
        return rep.load(Account.class, id);
    }

    public static Account loadActive(final JpaRepository rep, String id) {
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
