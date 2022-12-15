package sample.context.lock;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Component;

import sample.InvocationException;

/**
 * ID単位のロックを表現します。
 * low: ここではシンプルに口座単位のIDロックのみをターゲットにします。
 * low: 通常はDBのロックテーブルに"for update"要求で悲観的ロックをとったりしますが、サンプルなのでメモリロックにしてます。
 * 
 * @author jkazama
 */
@Component
public class IdLockHandler {

    private ConcurrentMap<Object, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();

    /** IDロック上で処理を実行します。 */
    public <T> T call(Object id, LockType lockType, final Callable<T> callable) {
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

    /** IDロック上で処理を実行します。 */
    public void call(Object id, LockType lockType, final Runnable runnable) {
        call(id, lockType, () -> {
            runnable.run();
            return null;
        });
    }

    private void writeLock(final Object id) {
        if (id == null) {
            return;
        }
        idLock(id).writeLock().lock();
    }

    private ReentrantReadWriteLock idLock(final Object id) {
        return lockMap.computeIfAbsent(id, v -> new ReentrantReadWriteLock());
    }

    public void readLock(final Object id) {
        if (id == null) {
            return;
        }
        idLock(id).readLock().lock();
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

    /**
     * ロック種別を表現するEnum。
     *
     * @author jkazama
     */
    public static enum LockType {
        /** 読み取り専用ロック */
        Read,
        /** 読み書き専用ロック */
        Write;

        public boolean isRead() {
            return !isWrite();
        }

        public boolean isWrite() {
            return this == Write;
        }
    }

    /** IdLock の対象と種別のペアを表現します。 */
    public static record IdLockPair(Object id, LockType lockType) {
    }

}
