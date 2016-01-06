package sample.model.asset;

/**
 * 摘要定数インターフェース。
 * 
 * @author jkazama
 */
public interface Remarks {

    /** 振込入金 */
    String CashIn = "cashIn";
    /** 振込入金(調整) */
    String CashInAdjust = "cashInAdjust";
    /** 振込入金(取消) */
    String CashInCancel = "cashInCancel";
    /** 振込出金 */
    String CashOut = "cashOut";
    /** 振込出金(調整) */
    String CashOutAdjust = "cashOutAdjust";
    /** 振込出金(取消) */
    String CashOutCancel = "cashOutCancel";

}
