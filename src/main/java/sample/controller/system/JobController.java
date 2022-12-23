package sample.controller.system;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sample.usecase.AssetAdminService;
import sample.usecase.MasterAdminService;

/**
 * システムジョブのUI要求を処理します。
 * low: 通常はバッチプロセス(または社内プロセスに内包)を別途作成して、ジョブスケジューラから実行される方式になります。
 * ジョブの負荷がオンライン側へ影響を与えないよう事前段階の設計が重要になります。
 * low: 社内/バッチプロセス切り出す場合はVM分散時の情報/排他同期を意識する必要があります。(DB同期/メッセージング同期/分散製品の利用 等)
 * low: メソッドは全てPOSTが望ましいですが、デモ用で叩きやすいようにGETを許容しています。
 *
 * @author jkazama
 */
@RestController
@RequestMapping("/system/job")
@RequiredArgsConstructor
public class JobController {
    private final AssetAdminService asset;
    private final MasterAdminService master;

    /** 営業日を進めます。 */
    @RequestMapping(value = "/daily/processDay", method = { RequestMethod.POST, RequestMethod.GET })
    public void processDay() {
        master.processDay();
    }

    /** 振込出金依頼を締めます。 */
    @RequestMapping(value = "/daily/closingCashOut", method = { RequestMethod.POST, RequestMethod.GET })
    public void closingCashOut() {
        asset.closingCashOut();
    }

    /** キャッシュフローを実現します。 */
    @RequestMapping(value = "/daily/realizeCashflow", method = { RequestMethod.POST, RequestMethod.GET })
    public void realizeCashflow() {
        asset.realizeCashflow();
    }

}
