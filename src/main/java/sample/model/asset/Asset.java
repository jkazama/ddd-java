package sample.model.asset;

import java.math.BigDecimal;

import lombok.Getter;
import sample.context.orm.JpaRepository;
import sample.util.Calculator;

/**
 * The asset of the account.
 */
@Getter
public class Asset {
    /** account ID */
    private final String id;

    private Asset(String id) {
        this.id = id;
    }

    public static Asset by(String accountId) {
        return new Asset(accountId);
    }

    public boolean canWithdraw(final JpaRepository rep, String currency, BigDecimal absAmount, String valueDay) {
        Calculator calc = Calculator.init(CashBalance.getOrNew(rep, id, currency).getAmount());
        for (Cashflow cf : Cashflow.findUnrealize(rep, id, currency, valueDay)) {
            calc.add(cf.getAmount());
        }
        for (CashInOut withdrawal : CashInOut.findUnprocessed(rep, id, currency, true)) {
            calc.add(withdrawal.getAbsAmount().negate());
        }
        calc.add(absAmount.negate());
        return 0 <= calc.decimal().signum();
    }
}
