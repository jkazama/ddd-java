package sample.model;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.sql.DataSource;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import sample.context.DomainEntity;
import sample.context.orm.OrmRepository;
import sample.context.orm.TxTemplate;

public class DomainTester {

    private final OrmRepository rep;
    private final PlatformTransactionManager txm;
    private final MockDomainHelper dh;

    private DomainTester(JdbcAggregateTemplate jdbcTemplate, DataSource dataSource) {
        this.txm = new JdbcTransactionManager(dataSource);
        this.dh = new MockDomainHelper();
        this.rep = OrmRepository.of(dh, dataSource, jdbcTemplate);
    }

    public <T> T tx(Function<OrmRepository, T> fn) {
        return TxTemplate.of(txm).tx(() -> {
            T ret = fn.apply(rep);
            if (ret instanceof DomainEntity) {
                ret.hashCode(); // for lazy loading
            }
            return ret;
        });
    }

    public void tx(Consumer<OrmRepository> consume) {
        tx(rep -> {
            consume.accept(rep);
            return true;
        });
    }

    public void txInitializeData(InitializeDataConsumer consume) {
        tx(rep -> {
            consume.initialize(rep);
        });
    }

    public static DomainTester create(JdbcAggregateTemplate jdbcTemplate, DataSource dataSource) {
        return new DomainTester(jdbcTemplate, dataSource);
    }

    public static interface InitializeDataConsumer {
        void initialize(OrmRepository rep);
    }
}
