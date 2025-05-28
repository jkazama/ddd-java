package sample.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import sample.ActionStatusType;
import sample.context.DomainHelper;
import sample.model.account.Account;
import sample.model.account.Account.AccountStatusType;
import sample.model.account.FiAccount;
import sample.model.asset.CashBalance;
import sample.model.asset.CashInOut;
import sample.model.asset.Cashflow;
import sample.model.asset.Cashflow.CashflowType;
import sample.model.asset.Cashflow.RegCashflow;
import sample.model.master.SelfFiAccount;
import sample.util.TimePoint;

/**
 * A support component for the data generation.
 * <p>
 * It is aimed for master data generation at the time of a test and the
 * development,
 * Please do not use it in the production.
 */
public class DataFixtures {

    // account

    public static Account.AccountBuilder acc(String id) {
        return Account.builder()
                .id(id)
                .name(id)
                .mail("hoge@example.com")
                .statusType(AccountStatusType.NORMAL);
    }

    public static FiAccount.FiAccountBuilder fiAcc(
            DomainHelper helper, String accountId, String category, String currency) {
        return FiAccount.builder()
                .id(helper.uid().generate(FiAccount.class.getSimpleName()))
                .accountId(accountId)
                .category(category)
                .currency(currency)
                .fiCode(category + "-" + currency)
                .fiAccountId("FI" + accountId);
    }

    // asset

    public static CashBalance.CashBalanceBuilder cb(
            DomainHelper helper, String accountId, LocalDate baseDay, String currency, String amount) {
        var now = helper.time().date();
        return CashBalance.builder()
                .id(helper.uid().generate(CashBalance.class.getSimpleName()))
                .accountId(accountId)
                .baseDay(baseDay)
                .currency(currency)
                .amount(new BigDecimal(amount))
                .updateDate(now);
    }

    public static Cashflow.CashflowBuilder cf(
            DomainHelper helper, String accountId, String amount, LocalDate eventDay, LocalDate valueDay) {
        var now = helper.time().date();
        return Cashflow.builder()
                .id(helper.uid().generate(Cashflow.class.getSimpleName()))
                .accountId(accountId)
                .currency("JPY")
                .amount(new BigDecimal(amount))
                .cashflowType(CashflowType.CASH_IN)
                .remark("cashIn")
                .eventDay(eventDay)
                .eventDate(now)
                .valueDay(valueDay)
                .statusType(ActionStatusType.UNPROCESSED)
                .updateActor("dummy")
                .updateDate(now);
    }

    public static RegCashflow.RegCashflowBuilder cfReg(String accountId, String amount, LocalDate valueDay) {
        return RegCashflow.builder()
                .accountId(accountId)
                .currency("JPY")
                .amount(new BigDecimal(amount))
                .cashflowType(CashflowType.CASH_IN)
                .remark("cashIn")
                .valueDay(valueDay);
    }

    // eventDay(T+1) / valueDay(T+3)
    public static CashInOut.CashInOutBuilder cio(
            String id, String accountId, String absAmount, boolean withdrawal, TimePoint now) {
        return CashInOut.builder()
                .id(id)
                .accountId(accountId)
                .currency("JPY")
                .absAmount(new BigDecimal(absAmount))
                .withdrawal(withdrawal)
                .requestDay(now.day())
                .requestDate(now.date())
                .eventDay(now.day().plusDays(1))
                .valueDay(now.day().plusDays(3))
                .targetFiCode("tFiCode")
                .targetFiAccountId("tFiAccId")
                .selfFiCode("sFiCode")
                .selfFiAccountId("sFiAccId")
                .statusType(ActionStatusType.UNPROCESSED)
                .updateActor("dummy")
                .updateDate(now.date())
                .cashflowId(null);
    }

    // master

    public static SelfFiAccount.SelfFiAccountBuilder selfFiAcc(DomainHelper helper, String category, String currency) {
        return SelfFiAccount.builder()
                .id(helper.uid().generate(SelfFiAccount.class.getSimpleName()))
                .category(category)
                .currency(currency)
                .fiCode(category + "-" + currency)
                .fiAccountId("xxxxxx");
    }

}
