package org.z.global.util;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;

public class JodaUtil {

	public static DateTime toDateTime(String time, String pattern) {
		if (EmptyUtil.isEmpty(time) || EmptyUtil.isEmpty(pattern)) {
			return null;
		}
		try {
			return DateTimeFormat.forPattern(pattern).parseDateTime(time);
		} catch (Exception e) {
			return null;
		}
	}

	public static int getDays(DateTime startTime, DateTime endTime) {
		if (startTime == null || endTime == null) {
			return 0;
		}
		Period period = new Period(startTime, endTime, PeriodType.days());
		return period.getDays() + 1;
	}

	public static String toString(DateTime dateTime, String pattern) {
		if (EmptyUtil.isEmpty(dateTime) || EmptyUtil.isEmpty(pattern)) {
			return "";
		}
		return dateTime.toString(pattern);

	}
}
