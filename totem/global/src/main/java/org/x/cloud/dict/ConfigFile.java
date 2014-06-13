package org.x.cloud.dict;

import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.x.cloud.util.StringUtil;

public class ConfigFile {
	protected static Logger logger = LoggerFactory.getLogger(ConfigFile.class);
	private ResourceBundle rb = null;
	private static ConfigFile rockConfig = null;
	private static ConfigFile serverConfig = null;
	private static ConfigFile urlConfig = null;
	private static ConfigFile segment = null;

	static {
		logger.info("load rock properities & server & url & pinyin properities.");
		rockConfig = new ConfigFile(ResourceBundle.getBundle("rock"));
		serverConfig = new ConfigFile(ResourceBundle.getBundle("server"));
		urlConfig = new ConfigFile(ResourceBundle.getBundle("url"));
		segment = new ConfigFile(ResourceBundle.getBundle("segment"));
	}

	public ConfigFile(ResourceBundle rb) {
		this.rb = rb;
	}

	public static ConfigFile rock() {
		return rockConfig;
	}

	public static ConfigFile segment() {
		return segment;
	}

	public static ConfigFile server() {
		return serverConfig;
	}

	public static ConfigFile url() {
		return urlConfig;
	}

	public String getItem(String item, String defaultValue) {
		String value = null;
		if (rb != null) {
			try {
				value = rb.getString(item.trim());
				value = value.trim();
			} catch (Exception e) {
				value = defaultValue;
			}
		}
		if (StringUtil.isEmpty(value)) {
			value = defaultValue;
		}
		return value;
	}

	public int getIntItem(String item, String defaultValue) {
		int i = 0;
		String value = getItem(item, defaultValue);
		try {
			i = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			logger.info(e.getMessage());
		}
		return i;
	}

	public boolean getBooleanItem(String item, boolean defaultValue) {
		boolean b = false;
		String value = getItem(item, (new Boolean(defaultValue)).toString());
		if (value != null && value.equalsIgnoreCase("true")) {
			b = true;
		}
		return b;
	}

	protected String getNodeValue(Node _node) {
		if (_node == null) {
			return null;
		}
		Node _firstChild = _node.getFirstChild();
		if (_firstChild == null) {
			return null;
		}
		String _text = _firstChild.getNodeValue();
		if (_text != null) {
			_text = _text.trim();
		}
		return _text;
	}

}
