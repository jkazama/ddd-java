package sample.controller.admin;

import java.util.List;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.*;

import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.FindCashInOut;
import sample.usecase.AssetAdminService;

/**
 * API controller of the asset domain in the organization.
 */
@RestController
@RequestMapping("/admin/asset")
public class AssetAdminController {

    private final AssetAdminService service;

    public AssetAdminController(AssetAdminService service) {
        this.service = service;
    }

    @GetMapping("/cio")
    public List<CashInOut> findCashInOut(@Valid FindCashInOut p) {
        return service.findCashInOut(p);
    }

}
