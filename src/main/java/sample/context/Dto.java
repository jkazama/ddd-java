package sample.context;

import java.io.Serializable;

/**
 * DTO(Data Transfer Object)を表現するマーカーインターフェース。
 * 
 * <p>本インターフェースを継承するDTOは、層(レイヤー)間をまたいで情報を取り扱い可能に
 * する役割を持ち、次の責務を果たします。
 * <ul>
 * <li>複数の情報の取りまとめによる通信コストの軽減
 * <li>可変情報の集約
 * <li>ドメイン情報の転送
 * <li>ドメインロジックを持たない、シンプルなバリューオブジェクトの転送
 * </ul>
 * 
 * @author jkazama
 */
public interface Dto extends Serializable {

}
