package sample.model.asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sample.ActionStatusType;
import sample.context.DomainHelper;
import sample.context.Dto;
import sample.context.orm.JpaActiveRecord;
import sample.context.orm.JpaRepository;
import sample.context.orm.JpqlBuilder;
import sample.model.DomainErrorKeys;
import sample.model.account.FiAccount;
import sample.model.asset.Cashflow.CashflowType;
import sample.model.asset.Cashflow.RegCashflow;
import sample.model.constraints.AbsAmount;
import sample.model.constraints.AccountId;
import sample.model.constraints.Currency;
import sample.model.constraints.CurrencyEmpty;
import sample.model.constraints.ISODate;
import sample.model.constraints.ISODateTime;
import sample.model.constraints.IdStr;
import sample.model.master.SelfFiAccount;
import sample.util.TimePoint;
import sample.util.Validator;

/**
 * 振込入出金依頼を表現するキャッシュフローアクション。
 * <p>
 * 相手方/自社方の金融機関情報は依頼後に変更される可能性があるため、依頼時点の状態を
 * 保持するために非正規化して情報を保持しています。
 * low: 相手方/自社方の金融機関情報は項目数が多いのでサンプル用に金融機関コードのみにしています。
 * 実際の開発ではそれぞれ複合クラス(FinantialInstitution)に束ねるアプローチを推奨します。
 * 
 * @author jkazama
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class CashInOut extends JpaActiveRecord<CashInOut> {

    private static final long serialVersionUID = 1L;

    /** ID(振込依頼No) */
    @Id
    @IdStr
    private String id;
    /** 口座ID */
    @AccountId
    private String accountId;
    /** 通貨 */
    @Currency
    private String currency;
    /** 金額(絶対値) */
    @AbsAmount
    private BigDecimal absAmount;
    /** 出金時はtrue */
    private boolean withdrawal;
    /** 依頼日 */
    @ISODate
    private LocalDate requestDay;
    /** 依頼日時 */
    @ISODateTime
    private LocalDateTime requestDate;
    /** 発生日 */
    @ISODate
    private LocalDate eventDay;
    /** 受渡日 */
    @ISODate
    private LocalDate valueDay;
    /** 相手方金融機関コード */
    @IdStr
    private String targetFiCode;
    /** 相手方金融機関口座ID */
    @AccountId
    private String targetFiAccountId;
    /** 自社方金融機関コード */
    @IdStr
    private String selfFiCode;
    /** 自社方金融機関口座ID */
    @AccountId
    private String selfFiAccountId;
    /** 処理種別 */
    @NotNull
    @Enumerated(EnumType.STRING)
    private ActionStatusType statusType;
    /** 更新者 */
    @AccountId
    private String updateActor;
    /** 更新日 */
    @ISODateTime
    private LocalDateTime updateDate;
    /** キャッシュフローID。処理済のケースでのみ設定されます。low: 実際は調整CFや消込CFの概念なども有 */
    private Long cashflowId;

    /**
     * 依頼を処理します。
     * <p>
     * 依頼情報を処理済にしてキャッシュフローを生成します。
     */
    public CashInOut process(final JpaRepository rep) {
        // low: 出金営業日の取得。ここでは単純な営業日を取得
        TimePoint now = rep.dh().time().tp();
        // 業務審査
        Validator.validate(v -> {
            v.verify(statusType.isUnprocessed(), DomainErrorKeys.STATUS_PROCESSING);
            v.verify(now.afterEqualsDay(eventDay), AssetErrorKeys.CIO_EVENT_DAY_AFTER_EQUALS_DAY);
        });
        // 処理済状態を反映
        this.setStatusType(ActionStatusType.PROCESSED);
        this.setUpdateActor(rep.dh().actor().id());
        this.setUpdateDate(now.getDate());
        this.setCashflowId(Cashflow.register(rep, regCf()).getId());
        return this.update(rep);
    }

    private RegCashflow regCf() {
        BigDecimal amount = withdrawal ? absAmount.negate() : absAmount;
        CashflowType cashflowType = withdrawal ? CashflowType.CashOut : CashflowType.CashIn;
        // low: 摘要はとりあえずシンプルに。実際はCashInOutへ用途フィールドをもたせた方が良い(生成元メソッドに応じて摘要を変える)
        String remark = withdrawal ? Remarks.CashOut : Remarks.CashIn;
        return RegCashflow.builder()
                .accountId(accountId)
                .currency(currency)
                .amount(amount)
                .cashflowType(cashflowType)
                .remark(remark)
                .eventDay(eventDay)
                .valueDay(valueDay)
                .build();
    }

    /**
     * 依頼を取消します。
     * <p>
     * "処理済みでない"かつ"発生日を迎えていない"必要があります。
     */
    public CashInOut cancel(final JpaRepository rep) {
        TimePoint now = rep.dh().time().tp();
        // 業務審査
        Validator.validate(v -> {
            v.verify(statusType.isUnprocessing(), DomainErrorKeys.STATUS_PROCESSING);
            v.verify(now.beforeDay(eventDay), AssetErrorKeys.CIO_EVENT_DAY_BEFORE_EQUALS_DAY);
        });
        // 取消状態を反映
        this.setStatusType(ActionStatusType.CANCELLED);
        this.setUpdateActor(rep.dh().actor().id());
        this.setUpdateDate(now.getDate());
        return this.update(rep);
    }

    /**
     * 依頼をエラー状態にします。
     * <p>
     * 処理中に失敗した際に呼び出してください。
     * low: 実際はエラー事由などを引数に取って保持する
     */
    public CashInOut error(final JpaRepository rep) {
        // 業務審査
        Validator.validate(v -> {
            v.verify(statusType.isUnprocessed(), DomainErrorKeys.STATUS_PROCESSING);
        });

        this.setStatusType(ActionStatusType.ERROR);
        this.setUpdateActor(rep.dh().actor().id());
        this.setUpdateDate(rep.dh().time().date());
        return this.update(rep);
    }

    /** 振込入出金依頼を返します。 */
    public static CashInOut load(final JpaRepository rep, String id) {
        return rep.load(CashInOut.class, id);
    }

    /** 未処理の振込入出金依頼一覧を検索します。 low: 可変条件実装例 */
    public static List<CashInOut> find(final JpaRepository rep, final FindCashInOut p) {
        var jpql = JpqlBuilder.of("FROM CashInOut cio")
                .equal("cio.currency", p.currency())
                .in("cio.statusType", p.statusTypes())
                .between("cio.eventDay", p.updFromDay(), p.updToDay())
                .orderBy("cio.updateDate DESC");
        return rep.tmpl().find(jpql.build(), jpql.args());
    }

    /** 振込入出金依頼の検索パラメタ。 low: 通常は顧客視点/社内視点で利用条件が異なる */
    @Builder
    public static record FindCashInOut(
            @CurrencyEmpty String currency,
            Set<ActionStatusType> statusTypes,
            @ISODate LocalDate updFromDay,
            @ISODate LocalDate updToDay) implements Dto {

        @AssertTrue(message = DomainErrorKeys.BEFORE_EQUALS_DAY)
        public boolean isUpdFromDay() {
            if (this.updFromDay == null || this.updToDay == null) {
                return false;
            }
            return this.updFromDay.isBefore(this.updToDay)
                    || this.updFromDay.isEqual(this.updToDay);
        }
    }

    /** 当日発生で未処理の振込入出金一覧を検索します。 */
    public static List<CashInOut> findUnprocessed(final JpaRepository rep) {
        var jpql = """
                FROM CashInOut cio
                WHERE cio.eventDay=?1 AND cio.statusType IN ?2
                ORDER BY cio.id
                """;
        return rep.tmpl().find(jpql, rep.dh().time().day(), ActionStatusType.UNPROCESSED_TYPES);
    }

    /** 未処理の振込入出金一覧を検索します。(口座別) */
    public static List<CashInOut> findUnprocessed(
            final JpaRepository rep, String accountId, String currency, boolean withdrawal) {
        var jpql = """
                FROM CashInOut cio
                WHERE cio.accountId=?1 AND cio.currency=?2
                 AND cio.withdrawal=?3 AND cio.statusType IN ?4
                 ORDER BY cio.id
                """;
        return rep.tmpl().find(
                jpql, accountId, currency, withdrawal, ActionStatusType.UNPROCESSED_TYPES);
    }

    /** 未処理の振込入出金一覧を検索します。(口座別) */
    public static List<CashInOut> findUnprocessed(final JpaRepository rep, String accountId) {
        var jpql = """
                FROM CashInOut cio
                WHERE cio.accountId=?1 AND cio.statusType IN ?2
                ORDER BY cio.updateDate DESC
                """;
        return rep.tmpl().find(
                jpql, accountId, ActionStatusType.UNPROCESSED_TYPES);
    }

    /** 出金依頼をします。 */
    public static CashInOut withdraw(final JpaRepository rep, final RegCashOut p) {
        DomainHelper dh = rep.dh();
        TimePoint now = dh.time().tp();
        // low: 発生日は締め時刻等の兼ね合いで営業日と異なるケースが多いため、別途DB管理される事が多い
        LocalDate eventDay = now.getDay();
        // low: 実際は各金融機関/通貨の休日を考慮しての T+N 算出が必要
        LocalDate valueDay = dh.time().dayPlus(3);

        // 業務審査
        Validator.validate(v -> {
            v.verifyField(0 < p.absAmount().signum(), "absAmount", "error.domain.AbsAmount.zero");
            boolean canWithdraw = Asset.of(p.accountId())
                    .canWithdraw(rep, p.currency(), p.absAmount(), valueDay);
            v.verifyField(canWithdraw, "absAmount", AssetErrorKeys.CIO_WITHDRAWAL_AMOUNT);
        });

        // 出金依頼情報を登録
        String uid = dh.uid().generate(CashInOut.class.getSimpleName());
        var acc = FiAccount.load(rep, p.accountId(), Remarks.CashOut, p.currency());
        var selfAcc = SelfFiAccount.load(rep, Remarks.CashOut, p.currency());
        String updateActor = dh.actor().id();
        return p.create(now, uid, eventDay, valueDay, acc, selfAcc, updateActor).save(rep);
    }

    /** 振込出金の依頼パラメタ。 */
    @Builder
    public static record RegCashOut(
            @AccountId String accountId,
            @Currency String currency,
            @AbsAmount BigDecimal absAmount) implements Dto {

        public CashInOut create(
                final TimePoint now, String id, LocalDate eventDay, LocalDate valueDay,
                final FiAccount acc, final SelfFiAccount selfAcc, String updActor) {
            var m = new CashInOut();
            m.setId(id);
            m.setAccountId(accountId);
            m.setCurrency(currency);
            m.setAbsAmount(absAmount);
            m.setWithdrawal(true);
            m.setRequestDay(now.getDay());
            m.setRequestDate(now.getDate());
            m.setEventDay(eventDay);
            m.setValueDay(valueDay);
            m.setTargetFiCode(acc.getFiCode());
            m.setTargetFiAccountId(acc.getFiAccountId());
            m.setSelfFiCode(selfAcc.getFiCode());
            m.setSelfFiAccountId(selfAcc.getFiAccountId());
            m.setStatusType(ActionStatusType.UNPROCESSED);
            m.setUpdateActor(updActor);
            m.setUpdateDate(now.getDate());
            m.setCashflowId(null);
            return m;
        }
    }

}
