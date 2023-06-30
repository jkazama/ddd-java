package sample.controller.system;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sample.usecase.admin.AssetAdminService;
import sample.usecase.admin.MasterAdminService;

/**
 * API controller of the system job.
 * <p>
 * the URL after "/system/job" assumes what is carried out from job scheduler,
 * it is necessary to make it inaccessible from the outside in L/B.
 */
@RestController
@RequestMapping("/system/job")
@RequiredArgsConstructor
public class JobController {
    private final AssetAdminService asset;
    private final MasterAdminService master;

    @PostMapping("/daily/processDay")
    public void processDay() {
        master.processDay();
    }

    @PostMapping("/daily/closingCashOut")
    public void closingCashOut() {
        asset.closingCashOut();
    }

    @PostMapping("/daily/realizeCashflow")
    public void realizeCashflow() {
        asset.realizeCashflow();
    }

}
