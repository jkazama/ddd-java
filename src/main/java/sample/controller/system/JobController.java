package sample.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import sample.usecase.*;

/**
 * API controller of the system job.
 * <p>the URL after "/system/job" assumes what is carried out from job scheduler,
 * it is necessary to make it inaccessible from the outside in L/B.
 * low: All methods should be POST, but for demonstration purposes, they allow GET.
 */
@RestController
@RequestMapping("/system/job")
public class JobController {

    @Autowired
    private AssetAdminService asset;
    @Autowired
    private MasterAdminService master;

    public JobController(AssetAdminService asset, MasterAdminService master) {
        this.asset = asset;
        this.master = master;
    }

    @RequestMapping(value = "/daily/processDay", method = { RequestMethod.POST, RequestMethod.GET })
    public void processDay() {
        master.processDay();
    }

    @RequestMapping(value = "/daily/closingCashOut", method = { RequestMethod.POST, RequestMethod.GET })
    public void closingCashOut() {
        asset.closingCashOut();
    }

    @RequestMapping(value = "/daily/realizeCashflow", method = { RequestMethod.POST, RequestMethod.GET })
    public void realizeCashflow() {
        asset.realizeCashflow();
    }

}
