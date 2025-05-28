package sample.context.mail;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * Send and receive mail.
 * low: In the sample, only the interface for sending mail is created. In
 * practice, it
 * also supports receiving emails such as POP3/IMAP.
 */
@Component
@Slf4j
public class MailHandler {
    @Value("${sample.mail.enabled:true}")
    private Boolean enable;

    public MailHandler send(final SendMail mail) {
        if (!enable) {
            log.info("Sent a dummy email. [" + mail.subject + "]");
            return this;
        }
        // low: There should be a lot of overhead in cooperation with external
        // resources, so it should be done asynchronously.
        // low: Send the contents of the substitution mapping of bodyArgs to body by
        // JavaMail etc.
        return this;
    }

    /** Mail sending parameters. */
    @Builder
    public static record SendMail(
            String address,
            String subject,
            String body,
            Map<String, String> bodyArgs) {
    }

}
