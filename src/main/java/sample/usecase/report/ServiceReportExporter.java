package sample.usecase.report;

import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import sample.context.orm.OrmRepository.DefaultRepository;
import sample.context.report.ReportHandler;
import sample.model.asset.CashInOut.FindCashInOut;

/**
 * Report exporter of the application layer.
 * <p>
 * Manage the transaction originally, please be careful not to call it in the
 * transaction of the service.
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ServiceReportExporter {
    private final MessageSource msg;
    private final DefaultRepository rep;
    private final PlatformTransactionManager tx;
    private final ReportHandler report; // low: It is not used because it is not implemented in the sample

    public ByteArrayResource exportCashInOut(final FindCashInOut p) {
        // low: Binary generation. Assumes an online download that allows you to specify
        // conditions.
        return new ByteArrayResource(new byte[0]);
    }

    public void exportFileCashInOut(String baseDay) {
        // low: File output to a specific directory. Assume use in jobs etc
    }

}
