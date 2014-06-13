package org.x.cloud.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.cyberneko.html.parsers.DOMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.x.cloud.util.StringUtil;

public class HtmlDom {
	protected static Logger logger = LoggerFactory.getLogger(HtmlDom.class);
	public Document document;
	public String content = null;
	public HtmlDomNode rootNode = null;
	public StringBuilder textBuffer = null;
	private boolean isTextBR = false;
	public ArrayList<HtmlDomNode> leafs = new ArrayList<HtmlDomNode>();
	public HashMap<Node, HtmlDomNode> mapping = new HashMap<Node, HtmlDomNode>();
	public HashSet<String> reservedTagTable = new HashSet<String>();
	public HashMap<String, ArrayList<HtmlDomFilter>> filteredTags = new HashMap<String, ArrayList<HtmlDomFilter>>();

	public HtmlDom() {
		this.addReservedTag("img");
		this.addFilterTag("embed", "skip", null, true);
		this.addFilterTag("script", "skip", null, true);
		this.addFilterTag("div", "skip", null, false);
	}

	public Attr createAttribute(String name) {
		return document.createAttribute(name);
	}

	public void removeAttribute(Node node, String prefix) {
		NamedNodeMap attrs = node.getAttributes();
		int index = 0;
		prefix = prefix.toLowerCase();
		while (index < attrs.getLength()) {
			Node attr = attrs.item(index);
			String name = attr.getNodeName().toLowerCase();
			if (name.startsWith(prefix)) {
				attrs.removeNamedItem(attr.getNodeName());
				continue;
			}
			index++;
		}
	}

	public void addReservedTag(String tagName) {
		reservedTagTable.add(tagName.toLowerCase());
	}

	public void addFilterTag(String tagName, String method, String[] params, boolean includeChild) {
		tagName = tagName.toLowerCase();
		ArrayList<HtmlDomFilter> list = filteredTags.get(tagName);
		if (list == null) {
			list = new ArrayList<HtmlDomFilter>();
			filteredTags.put(tagName, list);
		}
		list.add(new HtmlDomFilter(tagName, method, params, includeChild));
	}

	private boolean hasNextContentAfterBR(Node node) {
		while (node != null) {
			if (node.getNodeName().equalsIgnoreCase("br")) {
				return false;
			}
			if (mapping.containsKey(node)) {
				return true;
			}
			node = node.getNextSibling();
		}
		return false;
	}

	public boolean removeNode(Node node) {
		HtmlDomNode domNode = mapping.get(node);
		if (domNode == null) {
			return false;
		}
		domNode.remove();
		mapping.remove(node);
		return true;
	}

	public void clean() {
		this.clean(true);
	}

	public void clean(boolean enable) {
		rootNode = new HtmlDomNode(this.selectSingleNode("//body"));
		leafs.clear();
		mapping.clear();
		textBuffer = new StringBuilder();
		isTextBR = false;
		doFilter(rootNode.domNode, rootNode);
		while (leafs.size() > 0 && enable) {
			HtmlDomNode node = leafs.get(0);
			String content = mapping.containsKey(node.domNode) ? "" : StringUtil.trim(node.domNode.getTextContent());
			while (node != null && !reservedTagTable.contains(node.domNode.getNodeName().toLowerCase()) && node.childs.size() == 0 && node.domNode.getNodeType() != org.w3c.dom.Node.TEXT_NODE
					&& content.length() <= 1) {
				Node sibling = node.domNode.getNextSibling();
				if (node.domNode.getNodeName().equalsIgnoreCase("br")) {
					if (hasNextContentAfterBR(sibling)) {
						break;
					}
				}
				node.remove();
				node = node.parent;
				content = mapping.containsKey(node.domNode) ? "" : StringUtil.trim(node.domNode.getTextContent());
			}
			leafs.remove(0);
		}
	}

	/*
	 * 0---not 1:skip current-node 2:skip current and its child
	 */
	private int isFilteredTag(Node node) {
		String content = StringUtil.trim(node.getTextContent());
		String tagName = node.getNodeName().toLowerCase();
		ArrayList<HtmlDomFilter> list = filteredTags.get(tagName);
		for (int i = 0; list != null && i < list.size(); i++) {
			HtmlDomFilter filter = list.get(i);
			if (filter.method.equalsIgnoreCase("skip")) {
				return filter.includeChild ? 2 : 1;
			} else if (filter.method.equalsIgnoreCase("contains")) {
				for (int t = 0; t < filter.params.length; t++) {
					if (content.indexOf(filter.params[t]) >= 0) {
						return filter.includeChild ? 2 : 1;
					}
				}
			} else if (filter.method.equalsIgnoreCase("attr") && filter.params.length >= 2) {
				String p1 = filter.params[0];
				String p2 = filter.params[1];
				Node attrNode = node.getAttributes().getNamedItem(p1);
				if (attrNode != null && attrNode.getNodeValue().trim().startsWith(p2)) {
					return filter.includeChild ? 2 : 1;
				}
			}
		}
		return 0;
	}

	private boolean isHiddenNode(Node node) {
		if (node.getAttributes() == null) {
			return false;
		}
		Node _attr = node.getAttributes().getNamedItem("style");
		if (_attr != null) {
			String value = _attr.getNodeValue();
			boolean result = value != null && value.replaceAll("\\s", "").indexOf("display:none") >= 0;
			if (result) {
				return true;
			}
		}
		_attr = node.getAttributes().getNamedItem("type");
		if (_attr != null) {
			String value = _attr.getNodeValue();
			boolean result = value != null && value.replaceAll("\\s", "").indexOf("hidden") >= 0;
			if (result) {
				return true;
			}
		}
		return false;
	}

	private boolean doFilter(Node node, HtmlDomNode _parentNode) {
		if (node == null) {
			return false;
		}
		int filterTag = isFilteredTag(node);
		if (filterTag == 2) {
			return false;
		}
		if (node.getNodeType() == Node.TEXT_NODE) {
			String value = StringUtil.trim(node.getNodeValue());
			if (value.length() <= 1) {
				return false;
			}
		} else if (isHiddenNode(node)) {
			return false;
		}
		HtmlDomNode _newParentNode = _parentNode;
		if (filterTag == 0) {
			_newParentNode = _parentNode.addChild(node);
			mapping.put(node, _newParentNode);
		}
		int _count = 0;
		if (node.getChildNodes() != null) {
			for (int i = 0; i < node.getChildNodes().getLength(); i++) {
				if (doFilter(node.getChildNodes().item(i), _newParentNode)) {
					_count++;
				}
			}
		}
		if (_count == 0) {
			leafs.add(_newParentNode);
		}
		return true;
	}

	public NodeList selectNodes(String xpath) {
		return this.selectNodes(document, xpath);
	}

	public NodeList selectNodes(Node target, String xpath) {
		xpath = XPathTool.preProcessXPath(xpath);
		NodeList list = XPathTool.selectNodes(target, xpath);
		return list;
	}

	public String[] readImageNode(Node node, boolean removeLocal, String[] prefixFilters) {
		String[] result = new String[2];
		result[0] = node.getAttributes().getNamedItem("src").getNodeValue();
		if (removeLocal && !result[0].startsWith("http://")) {
			return null;
		}
		for (int i = 0; prefixFilters != null && i < prefixFilters.length; i++) {
			if (result[0].startsWith(prefixFilters[i])) {
				return null;
			}
		}
		if (node.getParentNode().getNodeName().equalsIgnoreCase("a")) {
			result[1] = node.getParentNode().getAttributes().getNamedItem("href").getNodeValue();
		} else {
			result[1] = result[0];
		}
		return result;
	}

	public Node selectSingleNode(String xpath) {
		return this.selectSingleNode(document, xpath);
	}

	public Node selectSingleNode(Node target, String xpath) {
		xpath = XPathTool.preProcessXPath(xpath);
		return XPathTool.selectSingleNode(target, xpath);
	}

	private void doGetOuterNode(HtmlDomNode node, StringBuilder htmlBuffer, StringBuilder textBuffer) {
		if (node == null) {
			return;
		} else if (node.domNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
			String str = node.domNode.getNodeValue();
			htmlBuffer.append(str);
			textBuffer.append(str);
			isTextBR = false;
		} else {
			if (isTextBR == false && textBuffer.length() > 0 && node.domNode.getNodeName().equalsIgnoreCase("br")) {
				textBuffer.append("&nbsp;&nbsp;");
				isTextBR = true;
			}
			htmlBuffer.append("<");
			htmlBuffer.append(node.domNode.getNodeName());
			htmlBuffer.append(" ");
			NamedNodeMap attrs = node.domNode.getAttributes();
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					org.w3c.dom.Node n = attrs.item(i);
					String name = n.getNodeName();
					String value = n.getNodeValue();
					htmlBuffer.append(name);
					htmlBuffer.append("=\"");
					htmlBuffer.append(value);
					htmlBuffer.append("\" ");
				}
			}
			htmlBuffer.append(">\n");
			for (Iterator<HtmlDomNode> i = node.childs.iterator(); i.hasNext();) {
				HtmlDomNode childNode = i.next();
				doGetOuterNode(childNode, htmlBuffer, textBuffer);
			}
			htmlBuffer.append("</");
			htmlBuffer.append(node.domNode.getNodeName());
			htmlBuffer.append(">\n");
		}
	}

	public String getTextContent(int length) {
		String textContent = StringUtil.trimContinue(textBuffer);
		if (length == 0) {
			return textContent;
		}
		return StringUtils.abbreviate(textContent, length);
	}

	public String getNodeHtml(Node node) {
		HtmlDomNode domNode = this.mapping.get(node);
		if (domNode == null) {
			return null;
		}
		textBuffer = new StringBuilder();
		return getInnerHtml(domNode);
	}

	public String getInnerHtml(HtmlDomNode node) {
		if (node == null) {
			return "";
		} else if (node.domNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
			textBuffer.append(node.domNode.getNodeValue());
			isTextBR = false;
			return textBuffer.toString();
		} else if (node.domNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
			LinkedHashSet<HtmlDomNode> childNodes = node.childs;
			if (childNodes.size() == 0) {
				return "";
			} else {
				StringBuilder htmlBuffer = new StringBuilder();
				for (Iterator<HtmlDomNode> i = childNodes.iterator(); i.hasNext();) {
					HtmlDomNode childNode = i.next();
					doGetOuterNode(childNode, htmlBuffer, textBuffer);
				}
				return htmlBuffer.toString();
			}
		} else {
			return "";
		}
	}

	public String getInnerHtml() {
		StringBuilder buffer = new StringBuilder();
		for (Iterator<HtmlDomNode> i = this.rootNode.childs.iterator(); i.hasNext();) {
			HtmlDomNode node = i.next();
			buffer.append(getInnerHtml(node));
		}
		return buffer.toString();
	}

	public String getNodeAttrValue(Node node, String name) {
		if (node == null || node.getAttributes() == null) {
			return null;
		}
		Node attr = node.getAttributes().getNamedItem(name);
		if (attr == null) {
			return null;
		}
		return attr.getNodeValue();
	}

	public ArrayList<String> selectXPath(String path) {
		NodeList list = this.selectNodes(path);
		if (list == null || list.getLength() == 0) {
			return null;
		}
		ArrayList<String> values = new ArrayList<String>();
		for (int i = 0; i < list.getLength(); i++) {
			String value = list.item(i).getTextContent();
			if (value == null || value.length() == 0) {
				continue;
			}
			values.add(value.trim().replaceAll("\r", "").replaceAll("\n", ""));
		}
		return values;
	}

	public boolean load(String content, String charSet) {
		this.content = content;
		DOMParser parser = new DOMParser();
		XMLInputSource source = new XMLInputSource(null, "xiaoming", null, new StringReader(content), charSet);
		try {
			parser.setFeature("http://xml.org/sax/features/namespaces", false);
			parser.setProperty("http://cyberneko.org/html/properties/default-encoding", charSet);
			parser.parse(source);
			document = parser.getDocument();
		} catch (Exception e) {
			logger.error("load", e);
			return false;
		}
		return true;
	}

}
