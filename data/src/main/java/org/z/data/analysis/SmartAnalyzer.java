package org.z.data.analysis;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;
import org.z.data.analysis.SmartTokenizerFactory.TokenMode;

public class SmartAnalyzer extends StopwordAnalyzerBase {

	/** Default maximum allowed token length */
	public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

	public static enum Mode {
		pharse, word
	}

	public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

	private Mode mode = null;
	private int minGram, maxGram;

	public SmartAnalyzer() {
		super(Version.LUCENE_46, STOP_WORDS_SET);
	}

	public SmartAnalyzer(Mode mode, int minGram, int maxGram) {
		super(Version.LUCENE_46, STOP_WORDS_SET);
		this.mode = mode;
		this.minGram = minGram;
		this.maxGram = maxGram;
	}

	/**
	 * Set maximum allowed token length. If a token is seen that exceeds this
	 * length then it is discarded. This setting only takes effect the next time
	 * tokenStream or tokenStream is called.
	 */
	public void setMaxTokenLength(int length) {
		maxTokenLength = length;
	}

	/**
	 * @see #setMaxTokenLength
	 */
	public int getMaxTokenLength() {
		return maxTokenLength;
	}

	@Override
	protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
		final SmartTokenizer src = new SmartTokenizer(reader);
		src.setMaxTokenLength(maxTokenLength);
		TokenStream tok = null;
		switch (mode) {
		case word:
			tok = new SmartWordFilter(src, minGram, maxGram, TokenMode.search);
			break;
		case pharse:
			tok = new SmartPharseFilter(src);
			break;
		}
		return new TokenStreamComponents(src, tok) {
			@Override
			protected void setReader(final Reader reader) throws IOException {
				src.setMaxTokenLength(SmartAnalyzer.this.maxTokenLength);
				super.setReader(reader);
			}
		};
	}

}
