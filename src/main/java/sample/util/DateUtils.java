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
 * 頻繁に利用される日時ユーティリティを表現します。
 */
public abstract class DateUtils {

    private static WeekendQuery WeekendQuery = new WeekendQuery();

    /** 指定された文字列(YYYY-MM-DD)を元に日付へ変換します。 */
    public static LocalDate day(String dayStr) {
        return dayOpt(dayStr).orElse(null);
    }

    /** 指定された文字列とフォーマット型を元に日時へ変換します。 */
    public static LocalDate day(String dateStr, DateTimeFormatter formatter) {
        return dayOpt(dateStr, formatter).orElse(null);
    }

    /** 指定された文字列とフォーマット文字列を元に日時へ変換します。 */
    public static LocalDate day(String dateStr, String format) {
        return day(dateStr, DateTimeFormatter.ofPattern(format));
    }

    /** 指定された文字列(YYYY-MM-DD)を元に日付へ変換します。 */
    public static Optional<LocalDate> dayOpt(String dayStr) {
        if (!StringUtils.hasText(dayStr)) {
            return Optional.empty();
        }
        return Optional.of(LocalDate.parse(dayStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE));
    }

    /** 指定された文字列とフォーマット型を元に日時へ変換します。 */
    public static Optional<LocalDate> dayOpt(String dateStr, DateTimeFormatter formatter) {
        if (!StringUtils.hasText(dateStr)) {
            return Optional.empty();
        }
        return Optional.of(LocalDate.parse(dateStr.trim(), formatter));
    }

    /** 指定された文字列とフォーマット型を元に日時へ変換します。 */
    public static Optional<LocalDate> dayOpt(String dateStr, String format) {
        return dayOpt(dateStr, DateTimeFormatter.ofPattern(format));
    }

    /** 従来の Date から LocalDateTime へ変換します。 */
    public static LocalDateTime date(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /** LocalDateTime から 従来の Date へ変換します。 */
    public static Date date(LocalDateTime date) {
        return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
    }

    /** 指定された文字列とフォーマット型を元に日時へ変換します。 */
    public static LocalDateTime date(String dateStr, DateTimeFormatter formatter) {
        return dateOpt(dateStr, formatter).orElse(null);
    }

    /** 指定された文字列とフォーマット文字列を元に日時へ変換します。 */
    public static LocalDateTime date(String dateStr, String format) {
        return date(dateStr, DateTimeFormatter.ofPattern(format));
    }

    /** 従来の Date から LocalDateTime へ変換します。 */
    public static Optional<LocalDateTime> dateOpt(Date date) {
        return date == null ? Optional.empty() : Optional.of(date(date));
    }

    /** LocalDateTime から 従来の Date へ変換します。 */
    public static Optional<Date> dateOpt(LocalDateTime date) {
        return date == null ? Optional.empty() : Optional.of(date(date));
    }

    /** 指定された文字列とフォーマット型を元に日時へ変換します。 */
    public static Optional<LocalDateTime> dateOpt(String dateStr, DateTimeFormatter formatter) {
        if (!StringUtils.hasText(dateStr))
            return Optional.empty();
        return Optional.of(LocalDateTime.parse(dateStr.trim(), formatter));
    }

    /** 指定された文字列とフォーマット型を元に日時へ変換します。 */
    public static Optional<LocalDateTime> dateOpt(String dateStr, String format) {
        return dateOpt(dateStr, DateTimeFormatter.ofPattern(format));
    }

    /** 指定された日付を日時へ変換します。 */
    public static LocalDateTime dateByDay(LocalDate day) {
        return dateByDayOpt(day).orElse(null);
    }

    public static Optional<LocalDateTime> dateByDayOpt(LocalDate day) {
        return Optional.ofNullable(day).map((v) -> v.atStartOfDay());
    }

    /** 指定した日付の翌日から1msec引いた日時を返します。 */
    public static LocalDateTime dateTo(LocalDate day) {
        return dateToOpt(day).orElse(null);
    }

    public static Optional<LocalDateTime> dateToOpt(LocalDate day) {
        return Optional.ofNullable(day).map((v) -> v.atTime(23, 59, 59));
    }

    /** 指定された日時型とフォーマット型を元に文字列(YYYY-MM-DD)へ変更します。 */
    public static String dayFormat(LocalDate day) {
        return dayFormatOpt(day).orElse(null);
    }

    public static Optional<String> dayFormatOpt(LocalDate day) {
        return Optional.ofNullable(day).map((v) -> v.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    /** 指定された日時型とフォーマット型を元に文字列へ変更します。 */
    public static String dateFormat(LocalDateTime date, DateTimeFormatter formatter) {
        return dateFormatOpt(date, formatter).orElse(null);
    }

    public static Optional<String> dateFormatOpt(LocalDateTime date, DateTimeFormatter formatter) {
        return Optional.ofNullable(date).map((v) -> v.format(formatter));
    }

    /** 指定された日時型とフォーマット文字列を元に文字列へ変更します。 */
    public static String dateFormat(LocalDateTime date, String format) {
        return dateFormatOpt(date, format).orElse(null);
    }

    public static Optional<String> dateFormatOpt(LocalDateTime date, String format) {
        return Optional.ofNullable(date).map((v) -> v.format(DateTimeFormatter.ofPattern(format)));
    }

    /** 日付の間隔を取得します。 */
    public static Optional<Period> between(LocalDate start, LocalDate end) {
        if (start == null || end == null)
            return Optional.empty();
        return Optional.of(Period.between(start, end));
    }

    /** 日時の間隔を取得します。 */
    public static Optional<Duration> between(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null)
            return Optional.empty();
        return Optional.of(Duration.between(start, end));
    }

    /** 指定営業日が週末(土日)か判定します。(引数は必須) */
    public static boolean isWeekend(LocalDate day) {
        Assert.notNull(day, "day is required.");
        return day.query(WeekendQuery);
    }

    /** 指定年の最終日を取得します。 */
    public static LocalDate dayTo(int year) {
        return LocalDate.ofYearDay(year, Year.of(year).isLeap() ? 366 : 365);
    }

    /** 週末判定用のTemporalQuery&gt;Boolean&lt;を表現します。 */
    public static class WeekendQuery implements TemporalQuery<Boolean> {
        @Override
        public Boolean queryFrom(TemporalAccessor temporal) {
            DayOfWeek dayOfWeek = DayOfWeek.of(temporal.get(ChronoField.DAY_OF_WEEK));
            return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        }
    }

}
