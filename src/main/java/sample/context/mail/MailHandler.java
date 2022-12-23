package sample.context.mail;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * メール送受信を行います。
 * low: サンプルではメール送信のI/Fのみ作ってます。実際はPOP3/IMAP等のメール受信もサポートしたり、
 * リターンメールのフォローアップをしたりします。
 * 
 * @author jkazama
 */
@Component
@Slf4j
public class MailHandler {
    /** メール利用可否 */
    @Value("${sample.mail.enabled:true}")
    private Boolean enable;

    /** メールを送信します。 */
    public MailHandler send(final SendMail mail) {
        if (!enable) {
            log.info("メールをダミー送信しました。 [" + mail.subject + "]");
            return this;
        }
        // low: 外部リソースとの連携でオーバーヘッドが結構発生するので、実際は非同期処理で行う。
        // low: bodyへbodyArgsを置換マッピングした内容をJavaMailなどで送信。
        return this;
    }

    /** メール送信パラメタ。 */
    @Builder
    public static record SendMail(
            String address,
            String subject,
            String body,
            Map<String, String> bodyArgs) {
    }

}
