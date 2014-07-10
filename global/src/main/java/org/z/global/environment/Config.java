package org.z.global.environment;

import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config extends ConfigFile {

	protected static Logger logger = LoggerFactory.getLogger(Config.class);
	private static Config dbConfig = null;
	private static Config segmentConfig = null;
	private static Config timerConfig = null;

	static {
		logger.info("load db & rock & segment properities.");
		dbConfig = new Config(ResourceBundle.getBundle("db"));
		segmentConfig = new Config(ResourceBundle.getBundle("segment"));
		timerConfig = new Config(ResourceBundle.getBundle("timer"));
	}

	public Config(ResourceBundle rb) {
		super(rb);
	}

	public static Config db() {
		return dbConfig;
	}

	public static Config segment() {
		return segmentConfig;
	}

	public static Config timer() {
		return timerConfig;
	}

}
