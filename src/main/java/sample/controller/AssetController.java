package sample.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

/**
 * API controller of the asset domain.
 */
@RestController
@RequestMapping("/asset")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService service;

    @GetMapping("/cio/unprocessedOut")
    public List<UserCashOut> findUnprocessedCashOut() {
        return service.findUnprocessedCashOut().stream()
                .map(UserCashOut::of)
                .toList();
    }

    @PostMapping("/cio/withdraw")
    public ResponseEntity<Map<String, String>> withdraw(
            @RequestBody @Valid UserRegCashOut p) {
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

    @Builder
    public static record UserCashOut(
            String id,
            String currency,
            BigDecimal absAmount,
            LocalDate requestDay,
            LocalDateTime requestDate,
            LocalDate eventDay,
            LocalDate valueDay,
            ActionStatusType statusType,
            LocalDateTime updateDate,
            String cashflowId) implements Dto {
        public static UserCashOut of(final CashInOut cio) {
            return UserCashOut.builder()
                    .id(cio.id())
                    .currency(cio.currency())
                    .absAmount(cio.absAmount())
                    .requestDay(cio.requestDay())
                    .requestDate(cio.requestDate())
                    .eventDay(cio.eventDay())
                    .valueDay(cio.valueDay())
                    .statusType(cio.statusType())
                    .updateDate(cio.updateDate())
                    .cashflowId(cio.cashflowId())
                    .build();
        }
    }

}
