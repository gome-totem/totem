package org.x.server.controller;

import java.net.UnknownHostException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.x.server.service.IDictConfig;

import com.gome.totem.sniper.util.Globalkey;
import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
@Controller
@RequestMapping(value="/eye/data")
public class DictConfigController {
	@Autowired
	private IDictConfig dict; 
	
	@RequestMapping(value = "/dictConfig")
    public void  dictConfig(HttpServletRequest req,HttpServletResponse resp) throws UnknownHostException {
		dict.list(req,resp);
	}
	
	@RequestMapping(value = "/addServer")
    public void  addServer(HttpServletRequest req,HttpServletResponse resp) throws UnknownHostException {
		dict.addServer(req,resp);
	}
	@RequestMapping(value = "/delServer")
    public void  delServer(HttpServletRequest req,HttpServletResponse resp) throws UnknownHostException {
		dict.delServer(req,resp);
	}
	@RequestMapping(value = "/updateServerExe")
    public void  updateServer(HttpServletRequest req,HttpServletResponse resp) throws UnknownHostException {
		dict.updateServer(req,resp);
	}
	@RequestMapping(value = "/checkData")
    public void  checkData(HttpServletRequest req,HttpServletResponse resp) throws UnknownHostException {
		dict.checkData(req,resp);
	}
	
	@RequestMapping(value = "/showServer")
    public ModelAndView  showServer(HttpServletRequest req,HttpServletResponse resp) throws UnknownHostException {
		Map<String, Object> map=dict.showServer(req,resp);
		return new ModelAndView("/dictData/showService.jsp",map);
	}
	
	@RequestMapping(value = "/getServices")
    public void  getServices(HttpServletRequest req,HttpServletResponse resp) throws UnknownHostException {
		String data=Globalkey.allServices;
		BasicDBList result=new BasicDBList();
		if(StringUtils.isNotBlank(data)){
			String[] datas=data.split(",");
			for (int i = 0; i < datas.length; i++) {
				result.add(new BasicDBObject("value",datas[i]).append("text", datas[i]));
			}
		}
		Write2PageUtil.write(resp, result);
	}
	
	@RequestMapping(value = "/addFilter")
    public void  addFilter(HttpServletRequest req,HttpServletResponse resp) throws UnknownHostException {
		BasicDBObject result=dict.addFilter(req,resp);
		Write2PageUtil.write(resp, result);
	}
	@RequestMapping(value = "/delFilter")
    public void  delFilter(HttpServletRequest req,HttpServletResponse resp) throws UnknownHostException {
		BasicDBObject result=dict.delFilter(req,resp);
		Write2PageUtil.write(resp, result);
	}
	
	@RequestMapping(value = "/initFilter")
    public void  initFilter(HttpServletRequest req,HttpServletResponse resp) throws UnknownHostException {
		BasicDBObject result=dict.initFilter(req,resp);
		Write2PageUtil.write(resp, result);
	}
	
	@RequestMapping(value = "/ascFilter")
    public void  ascFilter(HttpServletRequest req,HttpServletResponse resp) throws UnknownHostException {
		BasicDBObject result=dict.ascFilter(req,resp);
		Write2PageUtil.write(resp, result);
	}
	
}
