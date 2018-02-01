package sample.usecase.mail;

import java.util.*;

import lombok.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.*;
import org.springframework.transaction.support.*;

import sample.InvocationException;
import sample.context.mail.*;
import sample.context.mail.MailHandler.SendMail;
import sample.context.orm.JpaRepository.DefaultRepository;
import sample.model.account.Account;
import sample.model.asset.CashInOut;

/**
 * アプリケーション層のサービスメール送信を行います。
 * <p>独自にトランザクションを管理するので、サービスのトランザクション内で
 * 呼び出さないように注意してください。
 * 
 * @author jkazama
 */
@Component
@Setter
@SuppressWarnings("unused")
public class ServiceMailDeliver {

    @Autowired
    private MessageSource msg;
    @Autowired
    private DefaultRepository rep;
    @Autowired
    private PlatformTransactionManager tx;
    @Autowired
    private MailHandler mail;

    /** サービスメールを送信します。 */
    public void send(final String accountId, final ServiceMailCreator creator) {
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

    /** 出金依頼受付メールを送信します。 */
    public void sendWithdrawal(final CashInOut cio) {
        send(cio.getAccountId(), account -> {
            // low: 実際のタイトルや本文はDBの設定情報から取得
            String subject = "[" + cio.getId() + "] 出金依頼受付のお知らせ";
            String body = "{name}様 …省略…";
            Map<String, String> bodyArgs = new HashMap<>();
            bodyArgs.put("name", account.getName());
            return new SendMail(account.getMail(), subject, body, bodyArgs);
        });
    }

    /** メール送信情報の生成インターフェース */
    public static interface ServiceMailCreator {
        SendMail create(final Account account);
    }

}
