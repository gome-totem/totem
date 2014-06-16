package org.z.data.analysis;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.z.data.analysis.SmartTokenizerFactory.TokenMode;

public class LxqSplitShopName {
	//沒有字母或在數字，返回-1
	//有字母或數字，返回最後出現的位置的後一位
	public static int findLastAlphaOrnumeric(String src)
	{
		for(int i=src.length() - 1; i>=0; i--)
		{
			char c = src.charAt(i);
			if((c >= 'a')&&(c<='z') || (c >= 'A')&&(c<='z') || (c >= '0')&&(c<='9'))
			{
				return i+1;
			}
		}
		
		return -1;
	}

	/*
	public static void main(String[] args) throws MongoException, IOException {
		Map<String,Integer>  WordToNum= new HashMap<String,Integer>();
		//步驟一：數據加載
		Dictionary.init();
		System.out.println(ChineseSplit.toShortString("abc.efg"));
		String ss = "倍思（BASEUS）iphone5c信仰皮套（灰色）";
		int wordNum = 0;
		
		//步驟二：讀取數據
		List<BasicDBObject> list = new ArrayList<BasicDBObject>();
		List<String> srcList = new ArrayList<String>();
		List<String> RetList = new ArrayList<String>();
		try {
			String filePath = "/usr/local/liuxiangqian/test.txt";
			String encoding="utf-8";
			File file=new File(filePath);
			if(file.isFile() && file.exists())
			{ 
				//判断文件是否存在
				InputStreamReader read;
			
				read = new InputStreamReader(new FileInputStream(file),encoding);
				//考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while((lineTxt = bufferedReader.readLine()) != null){
					//System.out.println(lineTxt);
					wordNum++;
					list.add(new BasicDBObject("item", lineTxt));
				}
			}		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//List<BasicDBObject> list = new ArrayList<BasicDBObject>();
		//list.add(new BasicDBObject("item", ss));
		
		//步驟三：分詞並統計詞彙
		int i = 0;
		Iterator<BasicDBObject> it = list.iterator();
		int sortNum=0;
		
		SmartTokenizerFactory smart =new SmartTokenizerFactory();
		smart.init();
		while (it.hasNext()) {
			System.out.println(wordNum+"\t"+ (++sortNum));
			BasicDBObject keyword = it.next();
			String li = keyword.getString("item");
			int pos = findLastAlphaOrnumeric(li);
			String headStr="";
			if(pos>0)
			{
				headStr = li.substring(0,pos);
				li = li.substring(pos);
			}
			
			StringReader sr = new StringReader(li);
			SmartTokenizer src = new SmartTokenizer(sr);

			src.init();
			TokenStream ngr = src;

			ngr = new SmartWordFilter(ngr, 3, 5, TokenMode.search);
			ngr = new SmartRepeatFilter(ngr, TokenMode.search);
			
			
			try {
				//System.out.println(li);
				Set<String> lineSet = new HashSet<String>();
				if(pos>0)
				{
					lineSet.add(headStr);
				}
				
				while (ngr.incrementToken()) {
					OffsetAttribute s = ngr.getAttribute(OffsetAttribute.class);
					String oneWord = "" + ngr.getAttribute(CharTermAttribute.class);
					//System.out.println(oneWord);
					if(lineSet.contains(oneWord) == false)
					{
						lineSet.add(oneWord);
					}					
				}
				
				for(String term:lineSet)
				{
					if(WordToNum.containsKey(term))
					{
						int number = WordToNum.get(term);
						number++;
						
						WordToNum.remove(term);
						WordToNum.put(term, number);
					}
					else
					{
						WordToNum.put(term, 1);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}//while (it.hasNext())
		
		//步驟四：讀取數據對魅條數據進行中心詞彙提取
		it = list.iterator();
		while (it.hasNext()) {
			BasicDBObject keyword = it.next();
			String li = keyword.getString("item");
			int pos = findLastAlphaOrnumeric(li);
			if(li.contains("赛摩维多利亚的秘密"))
			{
				System.out.println(li);
			}
			
			String headStr="";
			if(pos>0)
			{
				headStr = li.substring(0,pos);
				li = li.substring(pos);
			}
			
			StringReader sr = new StringReader(li);
			SmartTokenizer src = new SmartTokenizer(sr);
			src.init();
			TokenStream ngr = src;

			ngr = new SmartWordFilter(ngr, 3, 5, TokenMode.search);
			ngr = new SmartRepeatFilter(ngr, TokenMode.search);
			try {
				List<String> lineArray = new ArrayList<String>();
				while (ngr.incrementToken()) {
					OffsetAttribute s = ngr.getAttribute(OffsetAttribute.class);
					String oneWord = "" + ngr.getAttribute(CharTermAttribute.class);
										
					lineArray.add(oneWord);
				}
				
				//System.out.print(li + "\t");
				String coreWord="";
				if(pos>0)
				{
					coreWord += headStr;
				}
				
				for(String term:lineArray)
				{					
					int number = WordToNum.get(term);
					float fileNum = (float)wordNum/number;
					if(fileNum > 200)
					{
						coreWord += term;
					}else
					{
						break;
					}
				}
				
				if(coreWord.contains("赛摩维多利亚的秘密"))
				{
					System.out.println(coreWord);
				}
				
				if(pos>0)
				{
					li = headStr + li;
				}
				
				srcList.add(li);
				RetList.add(coreWord);
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}//while (it.hasNext())

		//步驟五：將統計結果存放那個到文件中
		String filepath = "/usr/local/liuxiangqian/out.data";
		try {			
			File file = new File(filepath);
			File parent = file.getParentFile();
			if(parent!=null&&!parent.exists())
			{
				parent.mkdirs();
			}

			if(file.exists())
				file.delete();

			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		FileOutputStream tmpf = null;
		try {
			tmpf = new FileOutputStream(filepath,true);

			for(int n=0; n<srcList.size(); n++)
			{
				String src = srcList.get(n);
				String dest = RetList.get(n);
				String retStr = src + "\t" + dest;
				tmpf.write(retStr.getBytes());				
				tmpf.write("\n".getBytes());
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tmpf.flush();
		tmpf.close();
		
	}
*/
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException
	{
		String testStr = "iphone4s";
		
		SmartTokenizerFactory.init();
		StringReader sr = new StringReader(testStr);
		SmartTokenizer src = new SmartTokenizer();
		src.init(sr);
		TokenStream ngr = src;

		ngr = new SmartWordFilter(ngr, 3, 5, TokenMode.search);
		ngr = new SmartRepeatFilter(ngr, TokenMode.search);
	
		while (ngr.incrementToken()) {
			OffsetAttribute s = ngr.getAttribute(OffsetAttribute.class);
			String oneWord = "" + ngr.getAttribute(CharTermAttribute.class);
								
			System.out.println(oneWord);
		}
	}
}
