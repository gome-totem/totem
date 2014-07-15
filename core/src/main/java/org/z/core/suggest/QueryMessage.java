package org.z.core.suggest;

import java.util.*;

public class QueryMessage {
	// 基本信息
	String mName = null;
	String mParent = null;
	int mWeight = 0;
	int mType = 0; // -1 预留 0 无限定{无限定才走缓存} 1国家 2 城市
	// 汉词拼音
	public Vector<String> mPinyin = new Vector<String>();
}