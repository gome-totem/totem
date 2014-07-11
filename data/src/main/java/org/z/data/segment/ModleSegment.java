package org.z.data.segment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.environment.Const;
import org.z.global.util.StringUtil;


public class ModleSegment {
	protected static Logger logger = LoggerFactory.getLogger(ModleSegment.class);
	public static Set<String> mModleSet = new HashSet<String>();
	public static Set<String> mBrandSet = new HashSet<String>();

	public ModleSegment() {
	}

	static {
		init();
	}

	public static void init() {
		try {
			String encoding = "utf-8";
			
			File file = new File(StringUtil.append(Const.DictRootPath, "/modle.txt"));
			if (file.isFile() && file.exists()) {
				// 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				bufferedReader.readLine();
				while ((lineTxt = bufferedReader.readLine()) != null) {
					mModleSet.add(lineTxt);
				}
				read.close();

			} else {
				logger.info("找不到指定的文件");
			}
		} catch (Exception e) {
			logger.info("读取文件内容出错");
			e.printStackTrace();
		}
		
		
		try {
			String encoding = "utf-8";
			
			File file = new File(StringUtil.append(Const.DictRootPath, "/brand.txt"));
			if (file.isFile() && file.exists()) {
				// 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				bufferedReader.readLine();
				while ((lineTxt = bufferedReader.readLine()) != null) {
					mBrandSet.add(lineTxt);
				}
				
				read.close();

			} else {
				logger.info("找不到指定的文件");
			}
		} catch (Exception e) {
			logger.info("读取文件内容出错");
			e.printStackTrace();
		}
	}

	// 英文和数字混合情况下，如果分割
	private static String[] segEnglishAndNumber(String str) {
		String tmpStr = "";

		if (str.length() <= 2) {
			String[] retA = new String[1];
			retA[0] = str;

			return retA;
		}

		char curC = str.charAt(0);
		tmpStr += curC;
		for (int i = 0; i < str.length() - 1; i++) {
			curC = str.charAt(i);
			char nextC = str.charAt(i + 1);

			if (curC >= '0' && curC <= '9' || curC == '.') {
				if (nextC >= '0' && nextC <= '9' || nextC == '.') {
					tmpStr += nextC;
				} else {
					tmpStr += " ";
					tmpStr += nextC;
				}
			}

			if (curC >= 'a' && curC <= 'z') {
				if (nextC >= 'a' && nextC <= 'z') {
					tmpStr += nextC;
				} else {
					tmpStr += " " + nextC;
				}
			}
		}

		String[] tmpArr = tmpStr.split(" ");
		Vector<String> retVec = new Vector<String>();

		String oneStr = tmpArr[0];
		if (tmpArr.length == 1) {
			retVec.add(oneStr);
		} else {
			for (int i = 1; i < tmpArr.length; i++) {
				if (oneStr.length() <= 1) {
					// 前一字符串或当前字符串长度小于2
					oneStr += tmpArr[i];
					retVec.add(oneStr);
					oneStr = tmpArr[i];
				} else if (tmpArr[i].length() <= 1) {
					retVec.add(oneStr);
					oneStr += tmpArr[i];
					retVec.add(oneStr);
					oneStr = tmpArr[i];
				} else {
					retVec.add(oneStr);
					oneStr = tmpArr[i];
				}
			}

			String endStr = tmpArr[tmpArr.length - 1];
			if (endStr.length() > 1) {
				retVec.add(endStr);
			}
		}

		String[] retArr = new String[retVec.size()];
		for (int i = 0; i < retVec.size(); i++) {
			retArr[i] = retVec.elementAt(i);
		}
		return retArr;
	}

	private static Vector<String> segmentPossible(String modleStr) {
		modleStr = modleStr.toLowerCase();
		for (int i = 0; i < modleStr.length(); i++) {
			char tmpC = modleStr.charAt(i);
			if ((tmpC >= '0' && tmpC <= '9') || (tmpC >= 'a' && tmpC <= 'z')) {
				modleStr = modleStr.substring(i, modleStr.length());
				break;
			}
		}

		modleStr = modleStr.replaceAll("/", " ");
		modleStr = modleStr.replace("-", " ");
		String[] strArr = modleStr.split(" ");
		Vector<String> mVec = new Vector<String>();
		for (int i = 0; i < strArr.length; i++) {
			if (strArr[i] == "") {
				continue;
			}

			String[] segArr = segEnglishAndNumber(strArr[i]);
			for (int j = 0; j < segArr.length; j++) {
				mVec.add(segArr[j]);
			}
		}

		return mVec;
	}

	// 依据规则全切分：只返回有用的信息
	public static String segmentModleAll(String modleStr) {
		StringBuilder sb = new StringBuilder();
		Vector<String> posVec = segmentPossible(modleStr);
		for (int i = 0; i < posVec.size(); i++) {
			String tmpStr = posVec.elementAt(i);
			if (mModleSet.contains(tmpStr)) {
				if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(tmpStr);
			}
		}

		return sb.toString();
	}

	// 常规的前向最大切分
	public static Set<String> segmentMaxFront(String modleStr) {
		Set<String> retVec = new HashSet<String>();
		modleStr = modleStr.toLowerCase();
		modleStr.replaceAll("-", " ");
		modleStr.replaceAll("/", " ");
		String[] natureArr = modleStr.split(" ");

		for (int i = 0; i < natureArr.length; i++) {
			String tmpStr = natureArr[i];
			for (int j = 0; j < tmpStr.length() - 1; j++) {
				for (int k = tmpStr.length(); k >= j + 2; k--) {
					String possibleModle = tmpStr.substring(j, k);
					if (mBrandSet.contains(possibleModle)) {
						retVec.add(possibleModle);
						j = k;
						break;
					}
				}
			}
		}
		
		if(retVec.size() == 0)
		{
			for (int i = 0; i < natureArr.length; i++) {
				String tmpStr = natureArr[i];
				for (int j = 0; j < tmpStr.length() - 1; j++) {
					for (int k = tmpStr.length(); k >= j + 2; k--) {
						String possibleModle = tmpStr.substring(j, k);
						if (mModleSet.contains(possibleModle)) {
							retVec.add(possibleModle);
							j = k;
							break;
						}
					}
				}
			}
		}

		return retVec;
	}

	public static class HzPySeg {
		public String hz = "";
		public String py = "";
	}

	private static boolean isModle(char code) {
		if (code >= '0' && code <= '9' || code == '.') {
			return true;
		}

		if (code >= 'a' && code <= 'z') {
			return true;
		}

		return false;
	}

	public static HzPySeg segHzAndPy(String query) {
		query = query.toLowerCase();
		String pyStr = "";
		String hzStr = "";
		for (int i = 0; i < query.length(); i++) {
			char tmpC = query.charAt(i);
			if (isModle(tmpC)) {
				pyStr += tmpC;
				int len = hzStr.length();
				if (len == 0 || hzStr.charAt(len - 1) == ' ') {
					continue;
				} else {
					hzStr += " ";
				}
			} else {
				hzStr += tmpC;
				int len = pyStr.length();
				if (len == 0 || pyStr.charAt(len - 1) == ' ') {
					continue;
				} else {
					pyStr += " ";
				}
			}
		}

		// 拼音去除'.'号
		int begP = 0;
		for (int i = 0; i < pyStr.length(); i++) {
			char tmp = pyStr.charAt(i);
			if (tmp != '.') {
				begP = i;
				break;
			}
		}

		int endP = pyStr.length() - 1;
		for (; endP > 0; endP--) {
			char tmp = pyStr.charAt(endP);
			if (tmp != '.') {
				break;
			}
		}

		pyStr = pyStr.substring(begP, endP + 1);
		char[] tmpArr = pyStr.toCharArray();

		if (tmpArr.length > 2) {
			pyStr = "";

			char preC = tmpArr[0];
			char nextC = tmpArr[2];
			pyStr += preC;
			for (int i = 1; i < tmpArr.length - 1; i++) {
				preC = tmpArr[i - 1];
				nextC = tmpArr[i + 1];
				if (tmpArr[i] == '.') {
					if (preC >= '0' && preC <= '9' && nextC >= '0' && nextC <= '9') {
						tmpArr[i] = '#';
					}
				}

				pyStr += tmpArr[i];
			}

			pyStr += tmpArr[tmpArr.length - 1];
		} else {
			pyStr = pyStr.replace(".", "");
		}

		HzPySeg ret = new HzPySeg();
		pyStr = pyStr.replace(".", "");
		pyStr = pyStr.replace("#", ".");
		ret.hz = hzStr;
		ret.py = pyStr;

		return ret;
	}

	public static class mSegTerm
	{
		public String mStr;//切分出来的串
		public int mLevel;//1 词库有该词  2 词库没有该词 
	}
	
	static String[] guessBrand(String tmpStr)
	{
		if(tmpStr.length() <= 2)
		{
			String[] Arr = new String[1];
			Arr[0] = tmpStr;
			return Arr;
		}
		
		HashSet<String> tmpSet = new HashSet<String>();
		String[] tmpArr = {"500g","250g","32g","16g","8g","4g","2g", "1g"};
		//步骤一：猜测内存型号
		if(tmpStr.contains("g"))
		{
			for(int i=0; i<tmpArr.length; i++)
			{
				if(tmpStr.contains(tmpArr[i]))
				{
					tmpSet.add(tmpArr[i]);
					tmpStr = tmpStr.replaceAll(tmpArr[i], " ");
				}
			}
		}
		
		//步骤二：猜测品牌型号 前向最大匹配
		for (int j = 0; j < tmpStr.length() - 1; j++) {
			for (int k = tmpStr.length(); k >= j + 2; k--) {
				String possibleBrand = tmpStr.substring(j, k);
				if (mBrandSet.contains(possibleBrand)) {
					tmpSet.add(possibleBrand);
					
					tmpStr = tmpStr.replaceAll(possibleBrand, " ");
					j = k;
					break;
				}
			}
		}
		
		String[] leftArr = tmpStr.split(" ");
		for(int i=0; i<leftArr.length; i++)
		{
			if(leftArr[i] != null && leftArr[i] != "" && leftArr[i].length() > 0)
			{
				tmpSet.add(leftArr[i]);
			}
		}
		
		String[] gArr = new String[tmpSet.size()];
		int index=0;
		for(String tS:tmpSet)
		{
			gArr[index++] = tS;
		}
		return gArr;
	}
	
	//依据规则全切分：只返回有用的信息
	@SuppressWarnings("unused")
	public static Set<mSegTerm> NewSegmentModleAll(String modleStr)
	{
		modleStr = modleStr.toLowerCase();
		HashSet<String> tmpSet = new HashSet<String>();
		String[] tmpArr = {"500g","250g","32g","16g","8g","4g","2g", "1g", "i7", "i5", "i3"};
		//步骤一：猜测内存型号
		if(modleStr.contains("g"))
		{
			for(int i=0; i<tmpArr.length; i++)
			{
				if(modleStr.contains(tmpArr[i]))
				{
					tmpSet.add(tmpArr[i]);
					modleStr = modleStr.replaceAll(tmpArr[i], " ");
				}
			}
		}
		
		//步骤二：猜测品牌型号 前向最大匹配
		for (int j = 0; j < modleStr.length() - 1; j++) {
			for (int k = modleStr.length(); k >= j + 2; k--) {
				String possibleBrand = modleStr.substring(j, k);
				
//				if (mBrandSet.contains(possibleBrand)||mModleSet.contains(possibleBrand)) {
				if (mBrandSet.contains(possibleBrand)) {
					tmpSet.add(possibleBrand);
					
					modleStr = modleStr.replaceAll(possibleBrand, " ");
					break;
				}
			}
		}
		
		
		//步骤三： 对型号进行正常切分
		Set<mSegTerm> retSegSet = new HashSet<mSegTerm>();
		Set<String> retVec = new HashSet<String>();
		Set<String> noVec = new HashSet<String>();//未找到的
		Set<String> guessSet = new HashSet<String>();
		Vector<String> posVec =  segmentPossible(modleStr);
		for(int i=0; i<posVec.size(); i++)
		{
			String tmpStr = posVec.elementAt(i);
			if(mModleSet.contains(tmpStr))
			{
				retVec.add(tmpStr);
			}			
			else
			{
				noVec.add(tmpStr);
			}
		}
		
		
		//步骤四：收集切分结果
		Iterator<String> iter = retVec.iterator();
		while(iter.hasNext())
		{
			String str = iter.next();
			mSegTerm term = new mSegTerm();
			term.mStr = str;
			term.mLevel = 1;
			retSegSet.add(term);
		}
		
		for(String tmpStr: noVec)
		{
			String str = tmpStr;
			mSegTerm term = new mSegTerm();
			term.mStr = str;
			term.mLevel = 2;
			retSegSet.add(term);
		}
		
		for(String tmpStr: tmpSet)
		{
			String str = tmpStr;
			mSegTerm term = new mSegTerm();
			term.mStr = str;
			term.mLevel = 1;
			retSegSet.add(term);
		}
		
		return retSegSet;
	}
	
//	public static void main(String[] cmdline) {
//		String query = "威运高（vivanco）iphone3g游戏手柄";
//
//		HzPySeg ret = ModleSegment.segHzAndPy(query);
//
//		logger.info(query);
//		logger.info(ret.hz);
//		logger.info(ret.py);
//
//		String segmentModleAll = ModleSegment.segmentModleAll(ret.py);
//		logger.info(segmentModleAll);
//	}
	
	
	@SuppressWarnings("static-access")
	public static void main(String[] cmdline)
	{
		String query = "iphone4s";
		
		ModleSegment ms = new ModleSegment();
		HzPySeg ret = ms.segHzAndPy(query);
		
		logger.info(query);
		logger.info(ret.hz);
		logger.info(ret.py);
				
				
		//ms.init();
		
		Set<mSegTerm> retSet = ms.NewSegmentModleAll(ret.py);
		
//		Set<String> retSet = ms.segmentModleAll(ret.mPyStr);
		
//		Set<String> retSet = ms.segmentMaxFront(ret.mPyStr);
		Iterator<mSegTerm> iter = retSet.iterator();
		while(iter.hasNext())
		{
			mSegTerm tmpStr = iter.next();
			logger.info(tmpStr.mStr + " : " + tmpStr.mLevel);
		}
	}

}
