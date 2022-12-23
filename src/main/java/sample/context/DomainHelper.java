package sample.context;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import sample.context.actor.Actor;
import sample.context.actor.ActorSession;
import sample.context.uid.IdGenerator;

/**
 * ドメイン処理を行う上で必要となるインフラ層コンポーネントへのアクセサを提供します。
 * 
 * @author jkazama
 */
@Component
@RequiredArgsConstructor
public class DomainHelper {
    private final Timestamper time;
    private final IdGenerator uid;

    /**
     * @return ログイン中のユースケース利用者
     */
    public Actor actor() {
        return ActorSession.actor();
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
