package sample.context;

import java.util.*;

import org.springframework.stereotype.Component;

import sample.util.*;

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
    private String day;

    public Timestamper() {
        this.day = DateUtils.dayFormat(new Date());
    }

    /**
     * @return 営業日を返します。
     */
    public String day() {
        return day;
    }

    /**
     * low: 日時クラスなどは必要に応じてJodaTimeなどのリッチクラスを利用
     * @return 日時を返します。
     */
    public Date date() {
        return new Date();
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
     * @param day 更新営業日
     */
    public Timestamper daySet(String day) {
        this.day = day;
        return this;
    }

    //low: サンプル用の割り切り(T + n)算出メソッド。実際は休日含めた営業日の考慮が必要
    public String dayPlus(int i) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(DateUtils.date(day));
        cal.add(Calendar.DAY_OF_MONTH, i);
        return DateUtils.dayFormat(cal.getTime());
    }

}
