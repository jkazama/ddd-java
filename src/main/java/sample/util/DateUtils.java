package sample.util;

import java.text.*;
import java.util.*;

//low: サンプル用のサポートUtils。実際のプロジェクトではcommons-lang等のライブラリを利用してください。
public abstract class DateUtils {

	public static Date date(String day) {
		try {
			return new SimpleDateFormat("yyyyMMdd").parse(day);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static Date dateTo(String day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date(day));
		cal.add(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.MILLISECOND, -1);
		return cal.getTime();
	}
	
	public static String dayFormat(Date date) {
		return new SimpleDateFormat("yyyyMMdd").format(date);
	}
	
}
