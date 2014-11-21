package org.z.web.common;

import java.io.IOException;
import java.util.HashSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.zk.ServerDict;
import org.z.store.redis.RedisPool;
import com.mongodb.BasicDBObject;

public class RequestFilter implements Filter {

	protected String encoding = null;
	protected FilterConfig filterConfig = null;
	protected boolean ignore = true;
	protected static HashSet<String> filterExts = new HashSet<String>();
	protected static Logger logger = LoggerFactory.getLogger(RequestFilter.class);

	@Override
	public void destroy() {
		this.encoding = null;
		this.filterConfig = null;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (ignore || (request.getCharacterEncoding() == null)) {
			String characterEncoding = selectEncoding(request);
			if (characterEncoding != null) {
				request.setCharacterEncoding(characterEncoding);
				response.setCharacterEncoding(characterEncoding);
			}
		}
		if (request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest) request;
			HttpServletResponse resp = (HttpServletResponse) response;
			String uri = req.getServletPath();
			int index = uri.lastIndexOf(".");
			String ext = null;
			if (index > 0) {
				ext = uri.substring(index + 1, uri.length());
				if (filterExts.contains(ext)) {
					chain.doFilter(request, response);
					return;
				}
			}
			String[] urls = StringUtils.split(uri, '/');
			String pageName = urls[0];
			BasicDBObject oPage = ServerDict.self.pageListener.byId(pageName);
			if (oPage != null) {
				response.setContentType("text/html; charset=utf-8");
				response.setCharacterEncoding("utf-8");
				request.setCharacterEncoding("utf-8");
				Context context = new Context(false, req, resp);
				response.setCharacterEncoding(this.encoding);
				if (oPage.getBoolean("auth", false) == true && context.userId() == 0) {
					context.forceLogin(uri);
				}
				context.readHtmlPage(pageName);
				return;
			}
		}
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		RedisPool.use();
		logger.info("RequestFilter  init");
		filterExts.add("jsp");
		filterExts.add("htm");
		filterExts.add("css");
		filterExts.add("png");
		filterExts.add("gif");
		filterExts.add("jpg");
		filterExts.add("jpeg");
		filterExts.add("js");
		this.filterConfig = fConfig;
		this.encoding = fConfig.getInitParameter("encoding");
		String value = fConfig.getInitParameter("ignore");
		if (value == null)
			this.ignore = true;
		else if (value.equalsIgnoreCase("true"))
			this.ignore = true;
		else if (value.equalsIgnoreCase("yes"))
			this.ignore = true;
		else
			this.ignore = false;
	}

	protected String selectEncoding(ServletRequest request) {
		return (this.encoding);
	}

}
