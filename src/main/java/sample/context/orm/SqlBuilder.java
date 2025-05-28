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
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.util.StringUtils;

/**
 * SQL query builder for Spring Data JDBC.
 * <p>
 * This is the SQL equivalent of JpqlBuilder, providing similar functionality
 * for building dynamic SQL queries with named parameters.
 */
public final class SqlBuilder {
    private final StringBuilder sql;
    private final AtomicInteger index;
    private final List<String> conditions = new ArrayList<>();
    private final Map<String, Object> parameters = new HashMap<>();
    private Optional<String> groupBy = Optional.empty();
    private Optional<String> orderBy = Optional.empty();

    public SqlBuilder(String baseSql, int fromIndex) {
        this.sql = new StringBuilder(baseSql);
        this.index = new AtomicInteger(fromIndex);
    }

    public SqlBuilder(String baseSql, String staticCondition, int fromIndex) {
        this(baseSql, fromIndex);
        add(staticCondition);
    }

    public SqlBuilder condition(String condition, Object... values) {
        if (StringUtils.hasText(condition)) {
            int count = StringUtils.countOccurrencesOf(condition, "?");
            String[] paramNames = new String[count];
            for (int i = 0; i < count; i++) {
                String paramName = "param" + index.getAndIncrement();
                paramNames[i] = paramName;
                if (values != null && i < values.length) {
                    parameters.put(paramName, values[i]);
                }
            }
            String c = condition.replace("?", ":%s");
            conditions.add(String.format(c, (Object[]) paramNames));
        }
        return this;
    }

    private SqlBuilder add(String condition) {
        if (StringUtils.hasText(condition)) {
            this.conditions.add(condition);
        }
        return this;
    }

    /** Adds an equality condition. (Ignored when value is null) */
    public SqlBuilder equal(String field, Object value) {
        return ifValid(value, () -> {
            String paramName = "param" + index.getAndIncrement();
            conditions.add(String.format("%s = :%s", field, paramName));
            parameters.put(paramName, value);
        });
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

    /** Adds an inequality condition. (Ignored when value is null) */
    public SqlBuilder equalNot(String field, Object value) {
        return ifValid(value, () -> {
            String paramName = "param" + index.getAndIncrement();
            conditions.add(String.format("%s != :%s", field, paramName));
            parameters.put(paramName, value);
        });
    }

    /** Adds a like condition. (Ignored when value is null) */
    public SqlBuilder like(String field, String value, MatchMode mode) {
        return ifValid(value, () -> {
            String paramName = "param" + index.getAndIncrement();
            conditions.add(String.format("%s LIKE :%s", field, paramName));
            parameters.put(paramName, mode.parse(value));
        });
    }

    /**
     * Adds a like condition. [OR combination for multiple fields] (Ignored when
     * value is null)
     */
    public SqlBuilder like(List<String> fields, String value, MatchMode mode) {
        return ifValid(value, () -> {
            StringBuilder condition = new StringBuilder("(");
            for (String field : fields) {
                if (condition.length() != 1) {
                    condition.append(" OR ");
                }
                String paramName = "param" + index.getAndIncrement();
                condition.append(String.format("(%s LIKE :%s)", field, paramName));
                parameters.put(paramName, mode.parse(value));
            }
            condition.append(")");
            conditions.add(condition.toString());
        });
    }

    /** Adds an in condition. */
    public SqlBuilder in(String field, Collection<?> values) {
        return ifValid(values, () -> {
            String paramName = "param" + index.getAndIncrement();
            conditions.add(String.format("%s IN (:%s)", field, paramName));
            parameters.put(paramName, values);
        });
    }

    /** Adds a between condition. */
    public SqlBuilder between(String field, Date from, Date to) {
        if (from != null && to != null) {
            String paramNameFrom = "param" + index.getAndIncrement();
            String paramNameTo = "param" + index.getAndIncrement();
            conditions.add(String.format("%s BETWEEN :%s AND :%s", field, paramNameFrom, paramNameTo));
            parameters.put(paramNameFrom, from);
            parameters.put(paramNameTo, to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** Adds a between condition. */
    public SqlBuilder between(String field, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            String paramNameFrom = "param" + index.getAndIncrement();
            String paramNameTo = "param" + index.getAndIncrement();
            conditions.add(String.format("%s BETWEEN :%s AND :%s", field, paramNameFrom, paramNameTo));
            parameters.put(paramNameFrom, from);
            parameters.put(paramNameTo, to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** Adds a between condition. */
    public SqlBuilder between(String field, Number from, Number to) {
        if (from != null && to != null) {
            String paramNameFrom = "param" + index.getAndIncrement();
            String paramNameTo = "param" + index.getAndIncrement();
            conditions.add(String.format("%s BETWEEN :%s AND :%s", field, paramNameFrom, paramNameTo));
            parameters.put(paramNameFrom, from);
            parameters.put(paramNameTo, to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** Adds a between condition. */
    public SqlBuilder between(String field, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            String paramNameFrom = "param" + index.getAndIncrement();
            String paramNameTo = "param" + index.getAndIncrement();
            conditions.add(String.format("%s BETWEEN :%s AND :%s", field, paramNameFrom, paramNameTo));
            parameters.put(paramNameFrom, from);
            parameters.put(paramNameTo, to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** Adds a between condition. */
    public SqlBuilder between(String field, String from, String to) {
        if (isValid(from) && isValid(to)) {
            String paramNameFrom = "param" + index.getAndIncrement();
            String paramNameTo = "param" + index.getAndIncrement();
            conditions.add(String.format("%s BETWEEN :%s AND :%s", field, paramNameFrom, paramNameTo));
            parameters.put(paramNameFrom, from);
            parameters.put(paramNameTo, to);
        } else if (isValid(from)) {
            gte(field, from);
        } else if (isValid(to)) {
            lte(field, to);
        }
        return this;
    }

    /** Adds a [field] >= [value] condition. (Ignored when value is null) */
    public <Y> SqlBuilder gte(String field, final Y value) {
        return ifValid(value, () -> {
            String paramName = "param" + index.getAndIncrement();
            conditions.add(String.format("%s >= :%s", field, paramName));
            parameters.put(paramName, value);
        });
    }

    /** Adds a [field] > [value] condition. (Ignored when value is null) */
    public <Y> SqlBuilder gt(String field, final Y value) {
        return ifValid(value, () -> {
            String paramName = "param" + index.getAndIncrement();
            conditions.add(String.format("%s > :%s", field, paramName));
            parameters.put(paramName, value);
        });
    }

    /** Adds a [field] <= [value] condition. */
    public <Y> SqlBuilder lte(String field, final Y value) {
        return ifValid(value, () -> {
            String paramName = "param" + index.getAndIncrement();
            conditions.add(String.format("%s <= :%s", field, paramName));
            parameters.put(paramName, value);
        });
    }

    /** Adds a [field] < [value] condition. */
    public <Y> SqlBuilder lt(String field, final Y value) {
        return ifValid(value, () -> {
            String paramName = "param" + index.getAndIncrement();
            conditions.add(String.format("%s < :%s", field, paramName));
            parameters.put(paramName, value);
        });
    }

    /** Adds a group by clause. */
    public SqlBuilder groupBy(String groupBy) {
        this.groupBy = Optional.ofNullable(groupBy);
        return this;
    }

    /** Adds an order by clause. */
    public SqlBuilder orderBy(String orderBy) {
        this.orderBy = Optional.ofNullable(orderBy);
        return this;
    }

    /** Generates SQL. */
    public String build() {
        StringBuilder sql = new StringBuilder(this.sql.toString());
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            AtomicBoolean first = new AtomicBoolean(true);
            conditions.forEach(condition -> {
                if (!first.getAndSet(false)) {
                    sql.append(" AND ");
                }
                sql.append(condition);
            });
        }
        groupBy.ifPresent(v -> sql.append(" GROUP BY " + v));
        orderBy.ifPresent(v -> sql.append(" ORDER BY " + v));
        return sql.toString();
    }

    /** Returns the parameters associated with SQL. */
    public Map<String, Object> parameters() {
        return new HashMap<>(this.parameters);
    }

    /**
     * Creates a builder.
     *
     * @param baseSql Base SQL (does not include where / order by)
     * @return Builder information
     */
    public static SqlBuilder of(String baseSql) {
        return new SqlBuilder(baseSql, 1);
    }

    /**
     * Creates a builder.
     *
     * @param baseSql   Base SQL (does not include where / order by)
     * @param fromIndex Starting index for dynamically added condition clauses
     * @return Builder information
     */
    public static SqlBuilder of(String baseSql, int fromIndex) {
        return new SqlBuilder(baseSql, fromIndex);
    }

    /**
     * Creates a builder.
     *
     * @param baseSql         Base SQL (does not include where / order by)
     * @param staticCondition Where condition clause that is fixed without condition
     *                        specification (field is null, etc.)
     * @return Builder information
     */
    public static SqlBuilder of(String baseSql, String staticCondition) {
        return new SqlBuilder(baseSql, staticCondition, 1);
    }

    /**
     * Creates a builder.
     *
     * @param baseSql         Base SQL (does not include where / order by)
     * @param staticCondition Where condition clause that is fixed without condition
     *                        specification (field is null, etc.)
     * @param fromIndex       Starting index for dynamically added condition clauses
     * @return Builder information
     */
    public static SqlBuilder of(String baseSql, String staticCondition, int fromIndex) {
        return new SqlBuilder(baseSql, staticCondition, fromIndex);
    }

    /**
     * Match mode for LIKE operations.
     */
    public enum MatchMode {
        EXACT {
            @Override
            public String parse(String value) {
                return value;
            }
        },
        START {
            @Override
            public String parse(String value) {
                return value + "%";
            }
        },
        END {
            @Override
            public String parse(String value) {
                return "%" + value;
            }
        },
        ANYWHERE {
            @Override
            public String parse(String value) {
                return "%" + value + "%";
            }
        };

        public abstract String parse(String value);
    }
}