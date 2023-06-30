package sample.context.lock;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Component;

import sample.context.InvocationException;

/**
 * The lock of the ID unit.
 * low: It is simple and targets only the ID lock of the account unit here.
 * low: You take the pessimistic lock by "for update" demand on a lock table of
 * DB,
 * but usually do it to memory lock because it is a sample.
 */
@Component
public class IdLockHandler {

    private ConcurrentMap<Object, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();

    public <T> T call(Object id, LockType lockType, final Callable<T> callable) {
        if (lockType.isWrite()) {
            this.writeLock(id);
        } else {
            this.readLock(id);
        }
        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationException("error.Exception", e);
        } finally {
            this.unlock(id);
        }
    }

    public void call(Object id, LockType lockType, final Runnable runnable) {
        this.call(id, lockType, () -> {
            runnable.run();
            return null;
        });
    }

    private void writeLock(final Object id) {
        if (id == null) {
            return;
        }
        this.idLock(id).writeLock().lock();
    }

    private ReentrantReadWriteLock idLock(final Object id) {
        return lockMap.computeIfAbsent(id, v -> new ReentrantReadWriteLock());
    }

    public void readLock(final Object id) {
        if (id == null) {
            return;
        }
        this.idLock(id).readLock().lock();
    }

    public void unlock(final Object id) {
        if (id == null) {
            return;
        }

        ReentrantReadWriteLock idLock = idLock(id);
        if (idLock.isWriteLockedByCurrentThread()) {
            idLock.writeLock().unlock();
        } else {
            idLock.readLock().unlock();
        }
    }

    public static enum LockType {
        READ,
        WRITE;

        public boolean isRead() {
            return !isWrite();
        }

        public boolean isWrite() {
            return this == WRITE;
        }
    }

    public static record IdLockPair(Object id, LockType lockType) {
    }

}
