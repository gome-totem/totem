package org.z.data.suggest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//默认传入的数据都是处理过的：
//utf-8编码  
//大写字母都转化为了小写字母
//开头必须是：数字、英文字母或汉字
//query中不能包括字符： ?    算法中特殊使用字符
public class DoubleArray {
	protected static Logger logger = LoggerFactory.getLogger(DoubleArray.class);

	public class Trie
	{
		public Vector<Integer> mTrie = new Vector<Integer>();//对应的Trie树
		public Vector<Integer> mBegin = new Vector<Integer>();//对应位置的子节点开始位置
		public Vector<Integer> mLen = new Vector<Integer>();//对应位置对应的匹配query数目
		public Vector<Integer> mEnd = new Vector<Integer>();//对应位置对应的子节点结束位置，包括该位置
		public Vector<Integer> mIndex = new Vector<Integer>();//该节点成词，对应的mQuerys下标
	}
	
	public Trie mTrie = null;
	final static int MIDDLE_MATCH_NUMBER = 30;//默认中间匹配结果最小数目
	final int DEFAULT_LEN = 1024;
	public int Base[]     = new int[DEFAULT_LEN];
	public int Check[]    = new int[DEFAULT_LEN];
	public int Position[] = new int[DEFAULT_LEN];   //指向对应的子字符串对应位置
	public int TriePos[]  = new int[DEFAULT_LEN];   //存入的字符对应Trie树中的位置，该树组用来生成双数组用	
	public int ChildStart[] = new int[DEFAULT_LEN];  //对应位置孩子节点开始地址 减少查询数目提高速度用
	public int ChildEnd[]   = new int[DEFAULT_LEN];  //对应位置孩子节点终止位置 减少查询数目提高速度用  值为-1表示没有子节点
	
 	
	int pos = 1;
		
	public Vector<Pair<String,QueryMessage> > mQuerys = new Vector<Pair<String,QueryMessage> >();//用户访问串
	Vector<Pair<String,Integer> > mQueryIndex = new Vector<Pair<String,Integer> >();//query对应的mQuerys中位置信息
	public Map<Integer, Integer> charMap = new HashMap<Integer, Integer>();//每个字符的编码
	public HashMap<Integer,Integer> mHeadPos = new HashMap<Integer,Integer>();//开头词对应的双数组位置，也是Trie树开头词汇对应的位置
	public HashMap<Integer,Vector<Integer> > mHeadInter = new HashMap<Integer,Vector<Integer> >();//第二列及其以后格列对应的位置
	int maxCode = 0;// 最大编码
	
	public DoubleArray(Vector<Pair<String,QueryMessage> > Querys)
	{
		mQuerys = Querys;		
	}
	
	private void Extend_Array()
	{
		Base = Arrays.copyOf(Base, Base.length*2);  
      Check = Arrays.copyOf(Check, Check.length*2); 
      Position = Arrays.copyOf(Position, Position.length*2);
      TriePos = Arrays.copyOf(TriePos, TriePos.length*2);
      
      ChildStart = Arrays.copyOf(ChildStart, ChildStart.length*2);
      ChildEnd = Arrays.copyOf(ChildEnd, ChildEnd.length*2);
	}
	
	//返回结果小于0：无该字符的编码
	//返回结果大于0：是该字符的编码
	private int GetCharCode(int c)
	{
		if(charMap.containsKey(c))
		{
			return charMap.get(c);
		}
		
		return -1;
	}
	
	//对mQuerys进行字典排序
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void Init()
	{
		//步骤一：对query进行字典顺序排序
		for(int i=0; i<mQuerys.size(); i++)
		{
			Pair<String,QueryMessage> tmpM = mQuerys.elementAt(i);
			mQueryIndex.add(new Pair<String,Integer>(tmpM.first,i));	
		}
		
		//按照字典顺序进行排序
		class MyCompare implements Comparator //实现Comparator，定义自己的比较方法
		{
			public int compare(Object o1, Object o2) {
				Pair<String,Integer> e1=(Pair<String,Integer>)o1;
				Pair<String,Integer> e2=(Pair<String,Integer>)o2;
			
				if(e1.first.compareTo(e2.first) > 0)
				{
					return 1;
				}else if(e1.first.compareTo(e2.first) < 0)
				{
					return -1;
				}else
				{
					return 0;
				}
			}
		}

		Comparator ct = new MyCompare();
		Collections.sort(mQueryIndex, ct);		
		
		//步骤二：对query中的字符进行编码处理
		//Map<Integer, Integer> charMap = new HashMap<Integer, Integer>();//每个字符的编码
		//HashMap<Integer,Integer> mHeadPos = new HashMap<Integer,Integer>();//开头词对应的双数组位置，也是Trie树开头词汇对应的位置
		Vector<Pair<String,Integer> > queryIn = new Vector<Pair<String,Integer> >();
		for(int i=0; i<mQueryIndex.size(); i++)
		{
			queryIn.add(mQueryIndex.elementAt(i));			
		}
		
		int row=0;
		int code=1;
		while(queryIn.isEmpty() == false)
		{
			for(int i=0; i<queryIn.size(); i++)
			{
				String tmpStr = queryIn.elementAt(i).first;								
				
				char c = tmpStr.charAt(row);
				int key = (int)c;
				
				//为没有编码的字符进行编码处理
				if(charMap.containsKey(key) == false)
				{
					charMap.put(key, code);				
					if((row == 0)&&(mHeadPos.containsKey(key) == false))
					{
						mHeadPos.put(key, code - 1);
					}
					code++;
					
					if(row == tmpStr.length() - 1)
					{
						queryIn.remove(i);
						i--;
					}					
				}else
				{
					if(row == tmpStr.length() - 1)
					{
						queryIn.remove(i);
						i--;
					}								
				}
			}	
			row++;
		}		
		
		maxCode = code;
		
		logger.info("max code is " + code);
	}
	
	private void PrintRawQuery()
	{
		/*
		logger.info("原始数据：==================================================");
		for(int i=0; i<mQuerys.size(); i++)
		{
			logger.info(i + ":  " + mQuerys.elementAt(i).first );
		}
		*/
	}
	
	private void PrintQueryIndex()
	{
		/*
		logger.info("排序后的数据：===============================================");
		for(int i=0; i<mQueryIndex.size(); i++)
		{
			logger.info(i + ":  " + mQueryIndex.elementAt(i).first);
		}
		*/
	}
	
	private void PrintTrie(Trie trie)
	{
		/*
		logger.info("==========================================================");
		logger.info("编号:");
		
		for(int i=0; i<trie.mTrie.size(); i++)
		{
			System.out.print(i + "	");
		}
		logger.info();
		
		logger.info("mTrie:");
		for(int i=0; i<trie.mTrie.size(); i++)
		{
			int cInt = trie.mTrie.elementAt(i);
			char c=(char)cInt;
			System.out.print(c + "	");
		}
		logger.info();
		
		logger.info("mBegin:");
		for(int i=0; i<trie.mBegin.size();i++)
		{
			System.out.print(trie.mBegin.elementAt(i) + "	");
		}
		logger.info();
		
		logger.info("mEnd:");
		for(int i=0; i<trie.mEnd.size();i++)
		{
			System.out.print(trie.mEnd.elementAt(i) + "	");
		}
		logger.info();
		
		logger.info("mLen:");
		for(int i=0; i<trie.mLen.size(); i++)
		{
			System.out.print(trie.mLen.elementAt(i) + "	");
		}
		logger.info();
		
		logger.info("mIndex:");
		for(int i=0; i<trie.mIndex.size(); i++)
		{
			System.out.print(trie.mIndex.elementAt(i) + "	");
		}
		logger.info();
		logger.info();
		*/
	}
	
	private void PrintDoubleArray(int num)
	{
		/*
		if(num <= 15)
		{
			num=15;
		}
		
		if(num > Base.length)
		{
			num=Base.length;
		}
		
		logger.info("===============================================================");
		logger.info("编码：");
		Iterator iter = charMap.entrySet().iterator();
		while(iter.hasNext())
		{
			Map.Entry<Integer,Integer> entry = (Map.Entry<Integer,Integer>) iter.next();
			int key = entry.getKey();
			int code = entry.getValue();
			logger.info((char)key + " —— " + code);
			
		}
		
		for(int i=0; i<num; i++)
		{
			System.out.print(i + "	");
		}
		logger.info();
		
		logger.info("Base[]:");
		for(int i=0; i<num; i++)
		{
			System.out.print(Base[i] + "	");
		}
		logger.info();
		
		logger.info("Position[]:");
		for(int i=0; i<num; i++)
		{
			System.out.print(Position[i] + "	");
		}
		logger.info();
		
		logger.info("Check[]:");
		for(int i=0; i<num; i++)
		{
			System.out.print(i+ "——" + Check[i] + "	");
		}
		logger.info();
		
		
		logger.info("TriePos[]:");
		for(int i=0; i<num; i++)
		{
			System.out.print(TriePos[i] + "	");
		}
		logger.info();
		
		logger.info("Scope");
		for(int i=0; i<num; i++)
		{
			System.out.print(ChildStart[i] + "	");
		}
		logger.info();
		for(int i=0; i<num; i++)
		{
			System.out.print(ChildEnd[i] + "	");
		}
		logger.info();
		*/
	}
	
	private Trie CreateTrie()
	{
		Trie trie = new Trie();
		//构建Trie树
		int row=0;//列序号
		int parentIndex = 0;//父节点对应的Trie数组位置
		int parentEnd = 0;//上一层节点的终止位置
		
		while(mQueryIndex.isEmpty() == false)
		{
			char preC='?';//本列前一个字符 字符不能是"?"，已经要求输入的字符串中不能有"?"号
			int preIndex=0;//父节点的数组下标
			int deleteIndex=0;//mQueryIndex中的下标
			boolean preIsWord=false;//判断父节点是否成词
			int  num=0;//统计第row个字母是preC的query数目
	
			if(row == 0)//输入第一列
			{
				for(int i=0; i<mQueryIndex.size(); i++)
				{
					Pair<String,Integer> curStrAndIndex = mQueryIndex.elementAt(i);//当前词汇及其对应的数组下标
					String curStr = curStrAndIndex.first;
					char curC = curStr.charAt(row);
					
					//记录前缀对应的结果数目
					if(curC == preC)
					{
						//boolean 
						if(row < curStr.length()-1)//记录后续子节点数目不包括空儿子
						{
							num++;
						}
						
						if(i == mQueryIndex.size()-1)//最后一条query
						{
							trie.mLen.add(num);
							trie.mTrie.add((int)preC);
							
							if(preIsWord)
							{
								//如果前一个字符是一个query的结尾
								trie.mIndex.add(preIndex);
								mQueryIndex.remove(deleteIndex);
								i--;
							} else {
								//如果前一个字符不是一个query的结尾
								trie.mIndex.add(-1);
							}
							
							preC = '?';
						}
					}else
					{
						//记录下该字的长度
						if(preC != '?')
						{
							trie.mLen.add(num);
							trie.mTrie.add((int)preC);
							if(preIsWord)
							{
								//如果前一个字符是一个query的结尾
								trie.mIndex.add(preIndex);
								mQueryIndex.remove(deleteIndex);
								i--;
							}else
							{
								//如果前一个字符不是一个query的结尾
								trie.mIndex.add(-1);
							}
						}						
						
						//下一个节点求子节点的初始化
						if(row < curStr.length()-1)
						{
							num=1;
						}else
						{							
							num=0;
						}
						
						preC=curC;
						if(row == curStr.length()-1)
						{
							//记录一个query
							preIndex=curStrAndIndex.second;//记录成词query在数组中的下标
							preIsWord=true;	
							deleteIndex=i;
						}else
						{
							preIsWord=false;								
						}
						
						if(i == mQueryIndex.size()-1)//最后一条query
						{
							trie.mLen.add(num);
							trie.mTrie.add((int)preC);
							if(preIsWord)
							{
								//如果前一个字符是一个query的结尾
								trie.mIndex.add(preIndex);
								mQueryIndex.remove(deleteIndex);
								i--;
							}else
							{
								//如果前一个字符不是一个query的结尾
								trie.mIndex.add(-1);
							}
							preC = '?';
						}
					}
					
				}
				parentEnd=trie.mTrie.size();//第一层字符数目
			}else
			{
				//for(int i=0; i<mQueryIndex.size(); i++)
				int i=0;
				while(true)
				{
					//输入非第一列数据
					while(trie.mLen.elementAt(parentIndex) == 0)
					{
						trie.mBegin.add(-1);//没有后续节点
						parentIndex++;//找到有后续节点的父亲
						
						if(parentIndex >= parentEnd)
						{
							break;
						}
					}
									
					if(parentIndex >= parentEnd)
					{
						break;
					}	
					
					trie.mBegin.add(trie.mTrie.size());//后续节点开始的位置
					
					int len = trie.mLen.elementAt(parentIndex);
					len += i;
					for(;i<len; i++)
					{
						Pair<String,Integer> curStrAndIndex = mQueryIndex.elementAt(i);//当前词汇及其对应的数组下标
						String curStr = curStrAndIndex.first;
						char curC = curStr.charAt(row);
						
						//记录前缀对应的结果数目
						if(curC == preC)
						{
							if(row < curStr.length()-1)//记录后续子节点数目不包括空儿子
								num++;
							
							if(i == len-1)
							{
								//说明已经到最后一个子节点了
								trie.mLen.add(num);
								trie.mTrie.add((int)preC);
								if(preIsWord)
								{
									//如果前一个字符是一个query的结尾
									trie.mIndex.add(preIndex);
									mQueryIndex.remove(deleteIndex);
									i--;
									len--;
								}else
								{
									//如果前一个字符不是一个query的结尾
									trie.mIndex.add(-1);
								}
								preC = '?';
							}
						}else
						{
							//记录下该字的长度
							if(preC != '?')//preC是"?"说明目前刚开始填充该列数据
							{
								trie.mLen.add(num);
								trie.mTrie.add((int)preC);
								if(preIsWord)
								{
									//如果前一个字符是一个query的结尾
									trie.mIndex.add(preIndex);
									mQueryIndex.remove(deleteIndex);
									i--;
									len--;
								}else
								{
									//如果前一个字符不是一个query的结尾
									trie.mIndex.add(-1);
								}
							}
							
							//下一个节点求子节点的初始化
							if(row < curStr.length()-1)
							{
								num=1;
							}else
							{
								num=0;
							}
							
							preC=curC;
							if(row == curStr.length()-1)
							{
								//记录一个query
								preIndex=curStrAndIndex.second;//记录成词query在数组中的下标
								preIsWord=true;	
								deleteIndex=i;
							}else
							{
								preIsWord=false;								
							}
							
							if(i == len-1)
							{
								//说明已经到最后一个子节点了
								trie.mLen.add(num);
								trie.mTrie.add((int)preC);
								if(preIsWord)
								{
									//如果前一个字符是一个query的结尾
									trie.mIndex.add(preIndex);
									mQueryIndex.remove(deleteIndex);
									i--;
									len--;
								}else
								{
									//如果前一个字符不是一个query的结尾
									trie.mIndex.add(-1);
								}
								preC = '?';							
							}							
						}						
					}
					
					
					parentIndex++;//读取下一个父节点					
				}
				
				parentEnd = trie.mTrie.size();
			}
			
			row++;//读取下一列			
		}	
		
		PrintTrie(trie);
		
		//最后一列字符肯定没有子节点
		int lenForBegin = trie.mTrie.size() - trie.mBegin.size();//需要补充-1的个数
		for(int i=0; i<lenForBegin; i++)
		{
			trie.mBegin.add(-1);
		}
		
		//计算每个节点对应的子节点长度并记录每个节点的父
		for(int i=0; i<trie.mTrie.size(); i++)
		{			
			int len=trie.mLen.elementAt(i);//前缀匹配的单词数目
			int begin=trie.mBegin.elementAt(i);//子节点开始的地方
			int num=0;
			int end=begin;
			if(begin == -1)
			{
				trie.mEnd.add(-1);
				continue;
			}
			
			for(;end<trie.mTrie.size();end++)
			{
				int cbegin=trie.mIndex.elementAt(end);
				if(cbegin >= 0)//这儿是>=号，千万不要为>
				{
					num++;
				}
				
				int cNum=trie.mLen.elementAt(end);
				num += cNum;
				
				if(num == len)
				{
					break;
				}				
			}
			
			trie.mEnd.add(end);			
		}
		
		return trie;
	}
	
	//返回-1表示不需要插入 | 同时要计算base值 | 同时收集各个词出现的位置mHeadInter
	private int ComputeMinBase(int pos, Trie trie)
	{
		//步骤一：空位置和无后缀位置的判断
		if(Check[pos] == 0)
		{
			//该处为一个空隙
			return Integer.MIN_VALUE;			
		}
		
		if(Base[pos] == -pos)
		{
			//该前缀没有后续节点
			return Integer.MIN_VALUE;
		}
		
		//步骤二：子节点编码获取
		Vector<Integer> codes = new Vector<Integer>();//pos子节点的编码
		int triePos = TriePos[pos];//对应的trie树位置
				
		for(int i=trie.mBegin.elementAt(triePos);i<=trie.mEnd.elementAt(triePos);i++)
		{
			int childWord=trie.mTrie.elementAt(i);
			int code=GetCharCode(childWord);
			codes.add(code);
		}
		
		//步骤三：查找可插入位置，即计算合适的base值
		int baseValue = 0;
		while(true)
		{
			boolean beFound = true;
			for(int i=0; i<codes.size(); i++)
			{
				int childPos = baseValue + codes.elementAt(i);
				while(childPos >= Base.length)
				{
					//保证Base[]空间充足
					Extend_Array();
				}
				
				if(Check[childPos] != 0)
				{
					//无法放下子节点
					beFound = false;
					break;
				}
			}
			
			if(beFound)
			{
				//找到合适的base值
				return baseValue;
			}else
			{
				baseValue++;
				if(baseValue == pos)//约定任何Base值不能等于其下标
				{
					baseValue++;
				}
				beFound=true;
			}
		}
	}
	
	//插入子节点，同时判断子节点是否成词，成词就记录词对应的树组下标
	void InsertChild(int pos,Trie trie)
	{
		//步骤一：获取子节点编码
		Vector<Integer> words = new Vector<Integer>();//各个子节点字符
		Vector<Integer> codes = new Vector<Integer>();//pos子节点的编码
		Vector<Boolean> beWords = new Vector<Boolean>();//成词
		Vector<Boolean> hasChild = new Vector<Boolean>();//无后续词
		Vector<Integer> posInTrie = new Vector<Integer>();//记录子节点在trie树中的位置
 		int triePos = TriePos[pos];//对应的trie树位置
 		
 		//logger.info("Pos: " + pos  + "  parentPriePos:" + triePos);
		for(int i=trie.mBegin.elementAt(triePos);i<=trie.mEnd.elementAt(triePos);i++)
		{
			int childWord=trie.mTrie.elementAt(i);
			int code=GetCharCode(childWord);
			words.add(childWord);
			codes.add(code);//收集编码
			posInTrie.add(i);
			
			//判断是否成词
			if(trie.mIndex.elementAt(i) >= 0)
			{
				beWords.add(true);
			}else
			{
				beWords.add(false);
			}
			
			//判断是否有子节点
			if(trie.mLen.elementAt(i) > 0)
			{
				hasChild.add(true);
			}else
			{
				hasChild.add(false);
			}
		}
		
		//步骤二：修改check值，记录数据对应的trie树位置
		for(int i=0; i<codes.size(); i++)
		{
			int code=codes.elementAt(i);			
			Check[Math.abs(Base[pos])+code] = pos;
			TriePos[Math.abs(Base[pos])+code] = posInTrie.elementAt(i);//记录字符对应的trie树中的位置
			
			//记录每个字符非第一个字符出现的位置
			int keyWord = words.elementAt(i);
			
			if(mHeadInter.containsKey(keyWord))
			{
				mHeadInter.get(keyWord).add(Math.abs(Base[pos])+code);
			}else
			{
				Vector<Integer> tmpV = new Vector<Integer>();
				tmpV.add(Math.abs(Base[pos])+code);
				mHeadInter.put(keyWord, tmpV);
			}
			
			if(beWords.elementAt(i) && (hasChild.elementAt(i)==false))//成词且没有后续节点
			{
				Base[Math.abs(Base[pos])+code] = -(Math.abs(Base[pos])+code);
			}
			
			
			//判断是否成词，成词就记录词汇对应的树组位置
			int childTriePos = posInTrie.elementAt(i);//对应的trie树位置
			Position[Math.abs(Base[pos])+code] = trie.mIndex.elementAt(childTriePos);
		}
	}
	
	public void CreateDoubleArray()
	{
		//步骤一：对mQuerys进行字典排序，并编码==============================================================
		Init();
		PrintRawQuery();
		PrintQueryIndex();
		
		//步骤二：构建Trie树================================================================================
		Trie trie = CreateTrie();		
		mTrie = trie;
		PrintTrie(trie);
		
		
		//步骤三：构建双数组================================================================================
		//约定：base[]值为负号表示成词
		//      负号的绝对值等于base的下标，表示该前缀没有子节点
		//                  不等于base的下标，表示该前缀有子节点
		//      
		//1. 写第一列数据
		while(mHeadPos.size() > Base.length)
		{
			//保证存放第一列数据空间充足
			Extend_Array();
		}
		
		//asznjy
		Check[0] = Integer.MIN_VALUE;
		for(int i=1; i<=mHeadPos.size(); i++)
		{
			if((trie.mIndex.elementAt(i-1) >= 0)&&(trie.mLen.elementAt(i-1) == 0))
			{
				Base[i] = -i;//说明成词，且没有后续节点  也就是mBegin.elementAt(i-1)==-1
			}
			
			Check[i] = Integer.MIN_VALUE;//表示没有父节点		
			TriePos[i] = i-1;//对应trie树的位置
			
			//如果成词记录词对应的树组位置
			Position[i] = trie.mIndex.elementAt(i-1);
		}		
		
		//2. 存放其他各列数据
		for(int i=1; i<Base.length; i++)
		{
			//计算合适的base值
			int baseValue = ComputeMinBase(i,trie);
			if(baseValue == Integer.MIN_VALUE)
			{
				ChildStart[i] = -1;
				ChildEnd[i] = -1;
				//无后续节点
				continue;
			}else
			{
				int index = TriePos[i];
				
				//这儿千万注意了
				int trieBegin = trie.mBegin.elementAt(index);
				int trieEnd = trie.mEnd.elementAt(index);
				int maxCode=0;//最大的子节点编码
				int minCode=Integer.MAX_VALUE;//最小的子节点编码
				for(int k=trieBegin; k<=trieEnd; k++ )
				{
					int tmpWord = trie.mTrie.elementAt(k);
					int tmpCode = charMap.get(tmpWord);
					if(maxCode < tmpCode)
					{
						maxCode = tmpCode;
					}
					
					if(minCode > tmpCode)
					{
						minCode = tmpCode;
					}
				}
				
				ChildStart[i] = minCode + baseValue;
				ChildEnd[i] = maxCode +  baseValue;
				
				if(ChildEnd[i] < ChildStart[i])
				{
					logger.info("Start: " + ChildStart[i] + "  End: " + ChildEnd[i]);
				}
				
				Base[i] = baseValue;//记录新基址
			}
			
			InsertChild(i,trie);
		}
		
		//3 打印双数组结果
		PrintDoubleArray(40);
		
	}	
	
	void PrintOneQuery(int tmpIndex)
	{
		/*
		 int index = Position[tmpIndex];
		 String oneQuery = mQuerys.elementAt(index).first;
		 logger.info(oneQuery);
		 */
	}
	//查找前缀匹配的所有的所有词汇,开始位置为startBaseIndex
	//如果没有词汇返回null
	public Vector<Integer> FindWordsByPrefix(String prefixStr)
	{
		if(prefixStr.length() == 0)
		{
			return null;
		}
		
		//步骤一：字符串编码
		//logger.info("query编码==========================================================================================");
		Vector<Integer> prefixCodes = new Vector<Integer>();
		for(int i=0; i<prefixStr.length(); i++)
		{			
			int tmpWord = (int) (prefixStr.charAt(i));
			int tmpCode = GetCharCode(tmpWord);			
						
			if(tmpCode == -1)
			{
				return null;
			}
			
			if((i == 0)&&(mHeadPos.containsKey(tmpWord) == false))
			{
				//没有tmpWord开头的词汇
				return null;
			}
			
			prefixCodes.add(tmpCode);
		}
		
		//步骤二：词汇对应的状态查找，即找到词汇对应的Base下标
		int baseIndex = prefixCodes.elementAt(0);//
		int preIndex = baseIndex;//前一个状态
      for(int i=1; i<prefixCodes.size();i++)
        {
    	  	int tmpCode = prefixCodes.elementAt(i);
    	  	baseIndex = Math.abs(Base[baseIndex]) + tmpCode;//转化到下一个状态
    	  	if(baseIndex >= Base.length || Check[baseIndex] != preIndex)
    	  	{
    	  		//没有该前缀对应的词汇
    	  		return null;
    	  	}
        	
    	  	preIndex = baseIndex;
        }
      
      	//步骤三：收集词汇作为前缀的所有词汇
		LinkedList<Integer> childBase = new LinkedList<Integer>();
		Vector<Integer> resultWords = new Vector<Integer>();
		childBase.add(baseIndex);
		while(childBase.isEmpty() == false)
		{
		     //判断该前缀是否有子节点，无子节点且不成词就返回null，无子节点但成词就返回该词汇对应的下标，有字节单独处理
			int tmpIndex = childBase.get(0);
			if(Base[tmpIndex] == -tmpIndex)//无子节点（自然也成词）
			{				
				PrintOneQuery(tmpIndex);
				resultWords.add(Position[tmpIndex]);
			}else
			{
				if(Position[tmpIndex] >= 0)//有子节点且成词
				{			
					resultWords.add(Position[tmpIndex]);	
					
					int baseStart = ChildStart[tmpIndex];//Math.abs(Base[tmpIndex]);
					int baseEnd = ChildEnd[tmpIndex];//baseStart+maxCode;

					for(int i= baseStart; i<=baseEnd; i++)
					{
						if(Check[i] == tmpIndex)//找到子节点
						{
							childBase.add(i);
						}
					}
				}else//有子节点不成词
				{
					int baseStart = ChildStart[tmpIndex];//Math.abs(Base[tmpIndex]);
					int baseEnd = ChildEnd[tmpIndex];//baseStart+maxCode;

					for(int i= baseStart; i<=baseEnd; i++)
					{
						if(Check[i] == tmpIndex)//找到子节点
						{
							childBase.add(i);
						}
					}						
				}
			}
			
			childBase.remove(0);
		}
		
		if(resultWords.size() == 0)
		{
			return null;
		}
		
		return resultWords;
	}	
	
	//指定第一个字符在双数组中的匹配位置，进行前缀匹配，如果制定的非头字符那么就是从中间进行前缀匹配
	//比如：输入 诺基亚    指定位置为：我爱诺基亚中的诺开始，那么会 匹配出 我爱诺基亚
	private Vector<Integer> FindWordsByPrefixAndStartPos(String prefixStr, int pos)
	{
		//步骤一：字符串编码
		//logger.info("query code========================================");
		Vector<Integer> prefixCodes = new Vector<Integer>();
		for(int i=0; i<prefixStr.length(); i++)
		{			
			int tmpWord = (int) (prefixStr.charAt(i));
			int tmpCode = GetCharCode(tmpWord);			
			
			//logger.info((char)tmpWord + " —— " + tmpCode);
			
			if(tmpCode == -1)
			{
				return null;
			}
						
			prefixCodes.add(tmpCode);
		}
		
		//步骤二：词汇对应的状态查找，即找到词汇对应的Base下标
		int baseIndex = pos;//
		int preIndex = baseIndex;//前一个状态
      for(int i=1; i<prefixCodes.size();i++)
        {
    	  	int tmpCode = prefixCodes.elementAt(i);
    	  	baseIndex = Math.abs(Base[baseIndex]) + tmpCode;//转化到下一个状态
    	  	if(baseIndex >= Base.length || Check[baseIndex] != preIndex)
    	  	{
    	  		//没有该前缀对应的词汇
    	  		return null;
    	  	}
        	
    	  	preIndex = baseIndex;
        }
      
      	//步骤三：收集词汇作为前缀的所有词汇
      LinkedList<Integer> childBase = new LinkedList<Integer>();
		Vector<Integer> resultWords = new Vector<Integer>();
		childBase.add(baseIndex);
		while(childBase.isEmpty() == false)
		{
		     //判断该前缀是否有子节点，无子节点且不成词就返回null，无子节点但成词就返回该词汇对应的下标，有字节单独处理
			int tmpIndex = childBase.get(0);
			if(Base[tmpIndex] == -tmpIndex)//无子节点（自然也成词）
			{				
				PrintOneQuery(tmpIndex);
				resultWords.add(Position[tmpIndex]);
			}else
			{
				if(Position[tmpIndex] >= 0)//有子节点且成词
				{
					PrintOneQuery(tmpIndex);					
					resultWords.add(Position[tmpIndex]);	
					
					int baseStart = ChildStart[tmpIndex];//Math.abs(Base[tmpIndex]);
					int baseEnd = ChildEnd[tmpIndex];//baseStart+maxCode;
					
					for(int i= baseStart; i<=baseEnd; i++)
					{
						if(Check[i] == tmpIndex)//找到子节点
						{
							childBase.add(i);
						}
					}
				}else//有子节点不成词
				{
					int baseStart = ChildStart[tmpIndex];//Math.abs(Base[tmpIndex]);
					int baseEnd = ChildEnd[tmpIndex];//baseStart+maxCode;
					
					for(int i= baseStart; i<=baseEnd; i++)
					{
						if(Check[i] == tmpIndex)//找到子节点
						{
							childBase.add(i);
						}
					}						
				}
			}
			
			childBase.remove(0);
		}
		
		if(resultWords.size() == 0)
		{
			return null;
		}
		
		return resultWords;		
	}
	
	//查找非前缀匹配但是又包含该子串的词汇  最少结果数目为尽力找到MIDDLE_MATCH_NUMBER，最大有maxResult决定
	public Vector<Integer> FindWordsBySubstringNotPrefix(String subStr, int maxResult)
	{
		//步骤一：判断是否是空查询
		if(subStr.length() == 0)
		{
			return null;
		}
		
		//步骤二：约定最小结果数目
		if(maxResult < MIDDLE_MATCH_NUMBER)
		{
			maxResult = MIDDLE_MATCH_NUMBER;
		}
		
		//步骤三：获取可能的匹配开始位置
		int startKey = (int) subStr.charAt(0);
		Vector<Integer> startVec;//第一个字符匹配的开始位置
		if(mHeadInter.containsKey(startKey))
		{
			startVec = mHeadInter.get(startKey);
		}else
		{
			return null;
		}
		
		//步骤四：查找匹配词汇
		Vector<Integer> resultVec = new Vector<Integer>();
		for(int i=0; i<startVec.size(); i++)
		{
			int startPos = startVec.elementAt(i);
			Vector<Integer> tmpVec = FindWordsByPrefixAndStartPos(subStr,startPos);
			if(tmpVec == null)
			{
				continue;
			}
			
			for(int j=0; j< tmpVec.size(); j++)
			{
				int Index = tmpVec.elementAt(j);
				resultVec.add(Index);
				if(resultVec.size() >= maxResult)
				{
					return resultVec;
				}
			}
		}
		
		if(resultVec.size() == 0)
		{
			return null;
		}
		
		return resultVec;
	}
	
	//输入字串口，判断是否成词
	//主要为分词创建
	public boolean isWord(String prefixStr)
	{
		if(prefixStr.length() == 0)
		{
			return false;
		}
		
		//步骤一：字符串编码
		//logger.info("query编码========================================");
		Vector<Integer> prefixCodes = new Vector<Integer>();
		for(int i=0; i<prefixStr.length(); i++)
		{			
			int tmpWord = (int) (prefixStr.charAt(i));
			int tmpCode = GetCharCode(tmpWord);			
			
			//logger.info((char)tmpWord + " —— " + tmpCode);
			
			if(tmpCode == -1)
			{
				return false;
			}
			
			if((i == 0)&&(mHeadPos.containsKey(tmpWord) == false))
			{
				//没有tmpWord开头的词汇
				return false;
			}
			
			prefixCodes.add(tmpCode);
		}
		
		//步骤二：词汇对应的状态查找，即找到词汇对应的Base下标
		int baseIndex = prefixCodes.elementAt(0);//
		int preIndex = baseIndex;//前一个状态
      for(int i=1; i<prefixCodes.size();i++)
        {
    	  	int tmpCode = prefixCodes.elementAt(i);
    	  	baseIndex = Math.abs(Base[baseIndex]) + tmpCode;//转化到下一个状态
    	  	
    	  	if(baseIndex >= Base.length)//防止越界
    	  		return false;
    	  	
    	  	//logger.info("Check.length: " + Check.length);
    	  	//logger.info("baseIndex:    " + baseIndex);
    	  	if(Check[baseIndex] != preIndex)
    	  	{
    	  		//没有该前缀对应的词汇
    	  		return false;
    	  	}
        	
    	  	preIndex = baseIndex;
        }
      
      if(Position[preIndex] >= 0)
      	{
    	  return true;
      	}
      
		return false;
	}

	/*
	public static void main(String [] args)
	{
		Vector<Pair<String,QueryMessage> > Querys = new Vector<Pair<String,QueryMessage> >();
		ArrayList<String> queryForTest = new ArrayList<String>();
		queryForTest.add("啊");
		queryForTest.add("阿根廷");
		queryForTest.add("阿胶");
		queryForTest.add("阿拉伯人");
		queryForTest.add("埃及");
		queryForTest.add("阿拉");
		queryForTest.add("阿根");
		queryForTest.add("阿姨");
		queryForTest.add("埃森哲");
		queryForTest.add("我爱埃森哲");
		queryForTest.add("诺基亚埃森哲");
		queryForTest.add("埃森哲诺基亚");
		queryForTest.add("亲爱的苹果");
		queryForTest.add("小苹果");
		queryForTest.add("外婆的苹果树");
		queryForTest.add("外婆的世界多么美好苹果树");
		queryForTest.add("iphone手机");
		queryForTest.add("厢式热水器");
		queryForTest.add("0元购机");
		queryForTest.add("0元");
		queryForTest.add("0元购");
		queryForTest.add("003");
		queryForTest.add("096");
		queryForTest.add("0849");
		queryForTest.add("0w-40");
		queryForTest.add("0元购手机");
		
		Random rnd = new Random();
		for(String str:queryForTest)
		{
			QueryMessage qm = new QueryMessage();
			qm.mQueryNum = rnd.nextInt(100);
			Pair<String,QueryMessage> aP = new Pair<String,QueryMessage>(str,qm);
			Querys.add(aP);		
		}
		
		DoubleArray da=new DoubleArray(Querys);	
		da.CreateDoubleArray();
		Vector<Integer> resultV = da.FindWordsBySubstringNotPrefix("厢", 10);
		//Vector<Integer> resultV = da.FindWordsByPrefix("厢");
		logger.info("查询结果=====================================");
				
		 //int index = Position[tmpIndex];
		 //String oneQuery = mQuerys.elementAt(index).first;
		 //logger.info(oneQuery);
		 
		if(resultV == null)
		{
			logger.info("没有找到结果");
			return;
		}
		
		for(int i=0; i< resultV.size(); i++)
		{
			int index = resultV.elementAt(i);
			
			Pair<String,QueryMessage> tmpPair = da.mQuerys.elementAt(index);
			String oneWord = tmpPair.first;
					
			logger.info(oneWord);
		}

		logger.info("Test OK!");
	}*/
	
	public static ConfMessage readConfFile(String filePath)
	{
		try 
		{
			String encoding="utf-8";
			File file=new File(filePath);
			if(file.isFile() && file.exists())
			{ 
				//判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);//考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				bufferedReader.readLine();
				HashMap<String, Integer> confH = new HashMap<String, Integer>();
				while((lineTxt = bufferedReader.readLine()) != null){
					if(lineTxt.contains("="))
					{
						String[] tmpArray = lineTxt.split("=");
						if(tmpArray.length != 2)
						{
							continue;
						}
						
						String key = tmpArray[0].trim();
						int value = Integer.parseInt(tmpArray[1].trim());
						confH.put(key, value);
					}					
				}
				read.close();
				
				ConfMessage confM = new ConfMessage();
				if(confH.containsKey("returnnumber"))
				{
					confM.mReturnNumber = confH.get("returnnumber");
				}else
				{
					logger.info("Error: cannot find returnNumber in config file");
					return null;
				}
				
				if(confH.containsKey("middlenumber"))
				{
					confM.mMiddleNumber = confH.get("middlenumber");
				}else
				{
					logger.info("Error: cannot find middlenumber in config file");
					return null;
				}
				
				if(confH.containsKey("shoprate"))
				{
					confM.mShopRate = confH.get("shoprate");
					
				}else
				{
					logger.info("Error: cannot find shoprate in config file");
					return null;
				}
				
				if(confH.containsKey("hzlen"))
				{
					confM.mHzLen = confH.get("hzlen");
					
				}else
				{
					logger.info("Error: cannot find hzlen in config file");
					return null;
				}
				
				if(confH.containsKey("pylen"))
				{
					confM.mPyLen = confH.get("pylen");
					
				}else
				{
					logger.info("Error: cannot find pylen in config file");
					return null;
				}
				
				return confM;
			}else{
				logger.info("Error: cannot find file needed");
			}
		} catch (Exception e) 
		{
		    logger.error("Error: read file error",e);
		}
		
		return null;
	}
	
	static QueryMessage analyzeQueryMessage(String mStr)
	{
		if(mStr == null)
		{
			return null;
		}
		
		//不允许query中有问号
		if(mStr.contains("?") || mStr.contains("？"))
		{
			return null;
		}
		
		String[] messageStr = mStr.split("\t");
				
		for(int i=0; i<messageStr.length; i++)
		{
			String tmpStr = messageStr[i];
			tmpStr = tmpStr.toLowerCase();
			tmpStr = tmpStr.trim();
			messageStr[i] = tmpStr;
		}
		
		if(messageStr[0].length() < 2)
		{
			return null;
		}
		
		if(messageStr.length < 5)
		{
			return null;
		}
		
		//不允许query中有问号
		if(messageStr[0].contains("?") || messageStr[0].contains("？"))
		{
			return null;
		}

		QueryMessage qM = new QueryMessage();
		qM.mQuery = messageStr[0];//query
		qM.mQueryNum = Integer.parseInt(messageStr[1]);//访问数目
		qM.mProuductNum = Integer.parseInt(messageStr[2]);//产品数目
		
		int catNum = Integer.parseInt(messageStr[3]);//类别数目
		int beg = 4;
		int end = beg + catNum;
		if(end > messageStr.length)
		{
			return null;
		}
		
		for(int i=beg; i <end; i++) //类别id
		{
			qM.mCategoryID.add(messageStr[i]);
		}
		
		beg = end;
		end =  beg + catNum;
		if(end > messageStr.length)
		{
			return null;
		}
		
		for(int i=beg; i <end; i++)
		{
			qM.mCategoryName.add(messageStr[i]);//类别名词
		}
		
		beg = end;
		end =  beg + catNum;
		if(end > messageStr.length)
		{
			return null;
		}
		
		for(int i=beg; i <end; i++)
		{
			qM.mCatTimes.add(Integer.parseInt(messageStr[i]));//每个类别下query的访问次数
		}
		
		beg = end;
		end =  beg + catNum;
		if(end > messageStr.length)
		{
			return null;
		}
		
		for(int i=beg; i <end; i++)
		{
			qM.mCatProductNum.add(Integer.parseInt(messageStr[i]));//每个类别下query搜索到的产品数目
		}
			
		beg = end;
		if(beg > messageStr.length)
		{
			return null;
		}
		
		qM.mIsShopWord = false;
		int hasShop = Integer.parseInt(messageStr[beg++]);//是否有店铺信息
		if(hasShop > 0)
		{
			if(end> messageStr.length)//店铺信息不够
			{
				return null;
			}
			qM.mIsShopWord = true;
			qM.mShopName = messageStr[beg++];
			qM.mShopId = Integer.parseInt(messageStr[beg++]);
			qM.mShopUrl = messageStr[beg++];
			qM.mShopLogoUrl = messageStr[beg];
		}
			
		return qM;
	}
	
	static QueryMessage artificialWordsToQueryMessage(String str)
	{
		if(str == null)
		{
			return null;
		}
		
		//不允许query中有问号
		if(str.contains("?") || str.contains("？"))
		{
			return null;
		}
		
		String[] strS = str.split("\t");
		str = strS[0];
		str = str.toLowerCase();
		str = str.trim();
		
		if(str.length() == 0)
		{
			return null;
		}
		
		QueryMessage qM = new QueryMessage();
		qM.mQuery = str;
		qM.mQueryNum = 10;
		qM.mProuductNum = 10;
		qM.mIsShopWord = false;
		
		for(int i=1; i<strS.length; i++)
		{
			String pyStr=strS[i];
			pyStr = pyStr.trim();
			pyStr = pyStr.toLowerCase();
			
			qM.mPinyin.add(pyStr);
		}
		
		return qM;
	}
	
	
	
	
   //查找Trie树某个节点下面的所有词汇
	public Vector<Integer> FindWordsByPosInTrie(int pos)
	{	      
		//注意要使用LinkedList而不是Vector，处理大量的删除操作
		LinkedList<Integer> childBase = new LinkedList<Integer>();
		Vector<Integer> resultWords = new Vector<Integer>();
		childBase.add(pos);
		while(childBase.isEmpty() == false)
		{
		    int triePos = childBase.get(0);//对应trie树的位置
		     
		    int queryIndex = mTrie.mIndex.elementAt(triePos);
		    if(queryIndex > 0)
		     {
		    	resultWords.add(queryIndex);
		    	if(resultWords.size() > 100)
		    	{
		    		break;
		    	}
		     }
		    
		   int beg = mTrie.mBegin.elementAt(triePos);
		   int end = mTrie.mEnd.elementAt(triePos);
		   if(beg<0)
		    {
			   childBase.remove(0);
		    	continue;
		    }
		    
		   for(int i=beg; i<=end; i++)
		    {
		    	childBase.add(i);
		    }
		    
		    childBase.remove(0);
		}
		
		return resultWords;		
	}	
	
	
	//查找前缀匹配的所有的所有词汇,开始位置为startBaseIndex
	//如果没有词汇返回null
	public Vector<Integer> NewFindWordsByPrefix(String prefixStr)
	{
		if(prefixStr.length() == 0)
		{
			return null;
		}
		
		//步骤一：字符串编码
		//logger.info("query编码==========================================================================================");
		Vector<Integer> prefixCodes = new Vector<Integer>();
		for(int i=0; i<prefixStr.length(); i++)
		{			
			int tmpWord = (int) (prefixStr.charAt(i));
			int tmpCode = GetCharCode(tmpWord);			
						
			if(tmpCode == -1)
			{
				return null;
			}
			
			if((i == 0)&&(mHeadPos.containsKey(tmpWord) == false))
			{
				//没有tmpWord开头的词汇
				return null;
			}
			
			prefixCodes.add(tmpCode);
		}
		
		//步骤二：词汇对应的状态查找，即找到词汇对应的Base下标
		int baseIndex = prefixCodes.elementAt(0);//
		int preIndex = baseIndex;//前一个状态
      for(int i=1; i<prefixCodes.size();i++)
        {
    	  	int tmpCode = prefixCodes.elementAt(i);
    	  	baseIndex = Math.abs(Base[baseIndex]) + tmpCode;//转化到下一个状态
    	  	if(baseIndex >= Base.length || Check[baseIndex] != preIndex)
    	  	{
    	  		//没有该前缀对应的词汇
    	  		return null;
    	  	}
        	
    	  	preIndex = baseIndex;
        }
      
      	//步骤三：收集词汇作为前缀的所有词汇
      int pos = TriePos[baseIndex];
      
		Vector<Integer> resultWords = FindWordsByPosInTrie(pos);		
		if(resultWords == null)
		{
			return null;
		}
		
		return resultWords;
	}	
	
	//指定第一个字符在双数组中的匹配位置，进行前缀匹配，如果制定的非头字符那么就是从中间进行前缀匹配
	//比如：输入 诺基亚    指定位置为：我爱诺基亚中的诺开始，那么会 匹配出 我爱诺基亚
	private Vector<Integer> NewFindWordsByPrefixAndStartPos(String prefixStr, int pos)
	{
		//步骤一：字符串编码
		//logger.info("query编码========================================");
		Vector<Integer> prefixCodes = new Vector<Integer>();
		for(int i=0; i<prefixStr.length(); i++)
		{			
			int tmpWord = (int) (prefixStr.charAt(i));
			int tmpCode = GetCharCode(tmpWord);			
			
			//logger.info((char)tmpWord + " —— " + tmpCode);
			
			if(tmpCode == -1)
			{
				return null;
			}
						
			prefixCodes.add(tmpCode);
		}
		
		//步骤二：词汇对应的状态查找，即找到词汇对应的Base下标
		int baseIndex = pos;//
		int preIndex = baseIndex;//前一个状态
      for(int i=1; i<prefixCodes.size();i++)
        {
    	  	int tmpCode = prefixCodes.elementAt(i);
    	  	baseIndex = Math.abs(Base[baseIndex]) + tmpCode;//转化到下一个状态
     	  	if(baseIndex >= Base.length || Check[baseIndex] != preIndex)
    	  	{
    	  		//没有该前缀对应的词汇
    	  		return null;
    	  	}
        	
    	  	preIndex = baseIndex;
        }
      
      	//步骤三：收集词汇作为前缀的所有词汇
      int triepos = TriePos[baseIndex];
		Vector<Integer> resultWords = FindWordsByPosInTrie(triepos);		
		
		if(resultWords == null)
		{
			return null;
		}
				
		return resultWords;		
	}
	
	//查找非前缀匹配但是又包含该子串的词汇  最少结果数目为尽力找到MIDDLE_MATCH_NUMBER，最大有maxResult决定
	public Vector<Integer> NewFindWordsBySubstringNotPrefix(String subStr, int maxResult)
	{
		//步骤一：判断是否是空查询
		if(subStr.length() == 0)
		{
			return null;
		}
		
		//步骤二：约定最小结果数目
		if(maxResult < MIDDLE_MATCH_NUMBER)
		{
			maxResult = MIDDLE_MATCH_NUMBER;
		}
		
		//步骤三：获取可能的匹配开始位置
		int startKey = (int) subStr.charAt(0);
		Vector<Integer> startVec;//第一个字符匹配的开始位置
		if(mHeadInter.containsKey(startKey))
		{
			startVec = mHeadInter.get(startKey);
		}else
		{
			return null;
		}
		
		//步骤四：查找匹配词汇
		Vector<Integer> resultVec = new Vector<Integer>();
		for(int i=0; i<startVec.size(); i++)
		{
			int startPos = startVec.elementAt(i);
			Vector<Integer> tmpVec = NewFindWordsByPrefixAndStartPos(subStr,startPos);
			if(tmpVec == null)
			{
				continue;
			}
			
			for(int j=0; j< tmpVec.size(); j++)
			{
				int Index = tmpVec.elementAt(j);
				resultVec.add(Index);
				if(resultVec.size() >= maxResult)
				{
					return resultVec;
				}
			}
		}
		
		return resultVec;
	}
	
	public static void main(String [] args)
	{
		ConfMessage mConf = null;
		DoubleArray mDA=null;//query双数组	
//		DoubleArray mDAPY=null;//query的拼音双数组
		
		mConf = readConfFile("./conf/suggest.conf");
		
		if(mConf == null)
		{
			logger.info("Error:  read config file has error");
			return;
		}
		
		//步骤一：中文双数组构建
		HashSet<String> QuerySet = new HashSet<String>();//防止词汇重复加载
		Vector<Pair<String,QueryMessage> > Querys = new Vector<Pair<String,QueryMessage> >();
		HashMap<String,QueryMessage> hzToQMHash = new HashMap<String,QueryMessage>();
		ArrayList<String> queryForTest = new ArrayList<String>();
		
		//加载数据文件
		try 
		{
			String encoding="utf-8";
			File file=new File("./data/suggest.data");
			if(file.isFile() && file.exists())
			{ 
				//判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);//考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				bufferedReader.readLine();
				while((lineTxt = bufferedReader.readLine()) != null){			
				   QueryMessage retQM = analyzeQueryMessage(lineTxt);
				   if((retQM == null) || (QuerySet.contains(retQM.mQuery)==true))
				   	{
					   //去掉不合法的信息
					   continue;
				    }
				   
				   Pair<String,QueryMessage> qP = new Pair<String,QueryMessage>(retQM.mQuery,retQM);
				   Querys.add(qP);
				   QuerySet.add(retQM.mQuery);
				   hzToQMHash.put(retQM.mQuery,retQM);

				   queryForTest.add(retQM.mQuery);
				}
								
				read.close();
			}else{
				logger.info("Error: suggest cannot find file needed");
			}
		} catch (Exception e) 
		{
		    logger.error("Error: suggest read file error",e);
		}
		
		
		//加载人工设定词汇
		Vector<Pair<String,QueryMessage>> artVec = new Vector<Pair<String,QueryMessage>>();
		try 
		{
			String encoding="utf-8";
			File file=new File("./data/artificialWords.data");
			
			if(file.isFile() && file.exists())
			{ 
				//判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);//考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				bufferedReader.readLine();
				while((lineTxt = bufferedReader.readLine()) != null){
				   QueryMessage retQM = artificialWordsToQueryMessage(lineTxt);
				   if((retQM == null) || (QuerySet.contains(retQM.mQuery)==true))
				   	{
					   //去掉不合法的信息
					   continue;
				    }
				   Pair<String,QueryMessage> qP = new Pair<String,QueryMessage>(retQM.mQuery,retQM);
				   Querys.add(qP);
				   QuerySet.add(retQM.mQuery);
				   artVec.add(qP);
				   queryForTest.add(retQM.mQuery);
				}				
				
				read.close();
			}else{
				logger.info("Error: artificialWords cannot find file needed");
			}
		} catch (Exception e) 
		{
		    logger.error("Error: artificialWords read file error",e);
		}
		
		
		

		mDA=new DoubleArray(Querys);	
		mDA.CreateDoubleArray();
		
		
		//Vector<Integer> resultV = mDA.FindWordsBySubstringNotPrefix("厢", 10);
		Vector<Integer> resultV = mDA.FindWordsByPrefix("手机联想");
		logger.info("Ther result =====================================");
				
		 //int index = Position[tmpIndex];
		 //String oneQuery = mQuerys.elementAt(index).first;
		 //logger.info(oneQuery);
		 
		if(resultV == null)
		{
			logger.info("cannot find");
			return;
		}
		
		for(int i=0; i< resultV.size(); i++)
		{
			int index = resultV.elementAt(i);
			
			Pair<String,QueryMessage> tmpPair = mDA.mQuerys.elementAt(index);
			String oneWord = tmpPair.first;
					
			logger.info(oneWord);
		}	
	}
}
