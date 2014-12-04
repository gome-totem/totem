package org.z.core.test;

public class test {
	public static void main(String[] args) {
		int[] s = {8,3,5,7,2};
		int temp =0;
		for (int i = 0; i < s.length-1; i++) {
			for (int j = s.length-1; j>i; --j) {
				if(s[j-1]>s[j]){
					temp=s[j];
					s[j]=s[j-1];
					s[j-1]=temp;
				}
			}
		}
		for (int i = s.length-1; i>0; i--) {
			for (int j = 0; j< i; j++) {
				if(s[j+1]>s[j]){
					temp=s[j];
					s[j]=s[j+1];
					s[j+1]=temp;
				}
			}
		}
		for (int i = 0; i < s.length; i++) {
			System.out.println(s[i]);
		}
		
	}

}
