package sample.usecase.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sample.context.Dto;

/**
 * メール配信イベントを表現します。
 * 
 * @author jkazama
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppMailEvent<T> implements Dto {
    private AppMailType mailType;
    private T value;

    public static <T> AppMailEvent<T> of(AppMailType mailType, T value) {
        return new AppMailEvent<T>(mailType, value);
    }

    /** メール配信種別を表現します。 */
    public static enum AppMailType {
        /** 振込出金依頼の登録受付完了 */
        FINISH_REQUEST_WITHDRAW;
    }
}
