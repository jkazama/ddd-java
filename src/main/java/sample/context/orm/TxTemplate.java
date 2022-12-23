package sample.context.orm;

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import sample.context.lock.IdLockHandler;
import sample.context.lock.IdLockHandler.IdLockPair;
import sample.context.lock.IdLockHandler.LockType;

/**
 * トランザクションの簡易ユーティリティです。
 * <p>
 * TransactionTemplate のサポートビルダー的な利用を想定します。
 * 使い回しではなくトランザクション毎に生成して利用するようにしてください。
 * 
 * @author jkazama
 */
public class TxTemplate {
    private Optional<IdLockHandler> idLock = Optional.empty();
    private Optional<IdLockPair> IdLockPair = Optional.empty();
    private final TransactionTemplate tmpl;

    public TxTemplate(PlatformTransactionManager txm) {
        this.tmpl = new TransactionTemplate(txm);
    }

    public TransactionTemplate origin() {
        return tmpl;
    }

    /** 参照専用トランザクションにします。 */
    public TxTemplate readOnly() {
        this.tmpl.setReadOnly(true);
        return this;
    }

    /** トランザクション種別を設定します。 */
    public TxTemplate propagation(Propagation propagation) {
        this.tmpl.setPropagationBehavior(propagation.value());
        return this;
    }

    /** トランザクション分離レベルを設定します。 */
    public TxTemplate isolation(Isolation isolation) {
        this.tmpl.setIsolationLevel(isolation.value());
        return this;
    }

    /** トランザクションタイムアウト(sec)を設定します。 */
    public TxTemplate timeout(int timeout) {
        this.tmpl.setTimeout(timeout);
        return this;
    }

    /** 指定したIDの参照ロックをかけます */
    public TxTemplate readIdLock(IdLockHandler idLock, Object id) {
        Assert.notNull(id, "id is required.");
        this.idLock = Optional.ofNullable(idLock);
        this.IdLockPair = Optional.of(new IdLockPair(id, LockType.READ));
        return this;
    }

    /** 指定したIDの更新ロックをかけます */
    public TxTemplate writeIdLock(IdLockHandler idLock, Object id) {
        Assert.notNull(id, "id is required.");
        this.idLock = Optional.ofNullable(idLock);
        this.IdLockPair = Optional.of(new IdLockPair(id, LockType.WRITE));
        return this;
    }

    /** トランザクション処理をおこないます。 */
    public void tx(Runnable runnable) {
        if (this.idLock.isPresent()) {
            this.idLock.get().call(this.IdLockPair.get().id(), this.IdLockPair.get().lockType(), () -> {
                tmpl.execute(status -> {
                    runnable.run();
                    return null;
                });
            });
        } else {
            tmpl.execute(status -> {
                runnable.run();
                return null;
            });
        }
    }

    /** トランザクション処理をおこないます。 */
    public <T> T tx(Supplier<T> supplier) {
        if (this.idLock.isPresent()) {
            return this.idLock.get().call(this.IdLockPair.get().id(), this.IdLockPair.get().lockType(),
                    () -> tmpl.execute(status -> supplier.get()));
        } else {
            return tmpl.execute(status -> supplier.get());
        }
    }

    public static TxTemplate of(PlatformTransactionManager txm) {
        return new TxTemplate(txm);
    }

}