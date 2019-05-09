package sample.util;

import java.util.Date;

import javax.validation.constraints.NotNull;

import lombok.*;
import sample.model.constraints.Day;

/**
 * Pair of a date and the date and time.
 * <p>A business day uses it with the case which is not 0:00.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimePoint {
    @Day
    private String day;
    @NotNull
    private Date date;

    /** day == targetDay */
    public boolean equalsDay(String targetDay) {
        return day.equals(targetDay);
    }

    /** day < targetDay */
    public boolean beforeDay(String targetDay) {
        return DateUtils.date(day).before(DateUtils.date(targetDay));
    }

    /** day <= targetDay */
    public boolean beforeEqualsDay(String targetDay) {
        return equalsDay(targetDay) || beforeDay(targetDay);
    }

    /** targetDay < day */
    public boolean afterDay(String targetDay) {
        return DateUtils.date(day).after(DateUtils.date(targetDay));
    }

    /** targetDay <= day */
    public boolean afterEqualsDay(String targetDay) {
        return equalsDay(targetDay) || afterDay(targetDay);
    }

    public static TimePoint by(String day) {
        return new TimePoint(day, DateUtils.date(day));
    }

}
