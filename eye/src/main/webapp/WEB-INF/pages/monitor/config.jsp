<%@page import="javax.servlet.http.HttpSession"%>
<%@page import="com.gome.totem.sniper.util.Globalkey"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="eye" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	HttpSession mySession = request.getSession();
	Object dict = session.getServletContext().getAttribute(Globalkey.DICT_SERVER);
 %>
<!DOCTYPE html>
<html>
  <head>
    <title>云眼-DICT配置页面</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <meta name="description" content="main pape of manager zookeeper"/>
    <meta name="author" content="dong zhou"/>

	<%@ include file="/pages/eye/jsp/common/commoncss.jspf" %>
    
  </head>
  <body>
	<div id="wrap">
		
		<div class="container">
			<div class="page-header">
				<div class="span2" style="font-family: 微软雅黑; font-size: 24px; font-weight: bold;line-height: 27px;margin-left: 0px;">初始化服务器</div>
				<div class="span9 form-inline">
					<input type="text" data-toggle-name="add-server-ip" class="input-medium" placeholder="Ip Address">
					<div class="btn-group" data-toggle-name="add-router-app-radio" data-toggle="buttons-radio">
						<button type="button" class="btn " data-toggle-type="router" >Router</button>
						<button type="button" class="btn " data-toggle-type="app">App</button>
					</div>
					<button data-toggle="button" class="btn" data-toggle-name="add-router-app-btn" data-placement="right" data-toggle="popover" data-original-title="">确定</button>
					<% if(dict !=null ){  %>
					<button data-toggle="button" class="btn btn-primary" id="init-as-last-config" style="float: right; margin-right: 10px;">按上次配置的启动</button>
					<%} %>
				</div>
				<p><button class="btn btn-primary" disabled="disabled" id="add-router-app-finish-btn">完成</button></p>
			</div>
			<div class="lead" style="color:#DD1144;"></div>
			<div class="span5" id="right_head"></div>
			<div><div id="zooTree-init"></div></div>
		</div>
		<div id="push"></div>
	</div>
		
	<div class="well" style="display:none;">
		<ul id="zooNav-init">
			<li><span>ZOOKEEPER</span>
				<ul></ul>
			</li>
		</ul>
	</div>
	
	<div class="eye-hide">
		<span id="dictval"><%=dict %></span>
	</div>
	<div id="delete-update-server-in-init" title="修改或删除节点" class="eye-hide">
		<p class="btn-group" id="delete-update-server-radio" data-toggle="buttons-radio">
			<button type="button" name="delate" class="btn delete-btn">删除此节点</button>
			<button type="button" name="update" class="btn undate-btn">修改节点名称</button>
		</p>
		<input type="text" name="name" id="delete-update-server-input" class="text ui-widget-content ui-corner-all input-medium eye-hide" placeholder="修改节点名称"/>
	</div>
	<div id="delete-update-server-in-finish" title="初始化提示" class="eye-hide">
		<p>开始初始化服务器</p>
	</div>
	<div id="start-as-last-modify-div" title="初始化提示" class="eye-hide">
		<p>确定按上次的配置启动服务器?</p>
	</div>
	<%@ include file="../footer.jsp" %>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery.orgchart.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-error.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-moni-config.js"></script>
	
  </body>
</html>
