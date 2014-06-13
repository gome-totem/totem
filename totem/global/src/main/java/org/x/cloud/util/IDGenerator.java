package org.x.cloud.util;

/**
 * 
 * @author 蒋礼俊
 * @version 2013-10-16下午5:16:20
 */
public class IDGenerator {

	private IDGenerator() {
	}

	public static String generate(String lastId) {
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
		String id = "000";
		long i = 0;
		while (true) {
			id = IDGenerator.generate(id);
			System.out.println(id);
			if (id.equals("000")) {
				break;
			}
			i++;
		}
		System.out.println(i);
	}
}
