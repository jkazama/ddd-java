package sample.context.mail;

import java.util.Map;

import lombok.Setter;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import sample.context.Dto;

/**
 * Send and receive mail.
 * low: In the sample, only I / F for sending mail is created. In practice it also supports receiving emails such as POP3 / IMAP.
 */
@Component
@Setter
public class MailHandler {
    private static final Logger logger = LoggerFactory.getLogger(MailHandler.class);

    @Value("${sample.mail.enable:true}")
    private boolean enable;

    /** メールを送信します。 */
    public MailHandler send(final SendMail mail) {
        if (!enable) {
            logger.info("Sent a dummy email. [" + mail.subject + "]");
            return this;
        }
        // low: There should be a lot of overhead in cooperation with external resources, so it should be done asynchronously.
        // low: Send the contents of the substitution mapping of bodyArgs to body by JavaMail etc.
        return this;
    }

    @lombok.Value
    public static class SendMail implements Dto {
        private static final long serialVersionUID = 1L;
        private String address;
        private String subject;
        private String body;
        private Map<String, String> bodyArgs;
    }

}
