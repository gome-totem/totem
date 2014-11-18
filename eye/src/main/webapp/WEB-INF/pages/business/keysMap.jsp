<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<head>
		<title>搜索地图</title>
		<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
		<meta name="description" content="main pape of manager zookeeper" />
		<meta name="author" content="zoudexiong" />
		<%@ include file="/pages/eye/jsp/common/commoncss.jspf" %>
		<link href="${initParam.cdnCssServerUrl}/pages/css/map.css" rel="stylesheet"/>
	</head>
	<body>
		<%@ include file="../header.jsp"%>
		<div class="container" style="width: auto;">
		<div class="main" style="overflow:hidden;zoom:1;position:relative;">
			<div style="width:100%;height:600px;border:1px solid gary;margin-top: 60px;" id="gomekeys"></div>
			<div id="showPanelBtn" style="position:absolute;font-size:14px;top:50%;margin-top:0px;right:0px;width:20px;padding:10px 10px;color:#999;cursor:pointer;text-align:center;height:95px;background:rgba(255,255,255,0.9);-webkit-transition: all 0.5s ease-in-out;transition: all 0.5s ease-in-out;font-family:'微软雅黑';font-weight:bold;">展开面板<br/>&lt;</div>
			<div id="panelWrap" style="width:0px;position:absolute;top:60px;right:0px;height:100%;overflow:auto;-webkit-transition: all 0.5s ease-in-out;transition: all 0.5s ease-in-out;">
				<h4>热门关键词</h4>
				<div style="width:20px;height:200px;margin:-100px 0 0 -225px;color:#999;position:absolute;opacity:0.5;top:50%;left:50%;"><ul id="question"></ul></div>
				<div id="panel" style="position:absolute;"></div>
			</div>
		</div>
		<div id="result">
			<dl>
				<dt>搜索排行榜</dt>
				<dd>
					<a class="btn inRight" data-toggle="modal" data-backdrop="false" href="#keys_100" >关键词 TOP 100</a>
					<a class="btn inRight" data-toggle="modal" data-backdrop="false" href="#city_100" >CITY TOP 100</a>
				</dd>
				<dt>城市 关键词</dt>
				<dd>
					<table id="keyTOcity">
					</table>
				</dd>
			</dl>
		</div>
		<div class="modal hide fade" id="keys_100">
			<div class="modal-header">
		    <a class="close" data-dismiss="modal">×</a>
			    <h3>关键词TOP100</h3>
			</div>
			<div class="modal-body">
			    <p id="k_100"></p>
			</div>
			<div class="modal-footer">
			    <a href="#" class="btn" data-dismiss="modal">关闭</a>
		    </div>
		</div>
		<div class="modal hide fade" id="city_100">
	   	<div class="modal-header">
				<a class="close" data-dismiss="modal">×</a>
				<h3>城市TOP100</h3>
			</div>
			<div class="modal-body">
			    <p id="q_100"></p>
			</div>
			<div class="modal-footer">
			    <a href="#" class="btn" data-dismiss="modal">关闭</a>
		    </div>
	    </div>
			<hr>
		  <footer>
		  <center>
		    <p>&copy; Company 2013<span style="color:#DD1144;"> &nbsp;国美在线</span> CLOUD EYE (GOME <span style="color:#DD1144;">TOTEM</span> MBP)</p>
		  </center>
		  </footer>
		</div> 
		<script src="/pages/js/jquery-1.9.1.js"></script>
		<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=F9628003c8bbe202e250e4d564bb3e46"></script>
		<script type="text/javascript" src="http://api.map.baidu.com/library/SearchInfoWindow/1.5/src/SearchInfoWindow_min.js"></script>
		<link rel="stylesheet" href="http://api.map.baidu.com/library/SearchInfoWindow/1.5/src/SearchInfoWindow_min.css" />
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-busi-map.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-busi-keysBall.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/bootstrap-modal.js"></script>
	</body>
</html>