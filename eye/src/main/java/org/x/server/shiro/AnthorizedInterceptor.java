package org.x.server.shiro;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UrlPathHelper;

public class AnthorizedInterceptor implements HandlerInterceptor{

	public void afterCompletion(HttpServletRequest arg0,
			HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
	}

	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
			Object arg2, ModelAndView arg3) throws Exception {
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object arg2) throws Exception {
		Subject currentUser = SecurityUtils.getSubject();
		String contextPath = request.getScheme()+"://"+request.getServerName()    +":"+request.getServerPort()+request.getContextPath()+"/";
		String lookupPath = new UrlPathHelper().getLookupPathForRequest(request);
		boolean unInclude=checkUnInclude(lookupPath);
		if(!unInclude &&!currentUser.isAuthenticated() ){
			System.out.println("-------------------会话Session无效，需重新登陆-----------");
			response.sendRedirect(contextPath);
			return false;
		}
		return true;
	}

	private boolean checkUnInclude(String lookupPath) {
		boolean flag= false;
		List<Object> unInclude=new ArrayList<Object>();
		unInclude.add("/cloud/eye/login");
		unInclude.add("/cloud/eye/verifyUser");
		unInclude.add("/cloud/business/edmServlet");
		unInclude.add("/dispacher/categoryAPI");
		for (Object object : unInclude) {
			if(StringUtils.equals(object.toString(), lookupPath)){
				flag=true;
				break;
			}
		}
		return flag;
	}

}
