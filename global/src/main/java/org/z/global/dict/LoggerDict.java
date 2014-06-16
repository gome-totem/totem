package org.z.global.dict;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LoggerDict {

	public static enum LogType {
		SYSLOG, SEARCHLOG, CLICKLOG, INDEXLOG
	}

	static {
		MDC.put("ip", Global.ServerIP);
	}

	public static Logger getLogger(LogType type) {
		Logger logger = LoggerFactory.getLogger("SYSLOG");
		switch (type) {
		case SEARCHLOG:
			logger = LoggerFactory.getLogger("SearchLog");
			break;
		case CLICKLOG:
			logger = LoggerFactory.getLogger("ClickLog");
			break;
		case INDEXLOG:
			logger = LoggerFactory.getLogger("IndexLog");
			break;
		}
		return logger;
	}


}
