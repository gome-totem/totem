package org.x.server.shiro;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;

/**
 * 验证规则：只要某一个角色符合条件就算验证成功
 * autor：xiong1989win@126.com
 * **/
public class AtListOneRolesAuthorizationFilter extends AuthorizationFilter{

	@Override
	protected boolean isAccessAllowed(ServletRequest request,
			ServletResponse response, Object mappedValue) throws Exception {
		Subject subject = getSubject(request, response);  
		String[] rolesArray = (String[]) mappedValue;
		if (rolesArray == null || rolesArray.length == 0){  
		    return true;  
		}  
		for(int i=0;i<rolesArray.length;i++){
			if(subject.hasRole(rolesArray[i])){  
				return true;  
			}  
		}  
		return false;  
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response)throws IOException {
		return super.onAccessDenied(request, response);
	}
	
}
