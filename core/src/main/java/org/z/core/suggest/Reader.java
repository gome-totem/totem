package org.z.core.suggest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.environment.Const;

import com.mongodb.BasicDBList;
import com.mongodb.util.JSON;

//提供服务
public class Reader {
	protected static Logger logger = LoggerFactory.getLogger(Reader.class);
	protected static Conf mConf = new Conf();
	protected static boolean enableCache = false;
	protected static DoubleArray mDA = new DoubleArray();// query双数组
	protected static DoubleArray mDAPY = new DoubleArray();// query的拼音双数组
	protected static HashMap<String, String> mKeyValue = new HashMap<String, String>();
	protected static HashMap<String, Vector<Pair<String, QueryMessage>>> mPyToHzMap = new HashMap<String, Vector<Pair<String, QueryMessage>>>();// 拼音到汉字的影射

	private static void loadPyToHzMap(String filePath) {
		try {
			String encoding = "utf-8";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) {
				// 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				bufferedReader.readLine();

				// 构建query和querymessage的映射关系
				HashMap<String, Integer> queryToIndexHash = new HashMap<String, Integer>();
				for (int i = 0; i < mDA.mQueryIndex.size(); i++) {
					String key = mDA.mQueryIndex.elementAt(i).first;
					int index = mDA.mQueryIndex.elementAt(i).second;
					queryToIndexHash.put(key, index);
				}

				// mPyToHzMap
				while ((lineTxt = bufferedReader.readLine()) != null) {
					lineTxt = lineTxt.toLowerCase().trim();
					String[] pyAndHz = lineTxt.split("\t");

					Vector<Pair<String, QueryMessage>> tmpVec = new Vector<Pair<String, QueryMessage>>();
					String pyStr = pyAndHz[0];

					pyStr = pyStr.replaceAll(" ", "");

					for (int i = 1; i < pyAndHz.length; i++) {
						// public Vector<Pair<String,QueryMessage> > mQuerys =
						// new Vector<Pair<String,QueryMessage> >();//用户访问串
						// Vector<Pair<String,Integer> > mQueryIndex = new
						// Vector<Pair<String,Integer>
						// >();//query对应的mQuerys中位置信息
						String hzStr = pyAndHz[i];

						int index = queryToIndexHash.get(hzStr);
						QueryMessage tmpQm = mDA.mQuerys.elementAt(index).second;
						Pair<String, QueryMessage> tmpP = new Pair<String, QueryMessage>(hzStr, tmpQm);
						tmpVec.add(tmpP);
					}

					mPyToHzMap.put(pyStr, tmpVec);
				}
				read.close();
			} else {
				logger.info(filePath + " cannot be found");
			}
		} catch (Exception e) {
			logger.info(filePath + " is readed error");
			e.printStackTrace();
		}
	}

	private static void loadCacheFile(String pathDir) {
		// mKeyValue
		String filePath = pathDir + "cache.data";
		try {
			String encoding = "utf-8";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) {
				// 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				bufferedReader.readLine();

				// mPyToHzMap
				while ((lineTxt = bufferedReader.readLine()) != null) {
					String[] keyAndValue = lineTxt.split("\t");
					String key = keyAndValue[0];
					String value = keyAndValue[1];
					mKeyValue.put(key, value);
				}
				read.close();
			} else {
				logger.info(filePath + " cannot be found");
			}
		} catch (Exception e) {
			logger.info(filePath + " is readed error");
			e.printStackTrace();
		}
	}

	// 工作内容：
	// 1. 读取配置文件
	// 2. 生成中文双数组
	// 3. 构建词典
	// 4. 生成拼音双数组
	public static void init() {
		// 步骤二：加载query双数组
		logger.info("start to load the data for double array for hanzi ...............");
		mDA.Init(Const.ConfigPath + "/suggest/", "hz");
		logger.info("finish loading double array for hanzi");

		// 步骤三：加载拼音和query的映射信息 该步骤依赖mDA,所以必须先加载mDA
		logger.info("start to load the file  pinyin to query ...............");
		loadPyToHzMap(Const.ConfigPath + "/suggest/pytohzmap.data");
		logger.info("finish loading file");

		// 步骤四：加载query的拼音双数组
		logger.info("start to load double array for pinyin...............");
		mDAPY.Init(Const.ConfigPath + "/suggest/", "py");
		logger.info("finish loading double array for pinyin");

		// 步骤五：加载缓存文件
		logger.info("start to load cache file...................");
		loadCacheFile(Const.ConfigPath + "/suggest/");
		logger.info("finish loading cache file...................");
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
	static Vector<QueryMessage> NewSearchSuggestQuery(String inStr, int type) {
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

				if (type > 0) {
					if (type != tM.mType) {
						continue;
					}
				}

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

					if (type > 0) {
						if (type != tM.mType) {
							continue;
						}
					}

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

					if (type > 0) {
						if (type != tM.mType) {
							continue;
						}
					}
					//
					// if(tM.mName.contentEquals("巴黎"))
					// {
					// System.out.println(tM.mName + " " + tM.mWeight);
					// }
					insertSortForQueryMessage(retMessageVec, tM, mConf.mReturnNum);
				}
			}

			if (retMessageVec.size() >= mConf.mReturnNum) {
				return retMessageVec;
			}
		}

		// 步骤六：拼音中间匹配搜索
		Vector<Integer> tmpMidPrefix = mDAPY.NewFindWordsBySubstringNotPrefix(inStr, mConf.mMiddleNum);

		if (tmpMidPrefix != null) {
			// 获取pyMessagePrefix,对pyMessagePrefix结果进行排序,同时注意避免与以前搜索结果的重复resultSet******************
			for (int i = 0; i < tmpMidPrefix.size(); i++) {
				int index = tmpMidPrefix.elementAt(i);
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

					if (type > 0) {
						if (type != tM.mType) {
							continue;
						}
					}

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
	static public BasicDBList searchBy(String inStr, int type) {
		// 处理原串
		inStr = inStr.toLowerCase();
		inStr = inStr.trim();

		// 前向串口
		String priorStr = inStr;
		priorStr = priorStr.replaceAll(" ", "");

		// 反向串口
		String reverseStr = "";
		boolean hasSpace = false;

		if (inStr.contains(" ")) {
			String[] tmpArray = inStr.split(" ");

			Vector<String> tmpVec = new Vector<String>();
			for (int i = 0; i < tmpArray.length; i++) {
				if (tmpArray[0].length() > 0) {
					tmpVec.add(tmpArray[i]);
				}
			}

			if (tmpVec.size() == 2) {
				// 目前，只处理中间一个空格的情况
				reverseStr = tmpVec.elementAt(1) + tmpVec.elementAt(0);// 倒叙
				hasSpace = true;
			}
		}

		// 步骤一： 缓存查找
		int preNum = 0;// 前向查找结果数目

		if (enableCache && type == -1) {
			if (mKeyValue.containsKey(priorStr)) {
				String valueStr = mKeyValue.get(priorStr);
				BasicDBList retValue = (BasicDBList) JSON.parse(valueStr);

				int number = retValue.size();
				preNum = number;
				if ((hasSpace == false) || number >= mConf.mReturnNum - 2) {
					return retValue;
				} else {
					int revNum = 0;
					if (mKeyValue.containsKey(reverseStr)) {
						String reverseValue = mKeyValue.get(reverseStr);
						BasicDBList revBD = (BasicDBList) JSON.parse(reverseValue);

						Object right = revBD.get(1);
						revNum = ((BasicDBList) right).size();// 结果数目

						return (revNum > preNum) ? revBD : retValue;
					}
				}

				return retValue;

			} else if (hasSpace = true) {
				if (mKeyValue.containsKey(reverseStr)) {
					String reverseValue = mKeyValue.get(reverseStr);
					BasicDBList revBD = (BasicDBList) JSON.parse(reverseValue);

					return revBD;
				}
			}
		}

		// 步骤二： 双数组查找
		Vector<QueryMessage> qmVec = null;
		qmVec = NewSearchSuggestQuery(priorStr, type);
		if (qmVec == null) {
			preNum = 0;
		} else {
			preNum = qmVec.size();
		}

		if (preNum < mConf.mReturnNum - 2) {
			if (hasSpace) {
				Vector<QueryMessage> qmVecD = NewSearchSuggestQuery(reverseStr, type);
				int dNum = 0;
				if (qmVecD == null) {
					dNum = 0;
				} else {
					dNum = qmVecD.size();
				}

				if (dNum > preNum) {
					qmVec = qmVecD;
				}
			}
		}

		// 步骤三：判断是否有结果
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

	public static void main(String[] args) {
		// 步骤一：初始化中英文双数组，初始化词典
		logger.info("初始化开始... ...");
		init();// *****************************************************************************
		logger.info("初始化结束... ...");

		String searchWord = "bl";// *************************************************
		int type = 0;// -1 预留 0 无限定{无限定才走缓存} 1国家 2 城市
		// 步骤二：获取结果，json格式
		logger.info(searchWord + "搜索结果为：");

		BasicDBList jsonStr = null;

		jsonStr = searchBy(searchWord, type);// **************************
		System.out.println(jsonStr.toString());
	}
}
