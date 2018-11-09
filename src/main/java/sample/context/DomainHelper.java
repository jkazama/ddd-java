package sample.context;

import org.springframework.stereotype.Component;

import sample.context.actor.*;
import sample.context.uid.IdGenerator;

/**
 * ドメイン処理を行う上で必要となるインフラ層コンポーネントへのアクセサを提供します。
 * 
 * @author jkazama
 */
@Component
public class DomainHelper {

    private final ActorSession actorSession;
    private final Timestamper time;
    private final IdGenerator uid;

    public DomainHelper(
            ActorSession actorSession,
            Timestamper time,
            IdGenerator uid) {
        this.actorSession = actorSession;
        this.time = time;
        this.uid = uid;
    }

    /**
     * @return ログイン中のユースケース利用者
     */
    public Actor actor() {
        return actorSession.actor();
    }

    /**
     * @return ログインセッション情報
     */
    public ActorSession actorSession() {
        return actorSession;
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
