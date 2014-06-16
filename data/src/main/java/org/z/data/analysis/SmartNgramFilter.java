package org.z.data.analysis;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.data.analysis.SmartTokenizerFactory.TokenMode;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public final class SmartNgramFilter extends TokenFilter {
	protected SmartNgramFilter(TokenStream input) {
		super(input);
	}

	private int curTermLength;
	private int curPos;
	private String content = null;
	private boolean spMatch = true;
	private boolean started = true;
	private boolean getWord = true;
	private BasicDBList basicDBList = new BasicDBList();
	private int gramSize;
	private int i = 0, j = 0;
	private int start, end;
	private int Level;
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final LevelAttribute LevelAtt = addAttribute(LevelAttribute.class);
	protected static Logger logger = LoggerFactory.getLogger(SmartNgramFilter.class);
	private int minGram, maxGram;
	private TokenMode mode = null;

	public SmartNgramFilter(TokenStream in, int minGram, int maxGram, TokenMode mode) {
		super(in);
		if (minGram < 1) {
			throw new IllegalArgumentException("minGram must be greater than zero");
		}
		if (minGram > maxGram) {
			throw new IllegalArgumentException("minGram must not be greater than maxGram");
		}
		this.minGram = minGram;
		this.maxGram = maxGram;
		this.mode = mode;
		logger.debug("SmartWordFilter start N-Gram rang [{}]--[{}]", new Object[] { minGram, maxGram });
	}

	@Override
	public final boolean incrementToken() throws IOException {
		if (getWord) {
			while (input.incrementToken()) {
				int len = termAtt.length();
				if (!typeAtt.type().equals("<SP>") || len < minGram) {
					return true;
				}
				content = termAtt.toString();
				start = offsetAtt.startOffset();
				end = offsetAtt.endOffset();
				Level = LevelAtt.getLevel();
				BasicDBObject ob = new BasicDBObject();
				ob.append("w", content).append("s", start).append("e", end).append("l", Level);
				basicDBList.add(ob);
			}
			getWord = false;
		}
		int size = basicDBList.size();
		switch (mode) {
		case index:
			while (i < size) {// 保留数据返回
				BasicDBObject key = (BasicDBObject) basicDBList.get(i);
				returnToken(key);
				i++;
				return true;
			}
			break;

		}

		while (j < size) {
			BasicDBObject key = (BasicDBObject) basicDBList.get(j);
			if (spMatch)
				content = key.getString("w");
			return specialWord(key);
		}
		return false;
	}

	private boolean specialWord(BasicDBObject value) {// N-Gram
		if (spMatch) {
			String xString = letterSplit(content, value); // 字母匹配关键字，返回不匹配部分。
			if (!xString.equals(content)) {
				content = xString;
				spMatch = false;
				return true;
			}
		}
		if (started) {
			started = false;
			curTermLength = content.length();
			gramSize = minGram;
		}

		if (curTermLength <= gramSize) {
			addString(value, 3);
			return true;
		}
		termAtt.setEmpty().append(content, curPos, curPos + gramSize);
		LevelAtt.setLevel(3);
		offsetAtt.setOffset(value.getInt("s"), value.getInt("e"));
		curPos++;
		if (curPos + gramSize > curTermLength) {
			curPos = 0;
			if (gramSize < maxGram) {
				gramSize++;
			} else {
				started = true;
				spMatch = true;
				j++;
			}
		}
		return true;

	}

	private void addString(BasicDBObject key, int level) {
		termAtt.setEmpty().append(content);
		offsetAtt.setOffset(key.getInt("s"), key.getInt("e"));
		LevelAtt.setLevel(level);
		spMatch = true;
		started = true;
		j++;
	}

	private void returnToken(BasicDBObject key) {
		termAtt.setEmpty().append(key.getString("w"));
		offsetAtt.setOffset(key.getInt("s"), key.getInt("e"));
		LevelAtt.setLevel(key.getInt("l"));
	}

	private String letterSplit(String str, BasicDBObject a) {
		int index = 0;
		String maxToken = "";
		List<String> list = null;
		while (index < str.length()) {
			maxToken += str.charAt(index);
			list = SmartTokenizer.readKeywors(maxToken);
			if (list.size() != 0) {
				String key = list.get(0);
				if (maxToken.equalsIgnoreCase(key)) {
					termAtt.setEmpty().append(key);
					LevelAtt.setLevel(2);
					offsetAtt.setOffset(a.getInt("s"), a.getInt("e"));
					str = str.substring(index + 1);
					return str;
				}
			}
			index++;
		}
		return str;
	}

	public static void main(String[] args) {
	}
}
