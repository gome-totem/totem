package com.x.server.test;

import java.text.DecimalFormat;

public class TestDecimal {

	protected static DecimalFormat df = new DecimalFormat("#.00");
	
	public static void main(String args[]){
		
		
		System.out.println(df.format(22));
		
	}
}
