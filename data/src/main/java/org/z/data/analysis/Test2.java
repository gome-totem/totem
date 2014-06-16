package org.z.data.analysis;

import java.io.IOException;

import org.z.global.dict.Global;
import org.z.global.util.StringUtil;

public class Test2 {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		 String list=StringUtil.loadFileContent(Global.ConfigPath+"/split/en.txt", "utf-8", true);
		 String[] ens =list.split("\\s");
		 for(String s :ens){
			  System.out.println(s);
		 }

		// Dictionary.init();
		// SmartTokenizerFactory.init();
		// String query = "威运高（vivanco）iphone3g游戏手柄";
		// query = query.replaceAll("\t", " ");
		// query = query.toLowerCase();
		// // System.out.println(query);
		//
		// // 分词切分
		// StringReader sr = new StringReader(query);
		// SmartTokenizer src = new SmartTokenizer(sr);
		//
		// src.init(sr);
		// TokenStream ngr = src;
		//
		// ngr = new SmartWordFilter(ngr, 3, 5, TokenMode.search);
		// ngr = new SmartRepeatFilter(ngr, TokenMode.search);
		//
		// Set<String> lineSet = new HashSet<String>();
		// while (ngr.incrementToken()) {
		// OffsetAttribute s = ngr.getAttribute(OffsetAttribute.class);
		// String oneWord = "" + ngr.getAttribute(CharTermAttribute.class);
		//
		// System.out.println(oneWord);
		// if (lineSet.contains(oneWord) == false) {
		// lineSet.add(oneWord);
		// }
		// }

	}

}
