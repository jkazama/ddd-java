package sample.context.orm;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import sample.context.lock.IdLockHandler;
import sample.context.lock.IdLockHandler.*;

/**
 * It is a simple utility of transaction.
 * <p>This utility will assume a support builder usage of TransactionTemplate.
 * Please make sure to generate and use for each transaction.
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

    public TxTemplate readOnly() {
        this.tmpl.setReadOnly(true);
        return this;
    }

    public TxTemplate propagation(Propagation propagation) {
        this.tmpl.setPropagationBehavior(propagation.value());
        return this;
    }

    public TxTemplate isolation(Isolation isolation) {
        this.tmpl.setIsolationLevel(isolation.value());
        return this;
    }

    public TxTemplate timeout(int timeout) {
        this.tmpl.setTimeout(timeout);
        return this;
    }

    public TxTemplate readIdLock(IdLockHandler idLock, Serializable id) {
        Assert.notNull(id, "id is required.");
        this.idLock = Optional.ofNullable(idLock);
        this.IdLockPair = Optional.of(new IdLockPair(id, LockType.Read));
        return this;
    }

    public TxTemplate writeIdLock(IdLockHandler idLock, Serializable id) {
        Assert.notNull(id, "id is required.");
        this.idLock = Optional.ofNullable(idLock);
        this.IdLockPair = Optional.of(new IdLockPair(id, LockType.Write));
        return this;
    }

    public void tx(Runnable runnable) {
        if (this.idLock.isPresent()) {
            this.idLock.get().call(this.IdLockPair.get().getId(), this.IdLockPair.get().getLockType(), () -> {
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

    public <T> T tx(Supplier<T> supplier) {
        if (this.idLock.isPresent()) {
            return this.idLock.get().call(this.IdLockPair.get().getId(), this.IdLockPair.get().getLockType(),
                    () -> tmpl.execute(status -> supplier.get()));
        } else {
            return tmpl.execute(status -> supplier.get());
        }
    }

    public static TxTemplate of(PlatformTransactionManager txm) {
        return new TxTemplate(txm);
    }

}