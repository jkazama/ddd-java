package sample.usecase;

import java.util.Locale;
import java.util.concurrent.Callable;

import lombok.Setter;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.transaction.*;
import org.springframework.transaction.support.*;

import sample.InvocationException;
import sample.context.DomainHelper;
import sample.context.actor.Actor;
import sample.context.audit.AuditHandler;
import sample.context.lock.*;
import sample.context.lock.IdLockHandler.LockType;
import sample.context.orm.JpaRepository.DefaultRepository;
import sample.usecase.mail.ServiceMailDeliver;
import sample.usecase.report.ServiceReportExporter;

/**
 * ユースケースサービスの基底クラス。
 * 
 * @author jkazama
 */
@Setter
public abstract class ServiceSupport {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MessageSource msg;

	@Autowired
	private DomainHelper dh;
	@Autowired
	private DefaultRepository rep;
	@Autowired
	private PlatformTransactionManager tx;
	@Autowired
	private IdLockHandler idLock;

	@Autowired
	private AuditHandler audit;
	@Autowired
	private ServiceMailDeliver mail;
	@Autowired
	private ServiceReportExporter report;

	/** ドメイン層向けヘルパークラスを返します。 */
	protected DomainHelper dh() {
		return dh;
	}

	/** 標準スキーマのRepositoryを返します。 */
	protected DefaultRepository rep() {
		return rep;
	}

	/** IDロックユーティリティを返します。 */
	protected IdLockHandler idLock() {
		return idLock;
	}
	
	/** 監査ユーティリティを返します。 */
	protected AuditHandler audit() {
		return audit;
	}
	
	/** サービスメールユーティリティを返します。 */
	protected ServiceMailDeliver mail() {
		return mail;
	}

	/** サービスレポートユーティリティを返します。 */
	protected ServiceReportExporter report() {
		return report;
	}

	/** i18nメッセージ変換を行います。 */
	protected String msg(String message) {
		return msg.getMessage(message, null, message, Locale.getDefault());
	}

	/** 利用者を返します。 */
	protected Actor actor() {
		return dh.actor();
	}

	/** トランザクション処理を実行します。 low: クロージャの代わりにCallableで代替 */
	protected <T> T tx(final Callable<T> callable) {
		return new TransactionTemplate(tx).execute(new TransactionCallback<T>() {
			public T doInTransaction(TransactionStatus status) {
				try {
					return callable.call();
				} catch (RuntimeException e) {
					throw (RuntimeException) e;
				} catch (Exception e) {
					throw new InvocationException("error.Exception", e);
				}
			}
		});
	}

	/** 口座ロック付でトランザクション処理を実行します。 */
	protected <T> T tx(String accountId, LockType lockType, final Callable<T> callable) {
		return idLock.call(accountId, lockType, new Callable<T>() {
			public T call() throws Exception {
				return tx(callable);
			}
		});
	}

}
