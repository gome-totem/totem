package org.z.core.suggest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.environment.Const;
import org.z.global.util.PinyinUtil;
import org.z.store.mongdb.DataSet;

import com.mongodb.BasicDBList;

public class Writer {
	protected static Logger logger = LoggerFactory.getLogger(Writer.class);
	protected static Conf mConf = new Conf();
	public static DoubleArray mDA = null;// query双数组
	public static DoubleArray mDAPY = null;// query的拼音双数组
	protected static Vector<Pair<String, QueryMessage>> Querys = null;
	protected static HashMap<String, Vector<Pair<String, QueryMessage>>> mPyToHzMap = new HashMap<String, Vector<Pair<String, QueryMessage>>>();// 拼音到汉字的影射

	// 工作内容：
	// 1. 读取配置文件
	// 2. 生成中文双数组
	// 3. 构建词典
	// 4. 生成拼音双数组
	public static void init() {
		// 步骤一：中文双数组构建
		mDA = new DoubleArray(Querys);
		mDA.CreateDoubleArray();

		// 步骤二：构建拼音双数组基础数据结构
		Vector<Pair<String, QueryMessage>> QuerysPY = new Vector<Pair<String, QueryMessage>>();// 构建拼音访问树组
		@SuppressWarnings("rawtypes")
		Iterator iter = mPyToHzMap.entrySet().iterator();
		while (iter.hasNext()) {
			@SuppressWarnings("unchecked")
			Map.Entry<String, Vector<Pair<String, QueryMessage>>> entry = (Map.Entry<String, Vector<Pair<String, QueryMessage>>>) iter.next();
			String key = entry.getKey();
			QueryMessage tmpQM = new QueryMessage();
			tmpQM.mName = key;
			tmpQM.mParent = "";
			tmpQM.mType = -1;
			Pair<String, QueryMessage> aP = new Pair<String, QueryMessage>(key, tmpQM);
			QuerysPY.add(aP);
		}

		// 步骤三：构建拼音双数组
		mDAPY = new DoubleArray(QuerysPY);
		mDAPY.CreateDoubleArray();
	}

	static private Vector<Pair<String, QueryMessage>> getQueryMessageFromDB() {
		Vector<Pair<String, QueryMessage>> kqVec = new Vector<Pair<String, QueryMessage>>();
		String countrySql = "select country,continent,weight from country_dict";
		String citySql = "select city,country,weight from city_dict";
		String[][] rows = DataSet.query("dict", "yiqihi", countrySql, 0, 0);

		HashSet<String> singleNameSet = new HashSet<String>();
		for (int i = 0; i < rows.length; i++) {
			QueryMessage qm = new QueryMessage();
			qm.mName = rows[i][0];
			qm.mParent = rows[i][1];
			qm.mWeight = Integer.parseInt(rows[i][2]);
			qm.mType = 1;

			if (singleNameSet.contains(qm.mName)) {
				continue;
			} else {
				singleNameSet.add(qm.mName);
			}

			kqVec.add(new Pair<String, QueryMessage>(qm.mName, qm));
		}

		rows = DataSet.query("dict", "yiqihi", citySql, 0, 0);

		for (int i = 0; i < rows.length; i++) {
			QueryMessage qm = new QueryMessage();
			qm.mName = rows[i][0];
			qm.mParent = rows[i][1];
			qm.mWeight = Integer.parseInt(rows[i][2]);
			qm.mType = 2;

			if (qm.mName.contentEquals("巴黎")) {
				System.out.println(rows[i][2]);
			}

			qm.mName = qm.mName.replaceAll(" ", "");

			if (singleNameSet.contains(qm.mName)) {
				continue;
			} else {
				singleNameSet.add(qm.mName);
			}

			kqVec.add(new Pair<String, QueryMessage>(qm.mName, qm));
		}

		for (int i = 0; i < kqVec.size(); i++) {
			// 写中文文件
			QueryMessage qm = kqVec.elementAt(i).second;

			// 写拼音文件
			String[] pyS = PinyinUtil.toSpell(qm.mName);

			if (pyS[0].equals("") || pyS[1].equals("")) {
				logger.info(qm.mName + "拼音有空");
				continue;
			}

			Pair<String, QueryMessage> hzP = new Pair<String, QueryMessage>(qm.mName, qm);
			for (int j = 0; j < pyS.length; j++) {
				String py = pyS[j];
				if (mPyToHzMap.containsKey(py)) {
					mPyToHzMap.get(py).add(hzP);
				} else {
					Vector<Pair<String, QueryMessage>> tmpVec = new Vector<Pair<String, QueryMessage>>();
					tmpVec.add(hzP);
					mPyToHzMap.put(py, tmpVec);
				}
			}
		}

		return kqVec;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void createPyToHzQuerymessageHash() {
		// HashMap<String,Vector<Pair<String,QueryMessage> > > mPyToHzMap
		// 步骤一：创建数据文件
		String filepath = Const.ConfigPath + "/suggest/pytohzmap.data";
		File file = new File(filepath);
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}

		if (file.exists())
			file.delete();

		try {
			file.createNewFile();
		} catch (IOException e) {
			logger.error("createNewFile error", e);
		}

		// 步骤二：往数据文件中写数据
		FileOutputStream tmpf = null;
		try {
			tmpf = new FileOutputStream(filepath, true);
			tmpf.write("拼音	词汇一	词汇二	...	词汇n".getBytes());
			Iterator iter = mPyToHzMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, Vector<Pair<String, QueryMessage>>> entry = (Map.Entry<String, Vector<Pair<String, QueryMessage>>>) iter.next();
				String py = entry.getKey();
				Vector<Pair<String, QueryMessage>> hzQmVec = entry.getValue();
				String linetxt = "\n" + py;
				for (int i = 0; i < hzQmVec.size(); i++) {
					linetxt += "\t";
					linetxt += hzQmVec.elementAt(i).first;
				}

				tmpf.write(linetxt.getBytes());
			}
			tmpf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("creat file error", e);
		}
	}

	// 说明： fileprefix = "hz" 创建原始query的双数组相关文件
	// fileprefix = "py" 创建query的拼音相关的双数组文件
	@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
	private static void createfile(String fileprefix) {
		DoubleArray da = null;
		String prePath = Const.ConfigPath + "/suggest/";
		boolean isHz = false;
		if (fileprefix.equals("hz")) {
			da = mDA;
			isHz = true;
		} else {
			da = mDAPY;
		}

		// 创建相关文件
		String[] hzfileS = { "doubleArray", "query", "charmap", "headpos", "trieArray", "headinter" };

		try {
			for (int i = 0; i < hzfileS.length; i++) {
				String filepath = prePath + fileprefix + hzfileS[i] + ".data";
				File file = new File(filepath);
				File parent = file.getParentFile();
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}

				if (file.exists())
					file.delete();

				file.createNewFile();

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("create file error", e);
		}

		// 创建doubleArray文件==========================================================================================================================
		String filePath = prePath + fileprefix + "doubleArray.data";
		FileOutputStream tmpf = null;
		try {
			tmpf = new FileOutputStream(filePath, true);

			String headS = "Base數組	check數組	position數組	子節點開始位置	子節點節俗位置	對應的trie樹位置";
			tmpf.write(headS.getBytes());
			String valueStr = "";
			for (int i = 0; i < da.Base.length; i++) {
				// int tmpInt = da.Base[i];
				tmpf.write("\n".getBytes());
				valueStr = da.Base[i] + "\t" + da.Check[i] + "\t" + da.Position[i] + "\t" + da.ChildStart[i] + "\t" + da.ChildEnd[i] + "\t" + da.TriePos[i];
				tmpf.write(valueStr.getBytes());
			}
			tmpf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("create file error", e);
		}

		// 创建query==============================================================================================================
		try {
			filePath = prePath + fileprefix + "query.data";
			tmpf = new FileOutputStream(filePath, true);
			tmpf.write("query信息".getBytes());

			for (int i = 0; i < da.mQuerys.size(); i++) {
				QueryMessage qm = da.mQuerys.elementAt(i).second;
				String tmpStr = qm.mName + '\t' + qm.mParent + "\t" + qm.mType + "\t" + qm.mWeight;

				tmpf.write("\n".getBytes());
				tmpf.write(tmpStr.getBytes());
			}
			tmpf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("create file error", e);
		}

		// 创建charmap============================================================================================================
		Iterator iter = da.charMap.entrySet().iterator();
		try {
			filePath = prePath + fileprefix + "charmap.data";
			tmpf = new FileOutputStream(filePath, true);

			tmpf.write("每个字符对应的编码信息".getBytes());
			while (iter.hasNext()) {
				Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iter.next();
				int key = entry.getKey();
				int value = entry.getValue();

				String linetxt = key + "\t" + value;
				tmpf.write("\n".getBytes());
				tmpf.write(linetxt.getBytes());
			}

			tmpf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("create file error", e);
		}

		// 创建headpos============================================================================================================
		try {
			filePath = prePath + fileprefix + "headpos.data";
			tmpf = new FileOutputStream(filePath, true);

			iter = da.mHeadPos.entrySet().iterator();
			tmpf.write("双数组的头节点对应的位置".getBytes());
			while (iter.hasNext()) {
				Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iter.next();
				int key = entry.getKey();
				int value = entry.getValue();

				String linetxt = key + "\t" + value;
				tmpf.write("\n".getBytes());
				tmpf.write(linetxt.getBytes());
			}
			tmpf.close();
		} catch (IOException e) {
			logger.error("create file error", e);
		}

		// "triepos","trieindex","triebegin","trieend","trie"
		// 创建base文件
		try {
			filePath = prePath + fileprefix + "trieArray.data";
			tmpf = new FileOutputStream(filePath, true);
			String headS = "trieBegin" + "\t" + "trieEnd" + "\t" + "trieindex";
			tmpf.write(headS.getBytes());
			String valueStr = "";
			for (int i = 0; i < da.mTrie.mIndex.size(); i++) {
				tmpf.write("\n".getBytes());
				valueStr = da.mTrie.mBegin.elementAt(i) + "\t" + da.mTrie.mEnd.elementAt(i) + "\t" + da.mTrie.mIndex.elementAt(i);
				tmpf.write(valueStr.getBytes());
			}
			tmpf.close();
		} catch (IOException e) {
			logger.error("create file error", e);
		}
		try {
			filePath = prePath + fileprefix + "headinter.data";
			tmpf = new FileOutputStream(filePath, true);
			String headS = "word" + "\t" + "midpos" + "\t" + "......" + "\t" + "midpos";
			tmpf.write(headS.getBytes());
			Iterator it = da.mHeadInter.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, Vector<Integer>> entry = (Map.Entry<Integer, Vector<Integer>>) it.next();
				String valueStr = "\n" + entry.getKey();
				Vector<Integer> tVec = entry.getValue();
				for (int i = 0; i < tVec.size(); i++) {
					valueStr += "\t";
					valueStr += tVec.elementAt(i);
				}

				tmpf.write(valueStr.getBytes());
			}

			tmpf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("create file error", e);
		}
	}

	public static void createDataFileForSuggest() {
		// 创建拼音和汉字的对应信息
		createPyToHzQuerymessageHash();
		// 创建汉字双数组文件
		createfile("hz");
		// 创建拼音双数组文件
		createfile("py");
	}

	static boolean isAllPinyinOrDigit(String inStr) {
		if (inStr.matches("[0-9a-zA-Z]*") == true) {
			return true;
		}

		return false;
	}

	// 插入排序: destVec 目标集合
	// maxNum 目标集合最大数目
	static void insertSortForQueryMessage(Vector<QueryMessage> destVec, QueryMessage qm, int maxNum) {
		if (destVec.size() >= maxNum)// 按照逻辑其实只需要==就可以了
		{
			QueryMessage endQM = destVec.elementAt(maxNum - 1);
			if (endQM.mWeight >= qm.mWeight) {
				return;
			} else {
				destVec.remove(maxNum - 1);
			}
		}

		int insertPos = 0;
		for (insertPos = 0; insertPos < destVec.size(); insertPos++) {
			if (destVec.elementAt(insertPos).mWeight < qm.mWeight) {
				// 找到插入点
				break;
			}
		}

		destVec.insertElementAt(qm, insertPos);
	}

	// DoubleArray进行状态转换，Trie树进行查询
	// 返回结果： null 表示没有找到匹配的query
	@SuppressWarnings("unused")
	static Vector<QueryMessage> NewSearchSuggestQuery(String inStr) {
		// 步骤一：对输入进行小写转化,初始化插入最大值
		inStr = inStr.toLowerCase();
		inStr = inStr.replaceAll(" ", "");

		// 步骤二： 测试中文前缀匹配搜索
		Vector<QueryMessage> retMessageVec = new Vector<QueryMessage>();// 返回结果集合
		HashSet<String> resultSet = new HashSet<String>();// 防止重复

		Vector<Integer> resultHzPrefix = mDA.NewFindWordsByPrefix(inStr);// 中文前缀结果
		// 或却QueryMessage信息*****************

		if (resultHzPrefix != null) {
			// 获取结果并进行排序*****************
			for (int i = 0; i < resultHzPrefix.size(); i++) {
				int index = resultHzPrefix.elementAt(i);
				QueryMessage qM = mDA.mQuerys.elementAt(index).second;
				QueryMessage tM = new QueryMessage();
				tM.mName = qM.mName;
				tM.mParent = qM.mParent;
				tM.mPinyin = qM.mPinyin;
				tM.mType = qM.mType;
				tM.mWeight = qM.mWeight * 1000;
				if (resultSet.contains(tM.mName))
					continue;
				else
					resultSet.add(tM.mName);

				insertSortForQueryMessage(retMessageVec, tM, mConf.mReturnNum);
				// hzMessagePrefix.add(qM);
			}

			if (retMessageVec.size() >= mConf.mReturnNum) {
				return retMessageVec;
			}
		}

		// 步骤三：测试中文中间匹配搜索
		if (isAllPinyinOrDigit(inStr) == false)// 含有中文才进行中间匹配
		{
			Vector<Integer> resultHzMiddle = mDA.NewFindWordsBySubstringNotPrefix(inStr, mConf.mMiddleNum);// 中文前缀结果
			if (resultHzMiddle != null) {
				// 对中间匹配搜索结果进行排序并将结果插入返回集合，同时注意避免与以前搜索结果的重复resultSet******************
				// 获取结果并进行排序*****************
				for (int i = 0; i < resultHzMiddle.size(); i++) {
					int index = resultHzMiddle.elementAt(i);
					QueryMessage qM = mDA.mQuerys.elementAt(index).second;
					QueryMessage tM = new QueryMessage();
					tM.mName = qM.mName;
					tM.mParent = qM.mParent;
					tM.mPinyin = qM.mPinyin;
					tM.mType = qM.mType;
					tM.mWeight = qM.mWeight;
					if (resultSet.contains(tM.mName))
						continue;
					else
						resultSet.add(tM.mName);

					insertSortForQueryMessage(retMessageVec, tM, mConf.mReturnNum);
				}

				if (retMessageVec.size() >= mConf.mReturnNum) {
					return retMessageVec;
				}
			}
		}

		// 如果输入全是拼音就进行拼音搜索否则就返回
		if (isAllPinyinOrDigit(inStr) == false) {
			if (retMessageVec.size() == 0) {
				return null;
			}

			return retMessageVec;
		}

		HashSet<Integer> pySet = new HashSet<Integer>();

		// 步骤五：拼音搜索前缀
		Vector<QueryMessage> pyMessagePrefix = new Vector<QueryMessage>();
		Vector<Integer> resultPyPrefix = new Vector<Integer>();
		Vector<Integer> tmpPrefix = mDAPY.NewFindWordsByPrefix(inStr);// 只用最长的拼音
		if (tmpPrefix != null) {
			for (int j = 0; j < tmpPrefix.size(); j++) {
				int index = tmpPrefix.elementAt(j);
				if (pySet.contains(index) == false) {
					resultPyPrefix.add(index);
					pySet.add(index);
				}
			}
		}

		if (resultPyPrefix.size() > 0) {
			// 获取pyMessagePrefix,对pyMessagePrefix结果进行排序,同时注意避免与以前搜索结果的重复resultSet******************
			for (int i = 0; i < resultPyPrefix.size(); i++) {
				int index = resultPyPrefix.elementAt(i);
				String pyTmp = mDAPY.mQuerys.elementAt(index).first;// 获取一个匹配的拼音
				Vector<Pair<String, QueryMessage>> hzVec = mPyToHzMap.get(pyTmp);
				for (int j = 0; j < hzVec.size(); j++) {
					QueryMessage qM = hzVec.elementAt(j).second;
					QueryMessage tM = new QueryMessage();
					tM.mName = qM.mName;
					tM.mParent = qM.mParent;
					tM.mPinyin = qM.mPinyin;
					tM.mType = qM.mType;
					tM.mWeight = qM.mWeight * 1000;
					if (resultSet.contains(tM.mName))
						continue;
					else
						resultSet.add(tM.mName);

					insertSortForQueryMessage(retMessageVec, tM, mConf.mReturnNum);
				}
			}

			if (retMessageVec.size() >= mConf.mReturnNum) {
				return retMessageVec;
			}
		}

		// 步骤六：拼音中间匹配搜索
		Vector<QueryMessage> pyMessageMiddle = new Vector<QueryMessage>();
		Vector<Integer> resultPyMiddle = new Vector<Integer>();

		Vector<Integer> tmpMidPrefix = mDAPY.NewFindWordsBySubstringNotPrefix(inStr, mConf.mMiddleNum);

		if (resultPyMiddle.size() > 0) {
			// 获取pyMessagePrefix,对pyMessagePrefix结果进行排序,同时注意避免与以前搜索结果的重复resultSet******************
			for (int i = 0; i < resultPyMiddle.size(); i++) {
				int index = resultPyMiddle.elementAt(i);
				String pyTmp = mDAPY.mQuerys.elementAt(index).first;// 获取一个匹配的拼音
				Vector<Pair<String, QueryMessage>> hzVec = mPyToHzMap.get(pyTmp);
				for (int j = 0; j < hzVec.size(); j++) {
					QueryMessage qM = hzVec.elementAt(j).second;
					QueryMessage tM = new QueryMessage();
					tM.mName = qM.mName;
					tM.mParent = qM.mParent;
					tM.mPinyin = qM.mPinyin;
					tM.mType = qM.mType;
					tM.mWeight = qM.mWeight;
					if (resultSet.contains(tM.mName))
						continue;
					else
						resultSet.add(tM.mName);

					insertSortForQueryMessage(retMessageVec, tM, mConf.mReturnNum);
				}
			}
		}

		if (retMessageVec.size() == 0) {
			return null;
		}

		return retMessageVec;
	}

	// 返回null表示没有搜索结果
	static public BasicDBList NewGetJsonStringBySearchString(String inStr) {
		inStr = inStr.toLowerCase();
		inStr = inStr.replaceAll(" ", "");

		Vector<QueryMessage> qmVec = NewSearchSuggestQuery(inStr);

		// 步骤一：判断是否有结果
		// [ [ ["类别名称",catId，产品数目] [] ] [
		// {"key","产品数目,是否有店铺信息,店铺名词,店铺id,店铺url,logoUrl"} {} {} {} {} {} {} ] ]
		BasicDBList root = new BasicDBList();
		if (qmVec == null) {
			return root;
		}

		for (int i = 0; i < qmVec.size(); i++) {
			BasicDBList tmpQ = new BasicDBList();
			QueryMessage qm = qmVec.elementAt(i);

			tmpQ.add(qm.mName);
			tmpQ.add(qm.mParent);
			tmpQ.add(qm.mType);

			root.add(tmpQ);
		}
		return root;
	}

	private static Vector<String> getSubKeys(String key, int num) {
		int count = num;
		Vector<String> keyVec = new Vector<String>();

		if (key.length() < num) {
			count = key.length();
		}

		for (int i = 1; i <= count; i++) {
			String tmpStr = key.substring(0, i);
			keyVec.add(tmpStr);
		}

		return keyVec;
	}

	public static void NewCreateCacheFile() {
		// 步骤一：获取短串...........................................................................
		HashSet<String> cacheKeys = new HashSet<String>();
		for (int i = 0; i < mDA.mQuerys.size(); i++) {
			String key = mDA.mQuerys.elementAt(i).first;

			Vector<String> keyVec = getSubKeys(key, mConf.mHzLen);

			for (int j = 0; j < keyVec.size(); j++) {
				String oneKey = keyVec.elementAt(j);
				if (cacheKeys.contains(oneKey) == false) {
					cacheKeys.add(oneKey);
				}
			}
		}

		for (int i = 0; i < mDAPY.mQuerys.size(); i++) {
			String key = mDAPY.mQuerys.elementAt(i).first;

			Vector<String> keyVec = getSubKeys(key, mConf.mPyLen);

			for (int j = 0; j < keyVec.size(); j++) {
				String oneKey = keyVec.elementAt(j);
				if (cacheKeys.contains(oneKey) == false) {
					cacheKeys.add(oneKey);
				}
			}
		}

		logger.info("The number of keys in cache is: " + cacheKeys.size());

		// 步骤二：创建缓存文件.................................................................................................
		try {
			String filepath = Const.ConfigPath + "/suggest/cache.data";
			File file = new File(filepath);
			File parent = file.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}

			if (file.exists())
				file.delete();

			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("create file error", e);
		}

		// 步骤三：访问双数组获取匹配的结果然后写到缓存文件中去..................................................
		String filePath = Const.ConfigPath + "/suggest/cache.data";
		FileOutputStream tmpf = null;
		try {
			tmpf = new FileOutputStream(filePath, true);

			tmpf.write("key	result".getBytes());
			String valueStr = "";
			Iterator<String> iterator = cacheKeys.iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				BasicDBList value = NewGetJsonStringBySearchString(key);
				if (value != null) {
					valueStr = key + "\t" + value.toString();
					tmpf.write("\n".getBytes());
					tmpf.write(valueStr.getBytes());
				}
			}
			tmpf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("create file error", e);
		}
	}

	public static boolean createAllFileForSuggest() {

		// 步骤一：获取词汇及其相关信息
		logger.info("start to create based file .......................");

		Querys = getQueryMessageFromDB();// ok

		// 步骤三：初始化中英文双数组，初始化词典
		logger.info("begin to init .......................");
		init();// *****************************************************************************

		// 步骤四：创建suggest可以直接加载文件
		logger.info("Start to create file for suggest engine..................");
		createDataFileForSuggest();// **********************************************************
		logger.info("Finish creating file..........................");

		// 步骤五：创建Suggest的缓存文件
		logger.info("Begin to create cache file..........................");
		NewCreateCacheFile();
		logger.info("Finished creating cache file..............................");

		return true;
	}

	public static void main(String[] args) {
		createAllFileForSuggest();
		System.out.println("Over!");
	}
}
