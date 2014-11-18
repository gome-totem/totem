<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>

  <head>
    <base href="<%=basePath%>">
    <title>云眼-TOTEM</title>
	 <meta http-equiv="pragma" content="no-cache">
	 <meta http-equiv="cache-control" content="no-cache">
	 <meta http-equiv="expires" content="0">    
	 <meta http-equiv="keywords" content="云眼 图腾 TOTEM EYE GOME">
	 <meta http-equiv="description" content="云眼-图腾框架后台业务管理和配置系统">
	 <meta name="author" content="dong zhou">
  </head>

  <frameset rows="19%,71%,10%" >
  		<frame src="<%=basePath %>/totem/jsp/common/header.jsp" noresize="noresize" border="0"  frameborder="no"/>
  		<frameset  cols="30%,70%" frameborder="no" border="0" framespacing="0">
	  		<frame src="<%=basePath %>/totem/jsp/component/nav.jsp" scrolling="no" sc noresize="noresize" border="0"  frameborder="no"/>
	  		<frame src="<%=basePath %>cool/consume.html" scrolling="auto" noresize="noresize" border="0"  frameborder="no" name="mainframe"/>
  		</frameset>
  		<frame src="<%=basePath %>/totem/jsp/common/footer.jsp" noresize="noresize" border="0"  frameborder="no"/>
  		<noframes>
			<body>您的浏览器无法处理框架！</body>
		</noframes>
  </frameset>
    
</html>
