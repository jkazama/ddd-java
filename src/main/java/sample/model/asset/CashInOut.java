package sample.model.asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sample.ActionStatusType;
import sample.context.DomainHelper;
import sample.context.Dto;
import sample.context.orm.JpaActiveRecord;
import sample.context.orm.JpaCriteria;
import sample.context.orm.JpaRepository;
import sample.model.account.FiAccount;
import sample.model.asset.Cashflow.CashflowType;
import sample.model.asset.Cashflow.RegCashflow;
import sample.model.constraints.AbsAmount;
import sample.model.constraints.AccountId;
import sample.model.constraints.Currency;
import sample.model.constraints.CurrencyEmpty;
import sample.model.constraints.Day;
import sample.model.constraints.IdStr;
import sample.model.master.SelfFiAccount;
import sample.util.DateUtils;
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
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@NamedQueries({
        @NamedQuery(name = "CashInOut.findUnprocessed", query = "from CashInOut c where c.eventDay=?1 and c.statusType in ?2 order by c.id"),
        @NamedQuery(name = "CashInOut.findAccUnprocessed1", query = "from CashInOut c where c.accountId=?1 and c.currency=?2 and c.withdrawal=?3 and c.statusType in ?4 order by c.id"),
        @NamedQuery(name = "CashInOut.findAccUnprocessed2", query = "from CashInOut c where c.accountId=?1 and c.statusType in ?2 order by c.updateDate desc") })
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
    /** 依頼日/日時 */
    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "day", column = @Column(name = "request_day")),
            @AttributeOverride(name = "date", column = @Column(name = "request_date")) })
    private TimePoint requestDate;
    /** 発生日 */
    @Day
    private LocalDate eventDay;
    /** 受渡日 */
    @Day
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
    @NotNull
    private Date updateDate;
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
        // 事前審査
        Validator v = validator();
        v.verify(statusType.isUnprocessed(), "error.ActionStatusType.unprocessing");
        v.verify(now.afterEqualsDay(eventDay), "error.CashInOut.afterEqualsDay");
        // 処理済状態を反映
        setStatusType(ActionStatusType.PROCESSED);
        setUpdateActor(rep.dh().actor().id());
        setUpdateDate(now.getDate());
        setCashflowId(Cashflow.register(rep, regCf()).getId());
        return update(rep);
    }

    private RegCashflow regCf() {
        BigDecimal amount = withdrawal ? absAmount.negate() : absAmount;
        CashflowType cashflowType = withdrawal ? CashflowType.CashOut : CashflowType.CashIn;
        // low: 摘要はとりあえずシンプルに。実際はCashInOutへ用途フィールドをもたせた方が良い(生成元メソッドに応じて摘要を変える)
        String remark = withdrawal ? Remarks.CashOut : Remarks.CashIn;
        return new RegCashflow(accountId, currency, amount, cashflowType, remark, eventDay, valueDay);
    }

    /**
     * 依頼を取消します。
     * <p>
     * "処理済みでない"かつ"発生日を迎えていない"必要があります。
     */
    public CashInOut cancel(final JpaRepository rep) {
        TimePoint now = rep.dh().time().tp();
        // 事前審査
        Validator v = validator();
        v.verify(statusType.isUnprocessing(), "error.ActionStatusType.unprocessing");
        v.verify(now.beforeDay(eventDay), "error.CashInOut.beforeEqualsDay");
        // 取消状態を反映
        setStatusType(ActionStatusType.CANCELLED);
        setUpdateActor(rep.dh().actor().id());
        setUpdateDate(now.getDate());
        return update(rep);
    }

    /**
     * 依頼をエラー状態にします。
     * <p>
     * 処理中に失敗した際に呼び出してください。
     * low: 実際はエラー事由などを引数に取って保持する
     */
    public CashInOut error(final JpaRepository rep) {
        validator().verify(statusType.isUnprocessed(), "error.ActionStatusType.unprocessing");

        setStatusType(ActionStatusType.ERROR);
        setUpdateActor(rep.dh().actor().id());
        setUpdateDate(rep.dh().time().date());
        return update(rep);
    }

    /** 振込入出金依頼を返します。 */
    public static CashInOut load(final JpaRepository rep, String id) {
        return rep.load(CashInOut.class, id);
    }

    /** 未処理の振込入出金依頼一覧を検索します。 low: criteriaベース実装例 */
    public static List<CashInOut> find(final JpaRepository rep, final FindCashInOut p) {
        // low: 通常であれば事前にfrom/toの期間チェックを入れる
        JpaCriteria<CashInOut> criteria = rep.criteria(CashInOut.class);
        criteria.equal("currency", p.currency());
        criteria.in("statusType", p.statusTypes());
        criteria.between("updateDate", DateUtils.date(p.updFromDay()),
                DateUtils.dateTo(p.updToDay()));
        return rep.tmpl().find(criteria.sortDesc("updateDate").result());
    }

    /** 当日発生で未処理の振込入出金一覧を検索します。 */
    public static List<CashInOut> findUnprocessed(final JpaRepository rep) {
        return rep.tmpl().find("CashInOut.findUnprocessed", rep.dh().time().day(), ActionStatusType.unprocessedTypes);
    }

    /** 未処理の振込入出金一覧を検索します。(口座別) */
    public static List<CashInOut> findUnprocessed(final JpaRepository rep, String accountId, String currency,
            boolean withdrawal) {
        return rep.tmpl().find("CashInOut.findAccUnprocessed1", accountId, currency, withdrawal,
                ActionStatusType.unprocessedTypes);
    }

    /** 未処理の振込入出金一覧を検索します。(口座別) */
    public static List<CashInOut> findUnprocessed(final JpaRepository rep, String accountId) {
        return rep.tmpl().find("CashInOut.findAccUnprocessed2", accountId, ActionStatusType.unprocessedTypes);
    }

    /** 出金依頼をします。 */
    public static CashInOut withdraw(final JpaRepository rep, final RegCashOut p) {
        DomainHelper dh = rep.dh();
        TimePoint now = dh.time().tp();
        // low: 発生日は締め時刻等の兼ね合いで営業日と異なるケースが多いため、別途DB管理される事が多い
        String eventDay = now.getDay();
        // low: 実際は各金融機関/通貨の休日を考慮しての T+N 算出が必要
        String valueDay = dh.time().dayPlus(3);

        // 事前審査
        Validator v = new Validator();
        v.verifyField(0 < p.absAmount().signum(), "absAmount", "error.domain.AbsAmount.zero");
        boolean canWithdraw = Asset.of(p.accountId()).canWithdraw(rep, p.currency(), p.absAmount(), valueDay);
        v.verifyField(canWithdraw, "absAmount", "error.CashInOut.withdrawAmount");

        // 出金依頼情報を登録
        String uid = dh.uid().generate(CashInOut.class.getSimpleName());
        var acc = FiAccount.load(rep, p.accountId(), Remarks.CashOut, p.currency());
        var selfAcc = SelfFiAccount.load(rep, Remarks.CashOut, p.currency());
        String updateActor = dh.actor().id();
        return p.create(now, uid, eventDay, valueDay, acc, selfAcc, updateActor).save(rep);
    }

    /** 振込入出金依頼の検索パラメタ。 low: 通常は顧客視点/社内視点で利用条件が異なる */
    @Builder
    public static record FindCashInOut(
            @CurrencyEmpty String currency,
            ActionStatusType[] statusTypes,
            @Day String updFromDay,
            @Day String updToDay) implements Dto {
    }

    /** 振込出金の依頼パラメタ。 */
    @Builder
    public static record RegCashOut(
            @AccountId String accountId,
            @Currency String currency,
            @AbsAmount BigDecimal absAmount) implements Dto {

        public CashInOut create(final TimePoint now, String id, String eventDay, String valueDay, final FiAccount acc,
                final SelfFiAccount selfAcc, String updActor) {
            var m = new CashInOut();
            m.setId(id);
            m.setAccountId(accountId);
            m.setCurrency(currency);
            m.setAbsAmount(absAmount);
            m.setWithdrawal(true);
            m.setRequestDate(now);
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
