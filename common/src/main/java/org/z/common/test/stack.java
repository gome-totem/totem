package org.z.common.test;

public class stack<E> {
	private E[] element;
	private int size =0;
	@SuppressWarnings("unchecked")
	public stack(){
		element =(E[])new Object[10];
	}
    public void push(E e){
    	element[size++]=e;
    }
    public E pop(){
    	 return element[size--];
    	
    }
    
    public static void main(String[] args) {
	}
}
