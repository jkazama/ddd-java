package sample.model.asset;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import sample.context.orm.JpaRepository;
import sample.util.Calculator;

/**
 * 口座の資産概念を表現します。
 * asset配下のEntityを横断的に取り扱います。
 * low: 実際の開発では多通貨や執行中/拘束中のキャッシュフローアクションに対する考慮で、サービスによってはかなり複雑になります。
 * 
 * @author jkazama
 */
@Builder
public record Asset(
                /** 口座ID */
                String id) {

        /**
         * 振込出金可能か判定します。
         * <p>
         * 0 <= 口座残高 + 未実現キャッシュフロー - (出金依頼拘束額 + 出金依頼額)
         * low: 判定のみなのでscale指定は省略。余力金額を返す時はきちんと指定する
         */
        public boolean canWithdraw(final JpaRepository rep, String currency, BigDecimal absAmount, LocalDate valueDay) {
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

        /** 口座IDに紐付く資産概念を返します。 */
        public static Asset of(String accountId) {
                return Asset.builder()
                                .id(accountId)
                                .build();
        }

}
