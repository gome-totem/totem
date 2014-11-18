package com.gome.totem.sniper.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class Test {

	/**
	 * @param args
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {

		String test = "key_default_value%0A%09%09%09%09%09%09%09%09%E4%BF%9D%E6%B8%A9%E6%9D%AF";

		System.out.println(URLDecoder.decode(test, "UTF-8"));

	}

}
