package sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder.Builder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;
import sample.context.DomainEntity;
import sample.context.SimpleObjectProvider;
import sample.context.Timestamper;
import sample.context.orm.JpaRepository.DefaultRepository;
import sample.context.uid.IdGenerator;
import sample.model.DataFixtures;
import sample.support.MockDomainHelper;

/**
 * Spring コンテナを用いない JPA のみに特化した検証用途。
 * <p>
 * model パッケージでのみ利用してください。
 */
public class EntityTestSupport {
    protected Timestamper time;
    protected IdGenerator uid;
    protected MockDomainHelper dh;
    protected EntityManagerFactory emf;
    protected DefaultRepository rep;
    protected PlatformTransactionManager txm;
    protected DataFixtures fixtures;

    /** テスト対象とするEntityクラス一覧 */
    private List<Class<?>> targetEntities = new ArrayList<>();

    @BeforeEach
    public final void setup() {
        setupPreset();
        dh = new MockDomainHelper();
        time = dh.time();
        uid = dh.uid();
        setupRepository();
        setupDataFixtures();
        before();
    }

    /** 設定事前処理。repインスタンス生成前 */
    protected void setupPreset() {
        // 各Entity検証で上書きしてください
    }

    /** 事前処理。repインスタンス生成後 */
    protected void before() {
        // 各Entity検証で上書きしてください
    }

    /**
     * {@link #setupPreset()}内で対象Entityを指定してください。
     */
    protected void targetEntities(Class<?>... list) {
        if (list != null) {
            this.targetEntities = Arrays.asList(list);
        }
    }

    /**
     * {@link #before()}内でモック設定値を指定してください。
     */
    protected void setting(String id, String value) {
        dh.setting(id, value);
    }

    @AfterEach
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

    /** トランザクション処理を行います。 */
    protected <T> T tx(Supplier<T> callable) {
        return new TransactionTemplate(txm).execute((status) -> {
            T ret = callable.get();
            if (ret instanceof DomainEntity) {
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

    // 簡易コンポーネントFactory
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
