package org.x.cloud.util;

public class HtmlDomFilter {

	public String tagName = null;
	public String method = null;
	public String[] params = null;
	public boolean includeChild = true;

	public HtmlDomFilter(String tagName, String method, String[] params) {
		this(tagName, method, params, true);
	}

	public HtmlDomFilter(String tagName, String method, String[] params, boolean includeChild) {
		this.tagName = tagName;
		this.method = method;
		this.params = params;
		this.includeChild = includeChild;
	}

}
