package org.z.data.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.data.analysis.SmartTokenizerFactory.TokenMode;

public final class SmartRepeatFilter extends TokenFilter{
	  protected static Logger logger = LoggerFactory.getLogger(SmartTokenizer.class);
	  private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
	  private final LevelAttribute levelAtt = addAttribute(LevelAttribute.class);
	private final CharArraySet previous = new CharArraySet(Version.LUCENE_46, 8, false);
	  private TokenMode mode=null;
	
	public SmartRepeatFilter(TokenStream input ,TokenMode mode) {
		super(input);
		this.mode=mode;
		logger.debug("SmartTokenizer Mode is [{}]",new Object[]{mode});
	}

	@Override
	public boolean incrementToken() throws IOException {
		  while (input.incrementToken()) {
		  boolean duplicate=false;
		  switch (mode) {
		  case search:
				 duplicate=rePeat(duplicate);
				break;
          case index:
        	  if( levelAtt.getLevel()!=1){
        		 duplicate=rePeat(duplicate);
    		      }
				break;
          case n_gramSeach:
        	  if( levelAtt.getLevel()==1){
        		 duplicate=rePeat(duplicate);
    		      }
				break;
			}
		      if (!duplicate) {
		        return true;
		      }
		    }
		    return false;
	}
	private boolean  rePeat(boolean duplicate){
	         final char term[] = termAttribute.buffer();
	         final int length = termAttribute.length();
	         duplicate = (previous.contains(term, 0, length));
		      char saved[] = new char[length];
		      System.arraycopy(term, 0, saved, 0, length);
		      previous.add(saved);
		      return duplicate;
	}

}
