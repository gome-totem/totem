package org.x.server.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.x.server.email.SendEmail;

import com.gome.totem.sniper.util.Globalkey;
import com.mongodb.BasicDBObject;
@Controller
@RequestMapping("/cloud/eye")
public class MailSendController {

	
	@RequestMapping(value = "/mailSend")	
	public void mailSend(HttpServletRequest req, HttpServletResponse resp) {
		String erro404=req.getParameter("data404");
		String erro500=req.getParameter("data500");
		String erro999=req.getParameter("data999");
		String erro888=req.getParameter("data888");
		String erroPercent=req.getParameter("percent");
		String mailReceivers=Globalkey.mailReceiver;
		String content=createContent(erro404,erro500,erro999,erro888,erroPercent);
		if(erro404.isEmpty() && erro500.isEmpty() && erro999.isEmpty() &&erro888.isEmpty()){
			return;
		}
		if(mailReceivers!=null){
			String[] receivers =mailReceivers.split(",");
			for (String receiver : receivers) {
				BasicDBObject info = new BasicDBObject();	
				info.append("email", receiver).append("subject", "Tomcat服务监控异常情况").append("content", content);
				send(info);
			}
		}
	}

	private String createContent(String erro404, String erro500, String erro999,String erro888,String erroPercent) {
		StringBuffer staticContent=new StringBuffer("紧急：Tomcat服务监控异常情况,如下:");
		if(erro404!=null && erro404.length()>0){
			staticContent.append("   -请求服务资源不存在404的地址为："+erro404.replace("|", ",       "));
		}
		if(erro500!=null && erro500.length()>0){
			staticContent.append("   -请求服务异常500的地址为："+erro500.replace("|", ",       "));
		}
		if(erro999!=null && erro999.length()>0){
			staticContent.append("   -请求服务连接错误的地址为："+erro999.replace("|", ",      "));
		}
		if(erro888!=null && erro888.length()>0){
			staticContent.append("   -请求服务连接超时的地址为："+erro888.replace("|", ",      "));
		}
		staticContent.append("请相关人员查明原因并解决！");
		if(Double.parseDouble(erroPercent)>30){
			staticContent.append("，紧急联系人方式：丁宏波-13426290206，蒋礼俊-18701614182");
		}
		return staticContent.toString();
	}

	private void send(BasicDBObject info) {
		SendEmail.useWebPowerSendEmail(info);
	}
	
}
