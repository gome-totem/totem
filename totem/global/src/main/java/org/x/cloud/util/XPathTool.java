package org.x.cloud.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.x.cloud.util.StringUtil;

public class XPathTool {
	protected static XPathFactory factoryXpath = XPathFactory.newInstance();
	protected static TransformerFactory tf = TransformerFactory.newInstance();
	protected static XPath xPath = factoryXpath.newXPath();
	protected static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	public static Document parseFile(String file) {
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(file));
			return document;
		} catch (Exception E) {
			return null;
		}
	}

	public static Document parseContent(String content) {
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes("UTF-8"));
			Document document = builder.parse(stream);
			return document;
		} catch (Exception E) {
			return null;
		}
	}

	private static void readTextNode(StringBuilder buffer, Node node) {
		if (node == null) {
			return;
		}
		if (node.getNodeName().compareToIgnoreCase("br") == 0) {
			buffer.append("\r\n");
		} else if (node.getNodeType() == Node.TEXT_NODE) {
			buffer.append(node.getNodeValue());
		} else if (node.hasChildNodes()) {
			NodeList childList = node.getChildNodes();
			int childLen = childList.getLength();
			for (int count = 0; count < childLen; count++) {
				readTextNode(buffer, childList.item(count));
			}
		}
	}

	public static String extractText(String html) {
		Document doc = parseContent(html);
		StringBuilder buffer = new StringBuilder();
		readTextNode(buffer, doc);
		return buffer.toString();
	}

	public static String toXml(Document doc) {
		try {
			Transformer t = tf.newTransformer();
			t.setOutputProperty("encoding", "UTF-8");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DOMSource source = new DOMSource(doc);
			t.transform(source, new StreamResult(bos));
			return bos.toString("UTF-8");
		} catch (Exception e) {
			return "<error>" + e.getMessage() + "/<error>";
		}
	}

	public static String toXml(Node node) {
		try {
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			t.setOutputProperty(OutputKeys.METHOD, "xml");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DOMSource source = new DOMSource(node);
			t.transform(source, new StreamResult(bos));
			return bos.toString("UTF-8");
		} catch (Exception e) {
			return "<error>" + e.getMessage() + "/<error>";
		}
	}

	public static String toHtml(Node node) {
		try {
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			t.setOutputProperty(OutputKeys.METHOD, "html");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DOMSource source = new DOMSource(node);
			t.transform(source, new StreamResult(bos));
			return bos.toString("UTF-8");
		} catch (Exception e) {
			return "<error>" + e.getMessage() + "/<error>";
		}
	}

	public static String preProcessXPath(String xpath) {
		final char[] charArray = xpath.toCharArray();
		processOutsideBrackets(charArray);
		xpath = new String(charArray);

		final Pattern pattern = Pattern.compile("(@[a-zA-Z]+)");
		final Matcher matcher = pattern.matcher(xpath);
		while (matcher.find()) {
			final String attribute = matcher.group(1);
			xpath = xpath.replace(attribute, attribute.toLowerCase());
		}
		return xpath;
	}

	/**
	 * Lower case any character outside the brackets.
	 * 
	 * @param array
	 *            the array to change
	 */
	private static void processOutsideBrackets(final char[] array) {
		final int length = array.length;
		int insideBrackets = 0;
		for (int i = 0; i < length; i++) {
			final char ch = array[i];
			switch (ch) {
			case '[':
			case '(':
				insideBrackets++;
				break;

			case ']':
			case ')':
				insideBrackets--;
				break;

			default:
				if (insideBrackets == 0) {
					array[i] = Character.toUpperCase(ch);
				}
			}
		}
	}

	public static NodeList selectNodes(Node target, String xpath) {
		try {
			XPathExpression expr = xPath.compile(xpath);
			NodeList nodes = (NodeList) expr.evaluate(target, XPathConstants.NODESET);
			return nodes;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Node selectSingleNode(Node target, String xpath) {
		try {
			NodeList list = selectNodes(target, xpath);
			if (list.getLength() == 0) {
				return null;
			} else {
				return list.item(0);
			}
		} catch (Exception e) {
			return null;
		}
	}

	public static String getChildNodeValue(Node target, String childName) {
		Node childNode = selectSingleNode(target, childName);
		return getNodeValue(childNode);
	}

	public static String getChildNodeAttr(Node target, String childName, String attrName) {
		Node childNode = selectSingleNode(target, childName);
		if (childNode == null) {
			return null;
		}
		return getNodeAttribute(childNode, attrName);
	}

	public static String getNodeAttribute(Node target, String attrName) {
		return getNodeAttribute(target, attrName, true);
	}

	public static String getNodeAttribute(Node target, String attrName, boolean trim) {
		NamedNodeMap _attrs = target.getAttributes();
		if (_attrs == null) {
			return null;
		}
		target = _attrs.getNamedItem(attrName);
		if (target == null || target.getNodeValue() == null) {
			return null;
		}
		if (trim) {
			return StringUtil.trim(target.getNodeValue());
		} else {
			return target.getNodeValue();
		}
	}

	protected static String getNodeValue(Node target) {
		if (target == null) {
			return null;
		}
		Node _firstChild = target.getFirstChild();
		if (_firstChild == null) {
			return null;
		}
		String _text = _firstChild.getNodeValue();
		if (_text != null) {
			_text = StringUtil.trim(_text);
		}
		return _text;
	}

}
