package sample.context.lock;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Component;

import lombok.val;
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

	private Map<Serializable, ReentrantReadWriteLock> lockMap = new HashMap<>();

	/** IDロック上で処理を実行します。 */
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

	private void writeLock(final Serializable id) {
		if (id == null) {
			return;
		}
		synchronized (lockMap) {
			idLock(id).writeLock().lock();
		}
	}

	private ReentrantReadWriteLock idLock(final Serializable id) {
		if (!lockMap.containsKey(id)) {
			lockMap.put(id, new ReentrantReadWriteLock());
		}
		return lockMap.get(id);
	}

	public void readLock(final Serializable id) {
		if (id == null) {
			return;
		}
		synchronized (lockMap) {
			idLock(id).readLock().lock();
		}
	}

	public void unlock(final Serializable id) {
		if (id == null) {
			return;
		}

		synchronized (lockMap) {
			val idLock = idLock(id);
			if (idLock.isWriteLockedByCurrentThread()) {
				idLock.writeLock().unlock();
			} else {
				idLock.readLock().unlock();
			}
		}
	}

	/**
	 * ロック種別を表現するEnum。
	 *
	 * @author jkazama
	 */
	public static enum LockType {
		/** 読み取り専用ロック */
		READ,
		/** 読み書き専用ロック */
		WRITE;

		public boolean isRead() {
			return !isWrite();
		}

		public boolean isWrite() {
			return this == WRITE;
		}
	}
}
