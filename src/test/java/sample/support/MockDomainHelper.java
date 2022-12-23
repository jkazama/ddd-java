package sample.support;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import sample.context.DomainHelper;
import sample.context.Timestamper;
import sample.context.uid.IdGenerator;

/** モックテスト用のドメインヘルパー */
public class MockDomainHelper extends DomainHelper {

    private Map<String, String> settingMap = new HashMap<>();

    public MockDomainHelper() {
        super(new Timestamper(
                LocalDate.of(2014, 11, 18)),
                new IdGenerator());
    }

    public MockDomainHelper setting(String id, String value) {
        settingMap.put(id, value);
        return this;
    }

}
