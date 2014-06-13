package org.z.common.hash;

public class UserHash {
	/**
	 * 一个目录下能放多少文件
	 */
	public static int splitSize = 10000;

	public static String getHashPathById(long userId) {
		long temp = userId;
		StringBuilder path = new StringBuilder();
		while (temp > splitSize) {
			temp = (long) temp / splitSize;
			path.append("/" + temp);
		}
		return path.toString();
	}

	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			System.out.println(i + "=" + UserHash.getHashPathById(i));
		}
	}
}
