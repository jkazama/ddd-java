package sample.model.asset;

import java.math.BigDecimal;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.*;
import sample.ActionStatusType;
import sample.context.Dto;
import sample.context.orm.*;
import sample.model.constraints.*;
import sample.model.constraints.Currency;
import sample.util.*;

/**
 * 入出金キャッシュフローを表現します。
 * キャッシュフローは振込/振替といったキャッシュフローアクションから生成される確定状態(依頼取消等の無い)の入出金情報です。
 * low: 概念を伝えるだけなので必要最低限の項目で表現しています。
 * low: 検索関連は主に経理確認や帳票等での利用を想定します
 * 
 * @author jkazama
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@NamedQueries({
        @NamedQuery(name = "Cashflow.findDoRealize", query = "from Cashflow c where c.valueDay=?1 and c.statusType in ?2 order by c.id"),
        @NamedQuery(name = "Cashflow.findUnrealize", query = "from Cashflow c where c.accountId=?1 and c.currency=?2 and c.valueDay<=?3 and c.statusType in ?4 order by c.id") })
public class Cashflow extends JpaActiveRecord<Cashflow> {

    private static final long serialVersionUID = 1L;

    /** ID */
    @Id
    @GeneratedValue
    private Long id;
    /** 口座ID */
    @AccountId
    private String accountId;
    /** 通貨 */
    @Currency
    private String currency;
    /** 金額 */
    @Amount
    private BigDecimal amount;
    /** 入出金 */
    @Enumerated(EnumType.STRING)
    @NotNull
    private CashflowType cashflowType;
    /** 摘要 */
    @Category
    private String remark;
    /** 発生日/日時 */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "day", column = @Column(name = "event_day")),
            @AttributeOverride(name = "date", column = @Column(name = "event_date")) })
    @NotNull
    private TimePoint eventDate;
    /** 受渡日 */
    @Day
    private String valueDay;
    /** 処理種別 */
    @Enumerated(EnumType.STRING)
    @NotNull
    private ActionStatusType statusType;
    /** 更新者 */
    @AccountId
    private String updateActor;
    /** 更新日 */
    @NotNull
    private Date updateDate;

    /**
     * キャッシュフローを処理済みにして残高へ反映します。
     */
    public Cashflow realize(final JpaRepository rep) {
        TimePoint now = rep.dh().time().tp();
        Validator v = validator();
        v.verify(canRealize(rep), "error.Cashflow.realizeDay");
        v.verify(statusType.isUnprocessing(), "error.ActionStatusType.unprocessing"); // 「既に処理中/処理済です」

        setStatusType(ActionStatusType.PROCESSED);
        setUpdateActor(rep.dh().actor().getId());
        setUpdateDate(now.getDate());
        update(rep);
        CashBalance.getOrNew(rep, accountId, currency).add(rep, amount);
        return this;
    }

    /**
     * キャッシュフローをエラー状態にします。
     * <p>処理中に失敗した際に呼び出してください。
     * low: 実際はエラー事由などを引数に取って保持する
     */
    public Cashflow error(final JpaRepository rep) {
        validator().verify(statusType.isUnprocessed(), "error.ActionStatusType.unprocessing");

        setStatusType(ActionStatusType.ERROR);
        setUpdateActor(rep.dh().actor().getId());
        setUpdateDate(rep.dh().time().date());
        return update(rep);
    }

    /**
     * キャッシュフローを実現(受渡)可能か判定します。
     */
    public boolean canRealize(final JpaRepository rep) {
        return rep.dh().time().tp().afterEqualsDay(valueDay);
    }

    public static Cashflow load(final JpaRepository rep, Long id) {
        return rep.load(Cashflow.class, id);
    }

    /**
     * 指定受渡日時点で未実現のキャッシュフロー一覧を検索します。(口座通貨別)
     */
    public static List<Cashflow> findUnrealize(final JpaRepository rep, String accountId, String currency,
            String valueDay) {
        return rep.tmpl().find("Cashflow.findUnrealize", accountId, currency, valueDay,
                ActionStatusType.unprocessedTypes);
    }

    /**
     * 指定受渡日で実現対象となるキャッシュフロー一覧を検索します。
     */
    public static List<Cashflow> findDoRealize(final JpaRepository rep, String valueDay) {
        return rep.tmpl().find("Cashflow.findDoRealize", valueDay, ActionStatusType.unprocessedTypes);
    }

    /**
     * キャッシュフローを登録します。
     * 受渡日を迎えていた時はそのまま残高へ反映します。
     */
    public static Cashflow register(final JpaRepository rep, final RegCashflow p) {
        TimePoint now = rep.dh().time().tp();
        Validator v = new Validator();
        v.checkField(now.beforeEqualsDay(p.getValueDay()),
                "valueDay", "error.Cashflow.beforeEqualsDay");
        v.verify();
        Cashflow cf = p.create(now, rep.dh().actor().getId()).save(rep);
        return cf.canRealize(rep) ? cf.realize(rep) : cf;
    }

    /** キャッシュフロー種別。 low: 各社固有です。摘要含めラベルはなるべくmessages.propertiesへ切り出し */
    public static enum CashflowType {
        /** 振込入金 */
        CashIn,
        /** 振込出金 */
        CashOut,
        /** 振替入金 */
        CashTransferIn,
        /** 振替出金 */
        CashTransferOut
    }

    /** 入出金キャッシュフローの登録パラメタ。  */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegCashflow implements Dto {
        private static final long serialVersionUID = 1L;
        @AccountId
        private String accountId;
        @Currency
        private String currency;
        @Amount
        private BigDecimal amount;
        @NotNull
        private CashflowType cashflowType;
        @Category
        private String remark;
        /** 未設定時は営業日を設定 */
        @DayEmpty
        private String eventDay;
        @Day
        private String valueDay;

        public Cashflow create(final TimePoint now, String updActor) {
            TimePoint eventDate = eventDay == null ? now : new TimePoint(eventDay, now.getDate());
            return new Cashflow(null, accountId, currency, amount, cashflowType, remark, eventDate, valueDay,
                    ActionStatusType.UNPROCESSED, updActor, now.getDate());
        }
    }

}
