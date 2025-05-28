package sample.context.orm;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import lombok.RequiredArgsConstructor;
import sample.context.ValidationException;
import sample.model.DomainErrorKeys;

/**
 * Simple Accessor for JdbcAggregateTemplate operations.
 * (you are formed every session, and please use it)
 * <p>
 * This provides JDBC-based operations using Spring Data JDBC's Criteria API
 * for dynamic query construction and JdbcAggregateTemplate for execution.
 */
@RequiredArgsConstructor(staticName = "of")
public class OrmTemplate {
    private final JdbcAggregateTemplate template;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * Get a single entity using Criteria-based query.
     * 
     * @param clazz           Entity class
     * @param criteriaBuilder Function to build criteria dynamically
     * @return Optional containing the entity or empty if not found
     */
    public <T> Optional<T> get(Class<T> clazz, Function<Criteria, Criteria> criteriaBuilder) {
        Criteria criteria = criteriaBuilder.apply(Criteria.empty());
        Query query = Query.query(criteria);

        return template.findOne(query, clazz);
    }

    /**
     * Get a single entity by property equality.
     * 
     * @param clazz        Entity class
     * @param propertyName Property name
     * @param value        Property value
     * @return Optional containing the entity or empty if not found
     */
    public <T> Optional<T> get(Class<T> clazz, String propertyName, Object value) {
        if (value == null) {
            return Optional.empty();
        }

        Criteria criteria = Criteria.where(propertyName).is(value);
        Query query = Query.query(criteria);

        return template.findOne(query, clazz);
    }

    /**
     * Get a single entity by multiple conditions.
     * 
     * @param clazz      Entity class
     * @param conditions Map of property names and values
     * @return Optional containing the entity or empty if not found
     */
    public <T> Optional<T> get(Class<T> clazz, Map<String, Object> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return Optional.empty();
        }

        Criteria criteria = buildCriteriaFromConditions(conditions);
        Query query = Query.query(criteria);

        return template.findOne(query, clazz);
    }

    /**
     * Load a single entity using Criteria-based query.
     * Throws ValidationException if not found.
     * 
     * @param clazz           Entity class
     * @param criteriaBuilder Function to build criteria dynamically
     * @return Entity instance
     * @throws ValidationException if entity not found
     */
    public <T> T load(Class<T> clazz, Function<Criteria, Criteria> criteriaBuilder) {
        Optional<T> v = get(clazz, criteriaBuilder);
        return v.orElseThrow(() -> ValidationException.of(DomainErrorKeys.ENTITY_NOT_FOUND));
    }

    /**
     * Load a single entity by property equality.
     * Throws ValidationException if not found.
     * 
     * @param clazz        Entity class
     * @param propertyName Property name
     * @param value        Property value
     * @return Entity instance
     * @throws ValidationException if entity not found
     */
    public <T> T load(Class<T> clazz, String propertyName, Object value) {
        Optional<T> v = get(clazz, propertyName, value);
        return v.orElseThrow(() -> ValidationException.of(DomainErrorKeys.ENTITY_NOT_FOUND, value.toString()));
    }

    /**
     * Load a single entity by multiple conditions.
     * Throws ValidationException if not found.
     * 
     * @param clazz      Entity class
     * @param conditions Map of property names and values
     * @return Entity instance
     * @throws ValidationException if entity not found
     */
    public <T> T load(Class<T> clazz, Map<String, Object> conditions) {
        Optional<T> v = get(clazz, conditions);
        return v.orElseThrow(() -> ValidationException.of(DomainErrorKeys.ENTITY_NOT_FOUND));
    }

    /**
     * Find entities using JdbcAggregateTemplate with Criteria-based Query.
     * This method demonstrates how to use Criteria API for dynamic queries.
     * 
     * @param clazz           Entity class
     * @param criteriaBuilder Function to build criteria dynamically
     * @return List of entities
     */
    public <T> List<T> find(Class<T> clazz, Function<Criteria, Criteria> criteriaBuilder) {
        Criteria criteria = criteriaBuilder.apply(Criteria.empty());
        Query query = Query.query(criteria);

        return template.findAll(query, clazz);
    }

    /**
     * Find entities by a simple property equality condition.
     * 
     * @param clazz        Entity class
     * @param propertyName Property name
     * @param value        Property value
     * @return List of entities
     */
    public <T> List<T> find(Class<T> clazz, String propertyName, Object value) {
        if (value == null) {
            return template.findAll(clazz);
        }

        Criteria criteria = Criteria.where(propertyName).is(value);
        Query query = Query.query(criteria);

        return template.findAll(query, clazz);
    }

    /**
     * Find entities with multiple conditions.
     * 
     * @param clazz      Entity class
     * @param conditions Map of property names and values
     * @return List of entities
     */
    public <T> List<T> find(Class<T> clazz, Map<String, Object> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return template.findAll(clazz);
        }

        Criteria criteria = buildCriteriaFromConditions(conditions);
        Query query = Query.query(criteria);

        return template.findAll(query, clazz);
    }

    /**
     * Find with pagination using Criteria.
     * 
     * @param clazz           Entity class
     * @param criteriaBuilder Function to build criteria
     * @param pageable        Pagination information
     * @return Page of entities
     */
    public <T> Page<T> find(Class<T> clazz,
            Function<Criteria, Criteria> criteriaBuilder,
            Pageable pageable) {
        Criteria criteria = criteriaBuilder.apply(Criteria.empty());
        Query query = Query.query(criteria).with(pageable);

        List<T> content = template.findAll(query, clazz);
        long total = template.count(Query.query(criteria), clazz);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Find with pagination using property equality.
     * 
     * @param clazz        Entity class
     * @param propertyName Property name
     * @param value        Property value
     * @param pageable     Pagination information
     * @return Page of entities
     */
    public <T> Page<T> find(Class<T> clazz, String propertyName, Object value, Pageable pageable) {
        if (value == null) {
            return template.findAll(clazz, pageable);
        }

        Criteria criteria = Criteria.where(propertyName).is(value);
        Query query = Query.query(criteria).with(pageable);

        List<T> content = template.findAll(query, clazz);
        long total = template.count(Query.query(criteria), clazz);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Find with pagination using multiple conditions.
     * 
     * @param clazz      Entity class
     * @param conditions Map of property names and values
     * @param pageable   Pagination information
     * @return Page of entities
     */
    public <T> Page<T> find(Class<T> clazz, Map<String, Object> conditions, Pageable pageable) {
        if (conditions == null || conditions.isEmpty()) {
            return template.findAll(clazz, pageable);
        }

        Criteria criteria = buildCriteriaFromConditions(conditions);
        Query query = Query.query(criteria).with(pageable);

        List<T> content = template.findAll(query, clazz);
        long total = template.count(Query.query(criteria), clazz);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Find all entities of a specific type.
     * 
     * @param clazz Entity class
     * @return List of all entities
     */
    public <T> List<T> findAll(Class<T> clazz) {
        return template.findAll(clazz);
    }

    /**
     * Find all entities with pagination.
     * 
     * @param clazz    Entity class
     * @param pageable Pagination information
     * @return Page of entities
     */
    public <T> Page<T> findAll(Class<T> clazz, Pageable pageable) {
        return template.findAll(clazz, pageable);
    }

    /**
     * Count entities by criteria.
     * 
     * @param clazz           Entity class
     * @param criteriaBuilder Function to build criteria
     * @return Count of entities
     */
    public <T> long count(Class<T> clazz, Function<Criteria, Criteria> criteriaBuilder) {
        Criteria criteria = criteriaBuilder.apply(Criteria.empty());
        Query query = Query.query(criteria);

        return template.count(query, clazz);
    }

    /**
     * Count entities by property equality.
     * 
     * @param clazz        Entity class
     * @param propertyName Property name
     * @param value        Property value
     * @return Count of entities
     */
    public <T> long count(Class<T> clazz, String propertyName, Object value) {
        if (value == null) {
            return template.count(clazz);
        }

        Criteria criteria = Criteria.where(propertyName).is(value);
        Query query = Query.query(criteria);

        return template.count(query, clazz);
    }

    /**
     * Count entities by multiple conditions.
     * 
     * @param clazz      Entity class
     * @param conditions Map of property names and values
     * @return Count of entities
     */
    public <T> long count(Class<T> clazz, Map<String, Object> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return template.count(clazz);
        }

        Criteria criteria = buildCriteriaFromConditions(conditions);
        Query query = Query.query(criteria);

        return template.count(query, clazz);
    }

    /**
     * Check if entities exist by criteria.
     * 
     * @param clazz           Entity class
     * @param criteriaBuilder Function to build criteria
     * @return true if entities exist
     */
    public <T> boolean exists(Class<T> clazz, Function<Criteria, Criteria> criteriaBuilder) {
        Criteria criteria = criteriaBuilder.apply(Criteria.empty());
        Query query = Query.query(criteria);

        return template.exists(query, clazz);
    }

    /**
     * Check if entities exist by property equality.
     * 
     * @param clazz        Entity class
     * @param propertyName Property name
     * @param value        Property value
     * @return true if entities exist
     */
    public <T> boolean exists(Class<T> clazz, String propertyName, Object value) {
        if (value == null) {
            return false;
        }

        Criteria criteria = Criteria.where(propertyName).is(value);
        Query query = Query.query(criteria);

        return template.exists(query, clazz);
    }

    /**
     * Check if entities exist by multiple conditions.
     * 
     * @param clazz      Entity class
     * @param conditions Map of property names and values
     * @return true if entities exist
     */
    public <T> boolean exists(Class<T> clazz, Map<String, Object> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return false;
        }

        Criteria criteria = buildCriteriaFromConditions(conditions);
        Query query = Query.query(criteria);

        return template.exists(query, clazz);
    }

    /**
     * Execute update/insert/delete operations using NamedParameterJdbcTemplate.
     * 
     * @param sql  SQL statement
     * @param args Query arguments
     * @return Number of affected rows
     */
    public int execute(String sql, final Object... args) {
        Map<String, Object> paramMap = createParameterMap(args);
        return namedParameterJdbcTemplate.update(sql, paramMap);
    }

    /**
     * Build Criteria from conditions map.
     * 
     * @param conditions Map of property names and values
     * @return Criteria object
     */
    private Criteria buildCriteriaFromConditions(Map<String, Object> conditions) {
        Criteria criteria = Criteria.empty();
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            if (entry.getValue() != null) {
                criteria = criteria.and(Criteria.where(entry.getKey()).is(entry.getValue()));
            }
        }
        return criteria;
    }

    /**
     * Create parameter map from positional arguments.
     * Supports both positional parameters (converted to named) and Map parameters.
     * 
     * @param args Query arguments
     * @return Parameter map
     */
    private Map<String, Object> createParameterMap(Object... args) {
        Map<String, Object> paramMap = new java.util.HashMap<>();

        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> argNamed = (Map<String, Object>) arg;
                    paramMap.putAll(argNamed);
                } else {
                    // Convert positional parameters to named parameters (param1, param2, etc.)
                    paramMap.put("param" + (i + 1), arg);
                }
            }
        }

        return paramMap;
    }

    /**
     * Find entities using JdbcAggregateTemplate with Criteria-based Query and Sort.
     * 
     * @param clazz           Entity class
     * @param criteriaBuilder Function to build criteria dynamically
     * @param sort            Sort specification
     * @return List of entities
     */
    public <T> List<T> find(Class<T> clazz, Function<Criteria, Criteria> criteriaBuilder, Sort sort) {
        Criteria criteria = criteriaBuilder.apply(Criteria.empty());
        Query query = Query.query(criteria).sort(sort);

        return template.findAll(query, clazz);
    }
}
