package sample.support;

import java.util.*;

import sample.context.*;
import sample.context.actor.ActorSession;
import sample.context.uid.IdGenerator;

/** モックテスト用のドメインヘルパー */
public class MockDomainHelper extends DomainHelper {

    private Map<String, String> settingMap = new HashMap<>();

    public MockDomainHelper() {
        super(new ActorSession(), new Timestamper("20141118"), new IdGenerator());
    }
    
    public MockDomainHelper setting(String id, String value) {
        settingMap.put(id, value);
        return this;
    }

}
