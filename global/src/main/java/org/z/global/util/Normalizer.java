package org.z.global.util;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.z.global.dict.Global;


public class Normalizer {

	public static Date formatDateTime(String content) {
		if (StringUtils.isEmpty(content))
			return null;
		return DateUtil.convertDate(content);
	}

	public static String[] formatTimes(String content) {
		int pos = content.indexOf(":");
		if (pos <= 0)
			return null;
		String[] results = new String[] { "", "" };
		for (int i = pos + 1; i < content.length(); i++) {
			char c = content.charAt(i);
			if (!Character.isDigit(c)) {
				break;
			}
			results[1] += c;
		}
		for (int i = pos - 1; i >= 0; i--) {
			char c = content.charAt(i);
			if (!Character.isDigit(c)) {
				break;
			}
			results[0] = c + results[0];
		}
		if (StringUtils.isEmpty(results[0]) || !StringUtils.isAlphanumeric(results[0])) {
			return null;
		}
		if (StringUtils.isEmpty(results[1]) || !StringUtils.isAlphanumeric(results[1])) {
			return null;
		}
		if (results[0].length() >= 3) {
			results[0] = results[0].substring(results[0].length() - 2, results[0].length());
		}
		if (results[1].length() >= 3) {
			results[1] = results[1].substring(results[1].length() - 2, results[1].length());
		}
		if (Integer.parseInt(results[0]) >= 24) {
			return null;
		}
		if (Integer.parseInt(results[1]) > 60) {
			return null;
		}
		return results;
	}

	public static String splitContent(String content, char... chars) {
		if (StringUtils.isEmpty(content))
			return "";
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			boolean isBreak = Character.isSpaceChar(c) || c == '\n' || c == '\t' || c == '\r' || c == ' ';
			for (int t = 0; !isBreak && t < chars.length; t++) {
				if (c == chars[t]) {
					isBreak = true;
					break;
				}
			}
			if (!isBreak) {
				buffer.append(c);
				continue;
			}
			if (buffer.length() > 0 && (i < content.length() - 1) && buffer.charAt(buffer.length() - 1) != Global.SPLITCHAR) {
				buffer.append(Global.SPLITCHAR);
			}
		}
		return buffer.toString();

	}

	public static ArrayList<String> formatLocations(String content) {
		String token = "";
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			switch (c) {
			case '→':
			case '、':
			case '。':
			case '-':
			case '/':
			case Global.SPLITCHAR:
				if (token.length() > 0) {
					result.add(token);
				}
				token = "";
				break;
			default:
				token += c;
				break;
			}
		}
		if (token.length() > 0) {
			result.add(token.trim());
		}
		return result;
	}

	public static void main(String[] args) {
		System.out.println(Normalizer.formatDateTime("2010-12-29 4:00"));
		System.out.println(Normalizer.formatTimes("工作日17:20"));
		System.out.println(Normalizer.formatTimes("2010-11-26 16:00"));
		System.out.println(Normalizer.formatTimes("周日或周一早6:00"));

		System.out.println(Normalizer.splitContent("上下班拼车 (小轿车)", '(', ')'));
		Normalizer.formatLocations("机场高速-北二环-广惠高速-新塘永和");

	}

}
