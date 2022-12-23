package sample.context.orm;

import java.math.RoundingMode;

import lombok.AllArgsConstructor;
import lombok.Data;
import sample.context.Dto;
import sample.context.orm.Sort.SortOrder;
import sample.util.Calculator;

/**
 * ページング情報を表現します。
 */
@Data
@AllArgsConstructor
public class Pagination implements Dto {
  public static final int DefaultSize = 100;
  /** ページ数(1開始) */
  private int page;
  /** ページあたりの件数 */
  private int size;
  /** トータル件数 */
  private Long total;
  /** トータル件数算出を無視するか */
  private boolean ignoreTotal;
  /** ソート条件 */
  private Sort sort;

  public Pagination() {
    this(1);
  }

  public Pagination(int page) {
    this(page, DefaultSize, null, false, new Sort());
  }

  public Pagination(int page, int size) {
    this(page, size, null, false, new Sort());
  }

  public Pagination(int page, int size, final Sort sort) {
    this(page, size, null, false, sort);
  }

  public Pagination(final Pagination req, long total) {
    this(req.getPage(), req.getSize(), total, false, req.getSort());
  }

  /** カウント算出を無効化します。 */
  public Pagination ignoreTotal() {
    this.ignoreTotal = true;
    return this;
  }

  /** ソート指定が未指定の時は与えたソート条件で上書きします。 */
  public Pagination sortIfEmpty(SortOrder... orders) {
    if (sort != null)
      sort.ifEmpty(orders);
    return this;
  }

  /** 最大ページ数を返します。total設定時のみ適切な値が返されます。 */
  public int getMaxPage() {
    return (total == null) ? 0
        : Calculator.init(total)
            .scale(0, RoundingMode.UP).divideBy(size).intValue();
  }

  /** 開始件数を返します。 */
  public int getFirstResult() {
    return (page - 1) * size;
  }

  public static Pagination of() {
    return new Pagination();
  }

  public static Pagination of(int page) {
    return new Pagination(page);
  }

  public static Pagination of(int page, int size) {
    return new Pagination(page, size);
  }

  public static Pagination of(int page, int size, Sort sort) {
    return new Pagination(page, size, sort);
  }

  public static Pagination of(Pagination req, long total) {
    return new Pagination(req, total);
  }

}
