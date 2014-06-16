package org.z.data.analysis;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.z.data.analysis.SmartTokenizerFactory.TokenMode;

import com.mongodb.BasicDBObject;

public class Test extends Thread {
	private List<BasicDBObject> list;
	private boolean isN = false;

	public Test(List<BasicDBObject> list, boolean isN) {
		this.list = list;
		this.isN = isN;
	}

	SmartAnalyzer sm = new SmartAnalyzer();

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		Iterator<BasicDBObject> it = list.iterator();
		while (it.hasNext()) {
			BasicDBObject keyword = it.next();
			String li = keyword.getString("item");
			StringReader sr = new StringReader(li);
			SmartTokenizer src = new SmartTokenizer(sr);
			SmartTokenizerFactory.init();
			src.init(sr);
			TokenStream ngr = src;
			if (isN) {
				ngr = new SmartWordFilter(ngr, 2, 2, TokenMode.search);
			}
			ngr = new SmartRepeatFilter(ngr, TokenMode.index);
			// TokenStream ngr = new PorterStemFilter(src);
			// ThaiWordFilter ngr = new SnowballFilter(Version.LUCENE_42, src);
			try {
				System.out.println(li);
				while (ngr.incrementToken()) {
					OffsetAttribute s = ngr.getAttribute(OffsetAttribute.class);
					LevelAttribute a = ngr.getAttribute(LevelAttribute.class);
					System.out.print(s.startOffset() + "(" + ngr.getAttribute(CharTermAttribute.class) + ")" + s.endOffset() + ",");
					System.out.println(" this word level =============" + a.getLevel());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}

	}

}
