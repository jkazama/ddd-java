package sample.usecase;

import java.util.concurrent.Callable;

import org.springframework.stereotype.Service;

import sample.context.Timestamper;

/**
 * サービスマスタドメインに対する社内ユースケース処理。
 * 
 * @author jkazama
 */
@Service
public class MasterAdminService extends ServiceSupport {

	/**
	 * 営業日を進めます。
	 * low: 実際はスレッドセーフの考慮やDB連携含めて、色々とちゃんと作らないとダメです。
	 */
	public void processDay() {
		audit().audit("営業日を進める", new Callable<Object>() {
			public Object call() throws Exception {
				Timestamper time = dh().time();
				time.daySet(time.dayPlus(1));
				return null;
			}
		});
	}
	
}
