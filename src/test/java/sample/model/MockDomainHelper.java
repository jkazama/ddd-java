package sample.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import sample.context.DomainHelper;
import sample.context.Timestamper;
import sample.context.uid.IdGenerator;

public class MockDomainHelper implements DomainHelper {

    private Map<String, String> settingMap = new HashMap<>();

    public Timestamper time() {
        return new Timestamper(LocalDate.of(2014, 11, 18));
    }

    public IdGenerator uid() {
        return new IdGenerator();
    }

    public MockDomainHelper setting(String id, String value) {
        settingMap.put(id, value);
        return this;
    }

}
