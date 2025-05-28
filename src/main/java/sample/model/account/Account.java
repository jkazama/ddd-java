package sample.model.account;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
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
@Table("ACCOUNT")
@Builder
public record Account(
        @Id @AccountId String id,
        @Name String name,
        @Email String mail,
        @NotNull AccountStatusType statusType) implements DomainEntity {

    public AccountBuilder copyBuilder() {
        return Account.builder()
                .id(this.id)
                .name(this.name)
                .mail(this.mail)
                .statusType(this.statusType);
    }

    public static Account load(final OrmRepository rep, String id) {
        return rep.load(Account.class, id);
    }

    public static Account loadActive(final OrmRepository rep, String id) {
        Account acc = load(rep, id);
        if (acc.statusType().inacitve()) {
            throw ValidationException.of("error.Account.loadActive");
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
