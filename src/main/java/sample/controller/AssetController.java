package sample.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import sample.ActionStatusType;
import sample.context.Dto;
import sample.context.actor.ActorSession;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.RegCashOut;
import sample.model.constraints.AbsAmount;
import sample.model.constraints.Currency;
import sample.usecase.AssetService;
import sample.util.TimePoint;

/**
 * 資産に関わる顧客のUI要求を処理します。
 *
 * @author jkazama
 */
@RestController
@RequestMapping("/asset")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService service;

    /** 未処理の振込依頼情報を検索します。 */
    @GetMapping("/cio/unprocessedOut")
    public List<UserCashOut> findUnprocessedCashOut() {
        return service.findUnprocessedCashOut().stream()
                .map(UserCashOut::of)
                .toList();
    }

    /**
     * 振込出金依頼をします。
     * low: 実際は状態を変えうる行為なのでPOSTですが、デモ用にGETでも処理できるようにしています。
     */
    @RequestMapping(value = "/cio/withdraw", method = { RequestMethod.POST, RequestMethod.GET })
    public ResponseEntity<Map<String, String>> withdraw(@Valid UserRegCashOut p) {
        var accountId = ActorSession.actor().id();
        return ResponseEntity.ok(Map.of("id", service.withdraw(p.toParam(accountId))));
    }

    public static record UserRegCashOut(
            @Currency String currency,
            @AbsAmount BigDecimal absAmount) implements Dto {
        public RegCashOut toParam(String accountId) {
            return RegCashOut.builder()
                    .accountId(accountId)
                    .currency(this.currency)
                    .absAmount(this.absAmount)
                    .build();
        }
    }

    /** 振込出金依頼情報の表示用Dto */
    @Builder
    public static record UserCashOut(
            String id,
            String currency,
            BigDecimal absAmount,
            TimePoint requestDate,
            String eventDay,
            String valueDay,
            ActionStatusType statusType,
            Date updateDate,
            Long cashflowId) implements Dto {
        public static UserCashOut of(final CashInOut cio) {
            return UserCashOut.builder()
                    .id(cio.getId())
                    .currency(cio.getCurrency())
                    .absAmount(cio.getAbsAmount())
                    .requestDate(cio.getRequestDate())
                    .eventDay(cio.getEventDay())
                    .valueDay(cio.getValueDay())
                    .statusType(cio.getStatusType())
                    .updateDate(cio.getUpdateDate())
                    .cashflowId(cio.getCashflowId())
                    .build();
        }
    }

}
