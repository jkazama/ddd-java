package sample.model.account;

import java.util.Map;

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
 * the financial institution account in an account.
 * <p>
 * Use it by an account activity.
 * low: The minimum columns with this sample.
 */
@Table("FI_ACCOUNT")
@Builder
public record FiAccount(
        @Id String id,
        @AccountId String accountId,
        @Category String category,
        @Currency String currency,
        @IdStr String fiCode,
        @AccountId String fiAccountId) implements DomainEntity {

    public FiAccountBuilder copyBuilder() {
        return FiAccount.builder()
                .id(this.id)
                .accountId(this.accountId)
                .category(this.category)
                .currency(this.currency)
                .fiCode(this.fiCode)
                .fiAccountId(this.fiAccountId);
    }

    public static FiAccount load(final OrmRepository rep, String accountId, String category, String currency) {
        Map<String, Object> conditions = Map.of("accountId", accountId, "category", category, "currency", currency);
        return rep.tmpl().load(FiAccount.class, conditions);
    }
}
