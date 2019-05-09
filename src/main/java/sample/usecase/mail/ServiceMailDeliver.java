package sample.usecase.mail;

import java.util.*;

import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import sample.InvocationException;
import sample.context.mail.MailHandler;
import sample.context.mail.MailHandler.SendMail;
import sample.context.orm.JpaRepository.DefaultRepository;
import sample.model.account.Account;
import sample.model.asset.CashInOut;
import sample.usecase.event.AppMailEvent;

/**
 * Mail deliver of the application layer.
 * <p>Manage the transaction originally, please be careful not to call it in the transaction of the service.
 */
@Component
@SuppressWarnings("unused")
public class ServiceMailDeliver {

    private final MessageSource msg;
    private final DefaultRepository rep;
    private final PlatformTransactionManager tx;
    private final MailHandler mail;
    
    public ServiceMailDeliver(
            MessageSource msg,
            DefaultRepository rep,
            PlatformTransactionManager tx,
            MailHandler mail) {
        this.msg = msg;
        this.rep = rep;
        this.tx = tx;
        this.mail = mail;
    }

    @EventListener(AppMailEvent.class)
    public void handleEvent(AppMailEvent<?> event) {
        switch (event.getMailType()) {
        case FinishRequestWithdraw:
            sendFinishRequestWithdraw((CashInOut)event.getValue());
            break;
        default:
            throw new IllegalStateException("Unsupported email type. [" + event + "]");
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
        new TransactionTemplate(tx).execute(status -> {
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
