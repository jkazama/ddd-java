package sample.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.FindCashInOut;
import sample.usecase.AssetAdminService;

/**
 * 資産に関わる社内のUI要求を処理します。
 *
 * @author jkazama
 */
@RestController
@RequestMapping("/admin/asset")
@RequiredArgsConstructor
public class AssetAdminController {
    private final AssetAdminService service;

    /** 未処理の振込依頼情報を検索します。 */
    @GetMapping("/cio")
    public List<CashInOut> findCashInOut(@Valid FindCashInOut p) {
        return service.findCashInOut(p);
    }

}
