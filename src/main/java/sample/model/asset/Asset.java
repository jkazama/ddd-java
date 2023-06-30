package sample.model.asset;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import sample.context.orm.OrmRepository;
import sample.util.Calculator;

/**
 * The asset of the account.
 */
@Builder
public record Asset(
        /** account ID */
        String id) {

    public boolean canWithdraw(final OrmRepository rep, String currency, BigDecimal absAmount, LocalDate valueDay) {
        var calc = Calculator.init(CashBalance.getOrNew(rep, id, currency).getAmount());
        Cashflow.findUnrealize(rep, id, currency, valueDay).stream()
                .map(Cashflow::getAmount)
                .forEach(calc::add);
        CashInOut.findUnprocessed(rep, id, currency, true).stream()
                .map(v -> v.getAbsAmount().negate())
                .forEach(calc::add);
        calc.add(absAmount.negate());
        return 0 <= calc.decimal().signum();
    }

    public static Asset of(String accountId) {
        return Asset.builder()
                .id(accountId)
                .build();
    }

}
