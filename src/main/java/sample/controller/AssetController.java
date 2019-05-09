package sample.controller;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.*;

import lombok.Value;
import sample.ActionStatusType;
import sample.context.Dto;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.RegCashOut;
import sample.usecase.AssetService;
import sample.util.TimePoint;

/**
 * API controller of the asset domain.
 */
@RestController
@RequestMapping("/asset")
public class AssetController {

    private final AssetService service;

    public AssetController(AssetService service) {
        this.service = service;
    }

    @GetMapping("/cio/unprocessedOut")
    public List<CashOutUI> findUnprocessedCashOut() {
        return service.findUnprocessedCashOut().stream()
            .map(CashOutUI::by)
            .collect(Collectors.toList());
    }

    /**
     * Request a withdrawal.
     * low: Actually, it is an action that can change the state, so it is POST, but it is also possible to process with GET for demonstration.
     */
    @RequestMapping(value = "/cio/withdraw", method = { RequestMethod.POST, RequestMethod.GET })
    public String withdraw(@Valid RegCashOut p) {
        return service.withdraw(p);
    }

    @Value
    public static class CashOutUI implements Dto {
        private static final long serialVersionUID = 1L;
        private String id;
        private String currency;
        private BigDecimal absAmount;
        private TimePoint requestDate;
        private String eventDay;
        private String valueDay;
        private ActionStatusType statusType;
        private Date updateDate;
        private Long cashflowId;

        public static CashOutUI by(final CashInOut cio) {
            return new CashOutUI(cio.getId(), cio.getCurrency(), cio.getAbsAmount(), cio.getRequestDate(),
                    cio.getEventDay(), cio.getValueDay(), cio.getStatusType(), cio.getUpdateDate(),
                    cio.getCashflowId());
        }
    }

}
