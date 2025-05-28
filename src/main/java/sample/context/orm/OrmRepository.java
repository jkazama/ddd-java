package sample.context.orm;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.sql.DataSource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import sample.context.DomainEntity;
import sample.context.DomainHelper;
import sample.context.Repository;
import sample.context.ValidationException;
import sample.model.DomainErrorKeys;

/**
 * Repository base implementation of JDBC.
 * <p>
 * This component provides simple JDBC implementation in form not to use a base
 * of Spring Data
 * to realize 1-n relations of Repository and Entity.
 */
@Component
@RequiredArgsConstructor(staticName = "of")
public class OrmRepository implements Repository {
    private final DomainHelper dh;
    private final DataSource dataSource;
    private final JdbcAggregateTemplate jdbcTemplate;

    @Override
    public DomainHelper dh() {
        return dh;
    }

    public OrmTemplate tmpl() {
        return OrmTemplate.of(this.jdbcTemplate, this.tmplJdbc());
    }

    public NamedParameterJdbcTemplate tmplJdbc() {
        return new NamedParameterJdbcTemplate(this.dataSource);
    }

    @Override
    public <T extends DomainEntity> T get(Class<T> clazz, Object id) {
        return jdbcTemplate.findById(id, clazz);
    }

    @Override
    public <T extends DomainEntity> T load(Class<T> clazz, Object id) {
        T entity = get(clazz, id);
        if (entity == null) {
            throw ValidationException.of(DomainErrorKeys.ENTITY_NOT_FOUND, id.toString());
        }
        return entity;
    }

    @Override
    public <T extends DomainEntity> boolean exists(Class<T> clazz, Object id) {
        return jdbcTemplate.existsById(id, clazz);
    }

    @Override
    public <T extends DomainEntity> List<T> findAll(Class<T> clazz) {
        Iterable<T> entities = jdbcTemplate.findAll(clazz);
        return StreamSupport.stream(entities.spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public <T extends DomainEntity> T save(T entity) {
        return jdbcTemplate.insert(entity);
    }

    @Override
    public <T extends DomainEntity> T saveOrUpdate(T entity) {
        if (this.exists(entity.getClass(), entity.id())) {
            return this.update(entity);
        } else {
            return this.save(entity);
        }
    }

    @Override
    public <T extends DomainEntity> T update(T entity) {
        return jdbcTemplate.save(entity);
    }

    @Override
    public <T extends DomainEntity> T delete(T entity) {
        jdbcTemplate.delete(entity);
        return entity;
    }

    // Additional convenience methods for Spring Data JDBC style operations

    /**
     * Save multiple entities.
     */
    public <T extends DomainEntity> Iterable<T> saveAll(Iterable<T> entities) {
        return jdbcTemplate.saveAll(entities);
    }

    /**
     * Find entity by ID with Optional.
     */
    public <T extends DomainEntity> Optional<T> findById(Class<T> clazz, Object id) {
        return Optional.ofNullable(jdbcTemplate.findById(id, clazz));
    }

    /**
     * Find all entities with sorting.
     */
    public <T extends DomainEntity> Iterable<T> findAll(Class<T> clazz, Sort sort) {
        return jdbcTemplate.findAll(clazz, sort);
    }

    /**
     * Find all entities with pagination.
     */
    public <T extends DomainEntity> Page<T> findAll(Class<T> clazz, Pageable pageable) {
        Iterable<T> entities = jdbcTemplate.findAll(clazz, pageable.getSort());
        List<T> content = StreamSupport.stream(entities.spliterator(), false)
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());

        long total = jdbcTemplate.count(clazz);
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Count all entities.
     */
    public <T extends DomainEntity> long count(Class<T> clazz) {
        return jdbcTemplate.count(clazz);
    }

    /**
     * Delete entity by ID.
     */
    public <T extends DomainEntity> void deleteById(Class<T> clazz, Object id) {
        jdbcTemplate.deleteById(id, clazz);
    }

    /**
     * Delete multiple entities.
     */
    public <T extends DomainEntity> void deleteAll(Iterable<? extends T> entities) {
        jdbcTemplate.deleteAll(entities);
    }

    /**
     * Delete all entities of specified type.
     */
    public <T extends DomainEntity> void deleteAll(Class<T> clazz) {
        jdbcTemplate.deleteAll(clazz);
    }

}
