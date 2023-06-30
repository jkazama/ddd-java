package sample.context.orm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.util.StringUtils;

public final class JpqlBuilder {
    private final StringBuilder jpql;
    private final AtomicInteger index;
    private final List<String> conditions = new ArrayList<>();
    private final List<Object> reservedArgs = new ArrayList<>();
    private final List<Object> args = new ArrayList<>();
    private Optional<String> groupBy = Optional.empty();
    private Optional<String> orderBy = Optional.empty();

    public JpqlBuilder(String baseJpql, int fromIndex) {
        this.jpql = new StringBuilder(baseJpql);
        this.index = new AtomicInteger(fromIndex);
    }

    public JpqlBuilder(String baseJpql, String staticCondition, int fromIndex) {
        this(baseJpql, fromIndex);
        add(staticCondition);
    }

    public JpqlBuilder condition(String condition, Object... values) {
        if (StringUtils.hasText(condition)) {
            int count = StringUtils.countOccurrencesOf(condition, "?");
            Object[] indexes = new Object[count];
            for (int i = 0; i < count; i++) {
                indexes[i] = index.getAndIncrement();
            }
            String c = condition.replace("?", "?%d");
            conditions.add(String.format(c, indexes));
            if (values != null) {
                args.addAll(Arrays.asList(values));
            }
        }
        return this;
    }

    private JpqlBuilder add(String condition) {
        if (StringUtils.hasText(condition)) {
            this.conditions.add(condition);
        }
        return this;
    }

    private JpqlBuilder reservedArgs(Object... args) {
        if (args != null) {
            this.reservedArgs.addAll(Arrays.asList(args));
        }
        return this;
    }

    /** 一致条件を付与します。(値がnullの時は無視されます) */
    public JpqlBuilder equal(String field, Object value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s = ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    private JpqlBuilder ifValid(Object value, Runnable command) {
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

    /** 不一致条件を付与します。(値がnullの時は無視されます) */
    public JpqlBuilder equalNot(String field, Object value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s != ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    /** like条件を付与します。(値がnullの時は無視されます) */
    public JpqlBuilder like(String field, String value, MatchMode mode) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s LIKE ?%d", field, index.getAndIncrement()));
            args.add(mode.parse(value));
        });
    }

    /** like条件を付与します。[複数フィールドに対するOR結合](値がnullの時は無視されます) */
    public JpqlBuilder like(List<String> fields, String value, MatchMode mode) {
        return ifValid(value, () -> {
            StringBuilder condition = new StringBuilder("(");
            for (String field : fields) {
                if (condition.length() != 1) {
                    condition.append(" OR ");
                }
                condition.append(String.format("(%s LIKE ?%d)", field, index.getAndIncrement()));
                args.add(mode.parse(value));
            }
            condition.append(")");
            conditions.add(condition.toString());
        });
    }

    /** in条件を付与します。 */
    public JpqlBuilder in(String field, Collection<?> values) {
        return ifValid(values, () -> {
            conditions.add(String.format("%s IN ?%d", field, index.getAndIncrement()));
            args.add(values);
        });
    }

    /** between条件を付与します。 */
    public JpqlBuilder between(String field, Date from, Date to) {
        if (from != null && to != null) {
            conditions.add(String.format(
                    "%s BETWEEN ?%d AND ?%d", field, index.getAndIncrement(), index.getAndIncrement()));
            args.add(from);
            args.add(to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** between条件を付与します。 */
    public JpqlBuilder between(String field, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            conditions.add(String.format(
                    "%s BETWEEN ?%d AND ?%d", field, index.getAndIncrement(), index.getAndIncrement()));
            args.add(from);
            args.add(to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** between条件を付与します。 */
    public JpqlBuilder between(String field, Number from, Number to) {
        if (from != null && to != null) {
            conditions.add(String.format(
                    "%s BETWEEN ?%d AND ?%d", field, index.getAndIncrement(), index.getAndIncrement()));
            args.add(from);
            args.add(to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** between条件を付与します。 */
    public JpqlBuilder between(String field, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            conditions.add(String.format(
                    "%s BETWEEN ?%d AND ?%d", field, index.getAndIncrement(), index.getAndIncrement()));
            args.add(from);
            args.add(to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** between条件を付与します。 */
    public JpqlBuilder between(String field, String from, String to) {
        if (isValid(from) && isValid(to)) {
            conditions.add(String.format(
                    "%s BETWEEN ?%d AND ?%d", field, index.getAndIncrement(), index.getAndIncrement()));
            args.add(from);
            args.add(to);
        } else if (isValid(from)) {
            gte(field, from);
        } else if (isValid(to)) {
            lte(field, to);
        }
        return this;
    }

    /** [フィールド]&gt;=[値] 条件を付与します。(値がnullの時は無視されます) */
    public <Y> JpqlBuilder gte(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s >= ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    /** [フィールド]&gt;[値] 条件を付与します。(値がnullの時は無視されます) */
    public <Y> JpqlBuilder gt(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s > ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    /** [フィールド]&lt;=[値] 条件を付与します。 */
    public <Y> JpqlBuilder lte(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s <= ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    /** [フィールド]&lt;[値] 条件を付与します。 */
    public <Y> JpqlBuilder lt(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s < ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    /** group by 条件句を付与します。 */
    public JpqlBuilder groupBy(String groupBy) {
        this.groupBy = Optional.ofNullable(groupBy);
        return this;
    }

    /** order by 条件句を付与します。 */
    public JpqlBuilder orderBy(String orderBy) {
        this.orderBy = Optional.ofNullable(orderBy);
        return this;
    }

    /** JPQLを生成します。 */
    public String build() {
        StringBuilder jpql = new StringBuilder(this.jpql.toString());
        if (!conditions.isEmpty()) {
            jpql.append(" WHERE ");
            AtomicBoolean first = new AtomicBoolean(true);
            conditions.forEach(condition -> {
                if (!first.getAndSet(false)) {
                    jpql.append(" AND ");
                }
                jpql.append(condition);
            });
        }
        groupBy.ifPresent(v -> jpql.append(" GROUP BY " + v));
        orderBy.ifPresent(v -> jpql.append(" ORDER BY " + v));
        return jpql.toString();
    }

    /** JPQLに紐付く実行引数を返します。 */
    public Object[] args() {
        List<Object> result = new ArrayList<>();
        result.addAll(this.reservedArgs);
        result.addAll(this.args);
        return result.toArray();
    }

    /**
     * ビルダーを生成します。
     *
     * @param baseJpql 基点となるJPQL (where / order by は含めない)
     * @return ビルダー情報
     */
    public static JpqlBuilder of(String baseJpql) {
        return new JpqlBuilder(baseJpql, 1);
    }

    /**
     * ビルダーを生成します。
     *
     * @param baseJpql  基点となるJPQL (where / order by は含めない)
     * @param fromIndex 動的に付与する条件句の開始インデックス(1開始)。
     *                  既に「field=?1」等で置換連番を付与しているときはその次番号。
     * @param args      既に付与済みの置換連番に紐づく引数
     * @return ビルダー情報
     */
    public static JpqlBuilder of(String baseJpql, int fromIndex, Object... args) {
        return new JpqlBuilder(baseJpql, fromIndex).reservedArgs(args);
    }

    /**
     * ビルダーを生成します。
     *
     * @param baseJpql        基点となるJPQL (where / order by は含めない)
     * @param staticCondition 条件指定無しに確定する where 条件句 (field is null 等)
     * @return ビルダー情報
     */
    public static JpqlBuilder of(String baseJpql, String staticCondition) {
        return new JpqlBuilder(baseJpql, staticCondition, 1);
    }

    /**
     * ビルダーを生成します。
     *
     * @param baseJpql        基点となるJPQL (where / order by は含めない)
     * @param staticCondition 条件指定無しに確定する where 条件句 (field is null 等)
     * @param fromIndex       動的に付与する条件句の開始インデックス(1開始)。
     *                        既に「field=?1」等で置換連番を付与しているときはその次番号。
     * @param args            既に付与済みの置換連番に紐づく引数
     * @return ビルダー情報
     */
    public static JpqlBuilder of(String baseJpql, String staticCondition, int fromIndex, Object... args) {
        return new JpqlBuilder(baseJpql, staticCondition, fromIndex).reservedArgs(args);
    }

}
