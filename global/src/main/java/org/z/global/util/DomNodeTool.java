package org.z.global.util;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DomNodeTool {

	protected static void doGetOuterHtml(Node node, StringBuffer buff) {
		if (node == null) {
			return;
		} else if (node.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
			String str = reduceWhitespace(node.getNodeValue());
			buff.append(str);
		} else {
			buff.append("<");
			buff.append(node.getNodeName());
			buff.append(" ");
			NamedNodeMap attrs = node.getAttributes();
			if (attrs != null)
				for (int i = 0; i < attrs.getLength(); i++) {
					org.w3c.dom.Node n = attrs.item(i);
					String name = n.getNodeName();
					String value = n.getNodeValue();
					buff.append(name);
					buff.append("=\"");
					buff.append(value);
					buff.append("\" ");
				}
			buff.append(">\n");
			NodeList bf = node.getChildNodes();
			for (int i = 0; i < bf.getLength(); i++) {
				Node childNode = bf.item(i);
				DomNodeTool.doGetOuterHtml(childNode, buff);
			}
			buff.append("</");
			buff.append(node.getNodeName());
			buff.append(">\n");

		}
	}

	public static String getInnerHtml(Node node) {
		if (node == null) {
			return "";
		} else if (node.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
			return reduceWhitespace(node.getNodeValue());
		} else if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
			NodeList childNodes = node.getChildNodes();
			if (childNodes.getLength() == 0) {
				return "";
			} else {
				StringBuffer buff = new StringBuffer();
				for (int i = 0; i < childNodes.getLength(); i++) {
					Node childNode = childNodes.item(i);
					DomNodeTool.doGetOuterHtml(childNode, buff);
				}
				return buff.toString();
			}
		} else {
			return "";
		}
	}

	public static void writeFile(String _fileName, String _content,
			String charSet) {
		try {
			FileOutputStream _fileStream = new FileOutputStream(_fileName);
			OutputStreamWriter _writer = new OutputStreamWriter(_fileStream,
					charSet);
			_writer.write(_content);
			_writer.close();
			_fileStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getOutterHtml(Node node) {
		if (node == null) {
			return "";
		} else if (node.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
			return reduceWhitespace(node.getNodeValue());
		} else if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
			NodeList childNodes = node.getChildNodes();
			if (childNodes.getLength() == 0) {
				StringBuffer buff = new StringBuffer();
				buff.append("<");
				buff.append(node.getNodeName());
				buff.append(" ");
				NamedNodeMap attrs = node.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++) {
					org.w3c.dom.Node n = attrs.item(i);
					String name = n.getNodeName();
					String value = n.getNodeValue();
					buff.append(name);
					buff.append("=\"");
					buff.append(value);
					buff.append("\" ");
				}
				buff.append("/>");
				return buff.toString();
			} else {
				StringBuffer buff = new StringBuffer();
				buff.append("<");
				buff.append(node.getNodeName());
				buff.append(" ");
				NamedNodeMap attrs = node.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++) {
					org.w3c.dom.Node n = attrs.item(i);
					String name = n.getNodeName();
					String value = n.getNodeValue();
					buff.append(name);
					buff.append("=\"");
					buff.append(value);
					buff.append("\" ");
				}
				buff.append(">\n");
				buff.append(getInnerHtml(node));
				buff.append("\n</");
				buff.append(node.getNodeName());
				buff.append(">\n");
				return buff.toString();
			}
		} else {
			return "";
		}
	}

	public static String escapeText(String text) {
		if (text == null)
			return null;
		return text.replaceAll("\n|\r|\t", "").trim();
	}

	public static String getNodeDesc(Node node) {
		if (node == null)
			return "null";
		StringBuffer stringbuffer = new StringBuffer();
		stringbuffer.append((new StringBuilder(String.valueOf(node
				.getNodeName()))).append("[").toString());
		if (node.getAttributes() != null) {
			for (int i = 0; i < node.getAttributes().getLength(); i++)
				stringbuffer.append((new StringBuilder(String.valueOf(node
						.getAttributes().item(i).getNodeName()))).append("=")
						.append(node.getAttributes().item(i).getNodeValue())
						.append(" ").toString());

		} else if (node.getNodeType() == 3)
			stringbuffer.append(node.getTextContent());
		stringbuffer.append("]");
		return stringbuffer.toString();
	}

	public static String reduceWhitespace(final String text) {
		final StringBuilder buffer = new StringBuilder(text.length());
		boolean whitespace = false;
		for (final char ch : text.toCharArray()) {

			// Translate non-breaking space to regular space.
			if (ch == (char) 160) {
				buffer.append(' ');
				whitespace = false;
			} else {
				if (whitespace) {
					if (!Character.isWhitespace(ch)) {
						buffer.append(ch);
						whitespace = false;
					}
				} else {
					if (Character.isWhitespace(ch)) {
						whitespace = true;
						buffer.append(' ');
					} else {
						buffer.append(ch);
					}
				}
			}
		}
		return escapeText(buffer.toString());
	}

}
