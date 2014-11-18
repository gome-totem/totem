package org.x.server.email;

import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

public class EDMConfig {
	//protected static Logger logger = LoggerFactory.getLogger(Logger.getLogger("SYSLOG"));	
	protected static Logger logger = Logger.getLogger("SYSLOG");
	private ResourceBundle rb = null;
	private static EDMConfig edmconfig = null;
		
		static {
			logger.info("load edm properities");
			edmconfig = new EDMConfig(ResourceBundle.getBundle("eyeconfig"));
		}
		
		public EDMConfig(ResourceBundle rb) {
			this.rb = rb;
		}
		
		public static EDMConfig edm() {
			return edmconfig;
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
			if (value.isEmpty() || value == null) {
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
		
		public long getLongItem(String item, String defaultValue) {
			long i = 0;
			String value = getItem(item, defaultValue);
			try {
				i = Long.parseLong(value);
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
	
