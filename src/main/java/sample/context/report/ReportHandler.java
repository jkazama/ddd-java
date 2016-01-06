package sample.context.report;

import org.springframework.stereotype.Component;

/**
 * 帳票処理を行います。
 * low: サンプルでは概念クラスだけ提供します。実際はCSV/固定長/Excel/PDFなどの取込/出力を取り扱います。
 * low: ExcelはPOI、PDFはJasperReportの利用が一般的です。(商用製品を利用するのもおすすめです)
 * 
 * @author jkazama
 */
@Component
public class ReportHandler {

}
