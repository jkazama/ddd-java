package sample.model.master;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
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
@Table("SELF_FI_ACCOUNT")
@Builder
public record SelfFiAccount(
        @Id String id,
        @Category String category,
        @Currency String currency,
        @IdStr String fiCode,
        @AccountId String fiAccountId) implements DomainEntity {

    public SelfFiAccountBuilder copyBuilder() {
        return SelfFiAccount.builder()
                .id(this.id)
                .category(this.category)
                .currency(this.currency)
                .fiCode(this.fiCode)
                .fiAccountId(this.fiAccountId);
    }

    public static SelfFiAccount load(final OrmRepository rep, String category, String currency) {
        return rep.tmpl().load(SelfFiAccount.class, criteria -> criteria
                .and("category").is(category)
                .and("currency").is(currency));
    }

}
