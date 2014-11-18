<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <title>云眼-监控页</title>
    
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <meta name="description" content="main pape of manager zookeeper"/>
    <meta name="author" content="dong zhou"/>
    <script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery-1.9.1.js"></script>
	<%@ include file="/pages/eye/jsp/common/commoncss.jspf" %>
    
  </head>
  <body>
  	<span id="monitorPage" style="display:none;">true</span>
	<div id="wrap">
	
		<div class="container">
			<div class="row-fluid">
				<%@include file="left.jsp" %>
				<%@include file="right.jsp" %>
			</div>
			<div id="push"></div>
		</div>
	<%@ include file="/footer.jsp" %>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery.orgchart.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/util.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/json2.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/template.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/webclient.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/monitor-base.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/monitor.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-error.js"></script>
	
</body>
</html>
