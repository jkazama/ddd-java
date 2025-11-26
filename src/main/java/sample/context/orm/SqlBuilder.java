package sample.context.orm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

/**
 * SQL query builder for Spring Data JDBC.
 * <p>
 * This is the SQL equivalent of JpqlBuilder, providing similar functionality
 * for building dynamic SQL queries with named parameters.
 */
public class SqlBuilder {

    private final StringBuilder sql;
    private final List<String> conditions = new ArrayList<>();
    private final Map<String, Object> reservedParams = new HashMap<>();
    private final Map<String, Object> params = new HashMap<>();
    private Optional<String> groupBy = Optional.empty();
    private Optional<String> orderBy = Optional.empty();

    public SqlBuilder(String baseSql) {
        this.sql = new StringBuilder(baseSql);
    }

    /**
     * Adds a condition clause.
     */
    public SqlBuilder condition(String condition, Map<String, Object> params) {
        if (StringUtils.hasText(condition)) {
            conditions.add(condition);
            if (params != null && !params.isEmpty()) {
                this.params.putAll(params);
            }
        }
        return this;
    }

    /** Adds an equality condition. (ignored if value is null) */
    public SqlBuilder equal(String field, Object value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s = :%s", field, field));
            params.put(field, convertValue(value));
        });
    }

    private Object convertValue(Object value) {
        if (value instanceof Enum) {
            return ((Enum<?>) value).name();
        } else if (value instanceof Collection) {
            // Convert Enum elements within the Collection as well
            return ((Collection<?>) value).stream()
                    .map(item -> item instanceof Enum ? ((Enum<?>) item).name() : item)
                    .toList();
        }
        return value;
    }

    private SqlBuilder ifValid(Object value, Runnable command) {
        if (isValid(value)) {
            command.run();
        }
        return this;
    }

    private boolean isValid(Object value) {
        if (value instanceof String) {
            return StringUtils.hasText((String) value);
        } else if (value instanceof Optional) {
            return ((Optional<?>) value).isPresent();
        } else if (value instanceof Object[]) {
            return value != null && 0 < ((Object[]) value).length;
        } else if (value instanceof Collection) {
            return value != null && 0 < ((Collection<?>) value).size();
        } else {
            return value != null;
        }
    }

    /** Adds an inequality condition. (ignored if value is null) */
    public SqlBuilder equalNot(String field, Object value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s != :%s", field, field));
            params.put(field, convertValue(value));
        });
    }

    /** Adds a LIKE condition. (ignored if value is null) */
    public SqlBuilder like(String field, String value, SqlMatchMode mode) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s LIKE :%s", field, field));
            params.put(field, mode.toMatchString(value));
        });
    }

    /**
     * Adds a LIKE condition with OR join on multiple fields. (ignored if value is
     * null)
     */
    public SqlBuilder like(List<String> fields, String value, SqlMatchMode mode) {
        return ifValid(value, () -> {
            StringBuilder condition = new StringBuilder("(");
            for (String field : fields) {
                if (condition.length() != 1) {
                    condition.append(" OR ");
                }
                condition.append(String.format("(%s LIKE :%s)", field, field));
                params.put(field, mode.toMatchString(value));
            }
            condition.append(")");
            conditions.add(condition.toString());
        });
    }

    /** Adds an IN condition. */
    public SqlBuilder in(String field, Collection<?> values) {
        return ifValid(values, () -> {
            // Convert Enum elements within the Collection to Enum names
            Collection<?> convertedValues = values.stream()
                    .map(item -> item instanceof Enum ? ((Enum<?>) item).name() : item)
                    .toList();
            conditions.add(String.format("%s IN (:%s)", field, field));
            params.put(field, convertedValues);
        });
    }

    /** Adds a BETWEEN condition. */
    public SqlBuilder between(String field, Date from, Date to) {
        if (from != null && to != null) {
            conditions.add(String.format(
                    "%s BETWEEN :%s AND :%s", field, field + "_from", field + "_to"));
            params.put(field + "_from", from);
            params.put(field + "_to", to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** Adds a BETWEEN condition. */
    public SqlBuilder between(String field, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            conditions.add(String.format(
                    "%s BETWEEN :%s AND :%s", field, field + "_from", field + "_to"));
            params.put(field + "_from", from);
            params.put(field + "_to", to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** Adds a BETWEEN condition. */
    public SqlBuilder between(String field, Number from, Number to) {
        if (from != null && to != null) {
            conditions.add(String.format(
                    "%s BETWEEN :%s AND :%s", field, field + "_from", field + "_to"));
            params.put(field + "_from", from);
            params.put(field + "_to", to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** Adds a BETWEEN condition. */
    public SqlBuilder between(String field, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            conditions.add(String.format(
                    "%s BETWEEN :%s AND :%s", field, field + "_from", field + "_to"));
            params.put(field + "_from", from);
            params.put(field + "_to", to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** Adds a BETWEEN condition. */
    public SqlBuilder between(String field, String from, String to) {
        if (isValid(from) && isValid(to)) {
            conditions.add(String.format(
                    "%s BETWEEN :%s AND :%s", field, field + "_from", field + "_to"));
            params.put(field + "_from", from);
            params.put(field + "_to", to);
        } else if (isValid(from)) {
            gte(field, from);
        } else if (isValid(to)) {
            lte(field, to);
        }
        return this;
    }

    /** Adds a [field] >= [value] condition. (ignored if value is null) */
    public <Y> SqlBuilder gte(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s >= :%s", field, field));
            params.put(field, convertValue(value));
        });
    }

    /** Adds a [field] > [value] condition. (ignored if value is null) */
    public <Y> SqlBuilder gt(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s > :%s", field, field));
            params.put(field, convertValue(value));
        });
    }

    /** Adds a [field] <= [value] condition. */
    public <Y> SqlBuilder lte(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s <= :%s", field, field));
            params.put(field, convertValue(value));
        });
    }

    /** Adds a [field] < [value] condition. */
    public <Y> SqlBuilder lt(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s < :%s", field, field));
            params.put(field, convertValue(value));
        });
    }

    /** Adds a GROUP BY clause. */
    public SqlBuilder groupBy(String groupBy) {
        this.groupBy = Optional.ofNullable(groupBy);
        return this;
    }

    /** Adds an ORDER BY clause. */
    public SqlBuilder orderBy(String orderBy) {
        this.orderBy = Optional.ofNullable(orderBy);
        return this;
    }

    /** Generates SQL. */
    public String build() {
        StringBuilder builder = new StringBuilder(this.sql.toString());
        if (!conditions.isEmpty()) {
            builder.append(" WHERE ");
            AtomicBoolean first = new AtomicBoolean(true);
            conditions.forEach(condition -> {
                if (!first.getAndSet(false)) {
                    builder.append(" AND ");
                }
                builder.append(condition);
            });
        }
        groupBy.ifPresent(v -> builder.append(" GROUP BY " + v));
        orderBy.ifPresent(v -> builder.append(" ORDER BY " + v));
        return builder.toString();
    }

    /** Generates SQL with pagination. */
    public String build(PageRequest pageRequest) {
        StringBuilder builder = new StringBuilder(this.sql.toString());
        if (!conditions.isEmpty()) {
            builder.append(" WHERE ");
            AtomicBoolean first = new AtomicBoolean(true);
            conditions.forEach(condition -> {
                if (!first.getAndSet(false)) {
                    builder.append(" AND ");
                }
                builder.append(condition);
            });
        }
        groupBy.ifPresent(v -> builder.append(" GROUP BY " + v));
        orderBy.ifPresent(v -> builder.append(" ORDER BY " + v));
        if (pageRequest != null) {
            builder.append(" LIMIT " + pageRequest.getPageSize() + " OFFSET "
                    + (long) pageRequest.getPageSize() * pageRequest.getPageNumber());
        }
        return builder.toString();
    }

    /** Generates SQL for count. */
    public String buildCount() {
        StringBuilder builder = new StringBuilder("SELECT COUNT(*) FROM (");
        builder.append(this.sql.toString());
        if (!conditions.isEmpty()) {
            builder.append(" WHERE ");
            AtomicBoolean first = new AtomicBoolean(true);
            conditions.forEach(condition -> {
                if (!first.getAndSet(false)) {
                    builder.append(" AND ");
                }
                builder.append(condition);
            });
        }
        builder.append(") AS count");
        return builder.toString();
    }

    /** Returns parameter values bound to the SQL. */
    public Map<String, Object> params() {
        Map<String, Object> result = new HashMap<>();
        result.putAll(this.reservedParams);
        result.putAll(this.params);
        return result;
    }

    /**
     * Creates a builder.
     *
     * @param baseSql Base SQL (should not include where / order by)
     * @return Builder information
     */
    public static SqlBuilder of(String baseSql) {
        return new SqlBuilder(baseSql);
    }

    /**
     * Creates a builder.
     *
     * @param baseSql         Base SQL (should not include where / order by)
     * @param staticCondition WHERE clause determined without condition
     *                        specification (e.g., field is null)
     * @return Builder information
     */
    public static SqlBuilder of(String baseSql, String staticCondition) {
        return of(baseSql).condition(staticCondition, Map.of());
    }

    /**
     * Creates a builder.
     *
     * @param baseSql         Base SQL (should not include where / order by)
     * @param staticCondition WHERE clause determined without condition
     *                        specification (e.g., field is null)
     * @param params          Parameter information bound to the base SQL
     * @return Builder information
     */
    public static SqlBuilder of(String baseSql, String staticCondition, Map<String, Object> params) {
        return of(baseSql).condition(staticCondition, params);
    }

}
