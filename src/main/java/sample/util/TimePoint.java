package sample.util;

import java.util.Date;

import javax.validation.constraints.NotNull;

import lombok.*;
import sample.model.constraints.Day;

/**
 * 日付と日時のペアを表現します。
 * <p>0:00に営業日切り替えが行われないケースなどでの利用を想定しています。
 * 
 * @author jkazama
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimePoint {
	/** 日付(営業日) */
	@Day
	private String day;
	/** 日付におけるシステム日時 */
	@NotNull
	private Date date;

	/** 指定日付と同じか。(day == targetDay) */
	public boolean equalsDay(String targetDay) {
		return day.equals(targetDay);
	}
	
	/** 指定日付よりも前か。(day < targetDay) */
	public boolean beforeDay(String targetDay) {
		return DateUtils.date(day).before(DateUtils.date(targetDay));
	}
	
	/** 指定日付以前か。(day <= targetDay) */
	public boolean beforeEqualsDay(String targetDay) {
		return equalsDay(targetDay) || beforeDay(targetDay);
	}

	/** 指定日付よりも後か。(targetDay < day) */
	public boolean afterDay(String targetDay) {
		return DateUtils.date(day).after(DateUtils.date(targetDay));
	}
	
	/** 指定日付以降か。(targetDay <= day) */
	public boolean afterEqualsDay(String targetDay) {
		return equalsDay(targetDay) || afterDay(targetDay);
	}

	/** 日付を元にTimePointを生成します。 */
	public static TimePoint by(String day) {
		return new TimePoint(day, DateUtils.date(day));
	}

}
