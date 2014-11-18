package com.x.server.test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestTime {

	public static void main(String args[]) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		System.out.println(sdf.format(new Date()));

	}
}
