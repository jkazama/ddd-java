package sample.context.orm;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jdbc.core.convert.EntityRowMapper;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.web.PagedModel;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Wrapper utility component that encapsulates JdbcClient.
 * <p>
 * For typical use cases, use find which does not depend on Spring Data.
 * For table-mapped entities, use findEntity.
 */
@Component
@RequiredArgsConstructor(staticName = "of")
public class JdbcFinder {
    private final JdbcClient jdbcClient;
    private final JdbcConverter jdbcConverter;
    private final RelationalMappingContext mappingContext;

    /** Gets the JdbcClient. */
    public JdbcClient client() {
        return jdbcClient;
    }

    /** Performs a simple search. */
    public <T> List<T> find(Class<T> type, SqlBuilder sqlBuilder) {
        return jdbcClient.sql(sqlBuilder.build())
                .params(sqlBuilder.params())
                .query(type)
                .list();
    }

    /** Performs a simple search. */
    public <T> List<T> find(Class<T> type, RowMapper<T> rowMapper, SqlBuilder sqlBuilder) {
        return jdbcClient.sql(sqlBuilder.build())
                .params(sqlBuilder.params())
                .query(rowMapper)
                .list();
    }

    /** Performs a simple search. (Spring Data compatible) */
    public <T> List<T> findEntity(Class<T> type, SqlBuilder sqlBuilder) {
        return jdbcClient.sql(sqlBuilder.build())
                .params(sqlBuilder.params())
                .query(createEntityRowMapper(type))
                .list();
    }

    @SuppressWarnings("unchecked")
    private <T> RowMapper<T> createEntityRowMapper(Class<T> type) {
        if (mappingContext == null || jdbcConverter == null) {
            throw new IllegalStateException(
                    "JdbcFinder is not properly initialized for EntityRowMapper. (not found mappingContext or jdbcConverter)");
        }
        RelationalPersistentEntity<T> entity = (RelationalPersistentEntity<T>) mappingContext
                .getRequiredPersistentEntity(type);
        return new EntityRowMapper<>(entity, jdbcConverter);
    }

    /** Performs a paged search. */
    public <T> PagedModel<T> find(Class<T> type, SqlBuilder sqlBuilder, Integer page, Integer size) {
        var pageRequest = PageRequest.of(page != null ? page : 0, size != null ? size : 100);
        String countSql = sqlBuilder.buildCount();
        String sql = sqlBuilder.build(pageRequest);
        Map<String, Object> params = sqlBuilder.params();
        Long total = jdbcClient.sql(countSql)
                .params(params)
                .query(Long.class)
                .single();
        List<T> list = jdbcClient.sql(sql)
                .params(params)
                .query(type)
                .list();
        return new PagedModel<>(new PageImpl<>(list, pageRequest, total));
    }

    /** Performs a paged search. */
    public <T> PagedModel<T> find(
            Class<T> type,
            RowMapper<T> rowMapper,
            SqlBuilder sqlBuilder,
            Integer page,
            Integer size) {
        var pageRequest = PageRequest.of(page != null ? page : 0, size != null ? size : 100);
        String countSql = sqlBuilder.buildCount();
        String sql = sqlBuilder.build(pageRequest);
        Map<String, Object> params = sqlBuilder.params();
        Long total = jdbcClient.sql(countSql)
                .params(params)
                .query(Long.class)
                .single();
        List<T> list = jdbcClient.sql(sql)
                .params(params)
                .query(rowMapper)
                .list();
        return new PagedModel<>(new PageImpl<>(list, pageRequest, total));
    }

    /** Performs a paged search. (Spring Data compatible) */
    public <T> PagedModel<T> findEntity(Class<T> type, SqlBuilder sqlBuilder, Integer page, Integer size) {
        var pageRequest = PageRequest.of(page != null ? page : 0, size != null ? size : 100);
        String countSql = sqlBuilder.buildCount();
        String sql = sqlBuilder.build(pageRequest);
        Map<String, Object> params = sqlBuilder.params();
        Long total = jdbcClient.sql(countSql)
                .params(params)
                .query(Long.class)
                .single();
        List<T> list = jdbcClient.sql(sql)
                .params(params)
                .query(createEntityRowMapper(type))
                .list();
        return new PagedModel<>(new PageImpl<>(list, pageRequest, total));
    }

    public static JdbcFinder of(JdbcClient jdbcClient) {
        return new JdbcFinder(jdbcClient, null, null);
    }

}
