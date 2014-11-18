package org.x.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.x.server.eye.Eye;

import com.gome.totem.sniper.util.Globalkey.ServletStatus;
import com.mongodb.BasicDBObject;

@Controller
@RequestMapping(value = "/cloud/eye")
public class LoginController {

	/**
	 * 登录
	 */
	private static final long serialVersionUID = 1971269617921229916L;

	private BasicDBObject loginStatus = null;

	@RequestMapping(value = "/login")	
	public ModelAndView login(HttpServletRequest req, HttpServletResponse resp) {
		String username = req.getParameter("username");
		String password = req.getParameter("password");
		BasicDBObject info = new BasicDBObject();

		Subject user = SecurityUtils.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken(username,
				password);
		token.setRememberMe(true);

		try {
			user.login(token);
			info.append("context", "登陆成功！");
			return this.pageForward(req, resp, ServletStatus.login);
		} catch (UnknownAccountException uae) {
			loginStatus = new BasicDBObject().append("type", "name").append(
					"error", "用户名不存在！");
			info.append("context", "登陆失败，原因：用户名不存在！");
			return this.pageForward(req, resp, ServletStatus.none);
		} catch (IncorrectCredentialsException ice) {
			loginStatus = new BasicDBObject().append("type", "pass").append(
					"error", token.getPrincipal() + " 的密码错误！");
			info.append("context", "登陆失败，原因：" + token.getPrincipal()
					+ " 的密码错误！");
			return this.pageForward(req, resp, ServletStatus.none);
		} catch (LockedAccountException lae) {
			loginStatus = new BasicDBObject().append("type", "name").append(
					"error", token.getPrincipal() + " 用户被冻结，请联系管理员解除！");
			info.append("context", "登陆失败，原因：" + token.getPrincipal() + " 被冻结！");
			return this.pageForward(req, resp, ServletStatus.none);
		} catch (AuthenticationException ae) {
			loginStatus = new BasicDBObject().append("type", "name").append(
					"error", "登录失败！");
			info.append("context", "登陆失败，原因：密码错误！");
			return this.pageForward(req, resp, ServletStatus.none);
		} finally {
			Eye.eyeLogger.printLog(info.append("action", "login"));
		}
	}
	@RequestMapping(value="/logout")
	public ModelAndView logout(HttpServletRequest req, HttpServletResponse resp) {
		Subject currentUser = SecurityUtils.getSubject();
		Eye.eyeLogger.printLog(new BasicDBObject().append("action", "logout")
				.append("context", "登出操作！"));
		currentUser.logout();
		return new ModelAndView("../../../login.jsp");
	}
	
	@RequestMapping(value="/verifyUser")
	public void verifyUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Subject currentUser = SecurityUtils.getSubject();
		resp.getWriter().println(currentUser.isAuthenticated());
	}

	protected ModelAndView pageForward(HttpServletRequest request,
			HttpServletResponse response, ServletStatus status) {
		if (ServletStatus.login.equals(status)) {
			return new ModelAndView("/firstPage.jsp");
		} else {
			request.setAttribute("loginStatus", loginStatus);
			return new ModelAndView("../../../login.jsp");
		}
	}
}
