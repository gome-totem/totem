<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
String foot = request.getContextPath();
String socket = request.getServerName()+":"+request.getServerPort()+foot+"/";
%>
<input id="hostName" type="hidden" value="<%= socket %>/cloud/eye">
<html>
<head>
<title>tomcat服务监控</title>
<script src="${initParam.cdnJsServerUrl}/pages/js/jquery-1.9.1.js"></script>
<script src="${initParam.cdnJsServerUrl}/pages/js/watch_detect.js"></script>
<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/webclient.js"></script>
<link href="${initParam.cdnCssServerUrl}/pages/css/base.css" rel="stylesheet"/>
<script type="text/javascript">
	function reDetect(){
		window.location.reload();
	}
</script>
</head>

<body>
  <dl class="setBox setBoxShow" style="float: left;width: 40%">
		<dt class="setHd">
			<span class="fr">
	 		<a js-triger="show" class="down" href="javascript:void(0)"></a>
	 		<%--|<a js-triger="close" class="close" href="javascript:void(0)"></a>--%>
			</span>
			<i class="ic"></i>
			监控列表
		</dt>
		<dd class="setTurn" >
		<div class="easyui-panel" style="height: 600px;">
			<table style="margin-left: 50px;font-weight:bold;width: 90%">
				<thead id="total">
					 <td width="30%">服务器IP</td><td width="20%">服务器PORT</td><td width="20%">请求状态</td>
				</thead>
				<tbody id="logs">
				</tbody>
			</table>
		</div>
		</dd>

		
		<dd class="bottomLine">
			<div class="line"><div class="line"></div></div>
		</dd>
	</dl>
	<dl class="setBox setBoxShow" style="float: left;width: 20%">
		<dt class="setHd">
			<span class="fr">
	 		<a js-triger="show" class="down" href="javascript:void(0)"></a>
	 		<%--|<a js-triger="close" class="close" href="javascript:void(0)"></a>--%>
			</span>
			<i class="ic"></i>
			监控结果
		</dt>
		<dd class="setTurn" style="height: 600px">
			<div align="right"> <a id="detectButton" href="#" class="easyui-linkbutton" onclick="reDetect()" style="margin-right: 50px;display: none">重新监控</a></div>
			<div id="load" align="center"><img src="/pages/images/load.gif" style="margin-top: 30px;"></div>
			<div id="result" style="font-size: 14px;font-style:normal;">
				
			</div>
		</dd>
		<dd class="bottomLine">
			<div class="line"><div class="line"></div></div>
		</dd>
	</dl>
	<%@ include file="../footer.jsp"%>
</body>
</html>
