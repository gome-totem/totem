<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags" %>
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
    <title>国美搜索Eye</title>
    <link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/base.css" />
	 <link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/business.css" />
</head>
<body>
<div class="demoWarp">
    <div class="demoTop clearfix">
        <a href="#"><div class="logo"></div></a>
        <ul class="nav">
            <li class="user" data="js-tipsBox">
                <i class="pic"><img src="/pages/images/touxiang.png" alt=""></i><em class="f">我的账户</em><b class="s"></b>
                <div class="tipsBox">
                    <p>您好！
                        <shiro:authenticated>
                    			<span style="color: black; float: none; font-size: 14px; font-weight: bold; font-family: 微软雅黑;">
                    				<b><shiro:principal/></b>
                    				&nbsp;&nbsp;&nbsp;&nbsp;<a href="/dispacher/userPage">修改密码</a>
                    			</span>
                    			<span style="float:right;width: 40px;">
                    				<a href="/cloud/eye/logout">退出</a>
                    			</span>
                    		</shiro:authenticated>
                			<shiro:notAuthenticated>
                				<a href="/login.jsp">请登录</a>		
                			</shiro:notAuthenticated>
                    	</p>
                    <p class="l">欢迎使用国美云架构系统(TOTEM)监控管理系统</p>
                    
                </div>
            </li>
            <li class="notice" data="js-tipsBox">
                <i class="ico"><em class="nums">2</em></i><b class="s"></b>
                <div class="tipsBox" style="width: 130px;">
                    <span><a href="javascript:void(0)">代办事项(待开发)</a></span>
                    <span><a href="javascript:void(0)">消息提醒(待开发)</a></span>
                </div>
            </li>
            <li class="links"><a id="tg_team" href="/dispacher/team">开发团队</a></li>
            
            <shiro:hasAnyRoles name="super,admin,busi,monitor,show,dataAnalyse,test">
	            <li class="links"><a id="tg_dataAnalyse" href="/dispacher/dataAnalyse">大数据分析</a></li>
	         </shiro:hasAnyRoles>
	         <shiro:hasAnyRoles name="super,admin,monitor,show,dataAnalyse,test,dev">
	            <li class="links"><a id="tg_monitor" href="/dispacher/monitorPage">监控系统</a></li>
	         </shiro:hasAnyRoles>
	         
	         <shiro:hasAnyRoles name="super,admin,busi,monitor,dataAnalyse,show,test,pubbusi,bz_dev">
	            <li class="links"><a id="tg_logManage" href="/dispacher/logManager">日志管理</a></li>
	         </shiro:hasAnyRoles>
	         
	         <shiro:hasAnyRoles name="super,admin,busi,monitor,show,test,prom">
	            <li class="links"><a id="tg_searchConfig" href="/dispacher/searchConfig">搜索设置</a></li>
            </shiro:hasAnyRoles>
            
            <shiro:hasAnyRoles name="super">
	            <li class="links"><a id="tg_searchManager" href="/dispacher/searchManager">站长工具</a></li>
            </shiro:hasAnyRoles>
        </ul>
    </div>