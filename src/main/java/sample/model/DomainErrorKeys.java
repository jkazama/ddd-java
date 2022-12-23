package sample.model;

/** 汎用ドメインにおけるエラーキー定数を表現します。 */
public interface DomainErrorKeys {

  // 既に処理中/処理済です。
  String STATUS_PROCESSING = "error.ActionStatusType.processing";
  /** 適切な期間を入力してください。 */
  String BEFORE_EQUALS_DAY = "error.LocalDate.beforeEqualsDay";
  /** 情報が見つかりませんでした。 */
  String ENTITY_NOT_FOUND = "error.Entity.notFound";

}
