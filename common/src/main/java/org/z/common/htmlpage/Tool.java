package org.z.common.htmlpage;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.dict.ConfigFile;
import org.z.global.dict.Global;
import org.z.global.dict.Global.OrderState;
import org.z.global.environment.Const;
import org.z.global.object.KeyValue;
import org.z.global.util.DBObjectUtil;
import org.z.global.util.DateUtil;
import org.z.global.util.EmptyUtil;
import org.z.global.util.StringUtil;
import org.z.global.util.TextUtil;
import org.z.store.mongdb.DataSet;

import sun.misc.BASE64Encoder;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

@SuppressWarnings("restriction")
public class Tool {
	protected static final Logger logger = LoggerFactory.getLogger(Tool.class);
	public static Tool instance = null;
	private Lang lang = null;
	private Calendar targetTime = Calendar.getInstance();
	protected static long stepByWeek = 604800000;
	protected static long stepByDay = 86400000;
	protected static long stepByHour = 3600000;
	private static final ConcurrentHashMap<String, String[]> constellationMap = new ConcurrentHashMap<String, String[]>();
	private static final ConcurrentHashMap<String, String[]> BuildMap = new ConcurrentHashMap<String, String[]>();
	private static final ConcurrentHashMap<String, String[]> BloodTypeMap = new ConcurrentHashMap<String, String[]>();
	private static final ConcurrentHashMap<String, String[]> MarriageMap = new ConcurrentHashMap<String, String[]>();
	public static final ConcurrentHashMap<String, String[]> MonthMap = new ConcurrentHashMap<String, String[]>();
	private static final int[] constellationEdgeDay = { 20, 19, 21, 21, 21, 22, 23, 23, 23, 23, 22, 22 };
	public static final String SeoTitle = ConfigFile.rock().getItem("SeoTitle", "一起嗨私人导游网_私人导游_留学生导游_法国中文导游_泰国私人导游_一起嗨海外导游旅行网");
	static {
		constellationMap.put("zh", new String[] { "水瓶座", "双鱼座", "牡羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "魔羯座" });
		constellationMap.put("en", new String[] { "Aquarius", "Pisces", "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio",
				"Sagittarius", "Capricorn" });
		BuildMap.put("zh", new String[] { "苗条", "匀称", "健壮", "高大", "小巧", "丰满", "高挑", "较胖", "较瘦", "运动型" });
		BuildMap.put("en", new String[] { "Slim", "symmetry", "robust", "tall", "small", "plump", "tall", "more fat", "lean", "Sports" });
		BloodTypeMap.put("zh", new String[] { "O", "A", "B", "AB", "稀有" });
		BloodTypeMap.put("en", new String[] { "O", "A", "B", "AB", "Rare" });
		MarriageMap.put("zh", new String[] { "单身", "恋爱中", "订婚", "已婚", "离异" });
		MarriageMap.put("en", new String[] { "Single", "Love", "engagement", "Married", "divorced" });
		MonthMap.put("zh", new String[] { "一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月" });
		MonthMap.put("en", new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" });
		instance = new Tool(new Lang("all", "zh"));
	}

	public Tool(Lang lang) {
		this.lang = lang;
	}

	public String seoTitle() {
		return SeoTitle;
	}

	public static String compress(String data) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DeflaterOutputStream zos = new DeflaterOutputStream(bos);
			zos.write(data.getBytes());
			zos.close();
			return new String(new BASE64Encoder().encodeBuffer(bos.toByteArray()));
		} catch (Exception ex) {
			return "ZIP_ERR";
		}
	}

	public static String compress(byte[] data) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DeflaterOutputStream zos = new DeflaterOutputStream(bos);
			zos.write(data);
			zos.close();
			return new String(new BASE64Encoder().encodeBuffer(bos.toByteArray()));
		} catch (Exception ex) {
			return "ZIP_ERR";
		}
	}

	public static String base64(String value) {
		try {
			return new String(new BASE64Encoder().encodeBuffer(StringUtil.toBytes(value)));
		} catch (Exception ex) {
			return value;
		}
	}

	public double formatDecimal(double v) {
		java.text.DecimalFormat myformat = new java.text.DecimalFormat("0.0");
		return Double.parseDouble(myformat.format(v));
	}

	public int multi(double v1, double v2) {
		return (int) Math.round(v1 * v2);
	}

	public double abs(double v) {
		return Math.abs(v);
	}

	public int abs(int v) {
		return Math.abs(v);
	}

	public String decode(String v) {
		try {
			return URLDecoder.decode(v, "utf-8");
		} catch (Exception e) {
			return v;
		}
	}

	public String iff(boolean b, String v1, String v2) {
		if (b == true) {
			return v1;
		}
		return v2;
	}

	public int iff(boolean b, int v1, int v2) {
		if (b == true) {
			return v1;
		}
		return v2;
	}

	public BasicDBObject dbObject() {
		return new BasicDBObject();
	}

	public boolean not(boolean v) {
		return !v;
	}

	public boolean equals(int value1, int value2) {
		return value1 == value2;
	}

	public boolean equals(String value1, String value2) {
		if (value1 == null || value2 == null) {
			return false;
		}
		return value1.equals(value2);
	}

	public int mod(int value1, int value2) {
		return value1 % value2;
	}

	public int byInt(BasicDBObject object, String fieldName) {
		String v = by(object, fieldName);
		if (StringUtil.isEmpty(v)) {
			return 0;
		}
		return Integer.parseInt(v);
	}

	public String fakePassword(String value) {
		StringBuilder buf = new StringBuilder();
		int index = value.length() - 3;
		for (int i = 0; i < index; i++) {
			buf.append(value.charAt(i));
		}
		for (int i = value.length() - 1; i >= index; i--) {
			buf.append(value.charAt(i));
		}
		return buf.toString();
	}

	public String by(BasicDBObject object, String fieldName) {
		if (!object.containsField(fieldName)) {
			return "";
		}
		return object.getString(fieldName);
	}

	public String by(BasicDBObject object, String fieldName, String itemName) {
		if (!object.containsField(fieldName)) {
			return "";
		}
		BasicDBObject oItem = (BasicDBObject) object.get(fieldName);
		if (oItem == null) {
			return "";
		}
		if (!oItem.containsField(itemName)) {
			return "";
		}
		return oItem.getString(itemName);
	}

	public String ifEven(int index, String v1, String v2) {
		if (index % 2 == 0) {
			return v2;
		}
		return v1;
	}

	public boolean isEven(int index) {
		if (index % 2 == 0) {
			return false;
		}
		return true;
	}

	public String[] toArray(String... values) {
		String[] result = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = values[i];
		}
		return result;
	}

	public int div(int value1, int value2) {
		return value1 / value2;
	}

	public String stringOf(Object o) {
		return String.valueOf(o);
	}

	public String encode(String value) {
		try {
			return java.net.URLEncoder.encode(value, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return value;
		}
	}

	public boolean isEmpty(String str) {
		return StringUtils.isEmpty(str);
	}

	public boolean isEmpty(BasicDBObject o, String fieldName) {
		return isEmpty(o, fieldName, null);
	}

	public boolean isEmpty(BasicDBObject o, String fieldName, String subFieldName) {
		if (!o.containsField(fieldName)) {
			return true;
		}
		if (StringUtils.isEmpty(subFieldName)) {
			return StringUtil.isEmpty(o.getString(fieldName));
		}
		BasicDBObject oItem = (BasicDBObject) o.get(fieldName);
		if (!oItem.containsField(subFieldName)) {
			return true;
		}
		return StringUtil.isEmpty(oItem.getString(subFieldName));
	}

	public boolean isEmpty(Object str) {
		if (str == null) {
			return true;
		}
		return StringUtils.isEmpty(String.valueOf(str));
	}

	public boolean isNull(Object o) {
		boolean b = o == null || EmptyUtil.isEmpty(o);
		if (b)
			return true;
		if (o instanceof ArrayList) {
			return ((ArrayList<?>) o).size() == 0;
		}
		return false;
	}

	public boolean isNotNull(Object o) {
		return isNull(o) == false;
	}

	public String join(String... values) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			b.append(values[i]);
		}
		return b.toString();
	}

	public String leftRightBy(int index) {
		if (index % 2 == 0) {
			return "right";
		} else {
			return "left";
		}
	}

	public int intOf(Object s) {
		String value = String.valueOf(s);
		if (StringUtils.isEmpty(value)) {
			return 0;
		}
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return 0;
		}
	}

	public long longOf(Object s) {
		String value = String.valueOf(s);
		if (StringUtils.isEmpty(value)) {
			return 0;
		}
		return Long.parseLong(value);
	}

	public String displayOf(boolean b) {
		return b ? "block" : "none";
	}

	public String displayOf(int v1, int v2) {
		return v1 == v2 ? "block" : "none";
	}

	public int currentMonth() {
		Calendar currentTime = Calendar.getInstance();
		currentTime.setTimeInMillis(System.currentTimeMillis());
		return currentTime.get(Calendar.MONTH) + 1;
	}

	public String extractText(String html, int maxWidth) {
		if (StringUtils.isEmpty(html)) {
			return "";
		}
		String content = TextUtil.extractText(html);
		if (maxWidth == 0) {
			return content;
		}
		return StringUtils.abbreviate(content, maxWidth);
	}

	public String abbreviate(String value, int maxWidth) {
		return StringUtils.abbreviate(value, maxWidth);
	}

	public KeyValue createKeyValue(String name, Object value) {
		return KeyValue.create(name, value);
	}

	public String iffEmpty(String v1, String v2) {
		if (StringUtil.isEmpty(v1)) {
			return v2;
		}
		return v1;
	}

	public boolean startsWith(String content, String c) {
		return content.startsWith(c);
	}

	public String nowDiff(long time) {
		return dateDiff(time);
	}

	public String dateDiff(long time) {
		targetTime.setTimeInMillis(time);
		String ago = lang.get("Global", "ago", "以前");
		Calendar currentTime = Calendar.getInstance();
		currentTime.setTimeInMillis(System.currentTimeMillis());
		long diff = currentTime.getTimeInMillis() - time;
		if (diff <= 0) {
			return "";
		}
		int y = (int) Math.round(diff / (stepByWeek * 50));
		if (y >= 1) {
			return y + lang.get("Global", "year", "年") + ago;
		}
		int m = (int) Math.round(diff / (stepByWeek * 4));
		if (m >= 1) {
			return m + lang.get("Global", "month", "月") + ago;
		}
		int w = (int) Math.round(diff / stepByWeek);
		if (w >= 1 && w <= 4) {
			return w + lang.get("Global", "week", "星期") + ago;
		}
		int d = (int) Math.round(diff / stepByDay);
		if (d >= 1 && d <= 7) {
			return d + lang.get("Global", "day", "天") + ago;
		}
		int h = (int) Math.round(diff / stepByHour);
		if (h >= 1 && h <= 24) {
			return h + lang.get("Global", "hour", "小时") + ago;
		}
		int n = Math.round((float) diff / 60000);
		if (n >= 1 && n <= 60) {
			return n + lang.get("Global", "minute", "分钟") + ago;
		}
		int s = Math.round((float) diff / 1000);
		if (s >= 1 && s <= 60) {
			return s + lang.get("Global", "second", "秒") + ago;
		}
		return diff + lang.get("Global", "millisecond", "毫秒") + ago;
	}

	public String lineBreak(String content) {
		return content.replaceAll("\r\n", "<br>").replaceAll("\n", "<br>");
	}

	public String escape(String content) {
		if (content == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0, len = content.length(); i < len; i++) {
			char c = content.charAt(i);
			switch (c) {
			case ' ':
				sb.append("&nbsp;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString().replaceAll("\r\n", "<br>").replaceAll("\n", "<br>");
	}

	public static String yearDiff(long time) {
		if (time == 0) {
			return "0";
		}
		try {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(System.currentTimeMillis());
			Calendar c1 = Calendar.getInstance();
			c1.setTimeInMillis(time);
			return String.valueOf(c.get(Calendar.YEAR) - c1.get(Calendar.YEAR));
		} catch (Exception e) {
			return "..";
		}
	}

	public int currentYear() {
		return DateUtil.getCurrentYear();
	}

	public boolean hasNextPage(int pageNumber, int pageSize, int totalCount) {
		int v = Math.round((totalCount / pageSize));
		if (totalCount % pageSize != 0) {
			v++;
		}
		return pageNumber < v;
	}

	public static int dayDiff(long date) {
		try {
			if (date >= System.currentTimeMillis()) {
				return -1;
			}
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(System.currentTimeMillis());
			Calendar c1 = Calendar.getInstance();
			c1.setTimeInMillis(date);
			int day = c.get(Calendar.DAY_OF_YEAR);
			int day1 = c1.get(Calendar.DAY_OF_YEAR);
			if (day >= day1) {
				return day - day1;
			} else {
				return 365 - day1 + day;
			}
		} catch (Exception e) {
			return -1;
		}
	}

	public int dec(int value, int delta) {
		return value - delta;
	}
	public int dec(float value, int delta) {
		return ((int)value) - delta;
	}
	public int dec(int value) {
		return dec(value, 1);
	}

	public long decNow(long time) {
		return time - System.currentTimeMillis();
	}

	public int inc(int value, int delta) {
		return value + delta;
	}

	public int inc(int value) {
		return inc(value, 1);
	}

	public String htmlBy(String content) {
		return StringUtil.htmlBy(content);
	}

	public int randomInt(int count) {
		return RandomUtils.nextInt(count);
	}

	public String postTimeTitleBy(int type) {
		switch (type) {
		case 0:
			return lang.get("Global", "postTime0", "上午9:00-12:00");
		case 1:
			return lang.get("Global", "postTime1", "中午1:00-4:00");
		case 2:
			return lang.get("Global", "postTime2", "下午4:00-7:00");
		case 3:
			return lang.get("Global", "postTime3", "晚上7:00-10:00");
		}
		return "";
	}

	public String policyCancelBy(int type) {
		switch (type) {
		case 0:
			return "灵活模式：在预订日期凌晨零点以前取消预订,全额退款,否则不退。";
		case 1:
			return "1天模式：1天以前取消预订,全额退款,否则不退。";
		case 3:
			return "3天模式：3天以前取消预订,全额退款,否则不退。";
		case 5:
			return "5天模式：5天以前取消预订,全额退款,否则不退。";
		case 7:
			return "7天模式：7天以前取消预订,全额退款,否则不退。";
		}
		return "";
	}

	public String bizTypeBy(int type) {
		switch (type) {
		case 5:
			return "旅行车辆出租";
		case 6:
			return "导游地陪";
		case 7:
			return "客栈别墅";
		}
		return "";
	}

	public String currencyNameBy(String type) {
		return currencyNameBy(intOf(type));
	}

	public String currencyNameBy(int type) {
		switch (type) {
		case 1:
			return "€";
		case 2:
			return "$";
		case 3:
			return "£";
		case 4:
			return "円";
		case 5:
			return "₩";
		case 6:
			return "฿";
		case 7:
			return "C";
		default:
			return "￥";
		}
	}

	public String currencyName(int type) {
		switch (type) {
		case 1:
			return "欧元";
		case 2:
			return "美元";
		case 3:
			return "英镑";
		case 4:
			return "日元";
		case 5:
			return "韩币";
		case 6:
			return "泰铢";
		case 7:
			return "泰铢";
		default:
			return "人民币";
		}
	}

	public String chargeWayBy(int type) {
		switch (type) {
		case 0:
			return "每小时";
		case 1:
			return "每天(8小时)";
		case 2:
			return "每天(10小时)";
		case 3:
			return "每次";
		case 5:
			return "每人";
		case 6:
			return "每个";
		default:
			return "每天";
		}
	}

	public String markBy(int bizType, int catalog) {
		switch (bizType) {
		case 5:
			return "<em class='grnbk'>车</em>";
		case 6:
			if (catalog == 0) {
				return "<em class='redbk'>导</em>";
			} else {
				return "<em class='bluebk'>线</em>";
			}
		case 7:
			return "<em class='bluebk'>房</em>";
		case 3:
			return "<em class='pinkbk'>卖</em>";
		}
		return "";
	}

	public String priceNameBy(Object value, int price) {
		BasicDBList items = (BasicDBList) value;
		for (int i = 0; i < items.size(); i++) {
			BasicDBObject oItem = (BasicDBObject) items.get(i);
			if (oItem.getBoolean("active") == false)
				continue;
			if (oItem.getInt("value") == price) {
				return oItem.getString("name");
			}
		}
		return "";
	}

	public String priceNameOptions(Object value, int price) {
		BasicDBList items = (BasicDBList) value;
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < items.size(); i++) {
			BasicDBObject oItem = (BasicDBObject) items.get(i);
			if (oItem.getBoolean("active") == false)
				continue;
			buffer.append("<option value=\"" + oItem.getString("name") + "\"");
			if (oItem.getInt("value") == price) {
				buffer.append(" selected >");
			} else {
				buffer.append(">");
			}
			buffer.append(oItem.getString("name"));
			buffer.append("</option>");
		}
		return buffer.toString();
	}

	public String timeOptions(int type) {
		StringBuilder buffer = new StringBuilder();
		switch (type) {
		case 0:
			for (int i = 1; i <= 10; i++) {
				buffer.append("<option value=\"" + i + "\">" + i + "小时</option>");
			}
			break;
		case 3:
			for (int i = 1; i <= 5; i++) {
				buffer.append("<option value=\"" + i + "\">" + i + "次</option>");
			}
			break;
		default:
			for (int i = 1; i <= 30; i++) {
				buffer.append("<option value=\"" + i + "\">" + i + "天</option>");
			}
			break;
		}
		return buffer.toString();
	}

	public String OptionsByDot(String content, String dot) {
		String[] values = content.split("\\" + dot);
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			buffer.append("<option value=\"" + values[i] + "\">" + values[i] + "</option>");
		}
		return buffer.toString();
	}

	public String chargeWayName(int type) {
		return chargeWayName(type, 5);
	}

	public String chargeWayName(int type, int bizType) {
		switch (type) {
		case 0:
			return bizType == 7 ? "小时数" : "预约小时数";
		default:
			return bizType == 7 ? "天数" : "预约天数";
		}
	}

	public String postTimeBy(int v) {
		switch (v) {
		case 0:
			return "3-7天";
		case 1:
			return "7-13天";
		case 2:
			return "13-21天";
		}
		return "13-21天";
	}

	public String numberOptions(int count, String name) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 1; i <= count; i++) {
			buffer.append("<option value=\"" + i + "\">" + i + name + "</option>");
		}
		return buffer.toString();
	}

	public String numberOptions(int count) {
		return numberOptions(count, "");
	}

	public String personOptions(int count) {
		return numberOptions(count, "人");
	}

	public String roomOptions(int count) {
		return numberOptions(count, "个");
	}

	public int[] readArray(int size, int step) {
		int count = size / step;
		int[] result = new int[count];
		for (int i = 1; i <= count; i++) {
			result[i - 1] = i;
		}
		return result;
	}

	public String bizWord(int bizType, int catalog) {
		if (bizType == Global.BizType.trip.ordinal()) {
			if (catalog == 0) {
				return "导";
			} else {
				return "线";
			}
		} else if (bizType == Global.BizType.car.ordinal()) {
			return "车";
		}
		return "";
	}

	public String formatDate(String timeStamp, String format) {
		long time = 0;
		if (timeStamp.indexOf("-") > 0) {
			Date d = DateUtil.convertDate(timeStamp);
			if (d == null) {
				return timeStamp;
			}
			time = d.getTime();
		} else if (StringUtils.isAlphanumeric(timeStamp)) {
			time = Long.parseLong(timeStamp);
		} else {
			return timeStamp;
		}
		return formatDate(time, format);
	}

	public String formatDate(Date date) {
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			return df.format(date);
		} catch (Exception e) {
			return "";
		}
	}

	public String formatShortDate(Date date) {
		try {
			SimpleDateFormat df = new SimpleDateFormat("MM-dd");
			return df.format(date);
		} catch (Exception e) {
			return "";
		}
	}
	public String formatDate(long timeStamp) {
		try {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(timeStamp);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			return df.format(c.getTime());
		} catch (Exception e) {
			return "";
		}
	}

	public String formatDate(long timeStamp, String format) {
		try {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(timeStamp);
			SimpleDateFormat df = new SimpleDateFormat(format);
			return df.format(c.getTime());
		} catch (Exception e) {
			return "";
		}
	}

	public String[] formatAPTime(long timeStamp) {
		try {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(timeStamp);
			SimpleDateFormat df = new SimpleDateFormat("HH:mm a");
			String content = df.format(c.getTime());
			String[] values = content.split(" ");
			return values;
		} catch (Exception e) {
			return new String[] { "", "" };
		}
	}

	public String repeatBy(String value, int count) {
		StringBuilder buf = new StringBuilder();
		for (int i = 1; i <= count; i++) {
			buf.append(value);
		}
		return buf.toString();
	}

	public Date addDay(int day) {
		Date date = new Date();
		date = DateUtil.addDay(date, day);
		return date;
	}

	public Date addDay(Date date, int day) {
		return DateUtil.addDay(date, day);
	}

	public String currentDateTime(int day) {
		try {
			Date date = new Date();
			date = DateUtil.addDay(date, day);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			return df.format(date);
		} catch (Exception e) {
			return "";
		}
	}

	public String currentDateTime() {
		return currentDateTime(0);
	}

	public long currentTimeMillis(int day) {
		Date date = new Date();
		date = DateUtil.addDay(date, day);
		return date.getTime();
	}

	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	public boolean readBool(String value, int index) {
		if (value == null) {
			return false;
		}
		if (index > value.length() - 1) {
			return false;
		}
		return value.charAt(index) == '1';
	}

	public String constellationBy(long time) {
		try {
			Calendar c1 = Calendar.getInstance();
			c1.setTimeInMillis(time);
			int month = c1.get(Calendar.MONTH);
			int day = c1.get(Calendar.DAY_OF_MONTH);
			String[] values = constellationMap.get("en");
			if (constellationMap.containsKey(this.lang.langName)) {
				values = constellationMap.get(this.lang.langName);
			}
			if (day < constellationEdgeDay[month]) {
				month = month - 1;
			}
			if (month >= 0) {
				return values[month];
			}
			return values[11];
		} catch (Exception e) {
			return "..";
		}
	}

	public String sexBy(int index) {
		switch (index) {
		case 1:
			return "男";
		case 2:
			return "女";
		}
		return "保密";
	}

	public String buildBy(int index) {
		if (index == 0) {
			return "";
		}
		String[] values = BuildMap.get("en");
		if (BuildMap.containsKey(this.lang.langName)) {
			values = BuildMap.get(this.lang.langName);
		}
		return values[index - 1];
	}

	public String bloodBy(int index) {
		if (index == 0) {
			return "";
		}
		String[] values = BloodTypeMap.get("en");
		if (BloodTypeMap.containsKey(this.lang.langName)) {
			values = BloodTypeMap.get(this.lang.langName);
		}
		return values[index - 1];
	}

	public String marriageBy(int index) {
		if (index == 0) {
			return "";
		}
		String[] values = MarriageMap.get("zh");
		if (MarriageMap.containsKey(this.lang.langName)) {
			values = MarriageMap.get(this.lang.langName);
		}
		return values[index - 1];
	}

	public String monthBy(int index) {
		if (index == 0) {
			return "";
		}
		String[] values = MonthMap.get("zh");
		if (MonthMap.containsKey(this.lang.langName)) {
			values = MonthMap.get(this.lang.langName);
		}
		return values[index - 1];
	}

	public static int monthBy(String constellationName) {
		String[] values = constellationMap.get("zh");
		for (int i = 0; i < values.length; i++) {
			if (values[i].equalsIgnoreCase(constellationName)) {
				return (i + 1);
			}
		}
		return 3;
	}

	public String tripTypeClassName(int type) {
		switch (type) {
		case 0:
			return "icon-travel-tag-b";
		case 1:
			return "icon-travel-tag-a";
		case 2:
			return "icon-travel-tag-e";
		case 3:
			return "icon-travel-tag-c";
		case 4:
			return "icon-travel-tag-d";
		}
		return "";
	}

	public String bizPageName(int bizType, int catalog) {
		switch (bizType) {
		case 5:
			return "car";
		case 6:
			if (catalog == 0) {
				return "guide";
			} else {
				return "trip";
			}
		case 7:
			return "room";
		}
		return "room";
	}

	public String bizUrl(BasicDBObject object) {
		int bizType = object.getInt("bizType", 6);
		int catalog = object.getInt("catalog", 0);
		long shortId = object.getLong("short_id", 0);
		return bizUrl(bizType, catalog, shortId);
	}

	public String bizUrl(int bizType, int catalog, long shortId) {
		switch (bizType) {
		case 5:
			return "/car/" + shortId;
		case 6:
			if (catalog == 0) {
				return "/guide/" + shortId;
			} else {
				return "/trip/" + shortId;
			}
		case 7:
			return "/room/" + shortId;
		}
		return "/guide/" + shortId;
	}

	public String bizTag(int bizType, int catalog) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<span class=\"");
		switch (bizType) {
		case 5:
			buffer.append("tag category3\">租车");
			break;
		case 6:
			if (catalog == 0) {
				buffer.append("tag category2\">导游");
			} else {
				buffer.append("tag category1\">线路");
			}
			break;
		default:
			buffer.append(">");
			break;
		}
		buffer.append("</span>");
		return buffer.toString();
	}

	public String repeatImage(String prefix, String url, Object count) {
		if (isEmpty(count)) {
			return "";
		}
		int num = Integer.parseInt(String.valueOf(count));
		if (num == 0) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append(prefix);
		for (int i = 0; i < num; i++) {
			buffer.append("<img src='" + url + "'/>");
		}
		return buffer.toString();
	}

	public String toString(List<?> o, String fieldName) {
		return DBObjectUtil.toString(o, fieldName);
	}

	public String trim(String v) {
		return StringUtil.trim(v);
	}

	public String hotCity(String city) {
		StringBuilder buffer = new StringBuilder();
		String city_ = "";
		try {
			city_ = java.net.URLEncoder.encode(city, "utf-8");
		} catch (Exception e) {
		}
		buffer.append("<li><a target='_blank' href='/searchtrip/city=" + city_);
		buffer.append("'>" + city + "导游地陪</a></li>");
		return buffer.toString();
	}

	protected String getPageButton(String url, String className, String text) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<li class='" + className + "' style='line-height:22px;width:100px;height:22px;'>");
		buffer.append("<a href='" + url + "'>" + text + "</a>");
		buffer.append("</li>");
		return buffer.toString();
	}

	public String getPager(int totalCount, int pageNumber, int pageSize, String url) {
		if (totalCount < pageSize) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		int pageCount = Math.round(totalCount / pageSize);
		if (totalCount % pageSize > 0) {
			pageCount++;
		}
		buffer.append("<ul>");
		if (pageNumber == 1) {
			buffer.append(getPageButton("javascript:void(0)", "", "上一页"));
		} else {
			buffer.append(getPageButton(url + "&p=" + (pageNumber - 1), "text", "上一页"));
		}
		if (pageNumber + 1 > pageCount) {
			buffer.append(getPageButton("javascript:void(0)", "", "下一页"));
		} else {
			buffer.append(getPageButton(url + "&p=" + (pageNumber + 1), "text", "下一页"));
		}
		buffer.append("</ul>");
		return buffer.toString();
	}

	public String getPagerHtml(int currentNum, int pageNumber, String js) {
		StringBuilder buffer = new StringBuilder();
		if (currentNum == pageNumber)
			buffer.append("<span class='cur'>" + currentNum + "</span>");
		else
			buffer.append("<a " + js + " href='javascript:void(0);'>" + currentNum + "</a>");
		return buffer.toString();
	}

	public static BasicDBObject randomUser() {
		int id = RandomUtils.nextInt(300);
		while (id <= 10) {
			id = RandomUtils.nextInt(300);
		}
		BasicDBObject oRmd = new BasicDBObject();
		oRmd.append("userId", id);
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select user_name,server from user where user_id=?",
				new String[] { String.valueOf(id) }, 0, 0);
		oRmd.append("userName", rows[0][0]);
		oRmd.append("server", rows[0][1]);
		return oRmd;
	}

	public String subStr(String str, int start, int end) {
		if (start < 0)
			start = 0;
		if (end >= str.length())
			return str;
		str = str.substring(start, end);
		return str;
	}

	public String fomatNumber(Object price) {
		double p = 0.00d;
		DecimalFormat df = new DecimalFormat("0.00");
		if (price instanceof Double) {
			p = (Double) price;
		}
		if (price instanceof Integer) {
			p = (Double) price;
		}

		return df.format(p);
	}

	public String moreFaectsSelect(String sFacets) {
		String showHtm = "";
		if (sFacets.length() > 9) {
			sFacets = sFacets.substring(0, 9);
		}
		String[] sdFacets = sFacets.split("\\|");
		for (String sf : sdFacets) {
			if (showHtm.length() < 1) {
				showHtm = "<span class='comcur' style='color: #FFFFFF;'>" + sf + "</span>";
			} else {
				showHtm = showHtm + "&nbsp;" + "<span class='comcur' style='color: #FFFFFF;'>" + sf + "</span>";
			}
		}
		return showHtm;
	}

	public String rank(int rank) {
		if (rank == 0) {
			rank = 2;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < rank; i++) {
			builder.append("<i class=\"icon-stars\"></i>");
		}
		return builder.toString();
	}

	public String readUserBar(String reqUserId, boolean isOwner, String pageName) {
		StringBuilder buffer = new StringBuilder();
		boolean b1 = pageName.equalsIgnoreCase("i");
		String className = b1 ? "current" : "";
		buffer.append("<li class='" + className + "'><a href='/i/" + reqUserId + "'>我的动态</a></li>");
		boolean b2 = b1 == false && pageName.equalsIgnoreCase("answers");
		className = b2 ? "current" : "";
		buffer.append("<li class='" + className + "'><a href='/answers/u=" + reqUserId + "'>我回答的问题</a></li>");
		if (isOwner) {
			className = b1 == false && b2 == false ? "current" : "";
			buffer.insert(0, "<li class='" + className + "'><a href='/offers'>控制面板</a></li>");
		}
		return buffer.toString();
	}

	public String rateStar(int star) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<em class='rating'>");
		for (int i = 1; i <= 5; i++) {
			buffer.append("<em class='star ");
			if (star >= i) {
				buffer.append(" selected '></em>");
			} else {
				buffer.append("'></em>");
			}
		}
		return buffer.toString();
	}

	public String locationBy(Object items) {
		BasicDBList locations = null;
		if (items instanceof String) {
			locations = (BasicDBList) JSON.parse(String.valueOf(items));
		} else {
			locations = (BasicDBList) items;
		}
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < locations.size(); i++) {
			BasicDBObject item = (BasicDBObject) locations.get(i);
			if (buf.length() > 0) {
				buf.append("&nbsp;&nbsp;&nbsp;");
			}
			buf.append(item.getString("value"));
		}
		return buf.toString();
	}

	public String requireClassBy(int index) {
		switch (index) {
		case 0:
			return "opGuider";
		case 1:
			return "opCar";
		case 2:
			return "opFriend";
		case 3:
			return "opTopic";
		case 4:
			return "opTrip";
		}
		return "";
	}

	public String tripItemNameBy(int type) {
		switch (type) {
		case 0:
			return "美食";
		case 1:
			return "活动";
		case 2:
			return "景点";
		case 3:
			return "人文历史";
		case 4:
			return "购物";
		case 5:
			return "情色";
		}
		return "";
	}

	public String orderStateName(int index) {
		if (index >= Global.OrderState.values().length) {
			return "异常";
		}
		OrderState state = Global.OrderState.values()[index];
		switch (state) {
		case Paid:
			return "支付成功";
		case Checked:
			return "订单成行";
		case Running:
			return "行程中";
		case Close:
			return "行程结束";
		case Dispute:
			return "行程纠纷";
		default:
			return "未支付";
		}
	}
	public String ticketStateName(int status){
		if (status >= 5) {
			return "异常";
		}
		switch (status) {
		case 1:
			return "正在定制航班";
		case 2:
			return "确认航班计划";
		case 3:
			return "等待出票";
		default:
			return "出票完成";
		}
		
	}
	
	public float sumprice(float price,int count){
	return 	price*count;
	}

	public BasicDBObject parseDay(long timestamp) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timestamp);
		int m = c.get(java.util.Calendar.MONTH) + 1;
		BasicDBObject oResult = new BasicDBObject();
		oResult.append("month", m);
		oResult.append("monthName", DateUtil.getMonthName(m));
		int day = c.get(java.util.Calendar.DAY_OF_MONTH);
		oResult.append("day", day);
		int week = c.get(Calendar.DAY_OF_WEEK) - 1;
		oResult.append("week", week);
		oResult.append("weekName", DateUtil.getWeekName(week));
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		oResult.append("date", df.format(c.getTime()));
		return oResult;
	}

	public BasicDBObject parseDay(Date d) {
		return parseDay(d.getTime());
	}

	public BasicDBObject parseDay(Date d, int day) {
		Date date = DateUtil.addDay(d, day);
		return parseDay(date.getTime());
	}

	public BasicDBObject hiDecode(String content) {
		content = TextUtil.easyDecompress(content);
		BasicDBObject oResult = (BasicDBObject) JSON.parse(content);
		return oResult;
	}

	public static void main(String[] args) {
	}

}
