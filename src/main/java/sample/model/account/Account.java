package sample.model.account;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.*;
import sample.ValidationException;
import sample.context.orm.*;
import sample.model.constraints.*;

/**
 * 口座を表現します。
 * low: サンプル用に必要最低限の項目だけ
 * 
 * @author jkazama
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Account extends JpaActiveRecord<Account> {

    private static final long serialVersionUID = 1L;

    /** 口座ID */
    @Id
    @AccountId
    private String id;
    /** 口座名義 */
    @Name
    private String name;
    /** メールアドレス */
    @Email
    private String mail;
    /** 口座状態 */
    @Enumerated(EnumType.STRING)
    @NotNull
    private AccountStatusType statusType;

    public static Account load(final JpaRepository rep, String id) {
        return rep.load(Account.class, id);
    }

    /** 有効な口座を返します。 */
    public static Account loadActive(final JpaRepository rep, String id) {
        Account acc = load(rep, id);
        if (acc.getStatusType().inacitve()) {
            throw new ValidationException("error.Account.loadActive");
        }
        return acc;
    }

    /** 口座状態を表現します。 */
    public static enum AccountStatusType {
        /** 通常 */
        NORMAL,
        /** 退会 */
        WITHDRAWAL;
        public boolean inacitve() {
            return this == WITHDRAWAL;
        }
    }

}
