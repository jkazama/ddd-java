package sample.context;

/**
 * ドメインオブジェクトのマーカーインターフェース。
 *
 * <p>本インターフェースを継承するドメインオブジェクトは、ある一定の粒度で とりまとめられたドメイン情報と、
 * それに関連するビジネスロジックを実行する役割を持ち、 次の責務を果たします。
 * <ul>
 * <li>ドメイン情報の管理
 * <li>ドメイン情報に対する振る舞い
 * </ul>
 *
 * <p>
 * ドメインモデルの詳細については次の書籍を参考にしてください。
 * <ul>
 * <li>「<a href="http://www.amazon.co.jp/dp/0321125215/">Domain-Driven Design</a>」　Eric Evans
 * <li>「<a href="http://www.amazon.co.jp/dp/0321127420/">Patterns of Enterprise Application Architecture</a>」　Martin Fowler
 * <li>「<a href="http://www.amazon.co.jp/dp/1932394583/">Pojos in Action</a>」　Chris Richardson
 * </ul>
 *
 * @author jkazama
 */
public interface Entity {

}
