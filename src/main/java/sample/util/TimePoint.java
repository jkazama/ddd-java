package sample.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sample.model.constraints.Day;

/**
 * 日付と日時のペアを表現します。
 * <p>
 * 0:00に営業日切り替えが行われないケースなどでの利用を想定しています。
 * 
 * @author jkazama
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimePoint {
    /** 日付(営業日) */
    @Day
    private LocalDate day;
    /** 日付におけるシステム日時 */
    @NotNull
    private LocalDateTime date;

    /** 指定日付と同じか。(day == targetDay) */
    public boolean equalsDay(LocalDate targetDay) {
        return day.equals(targetDay);
    }

    /** 指定日付よりも前か。(day < targetDay) */
    public boolean beforeDay(LocalDate targetDay) {
        return day.isBefore(targetDay);
    }

    /** 指定日付以前か。(day <= targetDay) */
    public boolean beforeEqualsDay(LocalDate targetDay) {
        return equalsDay(targetDay) || beforeDay(targetDay);
    }

    /** 指定日付よりも後か。(targetDay < day) */
    public boolean afterDay(LocalDate targetDay) {
        return day.isAfter(targetDay);
    }

    /** 指定日付以降か。(targetDay <= day) */
    public boolean afterEqualsDay(LocalDate targetDay) {
        return equalsDay(targetDay) || afterDay(targetDay);
    }

    /** 日付を元にTimePointを生成します。 */
    public static TimePoint of(LocalDate day) {
        return new TimePoint(day, DateUtils.dateByDay(day));
    }

}
