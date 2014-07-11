package org.z.data.analysis;

import java.io.Reader;
import java.util.Map;

import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeSource.AttributeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x.segment.dict.Dictionary;
import org.z.common.tree.RadixTreeImpl;
import org.z.global.environment.Const;
import org.z.global.util.StringUtil;



public class SmartTokenizerFactory extends TokenizerFactory {
	protected static Logger logger = LoggerFactory.getLogger(SmartTokenizerFactory.class);
	public static RadixTreeImpl<String> trie =null;

	
	public static enum TokenMode {
		search, index,n_gramSeach
	}

	public static boolean init() {
		Dictionary.init();
		trie = new RadixTreeImpl<String>();
		 String list=StringUtil.loadFileContent(Const.ConfigPath+"/split/en.txt", "utf-8", true);
		 String[] ens =list.split("\\s");
		 for(String s :ens){
			  trie.insert(s.trim(), s.trim());
		 }
		return true;
	}


	public SmartTokenizerFactory(Map<String, String> args) {
		super(args);
		if (!args.isEmpty()) {
			throw new IllegalArgumentException("Unknown parameters: " + args);
		}
	}

	@Override
	public SmartTokenizer create(AttributeFactory factory, Reader input) {
		return new SmartTokenizer(factory, input);
	}
}
