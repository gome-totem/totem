package org.z.data.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x.segment.parse.ChineseSplit;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public final class SmartTokenizer extends Tokenizer {
	public static final int DEFAULT_MIN_NGRAM_SIZE = 1;
	public static final int DEFAULT_MAX_NGRAM_SIZE = 2;
	private int maxTokenLength = SmartAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;
	protected static Logger logger = LoggerFactory.getLogger(SmartTokenizer.class);
	public static final int ZH = 0;
	public static final int EN = 1;
	public static final int SP = 2;
	public static final String[] TOKEN_TYPES = new String[] { "<ZH>", "<EN>", "<SP>" };
	private int pos = 0;
	private int startPos;
	private String tokenText;
	private int charsRead;
	@SuppressWarnings("unused")
	private boolean started;
	private boolean reserved;
	private BasicDBList tokenObjects = null;
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final LevelAttribute levelAtt = addAttribute(LevelAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

	public static ArrayList<String> readKeywors(String key) {
		return SmartTokenizerFactory.trie.searchPrefix(key, 1);

	}

	public SmartTokenizer() {
		super(null);
	}
	public SmartTokenizer(Reader input) {
		super(input);
	}

	public SmartTokenizer(AttributeFactory factory, Reader input) {
		super(factory, input);
	}

	public void init(Reader stream) {
		String[] searchList = { "-", "/", "-", "\"", "}", "{", "-", ":", "+", "[", "]", "　", "\\", "(", ")", "（", "）", "^", "o", "|" };
		String[] replacementList = { " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " " };
		char[] chars = new char[1024];
		int index = 0;
		int readSize;
		try {
			readSize = stream.read(chars, index, 1024);
			StringBuilder buffer = new StringBuilder();
			while (readSize > 0) {
				String c = new String(chars, 0, readSize).trim();
				buffer.append(c);
				readSize = stream.read(chars, index, 1024);
			}
			String context = buffer.toString().toLowerCase();
			context = StringUtils.replaceEach(context, searchList, replacementList);
			tokenObjects = handleSplit(context);
			// tokenObjects = SplitProcess(tokenObjects);
			for (int i = 0; i < tokenObjects.size(); i++) {
				BasicDBObject oToken = (BasicDBObject) tokenObjects.get(i);
				oToken.append("reserved", SmartTokenizerFactory.trie.searchPrefix(oToken.getString("w").toLowerCase(), 1).size() != 0);
			}
		} catch (IOException e) {
		}
	}

	/** Returns the next token in the stream, or null at EOS. */
	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();
		if (pos >= tokenObjects.size())
			return false;
		BasicDBObject oToken = (BasicDBObject) tokenObjects.get(pos);
		tokenText = oToken.getString("w");
		reserved = (Boolean) oToken.get("reserved");
		if (!StringUtils.isAsciiPrintable(tokenText)) {
			addString(1);
			typeAtt.setType(SmartTokenizer.TOKEN_TYPES[SmartTokenizer.ZH]);
			return true;
		} else if (reserved) {
			addString(1);
			typeAtt.setType(SmartTokenizer.TOKEN_TYPES[SmartTokenizer.EN]);
			return true;
		} else {
			addString(2);
			typeAtt.setType(SmartTokenizer.TOKEN_TYPES[SmartTokenizer.SP]);
		}
		return true;
	}

	public void addString(int level) {
		levelAtt.setLevel(level);
		termAtt.setEmpty().append(tokenText);
		offsetAtt.setOffset(correctOffset(startPos), correctOffset(startPos + 1));
		startPos++;
		pos++;
	}

	@Override
	public void end() {
		// set final offset
		final int finalOffset = correctOffset(charsRead);
		this.offsetAtt.setOffset(finalOffset, finalOffset);
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		started = false;
		pos = 0;
	}

	public void setMaxTokenLength(int length) {
		this.maxTokenLength = length;
	}

	/** @see #setMaxTokenLength */
	public int getMaxTokenLength() {
		return maxTokenLength;
	}

	private BasicDBList handleSplit(String skuName) {
		BasicDBList tokens = ChineseSplit.toList(skuName);
		return tokens;
	}

	@SuppressWarnings("unused")
	private BasicDBList SplitProcess(BasicDBList basiclist) {
		String str_zh = "", str_en = "";
		BasicDBList clonelist = new BasicDBList();
		int app_zh = 0, app_en = 0;
		int count = 0;
		BasicDBObject zh = null, en = null;
		for (int i = 0; i < basiclist.size(); i++) {
			BasicDBObject oWord = (BasicDBObject) basiclist.get(i);
			String key = oWord.getString("w");
			if (oWord.getString("b") != null) {
				count++;
				if (count % 2 == 0) {
					zh = new BasicDBObject();
					zh.append("w", key);
					if (!str_zh.equals("")) {
						BasicDBObject zhs = new BasicDBObject();
						zhs.append("w", str_zh);
						str_zh = "";
						app_zh = 0;
						clonelist.add(zhs);
					}
					if (!str_en.equals("")) {
						BasicDBObject ens = new BasicDBObject();
						ens.append("w", str_en);
						str_en = "";
						app_en = 0;
						clonelist.add(ens);
					}
					clonelist.add(zh);
					continue;
				}
			}
			;
			if (key.length() == 1 && !StringUtils.isAsciiPrintable(key)) {
				app_zh++;
				str_zh += key;
			} else if (key.length() == 1) {
				app_en++;
				str_en += key;
			} else {
				if (app_zh >= 2) {
					zh = new BasicDBObject();
					zh.append("w", str_zh);
					clonelist.add(zh);
				} else if (str_zh != "") {
					zh = new BasicDBObject();
					zh.append("w", str_zh);
					clonelist.add(zh);
				}
				if (app_en >= 2) {
					en = new BasicDBObject();
					en.append("w", str_en);
					clonelist.add(en);
				} else if (str_en != "") {
					en = new BasicDBObject();
					en.append("w", str_en);
					clonelist.add(en);
				}
				clonelist.add(basiclist.get(i));
				app_zh = 0;
				app_en = 0;
				str_zh = "";
				str_en = "";
			}
		}
		if (app_zh > 0) {
			zh = new BasicDBObject();
			zh.append("w", str_zh);
			clonelist.add(zh);
		}
		if (app_en > 0) {
			en = new BasicDBObject();
			en.append("w", str_en);
			clonelist.add(en);
		}
		return clonelist;
	}

	public static void main(String[] args) {

	}

}
