package sample.model.asset;

/** 資産ドメインにおけるエラーキー定数を表現します。 */
public interface AssetErrorKeys {

    // 受渡日を迎えていないため実現できません。
    String CF_REALIZE_DAY = "error.Cashflow.realizeDay";
    // 出金可能額を超えています。
    String CIO_WITHDRAWAL_AMOUNT = "error.CashInOut.withdrawAmount";
    // 未到来の発生日です。
    String CIO_EVENT_DAY_AFTER_EQUALS_DAY = "error.CashInOut.afterEqualsDay";
    // 既に発生日を迎えています。
    String CIO_EVENT_DAY_BEFORE_EQUALS_DAY = "error.CashInOut.beforeEqualsDay";

}
