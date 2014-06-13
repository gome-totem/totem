package org.x.cloud.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtil {

	protected static Logger logger = LoggerFactory.getLogger(DateUtil.class);

	public static Calendar convertCalendar(String _dateValue) {
		Date _date = convertDate(_dateValue);
		if (_date == null)
			return null;
		Calendar ca = Calendar.getInstance();
		ca.setTime(_date);
		return ca;
	}

	public static int diffDay(Date fromDate, Date toDate) {
		int dayFrom = getDayOfYear(fromDate);
		int dayTo = getDayOfYear(toDate);
		if (dayTo >= dayFrom) {
			return dayTo - dayFrom;
		} else {
			return 365 - dayFrom + dayTo;
		}
	}

	public static int diffYear(String value) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		int year = c.get(java.util.Calendar.YEAR);
		Date d = convertDate(value);
		if (d == null)
			return 0;
		Calendar ca = Calendar.getInstance();
		ca.setTime(d);
		return year - ca.get(java.util.Calendar.YEAR);
	}

	public static int getDayOfYear(Date _date) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTime(_date);
		return c.get(java.util.Calendar.DAY_OF_YEAR);
	}

	public static int getDayOfYear(long _timeStamp) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTimeInMillis(_timeStamp);
		return c.get(java.util.Calendar.DAY_OF_YEAR);
	}

	public static String getWeekName(int week) {
		switch (week) {
		case 0:
			return "星期日";
		case 1:
			return "星期一";
		case 2:
			return "星期二";
		case 3:
			return "星期三";
		case 4:
			return "星期四";
		case 5:
			return "星期五";
		case 6:
			return "星期六";
		}
		return "";
	}

	public static String getMonthName(int month) {
		switch (month) {
		case 1:
			return "一月";
		case 2:
			return "二月";
		case 3:
			return "三月";
		case 4:
			return "四月";
		case 5:
			return "五月";
		case 6:
			return "六月";
		case 7:
			return "七月";
		case 8:
			return "八月";
		case 9:
			return "九月";
		case 10:
			return "十月";
		case 11:
			return "十一月";
		case 12:
			return "十二月";
		}
		return "";
	}

	public static int getMonth(long _timeStamp) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTimeInMillis(_timeStamp);
		return c.get(java.util.Calendar.MONTH) + 1;
	}

	public static int getCurrentDay() {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		return c.get(java.util.Calendar.DAY_OF_YEAR);
	}

	public static String getCurrentDateFileName() {
		Date _date = new Date(System.currentTimeMillis());
		return formatDate(_date);
	}

	public static Date getDateByDay(int _dayOfYear) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.set(java.util.Calendar.DAY_OF_YEAR, _dayOfYear);
		return c.getTime();
	}

	public static int getYear(Date _date) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTime(_date);
		return c.get(java.util.Calendar.YEAR);
	}

	public static int getCurrentYear() {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		return c.get(java.util.Calendar.YEAR);
	}

	public static int getYearByAge(int age) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.add(java.util.Calendar.YEAR, -age);
		return c.get(java.util.Calendar.YEAR);
	}

	public static int getWeekNumber(Date _date) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTime(_date);
		int _number = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (_number == 0)
			_number = 7;
		return _number;
	}

	public static Date addDay(String _dateValue, int _day) {
		java.util.Calendar c = convertCalendar(_dateValue);
		c.add(java.util.Calendar.DAY_OF_YEAR, _day);
		return c.getTime();
	}

	public static Date addDay(Date _date, int _day) {
		if (_day == 0)
			return _date;
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTime(_date);
		c.add(java.util.Calendar.DAY_OF_YEAR, _day);
		return c.getTime();
	}

	public static Date addHour(String _dateValue, int _hour) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		Date _date = convertDate(_dateValue);
		c.setTime(_date);
		c.add(java.util.Calendar.HOUR_OF_DAY, _hour);
		return c.getTime();
	}

	public static Date addHour(Date _date, int _hour) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTime(_date);
		c.add(java.util.Calendar.HOUR_OF_DAY, _hour);
		return c.getTime();
	}

	public static Date convertDate(long time) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTimeInMillis(time);
		return c.getTime();
	}

	public static String toString(long time) {
		return formatDateTime(convertDate(time));
	}

	public static Date convertDate(String _mask, String _dateValue) {
		if (_dateValue.indexOf("null") >= 0)
			return null;
		if (StringUtils.isEmpty(_dateValue)) {
			return null;
		}
		Date date = null;
		SimpleDateFormat df = new SimpleDateFormat(_mask);
		try {
			date = df.parse(_dateValue);
		} catch (Exception e) {
			return null;
		}
		return (Date) date;
	}

	public static Date convertDate(String _dateValue) {
		if (_dateValue.indexOf(":") > 0) {
			return convertDate("yyyy-MM-dd HH:mm", _dateValue);
		} else {
			return convertDate("yyyy-MM-dd", _dateValue);
		}
	}

	public static int diffMinute(long timestamp) {
		long diff = System.currentTimeMillis() - timestamp;
		int value = Math.round(diff / 60000);
		return value;
	}

	public static String formatDateTime(Date _date) {
		SimpleDateFormat _format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return _format.format(_date);
	}

	public static String formatDate(Date _date) {
		SimpleDateFormat _format = new SimpleDateFormat("yyyy-MM-dd");
		return _format.format(_date);
	}

	public static String now() {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		SimpleDateFormat _format = new SimpleDateFormat("yyyy-MM-dd");
		return _format.format(c.getTime());
	}

	public static long nowLong() {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		SimpleDateFormat _format = new SimpleDateFormat("yyyy-MM-dd");
		String date = _format.format(c.getTime());
		return convertDate("yyyy-MM-dd", date).getTime();
	}

	public static java.util.Calendar today() {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		SimpleDateFormat _format = new SimpleDateFormat("yyyy-MM-dd");
		String date = _format.format(c.getTime());
		c.setTime(convertDate("yyyy-MM-dd", date));
		return c;
	}

	public static java.util.Calendar today(int year) {
		Calendar c = today();
		c.add(java.util.Calendar.YEAR, year);
		return c;
	}

	public static String formatShortDate(Date _date) {
		return formatShortDate(_date, '-');
	}

	public static String formatShortDate(Date _date, char seperateChar) {
		SimpleDateFormat _format = new SimpleDateFormat("MM" + seperateChar + "dd");
		return _format.format(_date);
	}

	public static String getTimeInterval(long _time1, long _time2) {
		int _time = Math.round(_time1 - _time2);
		if (_time < 1000)
			return _time + "毫秒";
		_time = Math.round(_time / 1000);
		if (_time < 60)
			return _time + "秒";
		_time = Math.round(_time / 60);
		if (_time < 60)
			return _time + "分钟";
		_time = Math.round(_time / 60);
		if (_time < 24)
			return _time + "小时";
		_time = Math.round(_time / 24);
		return _time + "天";
	}

	public static int getIntervalHour(long _time1, long _time2) {
		long _time = Math.round(Math.abs(_time1 - _time2));
		_time = Math.round(_time / 1000);
		_time = Math.round(_time / 60);
		_time = Math.round(_time / 60);
		return (int) _time;
	}

	public static boolean afterCurrentDate(String _dateValue) {
		Date _currentDate = new Date();
		Date _date = DateUtil.convertDate(_dateValue);
		return _date.after(_currentDate);
	}

	public static boolean beforeCurrentDate(String _dateValue) {
		Date _currentDate = new Date();
		Date _date = DateUtil.convertDate(_dateValue);
		return _date.before(_currentDate);
	}

	public static void main(String[] args) {
		System.out.println(nowLong());
		String fromDate = "2010-12-30";
		String toDate = "2011-01-02";
		System.out.println(diffDay(convertDate(fromDate), convertDate(toDate)));
	}

}
