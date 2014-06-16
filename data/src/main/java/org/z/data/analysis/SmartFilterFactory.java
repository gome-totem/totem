package org.z.data.analysis;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.z.data.analysis.SmartTokenizerFactory.TokenMode;

public class SmartFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {
	protected SmartFilterFactory(Map<String, String> args) {
		super(args);
	}

	public String mode = "mode";

	@Override
	public void inform(ResourceLoader loader) throws IOException {
		this.mode = this.getOriginalArgs().get(mode);
	}

	@Override
	public TokenStream create(TokenStream input) {
		if (this.mode.equalsIgnoreCase("word")) {
			return new SmartWordFilter(input, 3, 5, TokenMode.search);
		} else if (this.mode.equalsIgnoreCase("pharse")) {
			return new SmartPharseFilter(input);
		} else {
			return input;
		}
	}
}
