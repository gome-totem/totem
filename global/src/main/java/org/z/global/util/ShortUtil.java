package org.z.global.util;

import java.security.MessageDigest;

public class ShortUtil {
	public static String key = "XiaoMing";
	public static String[] chars = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
			"w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
			"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
	public static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static int getCodeSize(String name, int defaultSize) {
		int size = defaultSize;
		switch (name) {
		case "contient":
			size = 1;
			break;
		case "country":
			size = 2;
			break;
		case "location":
			size = 3;
			break;
		case "priceRange":
			size = 2;
			break;
		case "serviceTag":
			size = 2;
			break;
		case "priceTag":
			size = 2;
			break;
		}
		return size;
	}

	public static String md5(String s) {
		try {
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}

	public static String execute(String string, int size, int times) {
		String hex = md5(key + string);
		String result = "";
		String subHex = hex.substring(times * 8, (times + 1) * 8);
		long idx = Long.valueOf("3FFFFFFF", 16) & Long.valueOf(subHex, 16);
		for (int k = 0; k < size; k++) {
			int index = (int) (Long.valueOf("0000003D", 16) & idx);
			result += chars[index];
			idx = idx >> size;
		}
		return result;
	}

	public static String execute(String lastId) {
		boolean flag = true;
		char[] idChars = lastId.toCharArray();
		int startInt = idChars.length - 1;
		for (int i = startInt; i >= 0; i--) {
			if (flag) {
				char idChar = idChars[i];
				switch (idChar) {
				case '9':
					idChars[i] = 'a';
					flag = false;
					break;
				case 'z':
					idChars[i] = '0';
					// flag = false;
					break;
				// case 'Z':
				// idChars[i] = '0';
				// break;
				default:
					idChars[i] = (char) ((int) idChar + 1);
					flag = false;
					break;
				}
			} else {
				break;
			}
		}
		return new String(idChars);
	}

	public static void main(String[] args) {
		for (int i = 0; i < 4; i++) {
			System.out.println(ShortUtil.execute("const", 2, i));
		}
	}

}
