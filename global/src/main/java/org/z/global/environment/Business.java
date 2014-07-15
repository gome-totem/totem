package org.z.global.environment;

import org.z.global.dict.Global.PayCode;


public class Business {

	public enum ClassName {
		Bill, Call, Yundiz, Lookup, Root, Product, Sync, Topic, User, Activity, Appoint, Booking, Calendar, Comment, DataTable, Order, Recommend, Tuiba, SMS, Talk, TripOrder,AirTicketOrder
	}

	public enum Bill {
		read, requestWithdraw, readWithdraw, postWithdraw, buyPhoneCard, buyUserCard, addRecharge, recharge, addBill, addRecord, checkRecord, moreRecord,payUseBalance
	}

	public static void main(String[] args) {
		System.out.println(String.valueOf(Bill.addBill.ordinal()));
		System.out.println(String.valueOf(PayCode.Order.ordinal()));
	}

}
