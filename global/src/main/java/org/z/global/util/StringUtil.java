package org.z.global.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.xerial.snappy.Snappy;
import org.z.global.dict.Global;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class StringUtil {
	private static String delims = " +-*=/!:;{}(),.?'\"\\\t\n\r";
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final Pattern EMAILPATTERN;
	private static AtomicLong senderCounter = new AtomicLong(0);
	private static final Random rnd = new Random();
	private static long dateStep1 = 3600000 * 24 * 7;
	private static long dateStep2 = 3600000 * 24;
	private static long dateStep3 = 3600000;

	static {
		EMAILPATTERN = Pattern.compile(EMAIL_PATTERN);
	}

	public static String timeDiff(long time) {
		Calendar currentTime = Calendar.getInstance();
		currentTime.setTimeInMillis(System.currentTimeMillis());
		Calendar targetTime = Calendar.getInstance();
		targetTime.setTimeInMillis(time);
		int y = currentTime.get(Calendar.YEAR) - targetTime.get(Calendar.YEAR);
		String ago = "以前";
		long diff = currentTime.getTimeInMillis() - time;
		if (diff <= 0) {
			return "";
		}
		int h = Math.round(diff / dateStep3);
		if (h >= 1 && h <= 23) {
			return h + "小时" + ago;
		}
		int d = Math.round(diff / dateStep2);
		if (d >= 1 && d <= 7) {
			return d + "天" + ago;
		}
		int w = Math.round(diff / dateStep1);
		if (w >= 1 && w <= 4) {
			return w + "星期" + ago;
		}
		int m = currentTime.get(Calendar.MONTH) - targetTime.get(Calendar.MONTH);
		if (m >= 1 && m <= 12) {
			return m + "月" + ago;
		}
		int n = Math.round(diff / 60000);
		if (n >= 1 && n <= 60) {
			return n + "分钟" + ago;
		}
		int s = Math.round(diff / 1000);
		if (s >= 1 && s <= 60) {
			return s + "秒" + ago;
		}
		if (y >= 1) {
			return StringUtil.formatDateTime(targetTime.getTime());
		}
		return diff + "毫秒" + ago;
	}

	public static byte[] compress(byte[] orig) {
		try {
			return Snappy.compress(orig);
		} catch (Exception e) {
			return orig;
		}
	}

	public static byte[] uncompress(byte[] compressed) {
		try {
			return Snappy.uncompress(compressed);
		} catch (Exception e) {
			return compressed;
		}
	}
	public static String XSSFilter(String source) {
		StringBuilder result = new StringBuilder();
		Pattern pattern = Pattern.compile("[\u4e00-\u9fa5 a-zA-Z0-9]");// 中文,英文,数字

		char[] c = source.toCharArray();
		for (int i = 0; i < c.length; i++) {
			String v = String.valueOf(c[i]);
			boolean match = true;
			Matcher matcher = pattern.matcher(v);
			while (matcher.find()) {
				result.append(matcher.group());
				match = false;
			}
			if (match) {
				result.append(escape(v));
			}
		}
		return result.toString();
	}

	public static byte[] toBytes(String content) {
		try {
			return content.getBytes("utf-8");
		} catch (Exception e) {
			return content.getBytes();
		}
	}
	public static void quickSortByName(final String name, BasicDBList rows) {
		Collections.sort(rows, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				int p1 = ((BasicDBObject) o1).getInt(name, 0);
				int p2 = ((BasicDBObject) o2).getInt(name, 0);
				if (p1 > p2) {
					return 1;
				} else if (p1 < p2) {
					return -1;
				} else {
					return 0;
				}
			}

		});
	}

	public static String toString(byte[] bytes) {
		try {
			return new String(bytes, "utf-8");
		} catch (Exception e) {
			return new String(bytes);
		}
	}

	public static String quoted(String value) {
		return "'" + value + "'";
	}

	public static String quotedSpace(String value) {
		return " " + value + " ";
	}

	public static int intOf(String s) {
		if (StringUtils.isEmpty(s)) {
			return 0;
		}
		s = s.trim();
		if (s.equalsIgnoreCase("null")) {
			return 0;
		}
		if (!StringUtils.isNumeric(s)) {
			return 0;
		}
		return Integer.parseInt(s);
	}

	public static boolean isEmpty(String v) {
		if (StringUtils.isEmpty(v)) {
			return true;
		}
		if (v.equalsIgnoreCase("null") || v.equalsIgnoreCase("undefined")) {
			return true;
		}
		return false;
	}

	public static boolean notEmpty(String v) {
		return !isEmpty(v);
	}

	public static String urlDecode(String v) {
		try {
			return URLDecoder.decode(v, "UTF-8");
		} catch (Exception e) {
			return v;
		}
	}

	public static long longOf(String s) {
		if (StringUtils.isEmpty(s)) {
			return 0;
		}
		s = s.trim();
		if (s.equalsIgnoreCase("null")) {
			return 0;
		}
		if (!StringUtils.isAlphanumeric(s)) {
			return 0;
		}
		return Long.parseLong(s);
	}

	public static String htmlBy(String content) {
		StringBuilder buffer = new StringBuilder();
		content.replaceAll("\r", "");
		int pos = content.indexOf("\n");
		buffer.append("<p>");
		while (pos >= 0) {
			buffer.append(content.substring(0, pos));
			buffer.append("</p><p>");
			content = content.substring(pos + 1, content.length());
			pos = content.indexOf("\n");
		}
		buffer.append(content);
		buffer.append("</p>");
		return buffer.toString();
	}
	public static String readSpecial(String str) {
		StringBuilder buder = new StringBuilder();
		String regEx = "[A-Za-z0-9 \\u4e00-\\u9fa5]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		while (m.find()) {
			buder.append(m.group(0));
		}
		return buder.toString();
	}
	public static String normalize(String content) {
		int count = 1;
		int index = (int) Math.pow(2, count++);
		char[] values = content.toCharArray();
		while (index < values.length) {
			if (index == 0) {
				continue;
			}
			char c = values[index];
			values[index] = values[index - 1];
			values[index - 1] = c;
			index = (int) Math.pow(2, count++);
		}
		return String.valueOf(values);
	}

	public static String join(String[] values, String token) {
		StringBuilder buffer = new StringBuilder();
		HashSet<String> table = new HashSet<String>();
		for (int i = 0; i < values.length; i++) {
			String value = trim(values[i]);
			if (StringUtil.isEmpty(value) == true) {
				continue;
			}
			if (table.contains(value)) {
				continue;
			}
			table.add(value);
			if (buffer.length() > 0) {
				buffer.append(" ");
				buffer.append(token);
				buffer.append(" ");
			}
			buffer.append(value);
		}
		return buffer.toString();
	}

	public static String join(ArrayList<String> values, String token) {
		StringBuilder buffer = new StringBuilder();
		HashSet<String> table = new HashSet<String>();
		for (int i = 0; i < values.size(); i++) {
			String value = trim(values.get(i));
			if (StringUtil.isEmpty(value) == true) {
				continue;
			}
			if (table.contains(value)) {
				continue;
			}
			table.add(value);
			if (buffer.length() > 0) {
				buffer.append(" ");
				buffer.append(token);
				buffer.append(" ");
			}
			buffer.append(value);
		}
		return buffer.toString();
	}

	public static String join(BasicDBList values, String token) {
		StringBuilder buffer = new StringBuilder();
		HashSet<String> table = new HashSet<String>();
		for (int i = 0; values != null && i < values.size(); i++) {
			if (values.get(i) == null)
				continue;
			String value = trim(values.get(i).toString());
			if (StringUtil.isEmpty(value) == true) {
				continue;
			}
			if (table.contains(value)) {
				continue;
			}
			table.add(value);
			if (buffer.length() > 0) {
				buffer.append(" ");
				buffer.append(token);
				buffer.append(" ");
			}
			buffer.append(value);
		}
		return buffer.toString();
	}

	public static String trim(String text) {
		if (StringUtils.isEmpty(text)) {
			return "";
		}
		final StringBuilder buffer = new StringBuilder(text.length());
		for (final char ch : text.toCharArray()) {
			if (ch != (char) 160 && ch != '\t' && ch != '\n' && ch != '\r' && ch != ' ') {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}

	public static boolean validateEmail(final String hex) {
		Matcher matcher = EMAILPATTERN.matcher(hex);
		return matcher.matches();
	}

	public static boolean isAlphanumeric(String v) {
		for (int i = 0; i < v.length(); i++) {
			char c = v.charAt(i);
			if (StringUtils.isAsciiPrintable(String.valueOf(c)) == false) {
				return false;
			}
		}
		return true;
	}

	public static String trimContinue(String textBuffer) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(textBuffer);
		return trimContinue(buffer);
	}

	public static String trimContinue(StringBuilder textBuffer) {
		int index = 0;
		boolean lastIsEmpty = false;
		while (index < textBuffer.length()) {
			char ch = textBuffer.charAt(index);
			boolean isEmpty = false;
			if (ch == (char) 160 || ch == '\t' || ch == '\n' || ch == '\r' || ch == ' ' || ch == '　' || ch == '.' || ch == '-') {
				isEmpty = true;
			}
			if (!isEmpty) {
				lastIsEmpty = false;
				index++;
				continue;
			}
			if (lastIsEmpty && isEmpty) {
				textBuffer.deleteCharAt(index);
				continue;
			}
			textBuffer.setCharAt(index, ' ');
			lastIsEmpty = true;
			index++;
		}
		return textBuffer.toString();
	}

	public static String compressContinueChar(String content, char c, String replace) {
		int index = 0;
		StringBuilder buffer = new StringBuilder();
		int count = 0;
		while (index < content.length()) {
			char c1 = content.charAt(index++);
			if (c1 == c) {
				count++;
				continue;
			} else if (count >= 2) {
				buffer.append(replace);
				count = 0;
			}
			buffer.append(c1);
		}
		return buffer.toString();
	}

	public static String loadFileContent(String fileName, String charset, boolean ctrlf) {
		File file = new File(fileName);
		return loadFileContent(file, charset, true);
	}

	public static String loadFileContent(File file, String charset, boolean ctrlf) {
		if (!file.exists()) {
			return null;
		}
		try {
			FileInputStream _fileStream = new FileInputStream(file);
			InputStreamReader _reader = new InputStreamReader(_fileStream, charset);
			BufferedReader br = new BufferedReader(_reader);
			StringBuffer buffer = new StringBuffer();
			String _text = null;
			while (((_text = br.readLine()) != null)) {
				if (ctrlf == false) {
					_text = _text.replaceAll("\\s", "");
				}
				buffer.append(_text);
				if (ctrlf == true) {
					buffer.append(System.getProperty("line.separator"));
				}
			}
			_reader.close();
			_fileStream.close();
			return buffer.toString();
		} catch (Exception E) {
			return "";
		}
	}
	public static void deleteFile(String fileName) {
		try {
			File file = new File(fileName);
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File f : files) {
					FileUtils.forceDelete(f);
				}
			} else {
				if (!file.exists())
					return;
				FileUtils.forceDelete(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeToFile(String content, String fileName, String charset) {
		try {
			FileOutputStream fileStream = new FileOutputStream(fileName);
			OutputStreamWriter writer = new OutputStreamWriter(fileStream, charset);
			IOUtils.write(content, writer);
			writer.close();
			fileStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int parserInt(Integer value, int defaultValue) {
		return value == null ? defaultValue : value.intValue();
	}

	public static int parserInt(String content, int defaultValue) {
		if (content == null) {
			return defaultValue;
		}
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (c == '.') {
				break;
			}
			if (Character.isDigit(c)) {
				buffer.append(content.charAt(i));
			}
		}
		if (buffer.length() == 0) {
			return defaultValue;
		}
		return Integer.parseInt(buffer.toString());
	}

	public static String escape(String content) {
		if (content == null) {
			return "";
		}
		content = content.replaceAll("\r\n", "\\\\n").replaceAll("\n", "\\\\n");
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
			case '"':
				sb.append("&quot;");
				break;
			case '\'':
				sb.append("&apos;");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String toHtml(String content) {
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
		return sb.toString().replaceAll("\r\n", "<br/>").replaceAll("\n", "<br/>");
	}

	public static String createUniqueID() {
		return UUID.randomUUID().toString();
	}

	public static String createSenderId() {
		StringBuilder builder = new StringBuilder();
		builder.append(Global.ServerName);
		if (senderCounter.get() >= Long.MAX_VALUE) {
			senderCounter.set(0);
		}
		builder.append(String.valueOf(senderCounter.addAndGet(1)));
		builder.append("@");
		builder.append(rnd.nextInt(Integer.MAX_VALUE));
		return builder.toString();
	}

	public static String createPassword(int length) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < length; i++) {
			buffer.append(RandomUtils.nextInt(9));
		}
		return buffer.toString();
	}

	public static String formatDateTime(long timestamp) {
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(timestamp);
		return formatDateTime(time.getTime());
	}

	public static String formatDateTime(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try {
			return df.format(date);
		} catch (Exception e) {
			return null;
		}
	}

	public static String formatTime(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
		try {
			return df.format(date);
		} catch (Exception e) {
			return null;
		}
	}

	public static String formatFullTime(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss");
		try {
			return df.format(date);
		} catch (Exception e) {
			return null;
		}
	}

	public static String formatDate(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return df.format(date);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getDateTimestamp(String date) {
		if (date == null) {
			return String.valueOf(System.currentTimeMillis());
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return String.valueOf(df.parse(date).getTime());
		} catch (Exception e) {
			return String.valueOf(System.currentTimeMillis());
		}
	}

	public static String currentDateTime() {
		Date date = new Date();
		date.setTime(System.currentTimeMillis());
		return formatDate(date);
	}

	public static String currentTime() {
		Date date = new Date();
		date.setTime(System.currentTimeMillis());
		SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss");
		try {
			return df.format(date);
		} catch (Exception e) {
			return "";
		}
	}

	public static String zeroString(int size) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < size; i++) {
			buffer.append("0");
		}
		return buffer.toString();
	}

	private static String replaceFrom(String string, String source, String dest, boolean caseSensitive, int from[]) {
		if (source.compareTo("") == 0) {
			return string;
		}
		int stringLength = string.length();
		int sourceLength = source.length();
		int destLength = dest.length();
		for (; from[0] + sourceLength <= stringLength; from[0]++) {
			int compareResult;
			if (caseSensitive) {
				compareResult = string.substring(from[0], from[0] + sourceLength).compareTo(source);
			} else {
				compareResult = string.substring(from[0], from[0] + sourceLength).compareToIgnoreCase(source);
			}
			if (compareResult == 0) {
				int fromIndex = from[0];
				from[0] += destLength;
				return (new StringBuilder(String.valueOf(string.substring(0, fromIndex)))).append(dest)
						.append(string.substring(fromIndex + sourceLength, stringLength)).toString();
			}
		}

		return string;
	}

	private static String replaceFrom(String string, String source, String dest, boolean caseSensitive, int from) {
		int fromArray[] = new int[1];
		fromArray[0] = from;
		return replaceFrom(string, source, dest, caseSensitive, fromArray);
	}

	public static String replace(String string, String source, String dest, boolean caseSensitive) {
		return replaceFrom(string, source, dest, caseSensitive, 0);
	}

	private static String replaceAll(String string, String source, String dest, boolean caseSensitive, int from) {
		int fromArray[] = new int[1];
		fromArray[0] = from;
		String newString = replaceFrom(string, source, dest, caseSensitive, fromArray);
		from = fromArray[0];
		if (newString.compareTo(string) == 0) {
			return newString;
		} else {
			return replaceAll(newString, source, dest, caseSensitive, from);
		}
	}

	public static String replaceAll(String string, String source, String dest, boolean caseSensitive) {
		return replaceAll(string, source, dest, caseSensitive, 0);
	}

	private static String replaceWholeWordsFrom(String string, String pattern, String dest, boolean caseSensitive, int fromArray[]) {
		boolean frontOK = false;
		boolean backOK = false;
		int index = caseSensitive ? string.indexOf(pattern, fromArray[0]) : string.toUpperCase().indexOf(pattern.toUpperCase(), fromArray[0]);
		if (index == 0) {
			frontOK = true;
		} else if (index > 0 && delims.indexOf(string.charAt(index - 1)) >= 0) {
			frontOK = true;
		}
		if (frontOK) {
			if (index + pattern.length() >= string.length()) {
				backOK = true;
			} else if (delims.indexOf(string.charAt(index + pattern.length())) >= 0) {
				backOK = true;
			}
			if (backOK) {
				fromArray[0] = index - 1 >= 0 ? index + dest.length() : 0;
				return (new StringBuilder(String.valueOf(string.substring(0, index - 1 >= 0 ? index : 0)))).append(dest)
						.append(index + pattern.length() <= string.length() ? string.substring(index + pattern.length(), string.length()) : "").toString();
			}
		}
		if (index >= 0 && index + 1 < string.length()) {
			fromArray[0] = index + 1;
			return replaceWholeWordsFrom(string, pattern, dest, caseSensitive, fromArray);
		} else {
			return string;
		}
	}

	public static String replaceWholeWords(String string, String source, String dest, boolean caseSensitive) {
		int fromArray[] = new int[1];
		return replaceWholeWordsFrom(string, source, dest, caseSensitive, fromArray);
	}

	public static String replaceAllWholeWords(String string, String source, String dest, boolean caseSensitive) {
		int fromArray[] = new int[1];
		String oldResult = null;
		String result;
		for (result = replaceWholeWordsFrom(string, source, dest, caseSensitive, fromArray); oldResult == null || !result.equals(oldResult); result = replaceWholeWordsFrom(
				oldResult, source, dest, caseSensitive, fromArray)) {
			oldResult = result;
		}

		return result;
	}

	public static boolean doesWordExist(String string, String source, boolean caseSensitive) {
		for (StringTokenizer st = new StringTokenizer(string); st.hasMoreTokens();) {
			String token = st.nextToken();
			if (caseSensitive && token.compareTo(source) == 0 || !caseSensitive && token.toUpperCase().compareTo(source.toUpperCase()) == 0) {
				return true;
			}
		}

		return false;
	}

	public static boolean isValidPositiveInteger(String string) {
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) < '0' || string.charAt(i) > '9') {
				return false;
			}
		}

		return true;
	}

	public static String encodeHex(String value) {
		try {
			return String.valueOf(Hex.encodeHex(value.getBytes("UTF-8")));
		} catch (Exception e) {
			return value;
		}
	}

	public static String decodeHex(String value) {
		try {
			return new String(Hex.decodeHex(value.toCharArray()), "UTF-8");
		} catch (Exception e) {
			return value;
		}
	}

	public static boolean checkPath(String path) {
		if (path == null) {
			return false;
		}
		path = path.replaceAll("\\\\", File.separator);
		path = path.replaceAll("//", File.separator);
		String[] names = path.split(File.separator);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < names.length - 1; i++) {
			if (names[i].length() == 0) {
				buffer.append(File.separator);
				continue;
			}
			buffer.append(names[i]);
			File file = new File(buffer.toString());
			if (!file.exists()) {
				if (!file.mkdir()) {
					return false;
				}
			}
			buffer.append(File.separator);
		}
		return true;
	}

	public static String toJSON(int totalCount, String[][] rows, List<String> fieldNames) {
		StringBuilder builder = new StringBuilder();
		builder.append(",\"totalCount\":" + totalCount);
		builder.append(",\"items\":[");
		for (int i = 0; rows != null && i < rows.length; i++) {
			builder.append("{");
			for (int t = 0; t < fieldNames.size(); t++) {
				String value = rows[i][t];
				if (value.startsWith("[")) {
					builder.append("\"" + fieldNames.get(t) + ":" + value);
				} else {
					builder.append("\"" + fieldNames.get(t) + "\":\"" + value + "\"");
				}
				if (t < fieldNames.size() - 1) {
					builder.append(",");
				}
			}
			builder.append("}");
			if (i < rows.length - 1) {
				builder.append(",");
			}
		}
		builder.append("]");
		return builder.toString();
	}

	public static String toJSON(String[][] rows, List<String> fieldNames) {
		StringBuilder builder = new StringBuilder();
		for (int t = 0; rows != null && rows.length >= 1 && t < fieldNames.size(); t++) {
			if (t == 0) {
				builder.append(",");
			}
			String value = rows[0][t];
			if (value.startsWith("[")) {
				builder.append("\"" + fieldNames.get(t) + "\":" + value);
			} else {
				builder.append("\"" + fieldNames.get(t) + "\":\"" + StringUtil.escape(value) + "\"");
			}
			if (t < fieldNames.size() - 1) {
				builder.append(",");
			}
		}
		return builder.toString();
	}

	public static String fileShortName(String fileName) {
		int pos = fileName.lastIndexOf("/");
		if (pos < 0) {
			pos = fileName.lastIndexOf("\\");
		}
		if (pos > 0) {
			fileName = fileName.substring(pos + 1, fileName.length());
		} else {
			return fileName;
		}
		pos = fileName.lastIndexOf(".");
		if (pos > 0) {
			fileName = fileName.substring(0, pos);
		} else {
			return fileName;
		}
		return fileName;
	}

	public static String getLocalIP() {
		Enumeration<NetworkInterface> netInterfaces = null;
		String ip = "127.0.0.1";
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();
				String name = ni.getName().toLowerCase();
				if (name.startsWith("lo") || name.startsWith("vir") || name.startsWith("vmnet") || name.startsWith("wlan")) {
					continue;
				}
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					InetAddress ia = ips.nextElement();
					if (ia instanceof Inet4Address) {
						if (ia.getHostAddress().toString().startsWith("127")) {
							continue;
						} else {
							return ia.getHostAddress();
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return ip;
	}

	public static String getLocalName() {
		try {
			Runtime run = Runtime.getRuntime();
			Process proc = run.exec("hostname");
			StringWriter writer = new StringWriter();
			IOUtils.copy(proc.getInputStream(), writer, "utf-8");
			String name = StringUtil.trim(writer.toString());
			return name;
		} catch (Exception e) {
			return "unknow";
		}
	}

	public static String currentPath() {
		File file = new File(".");
		String path = file.getAbsolutePath();
		if (path.endsWith(".")) {
			path = path.substring(0, path.length() - 1);
		}
		if (!path.endsWith("/")) {
			path = path + "/";
		}
		return path;
	}
	public static String append(Object... strs) {
		if (strs == null || strs.length == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (Object str : strs) {
			sb.append(str);
		}
		return sb.toString();
	}

	public static void main(String[] args) {
	}
	
}
