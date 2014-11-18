<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="eye" uri="http://java.sun.com/jsp/jstl/core"%>
<script type="text/javascript">
	if (window.parent.length > 0) {
		window.parent.location = "/login.jsp";
	}
</script>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
    <title>欢迎登陆GOME云眼系统</title>
    <link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/base.css" />
</head>
<body>
<div class="loginBox">
    <div class="loginHd">
        <div class="logo"></div>
        <div class="text">国美云架构云眼系统</div>
    </div>
    <eye:set var="errorName" value="" />
    <eye:set var="errorPass" value="" />
    <eye:choose>
    	<eye:when test="${loginStatus.type eq 'name' }">
    		<eye:set var="errorName" value="${loginStatus.error }" />
    	</eye:when>
    	<eye:otherwise>
    		<eye:set var="errorPass" value="${loginStatus.error }" />
    	</eye:otherwise>
    </eye:choose>
    <form action="/cloud/eye/login" method="post">
	    <div class="loginBd">
	        <div class="login-item">
	           	<input type="text" class="nText" placeholder="用户名"  tabindex="1" login="name" name="username">
	            <p class="tips error">${errorName }</p>
	        </div>
	        <div class="login-item">
	            <input type="password" class="nText" placeholder="密码"  name="password" login="password" tabindex="2">
	            <p class="tips error">${errorPass }</p>                      
	        </div>
	        <div class="login-item actions">
	            <input type="submit" class="btnnuw" hidefocus="true" readOnly tabindex="3" value="登录" />                    
	        </div> 
	    </div>
    </form>
    <div class="loginFt nLowLeight">
        <p>我们将专注于国美在线搜索技术的提高</p>
        <p>在这里您将有机会接触到当前最新的技术并有机会处理海量数据</p>
        <p>我们相信对每个工程师来说都将是一个精彩的经历！</p>
        <p>欢迎您的加入！</p>
        <p style="text-align:right">——— 国美在线搜索组</p>
    </div>
</div>
</body>
</html>
