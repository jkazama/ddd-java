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
 * アプリケーション層のサービスメール送信を行います。
 * <p>独自にトランザクションを管理するので、サービスのトランザクション内で
 * 直接呼び出さないように注意してください。
 * 
 * @author jkazama
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

    /** メール配信要求を受け付けます。 */
    @EventListener(AppMailEvent.class)
    public void handleEvent(AppMailEvent<?> event) {
        switch (event.getMailType()) {
        case FinishRequestWithdraw:
            sendFinishRequestWithdraw((CashInOut)event.getValue());
            break;
        default:
            throw new IllegalStateException("サポートされないメール種別です。 [" + event + "]");
        }
    }
    
    /** 出金依頼受付メールを送信します。 */
    public void sendFinishRequestWithdraw(final CashInOut cio) {
        send(cio.getAccountId(), account -> {
            // low: 実際のタイトルや本文はDBの設定情報から取得
            String subject = "[" + cio.getId() + "] 出金依頼受付のお知らせ";
            String body = "{name}様 …省略…";
            Map<String, String> bodyArgs = new HashMap<>();
            bodyArgs.put("name", account.getName());
            return new SendMail(account.getMail(), subject, body, bodyArgs);
        });
    }

    /** サービスメールを送信します。 */
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
    
    /** メール送信情報の生成インターフェース */
    public static interface ServiceMailCreator {
        SendMail create(final Account account);
    }

}
