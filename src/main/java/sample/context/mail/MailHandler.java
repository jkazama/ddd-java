package sample.context.mail;

import java.util.Map;

import lombok.Setter;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import sample.context.Dto;

/**
 * メール送受信を行います。
 * low: サンプルではメール送信のI/Fのみ作ってます。実際はPOP3/IMAP等のメール受信もサポートしたり、
 * リターンメールのフォローアップをしたりします。
 * 
 * @author jkazama
 */
@Component
@Setter
public class MailHandler {
    private static final Logger logger = LoggerFactory.getLogger(MailHandler.class);

    /** メール利用可否 */
    @Value("${sample.mail.enable:true}")
    private boolean enable;

    /** メールを送信します。 */
    public MailHandler send(final SendMail mail) {
        if (!enable) {
            logger.info("メールをダミー送信しました。 [" + mail.subject + "]");
            return this;
        }
        // low: 外部リソースとの連携でオーバーヘッドが結構発生するので、実際は非同期処理で行う。
        // low: bodyへbodyArgsを置換マッピングした内容をJavaMailなどで送信。
        return this;
    }

    /** メール送信パラメタ。low: 実際はかなり多くの項目が関与するのでBuilderにした方が使い勝手が良いです */
    @lombok.Value
    public static class SendMail implements Dto {
        private static final long serialVersionUID = 1L;
        private String address;
        private String subject;
        private String body;
        private Map<String, String> bodyArgs;
    }

}
