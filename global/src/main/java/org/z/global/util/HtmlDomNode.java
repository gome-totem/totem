package org.z.global.util;

import java.util.LinkedHashSet;

import org.w3c.dom.Node;

public class HtmlDomNode {
	public Node domNode = null;
	public HtmlDomNode parent = null;
	public LinkedHashSet<HtmlDomNode> childs = new LinkedHashSet<HtmlDomNode>();

	public HtmlDomNode(Node domNode) {
		this.domNode = domNode;
	}

	public HtmlDomNode addChild(Node domNode) {
		HtmlDomNode node = new HtmlDomNode(domNode);
		node.parent = this;
		childs.add(node);
		return node;
	}

	public void remove() {
		HtmlDomNode parent_ = this.parent;
		if (parent_ != null) {
			parent_.removeChild(this);
		}
	}

	public boolean  removeChild(HtmlDomNode node) {
		return childs.remove(node);
	}
}
