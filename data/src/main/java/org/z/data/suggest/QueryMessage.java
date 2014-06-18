package org.z.data.suggest;

import java.util.*;

public class QueryMessage {
	//基本信息
	public String mQuery = null;//访问串
	public int mQueryNum = 0;//访问数目
	public int mProuductNum = 0;//对应的产品数目
	
	public Vector<String> mCategoryID = new Vector<String>();//类别id
	public Vector<String> mCategoryName = new Vector<String>();//类别名词
	public Vector<Integer> mCatTimes = new Vector<Integer>();//每个类别下的访问次数
	public Vector<Integer> mCatProductNum = new Vector<Integer>();//每个类别下query搜索到的产品数目
	
	//绑定的店铺信息
	public boolean mIsShopWord = false;//是否有店铺信息
	public String mShopName = null; //对应的店铺名词
	public int mShopId = 0;
	public String mShopUrl = null;//店铺对应的url
	public String mShopLogoUrl = null; //店铺对应的图标url
	
	//汉词拼音
	public Vector<String> mPinyin = new Vector<String>();;
}
