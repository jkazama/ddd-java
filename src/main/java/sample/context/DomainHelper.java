package sample.context;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sample.context.actor.*;
import sample.context.uid.IdGenerator;

/**
 * ドメイン処理を行う上で必要となるインフラ層コンポーネントへのアクセサを提供します。
 * 
 * @author jkazama
 */
@Component
@Setter
public class DomainHelper {

	@Autowired
	private ActorSession actorSession;
	@Autowired
	private Timestamper time;
	@Autowired
	private IdGenerator uid;

	/**
	 * @return ログイン中のユースケース利用者
	 */
	public Actor actor() {
		return actorSession.actor();
	}

	/**
	 * @return 日時ユーティリティ
	 */
	public Timestamper time() {
		return time;
	}

	/**
	 * @return ID生成ユーティリティ
	 */
	public IdGenerator uid() {
		return uid;
	}

}
