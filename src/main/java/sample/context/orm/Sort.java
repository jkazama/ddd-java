package sample.context.orm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Data;
import lombok.Value;
import sample.context.Dto;

/**
 * ソート情報を表現します。
 * 複数件のソート情報(SortOrder)を内包します。
 */
@Data
public class Sort implements Dto {
    /** ソート条件 */
    private final List<SortOrder> orders = new ArrayList<SortOrder>();

    /** ソート条件を追加します。 */
    public Sort add(SortOrder order) {
        orders.add(order);
        return this;
    }

    /** ソート条件(昇順)を追加します。 */
    public Sort asc(String property) {
        return add(SortOrder.asc(property));
    }

    /** ソート条件(降順)を追加します。 */
    public Sort desc(String property) {
        return add(SortOrder.desc(property));
    }

    /** ソート条件一覧を返します。 */
    public List<SortOrder> orders() {
        return orders;
    }

    /** ソート条件が未指定だった際にソート順が上書きされます。 */
    public Sort ifEmpty(SortOrder... items) {
        if (orders.isEmpty() && items != null) {
            orders.addAll(Arrays.asList(items));
        }
        return this;
    }

    /** 昇順でソート情報を返します。 */
    public static Sort ascBy(String property) {
        return new Sort().asc(property);
    }

    /** 降順でソート情報を返します。 */
    public static Sort descBy(String property) {
        return new Sort().desc(property);
    }

    /** フィールド単位のソート情報を表現します。 */
    @Value
    public static class SortOrder {
        private String property;
        private boolean ascending;

        public static SortOrder asc(String property) {
            return new SortOrder(property, true);
        }

        public static SortOrder desc(String property) {
            return new SortOrder(property, false);
        }
    }

}