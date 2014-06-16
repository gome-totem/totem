package org.z.data.analysis;

import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

public final class SmartPharseFilter extends TokenFilter {
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private int start, end;
	private int x, y;
	private int i = 1, j = 2;
	private InputWindowToken TokenArray[][] = new InputWindowToken[20][40];
	private int[] y_Ran = new int[20];
	private String beforeString;
	private String afterString;
	private String sp = " ";
	private InputWindowToken beforeToken;
	private InputWindowToken afterToken;
	private boolean getWord = true;

	/**
	 * Create a new GomePharseFilter
	 * 
	 */
	public SmartPharseFilter(TokenStream in) {
		super(in);
	}

	/**
	 * shingle 切词
	 * 
	 */
	@Override
	public final boolean incrementToken() throws IOException {
		while (getWord) { // 一次将词取完
			while (input.incrementToken()) {
				y++;
				beforeString = termAtt.toString();
				if (!StringUtils.isAsciiPrintable(beforeString)) {// 如果是汉字，直接返回
					y = 0;
					return true;
				}
				if (y == 1) {
					x++;
				}
				start = offsetAtt.startOffset();
				end = offsetAtt.endOffset();
				InputWindowToken TokenAdd = new InputWindowToken(beforeString,
						start, end);
				TokenArray[x][y] = TokenAdd; // 记录非汉字，
				y_Ran[x] = y;// 记录纵坐标
			}
			getWord = false;
		}
		while (i <= x) {
			if (TokenArray[i][2] == null) {
				InputWindowToken resultToken = TokenArray[i][1];
				beforeString = resultToken.termmbuff;
				start = resultToken.tokStart;
				end = resultToken.tokEnd;
				termAtt.setEmpty().append(beforeString);
				offsetAtt.setOffset(start, end);
				i++;
				return true;
			}
			while (j <= y_Ran[i]) {
				afterToken = TokenArray[i][j];
				beforeToken = TokenArray[i][j - 1];
				beforeString = beforeToken.termmbuff;
				start = beforeToken.tokStart;
				afterString = afterToken.termmbuff;
				end = afterToken.tokEnd;
				StringBuilder terms = new StringBuilder();
				terms.append(beforeString);
				terms.append(sp);
				terms.append(afterString);
				termAtt.setEmpty().append(terms);
				offsetAtt.setOffset(start, end);
				j++;
				return true;
			}
			j = 2;
			i++;
		}
		return false;
	}

	private class InputWindowToken {
		private String termmbuff;
		private int tokStart, tokEnd;

		public InputWindowToken(String termmbuff, int tokStart, int tokEnd) {
			this.termmbuff = termmbuff;
			this.tokStart = tokStart;
			this.tokEnd = tokEnd;
		}
	}
}
