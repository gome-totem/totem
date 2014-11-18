package com.gome.totem.sniper.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeUtil {

	private static Logger logger = LoggerFactory.getLogger(TimeUtil.class);
	static SimpleDateFormat wholeDayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
	static SimpleDateFormat shortDayFormat = new SimpleDateFormat("yyyy-MM-dd") ;
	static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd") ;
	
	public static String getStringFromDate(Long date){
		if (date==null) {
			return "" ;
		}
		return wholeDayFormat.format(date) ;
	}
	
	public static String getStringDate(Long date){
		if (date==null) {
			return "" ;
		}
		return dayFormat.format(date) ;
	}
	
	public static Date getWholeDateFromStr(String source){
		if (source!=null && source.trim().length()>0) {
			try {
				return wholeDayFormat.parse(source) ;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null ;
	}
	
	public static Timestamp getTimestampFromStr(String source){
		if (source!=null && source.length()>0) {
			try {
				return new Timestamp(wholeDayFormat.parse(source).getTime()) ;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null ;
	}
	
	public static long getTimestampByDay(int day) throws ParseException{
		Date date = shortDayFormat.parse(shortDayFormat.format(new Date()));
		return date.getTime() - day*86400000l;
	}
	
	public static String sayHelloByHour(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		
		if(hour >= 0 && hour < 4)   return "凌晨好！" ;
		if(hour >= 4 && hour < 7)   return "早上好！" ;
		if(hour >= 7 && hour < 11)  return "上午好！" ;
		if(hour >= 11 && hour < 14) return "中午好！" ;
		if(hour >= 14 && hour < 19) return "下午好！" ;
		if(hour >= 19 && hour < 22) return "晚上好！" ;
		if(hour >= 22 && hour < 24) return "深夜好！" ;
		
		else return "";
	}
	
	/**
	 * 获取今天0：00的long类型Time
	 * @return
	 */
	
	public static long getTodayLongTime(){
		long today = 0L;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = sdf.format(new Date());
		try {
			today = sdf.parse(strDate).getTime();
		} catch (ParseException e) {
			
		}
		return today;
	}
	
	/**
	 * 获取某一天的long类型日期
	 * @param n  负数为往前日期，例如昨天，参数为-1，明天参数为1
	 * @return
	 */
	public static long getLongTimeByToday(int n){
		return getTodayLongTime() + n* 24 * 60 * 60 * 1000;
	}
	
	public static long getTime(int howmany){
		long start = 0l;
		try {
			start = TimeUtil.getTimestampByDay(howmany);
		} catch (ParseException e) {
			logger.error("时间转化错误，错误信息：{}", new Object[]{e});
		}
		return start;
	}
	
	public static String firstDayOfMonth(){
        Calendar c = Calendar.getInstance();   
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH,1);//设置为1号,当前日期既为本月第一天
        return shortDayFormat.format(c.getTime());
	}
	
	public static long getTimestamp(){
		Date date1 = null,date2 = null;
		try {
			date1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("2013/10/01 00:00:00");
			date2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("1970/01/01 08:00:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		long l = date1.getTime() - date2.getTime() > 0 ? date1.getTime() - date2.getTime() : date2.getTime() - date1.getTime();
		       
		return l;
	}
	/**
	 * 把当前时间格式化成指定字符串
	 * @param yyyy/MM/dd
	 * @return
	 */
	public static String getTodayFormat(String format){
		String date = "";
		date = new SimpleDateFormat(format).format(new Date());
		return date;
	}
	
	/**
	 * long型时间格式化
	 * @param longDate
	 * @param pattern
	 * @return
	 */
	public static String format(long longDate,String pattern){
		String strDate = "";
		Date date = new Date(longDate);
		strDate = new SimpleDateFormat(pattern).format(date);
		return strDate;
	}
	
	public static void main(String[] args) {
//		System.out.println(getTodayLongTime());
//		System.out.println(getTime(0));
//		System.out.println(getTodayFormat("yyyy-MM-dd: HH:mm:ss"));
		Date date = new Date(1392376797080l);
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(sdf.format(date));
	}
}
