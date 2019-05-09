package sample.context.lock;

import java.io.Serializable;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Component;

import lombok.Value;
import sample.InvocationException;

/**
 * The lock of the ID unit.
 * low: It is simple and targets only the ID lock of the account unit here.
 * low: You take the pessimistic lock by "for update" demand on a lock table of DB,
 * but usually do it to memory lock because it is a sample.
 */
@Component
public class IdLockHandler {

    private ConcurrentMap<Serializable, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();

    public <T> T call(Serializable id, LockType lockType, final Callable<T> callable) {
        if (lockType.isWrite()) {
            writeLock(id);
        } else {
            readLock(id);
        }
        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationException("error.Exception", e);
        } finally {
            unlock(id);
        }
    }
    
    public void call(Serializable id, LockType lockType, final Runnable runnable) {
        call(id, lockType, () -> {
            runnable.run();
            return null;
        });
    }

    private void writeLock(final Serializable id) {
        if (id == null) {
            return;
        }
        idLock(id).writeLock().lock();
    }

    private ReentrantReadWriteLock idLock(final Serializable id) {
        return lockMap.computeIfAbsent(id, v -> new ReentrantReadWriteLock());
    }

    public void readLock(final Serializable id) {
        if (id == null) {
            return;
        }
        idLock(id).readLock().lock();
    }

    public void unlock(final Serializable id) {
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
        Read,
        Write;

        public boolean isRead() {
            return !isWrite();
        }

        public boolean isWrite() {
            return this == Write;
        }
    }
    
    @Value
    public static class IdLockPair {
        private Serializable id;
        private LockType lockType;
    }
    
}
