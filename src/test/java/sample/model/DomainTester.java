package sample.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder.Builder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;
import sample.context.DomainEntity;
import sample.context.DomainHelper;
import sample.context.Timestamper;
import sample.context.orm.OrmRepository;
import sample.context.orm.OrmRepository.DefaultRepository;
import sample.context.orm.TxTemplate;

public class DomainTester {

    private final EntityManagerFactory emf;
    private final DefaultRepository rep;
    private final PlatformTransactionManager txm;
    private final MockDomainHelper dh = new MockDomainHelper();

    public DomainTester(Collection<Class<?>> targetEntities) {
        this.emf = setupEntityManagerFactory(targetEntities);
        this.txm = new JpaTransactionManager(this.emf);
        this.rep = setupRepository(this.emf);
    }

    public void close() {
        emf.close();
    }

    private EntityManagerFactory setupEntityManagerFactory(
            Collection<Class<?>> targetEntities) {
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
        return emfBean.getObject();
    }

    private DefaultRepository setupRepository(EntityManagerFactory emf) {
        var rep = new DefaultRepository();
        rep.setEm(SharedEntityManagerCreator.createSharedEntityManager(emf));
        rep.setDh(this.dh);
        return rep;
    }

    public DomainHelper dh() {
        return this.dh;
    }

    public Timestamper time() {
        return this.dh.time();
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
            rep.flush();
            return true;
        });
    }

    public void txInitializeData(InitializeDataConsumer consume) {
        tx(rep -> {
            consume.initialize(rep);
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

    public static class DomainTesterBuilder {
        private final Collection<Class<?>> targetEntities = new ArrayList<>();

        public DomainTester build() {
            return new DomainTester(this.targetEntities);
        }

        public static DomainTesterBuilder from(Class<?>... targetEntities) {
            Assert.notNull(targetEntities, "Set Testing Target classes.");
            DomainTesterBuilder builder = new DomainTesterBuilder();
            for (Class<?> entity : targetEntities) {
                builder.targetEntities.add(entity);
            }
            return builder;
        }
    }

    public static interface InitializeDataConsumer {
        void initialize(OrmRepository rep);
    }

}
