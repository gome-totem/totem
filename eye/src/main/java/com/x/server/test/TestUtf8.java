package com.x.server.test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class TestUtf8 {

	public static void main(String args[]){
		String str = "%3A%22sum%28" ;
		
		try {
			System.out.println(URLDecoder.decode(str, "ISO-8859-1") ) ;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}
}
