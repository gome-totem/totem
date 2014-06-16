package org.z.global.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonTool {
	protected static Logger logger = LoggerFactory.getLogger(JsonTool.class);

	public static String toJsonForDataTable(int totalCount, String[][] rows) {
		if (totalCount == 0 && rows != null) {
			totalCount = rows.length;
		}
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append("{");
			if (rows == null || rows.length == 0) {
				buffer.append("'iTotalRecords':0,");
				buffer.append("'iTotalDisplayRecords':0,");
			} else {
				buffer.append("'iTotalRecords':" + totalCount + ",");
				buffer.append("'iTotalDisplayRecords':" + totalCount + ",");
			}
			buffer.append("'aaData':[");
			for (int i = 0; rows != null && i < rows.length; i++) {
				String[] values = rows[i];
				buffer.append("[");
				for (int m = 0; m < values.length; m++) {
					String value = values[m];
					if (value != null) {
						value = escape(value);
					}
					buffer.append("'" + value + "'");
					if (m < values.length - 1) {
						buffer.append(",");
					}
				}
				buffer.append("]");
				if (i < rows.length - 1) {
					buffer.append(",");
				}
			}
			buffer.append("]");
			buffer.append("}");
			return buffer.toString();
		} catch (Exception e) {
			logger.error("toJsonForDataTable", e);
			return "";
		}
	}

	public static String escape(String content) {
		if (content == null)
			return "";
		content = content.replaceAll("\r\n", "\\\\n").replaceAll("\n", "\\\\n");
		StringBuffer sb = new StringBuffer();
		for (int i = 0, len = content.length(); i < len; i++) {
			char c = content.charAt(i);
			switch (c) {
			case '&':
				sb.append("&amp;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case '\'':
				sb.append("&apos;");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

}
