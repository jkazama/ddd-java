package sample.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sample.model.constraints.ISODate;
import sample.model.constraints.ISODateTime;

/**
 * Pair of a date and the date and time.
 * <p>
 * A business day uses it with the case which is not 0:00.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimePoint {
    @ISODate
    private LocalDate day;
    @ISODateTime
    private LocalDateTime date;

    /** day == targetDay */
    public boolean equalsDay(LocalDate targetDay) {
        return day.equals(targetDay);
    }

    /** day < targetDay */
    public boolean beforeDay(LocalDate targetDay) {
        return day.isBefore(targetDay);
    }

    /** day <= targetDay */
    public boolean beforeEqualsDay(LocalDate targetDay) {
        return equalsDay(targetDay) || beforeDay(targetDay);
    }

    /** targetDay < day */
    public boolean afterDay(LocalDate targetDay) {
        return day.isAfter(targetDay);
    }

    /** targetDay <= day */
    public boolean afterEqualsDay(LocalDate targetDay) {
        return equalsDay(targetDay) || afterDay(targetDay);
    }

    public static TimePoint of(LocalDate day) {
        return new TimePoint(day, DateUtils.dateByDay(day));
    }

}
