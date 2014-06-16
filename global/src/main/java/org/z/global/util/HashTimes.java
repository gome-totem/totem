package org.z.global.util;

/**
 * @author xiaoming@yundiz.com
 * @version 创建时间：2013-3-27 下午12:21:35
 */

public class HashTimes {

	public static int use33(String key) {
		int hashCode = 0;
		int i = 0;
		int len = key.length();
		for (; i < len; i++) {
			hashCode = ((hashCode * 33) + key.codePointAt(i)) & 0x7fffffff;
		}
		return hashCode;
	}

	public static int use37(String key) {
		return 0;
	}

	public static void main(String[] args) {

		for (int i = 0; i < 100; i++) {
			System.out.println(i + "=" + HashTimes.use33(String.valueOf(i)) % 5);
		}
	}
}
