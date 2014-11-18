<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>My JSP 'addDictConfig.jsp' starting page</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
  </head>
  
  <body>
  	<form id="ff" method="post" action="/eye/data/addServer">
    	<table>
    		<c:if test="${nickName!='' && nickName!=null}">
    		<tbody>
	    		<tr >
	    			<td colspan="2" align="center" height="30px;" style="background-color:#E6E6E6 ">
	    				${ip}---服务详细信息
	    			</td>
	    		</tr>
	    		<tr>
	    			<td align="right" class="addServer">
	    				<div >${nickName}</div>
	    			</td>
	    			<td class="addServer">
	    				<div >
	    					${tags}
	    				</div>
	    			</td>
	    		</tr>
    		</tbody>
    		</c:if>
    		<c:if test="${nickName==null || nickName==''}">
    			<tbody>
	    		<tr >
	    			<td colspan="2" align="center" height="30px;" style="background-color:#E6E6E6;width: 100%">
	    				 ---暂无服务信息---
	    			</td>
	    		</tr>
    		</tbody>
    		</c:if>
    	</table>
    </form>
  <script type="text/javascript" src="/pages/js/dict-data-config.js"></script>
  <style type="text/css">
	.addServer{
		padding-left: 20px;
		border: solid 1px #E6E6E6;
	}
	</style>
  </body>
</html>
