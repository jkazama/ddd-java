package sample.context;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import sample.util.TimePoint;

/**
 * 日時ユーティリティコンポーネント。
 * low: このコンポーネントで単体テスト用にMock機能(常に同じ日時を返す)を用意しておくと便利です。
 * 
 * @author jkazama
 */
@Component
public class Timestamper {

    /** 営業日 */
    // low: サンプルではDataFixturesの初期化時に固定営業日(20141118)が設定されます。
    private LocalDate day;

    public Timestamper() {
        this(LocalDate.now());
    }

    public Timestamper(LocalDate day) {
        this.day = day;
    }

    /**
     * @return 営業日を返します。
     */
    public LocalDate day() {
        return day;
    }

    /**
     * @return 日時を返します。
     */
    public LocalDateTime date() {
        return LocalDateTime.now();
    }

    /**
     * @return 営業日/日時を返します。
     */
    public TimePoint tp() {
        return new TimePoint(day(), date());
    }

    /**
     * 営業日を更新します。
     * low: 営業日は静的なので日回しバッチ等で上書く必要があります
     * 
     * @param day 更新営業日
     */
    public Timestamper daySet(LocalDate day) {
        this.day = day;
        return this;
    }

    // low: サンプル用の割り切り(T + n)算出メソッド。実際は休日含めた営業日の考慮が必要
    public LocalDate dayPlus(int i) {
        return this.day.plusDays(i);
    }

}
