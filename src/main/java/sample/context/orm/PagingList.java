package sample.context.orm;

import java.util.List;

import lombok.Builder;
import sample.context.Dto;

/**
 * ページング一覧を表現します。
 * low: Spring Data の Pageable で実装できると望ましい
 *
 * @param <T> 結果オブジェクト(一覧の要素)
 */
@Builder
public record PagingList<T>(
    List<T> list, Pagination page) implements Dto {
}