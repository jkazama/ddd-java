package sample.support;

import java.util.*;

import sample.context.*;
import sample.context.actor.ActorSession;
import sample.context.uid.IdGenerator;

/** モックテスト用のドメインヘルパー */
public class MockDomainHelper extends DomainHelper {

    private Map<String, String> settingMap = new HashMap<>();

    public MockDomainHelper() {
        setActorSession(new ActorSession());
        setTime(new Timestamper("20141118"));
        setUid(new IdGenerator());
    }
    
    public MockDomainHelper setting(String id, String value) {
        settingMap.put(id, value);
        return this;
    }

}
