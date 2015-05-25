package sample.usecase;

import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sample.context.lock.IdLockHandler.LockType;
import sample.model.asset.*;
import sample.model.asset.CashInOut.FindCashInOut;

/**
 * 資産ドメインに対する社内ユースケース処理。
 * 
 * @author jkazama
 */
@Service
public class AssetAdminService extends ServiceSupport {

	/**
	 * 振込入出金依頼を検索します。
	 * low: 口座横断的なので割り切りでREADロックはかけません。
	 */
	@Transactional
	public List<CashInOut> findCashInOut(final FindCashInOut p) {
		return CashInOut.find(rep(), p);
	}
	
	/**
	 * 振込出金依頼を締めます。
	 */
	public void closingCashOut() {
		audit().audit("振込出金依頼の締め処理をする", new Callable<Object>() {
			public Object call() throws Exception {
				return tx(new Callable<Object>() {
					public Object call() throws Exception {
						closingCashOutInTx();
						return null;
					}
				});
			}
		});
	}
	
	private void closingCashOutInTx() {
		//low: 以降の処理は口座単位でfilter束ねしてから実行する方が望ましい。
		//low: 大量件数の処理が必要な時はそのままやるとヒープが死ぬため、idソートでページング分割して差分実行していく。
		for (final CashInOut cio : CashInOut.findUnprocessed(rep())) {
			//low: TX内のロックが適切に動くかはIdLockHandlerの実装次第。
			// 調整が難しいようなら大人しく営業停止時間(IdLock必要な処理のみ非活性化されている状態)を作って、
			// ロック無しで一気に処理してしまう方がシンプル。
			idLock().call(cio.getAccountId(), LockType.WRITE, new Callable<Object>() {
				public Object call() throws Exception {
					try {
						cio.process(rep());
						//low: SQLの発行担保。扱う情報に相互依存が無く、セッションキャッシュはリークしがちなので都度消しておく。
						rep().flushAndClear();
					} catch (Exception e) {
						logger.error("[" + cio.getId() + "] 振込出金依頼の締め処理に失敗しました。", e);
						try {
							cio.error(rep());
							rep().flush();
						} catch (Exception ex) {
							//low: 2重障害(恐らくDB起因)なのでloggerのみの記載に留める
						}
					}
					return null;
				}
			});
		}		
	}
	
	/**
	 * キャッシュフローを実現します。
	 * <p>受渡日を迎えたキャッシュフローを残高に反映します。
	 */
	public void realizeCashflow() {
		audit().audit("キャッシュフローを実現する", new Callable<Object>() {
			public Object call() throws Exception {
				return tx(new Callable<Object>() {
					public Object call() throws Exception {
						realizeCashflowInTx();
						return null;
					}
				});
			}
		});
	}

	private void realizeCashflowInTx() {
		//low: 日回し後の実行を想定
		String day = dh().time().day();
		for (final Cashflow cf : Cashflow.findDoRealize(rep(), day)) {
			idLock().call(cf.getAccountId(), LockType.WRITE, new Callable<Object>() {
				public Object call() throws Exception {
					try {
						cf.realize(rep());
						rep().flushAndClear();
					} catch (Exception e) {
						logger.error("[" + cf.getId() + "] キャッシュフローの実現に失敗しました。", e);
						try {
							cf.error(rep());
							rep().flush();
						} catch (Exception ex) {
						}
					}
					return null;
				}
			});
		}		
	}

	
}
