package com.x.server.test;

public class TestExtend {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Child child = new Child() ;
		for (byte b : child.bytes) {
			System.out.println(b);
		}
	}

}
