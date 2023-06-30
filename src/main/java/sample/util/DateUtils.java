package sample.util;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Date;
import java.util.Optional;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Support Utils for sample. Please use libraries such as commons-lang in actual
 * projects.
 */
public abstract class DateUtils {

    private static WeekendQuery WeekendQuery = new WeekendQuery();

    public static LocalDate day(String dayStr) {
        return dayOpt(dayStr).orElse(null);
    }

    public static LocalDate day(String dateStr, DateTimeFormatter formatter) {
        return dayOpt(dateStr, formatter).orElse(null);
    }

    public static LocalDate day(String dateStr, String format) {
        return day(dateStr, DateTimeFormatter.ofPattern(format));
    }

    public static Optional<LocalDate> dayOpt(String dayStr) {
        if (!StringUtils.hasText(dayStr)) {
            return Optional.empty();
        }
        return Optional.of(LocalDate.parse(dayStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE));
    }

    public static Optional<LocalDate> dayOpt(String dateStr, DateTimeFormatter formatter) {
        if (!StringUtils.hasText(dateStr)) {
            return Optional.empty();
        }
        return Optional.of(LocalDate.parse(dateStr.trim(), formatter));
    }

    public static Optional<LocalDate> dayOpt(String dateStr, String format) {
        return dayOpt(dateStr, DateTimeFormatter.ofPattern(format));
    }

    public static LocalDateTime date(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static Date date(LocalDateTime date) {
        return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime date(String dateStr, DateTimeFormatter formatter) {
        return dateOpt(dateStr, formatter).orElse(null);
    }

    public static LocalDateTime date(String dateStr, String format) {
        return date(dateStr, DateTimeFormatter.ofPattern(format));
    }

    public static Optional<LocalDateTime> dateOpt(Date date) {
        return date == null ? Optional.empty() : Optional.of(date(date));
    }

    public static Optional<Date> dateOpt(LocalDateTime date) {
        return date == null ? Optional.empty() : Optional.of(date(date));
    }

    public static Optional<LocalDateTime> dateOpt(String dateStr, DateTimeFormatter formatter) {
        if (!StringUtils.hasText(dateStr))
            return Optional.empty();
        return Optional.of(LocalDateTime.parse(dateStr.trim(), formatter));
    }

    public static Optional<LocalDateTime> dateOpt(String dateStr, String format) {
        return dateOpt(dateStr, DateTimeFormatter.ofPattern(format));
    }

    public static LocalDateTime dateByDay(LocalDate day) {
        return dateByDayOpt(day).orElse(null);
    }

    public static Optional<LocalDateTime> dateByDayOpt(LocalDate day) {
        return Optional.ofNullable(day).map((v) -> v.atStartOfDay());
    }

    public static LocalDateTime dateTo(LocalDate day) {
        return dateToOpt(day).orElse(null);
    }

    public static Optional<LocalDateTime> dateToOpt(LocalDate day) {
        return Optional.ofNullable(day).map((v) -> v.atTime(23, 59, 59));
    }

    public static String dayFormat(LocalDate day) {
        return dayFormatOpt(day).orElse(null);
    }

    public static Optional<String> dayFormatOpt(LocalDate day) {
        return Optional.ofNullable(day).map((v) -> v.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    public static String dateFormat(LocalDateTime date, DateTimeFormatter formatter) {
        return dateFormatOpt(date, formatter).orElse(null);
    }

    public static Optional<String> dateFormatOpt(LocalDateTime date, DateTimeFormatter formatter) {
        return Optional.ofNullable(date).map((v) -> v.format(formatter));
    }

    public static String dateFormat(LocalDateTime date, String format) {
        return dateFormatOpt(date, format).orElse(null);
    }

    public static Optional<String> dateFormatOpt(LocalDateTime date, String format) {
        return Optional.ofNullable(date).map((v) -> v.format(DateTimeFormatter.ofPattern(format)));
    }

    public static Optional<Period> between(LocalDate start, LocalDate end) {
        if (start == null || end == null)
            return Optional.empty();
        return Optional.of(Period.between(start, end));
    }

    public static Optional<Duration> between(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null)
            return Optional.empty();
        return Optional.of(Duration.between(start, end));
    }

    public static boolean isWeekend(LocalDate day) {
        Assert.notNull(day, "day is required.");
        return day.query(WeekendQuery);
    }

    public static LocalDate dayTo(int year) {
        return LocalDate.ofYearDay(year, Year.of(year).isLeap() ? 366 : 365);
    }

    public static class WeekendQuery implements TemporalQuery<Boolean> {
        @Override
        public Boolean queryFrom(TemporalAccessor temporal) {
            DayOfWeek dayOfWeek = DayOfWeek.of(temporal.get(ChronoField.DAY_OF_WEEK));
            return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        }
    }

}
