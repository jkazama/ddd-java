package sample.model.asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sample.ActionStatusType;
import sample.context.Dto;
import sample.context.orm.JpaActiveRecord;
import sample.context.orm.JpaRepository;
import sample.model.DomainErrorKeys;
import sample.model.constraints.AccountId;
import sample.model.constraints.Amount;
import sample.model.constraints.Category;
import sample.model.constraints.Currency;
import sample.model.constraints.ISODate;
import sample.model.constraints.ISODateEmpty;
import sample.model.constraints.ISODateTime;
import sample.util.TimePoint;
import sample.util.Validator;

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
@EqualsAndHashCode(callSuper = false)
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
    /** 発生日 */
    @ISODate
    private LocalDate eventDay;
    /** 発生日時 */
    @ISODateTime
    private LocalDateTime eventDate;
    /** 受渡日 */
    @ISODate
    private LocalDate valueDay;
    /** 処理種別 */
    @Enumerated(EnumType.STRING)
    @NotNull
    private ActionStatusType statusType;
    /** 更新者 */
    @AccountId
    private String updateActor;
    /** 更新日 */
    @ISODateTime
    private LocalDateTime updateDate;

    /**
     * キャッシュフローを処理済みにして残高へ反映します。
     */
    public Cashflow realize(final JpaRepository rep) {
        TimePoint now = rep.dh().time().tp();
        // 業務審査
        Validator.validate(v -> {
            v.verify(canRealize(rep), AssetErrorKeys.CF_REALIZE_DAY);
            v.verify(statusType.isUnprocessing(), DomainErrorKeys.STATUS_PROCESSING);
        });

        this.setStatusType(ActionStatusType.PROCESSED);
        this.setUpdateActor(rep.dh().actor().id());
        this.setUpdateDate(now.getDate());
        this.update(rep);
        CashBalance.getOrNew(rep, accountId, currency).add(rep, amount);
        return this;
    }

    /**
     * キャッシュフローをエラー状態にします。
     * <p>
     * 処理中に失敗した際に呼び出してください。
     * low: 実際はエラー事由などを引数に取って保持する
     */
    public Cashflow error(final JpaRepository rep) {
        // 業務審査
        Validator.validate(v -> {
            v.verify(statusType.isUnprocessed(), DomainErrorKeys.STATUS_PROCESSING);
        });

        this.setStatusType(ActionStatusType.ERROR);
        this.setUpdateActor(rep.dh().actor().id());
        this.setUpdateDate(rep.dh().time().date());
        return this.update(rep);
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
    public static List<Cashflow> findUnrealize(
            final JpaRepository rep, String accountId, String currency, LocalDate valueDay) {
        var jpql = """
                FROM Cashflow c
                WHERE c.accountId=?1 AND c.currency=?2
                 AND c.valueDay<=?3 AND c.statusType IN ?4
                ORDER BY c.id
                """;
        return rep.tmpl().find(jpql,
                accountId, currency, valueDay, ActionStatusType.UNPROCESSED_TYPES);
    }

    /**
     * 指定受渡日で実現対象となるキャッシュフロー一覧を検索します。
     */
    public static List<Cashflow> findDoRealize(final JpaRepository rep, LocalDate valueDay) {
        var jpql = """
                FROM Cashflow c
                WHERE c.valueDay=?1 AND c.statusType in ?2
                ORDER BY c.id
                """;
        return rep.tmpl().find(jpql, valueDay, ActionStatusType.UNPROCESSED_TYPES);
    }

    /**
     * キャッシュフローを登録します。
     * 受渡日を迎えていた時はそのまま残高へ反映します。
     */
    public static Cashflow register(final JpaRepository rep, final RegCashflow p) {
        TimePoint now = rep.dh().time().tp();
        // 業務審査
        Validator.validate(v -> {
            v.checkField(now.beforeEqualsDay(p.valueDay()),
                    "valueDay", "error.Cashflow.beforeEqualsDay");
        });
        Cashflow cf = p.create(now, rep.dh().actor().id()).save(rep);
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

    /** 入出金キャッシュフローの登録パラメタ。 */
    @Builder
    public static record RegCashflow(
            @AccountId String accountId,
            @Currency String currency,
            @Amount BigDecimal amount,
            @NotNull CashflowType cashflowType,
            @Category String remark,
            /** 未設定時は営業日を設定 */
            @ISODateEmpty LocalDate eventDay,
            @ISODate LocalDate valueDay) implements Dto {

        public Cashflow create(final TimePoint now, String updActor) {
            var eventDate = eventDay == null ? now : new TimePoint(eventDay, now.getDate());
            var m = new Cashflow();
            m.setAccountId(accountId);
            m.setCurrency(currency);
            m.setAmount(amount);
            m.setCashflowType(cashflowType);
            m.setRemark(remark);
            m.setEventDay(eventDate.getDay());
            m.setEventDate(eventDate.getDate());
            m.setValueDay(valueDay);
            m.setStatusType(ActionStatusType.UNPROCESSED);
            m.setUpdateActor(updActor);
            m.setUpdateDate(now.getDate());
            return m;
        }
    }

}
