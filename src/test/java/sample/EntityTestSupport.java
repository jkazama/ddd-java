package sample;

import java.util.*;
import java.util.function.Supplier;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.junit.*;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder.Builder;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.zaxxer.hikari.*;

import sample.context.*;
import sample.context.actor.ActorSession;
import sample.context.orm.JpaRepository.DefaultRepository;
import sample.context.uid.IdGenerator;
import sample.model.DataFixtures;
import sample.support.MockDomainHelper;

/**
 * this component is specialized only in JPA which did not use Spring container.
 * <p>Use it only with model package.
 */
public class EntityTestSupport {
    protected Timestamper time;
    protected ActorSession session;
    protected IdGenerator uid;
    protected MockDomainHelper dh;
    protected EntityManagerFactory emf;
    protected DefaultRepository rep;
    protected PlatformTransactionManager txm;
    protected DataFixtures fixtures;

    /** List of Entity classes to be targeted for a test */
    private List<Class<?>> targetEntities = new ArrayList<>();

    @Before
    public final void setup() {
        setupPreset();
        dh = new MockDomainHelper();
        time = dh.time();
        session = dh.actorSession();
        uid = dh.uid();
        setupRepository();
        setupDataFixtures();
        before();
    }

    /** It is before rep instance create */
    protected void setupPreset() {
        // Override entity test class.
    }

    /** After rep instance created */
    protected void before() {
        // Override entity test class.
    }

    /**
     * Set target Entity in {@link #setupPreset()}.
     * (it is necessary to set targetPackage or this)
     */
    protected void targetEntities(Class<?>... list) {
        if (list != null) {
            this.targetEntities = Arrays.asList(list);
        }
    }

    /**
     * Set a mock setting value in {@link #before()}.
     */
    protected void setting(String id, String value) {
        dh.setting(id, value);
    }

    @After
    public void cleanup() {
        emf.close();
    }

    protected void setupRepository() {
        setupEntityManagerFactory();
        rep = new DefaultRepository();
        rep.setDh(SimpleObjectProvider.of(dh));
        rep.setEm(SharedEntityManagerCreator.createSharedEntityManager(emf));
    }

    protected void setupDataFixtures() {
        fixtures = new DataFixtures(rep, txm, time, uid);
    }

    protected void setupEntityManagerFactory() {
        DataSource ds = EntityTestFactory.dataSource();
        Map<String, String> props = new HashMap<>();
        props.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
        Builder builder = new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), props, null)
                .dataSource(ds)
                .jta(false);
        if (!targetEntities.isEmpty()) {
            builder.packages(targetEntities.toArray(new Class<?>[0]));
        }
        LocalContainerEntityManagerFactoryBean emfBean = builder.build();
        emfBean.afterPropertiesSet();
        emf = emfBean.getObject();
        txm = new JpaTransactionManager(emf);
    }

    protected <T> T tx(Supplier<T> callable) {
        return new TransactionTemplate(txm).execute((status) -> {
            T ret = callable.get();
            if (ret instanceof Entity) {
                ret.hashCode(); // for lazy loading
            }
            return ret;
        });
    }

    protected void tx(Runnable command) {
        tx(() -> {
            command.run();
            rep.flush();
            return true;
        });
    }

    public static class EntityTestFactory {
        private static Optional<DataSource> ds = Optional.empty();

        static synchronized DataSource dataSource() {
            return ds.orElseGet(() -> {
                ds = Optional.of(createDataSource());
                return ds.get();
            });
        }

        private static DataSource createDataSource() {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            config.setUsername("");
            config.setPassword("");
            return new HikariDataSource(config);
        }
    }

}
