package sample.usecase.mail;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import sample.context.InvocationException;
import sample.context.mail.MailHandler;
import sample.context.mail.MailHandler.SendMail;
import sample.context.orm.OrmRepository.DefaultRepository;
import sample.context.orm.TxTemplate;
import sample.model.account.Account;
import sample.model.asset.CashInOut;
import sample.usecase.event.AppMailEvent;

/**
 * Mail deliver of the application layer.
 * <p>
 * Manage the transaction originally, please be careful not to call it in the
 * transaction of the service.
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ServiceMailDeliver {
    private final MessageSource msg;
    private final DefaultRepository rep;
    private final PlatformTransactionManager tx;
    private final MailHandler mail;

    @EventListener(AppMailEvent.class)
    public void handleEvent(AppMailEvent<?> event) {
        switch (event.mailType()) {
            case FINISH_REQUEST_WITHDRAW -> sendFinishRequestWithdraw((CashInOut) event.value());
            default -> throw new IllegalStateException("Unsupported email type. [" + event + "]");
        }
    }

    public void sendFinishRequestWithdraw(final CashInOut cio) {
        send(cio.getAccountId(), account -> {
            // low: Actual title and text are acquired from setting information
            String subject = "[" + cio.getId() + "] Notification of withdrawal request acceptance";
            String body = "{name} …";
            Map<String, String> bodyArgs = new HashMap<>();
            bodyArgs.put("name", account.getName());
            return new SendMail(account.getMail(), subject, body, bodyArgs);
        });
    }

    private void send(final String accountId, final ServiceMailCreator creator) {
        TxTemplate.of(tx).tx(() -> {
            try {
                mail.send(creator.create(Account.load(rep, accountId)));
                return null;
            } catch (RuntimeException e) {
                throw (RuntimeException) e;
            } catch (Exception e) {
                throw new InvocationException("errors.MailException", e);
            }
        });
    }

    public static interface ServiceMailCreator {
        SendMail create(final Account account);
    }

}
