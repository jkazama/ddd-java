package sample.context.actor;

import org.springframework.stereotype.Component;

/**
 * スレッドローカルスコープの利用者セッション。
 * low: 今回スコープ外ですが、認証ロジックに#bind/#unbindを組み込んで運用します。
 * 
 * @author jkazama
 */
@Component
public class ActorSession {

	private ThreadLocal<Actor> actorLocal = new ThreadLocal<Actor>();

	/** 利用者セッションへ利用者を紐付けます。 */
	public ActorSession bind(final Actor actor) {
		actorLocal.set(actor);
		return this;
	}

	/** 利用者セッションを破棄します。 */
	public ActorSession unbind() {
		actorLocal.remove();
		return this;
	}

	/**
	 * @return 有効な利用者を返します。紐付けされていない時は匿名者が返されます。
	 */
	public Actor actor() {
		Actor actor = actorLocal.get();
		return actor != null ? actor : Actor.Anonymous;
	}

}
