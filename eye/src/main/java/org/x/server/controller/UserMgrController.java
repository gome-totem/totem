package org.x.server.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.x.server.shiro.ShiroDBTools;

import com.mongodb.BasicDBObject;

@Controller
@RequestMapping(value = "/cloud/eye")
public class UserMgrController {
	@RequestMapping(value = "/user")
	protected void modifyPassword(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		BasicDBObject result = new BasicDBObject();
		Subject currentUser = SecurityUtils.getSubject();
		String username = (String) currentUser.getPrincipal();

		String pOldPwd = req.getParameter("uPwdOld");
		String pPwdNew = req.getParameter("uPwdNew");

		BasicDBObject user = ShiroDBTools.getUserByName(username);
		String sOldPwd = user.getString("password", "");
		// 验证旧密码是否一致
		if (!StringUtils.isEmpty(pOldPwd) && sOldPwd.equals(pOldPwd)) {
			if (ShiroDBTools.updatePasswordByName(username, pPwdNew) > 0) {
				result.append("msg", "success");
			} else {
				result.append("msg", "Error");
			}
		} else {
			result.append("msg", "unEq");
		}
		resp.getWriter().print(result);
	}
}
