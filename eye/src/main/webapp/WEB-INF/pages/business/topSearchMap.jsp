<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/bootstrap.min.css"/>
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/map.css"/>
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/base.css"/>
<link rel="stylesheet" href="http://api.map.baidu.com/library/SearchInfoWindow/1.5/src/SearchInfoWindow_min.css" />
<script type="text/javascript">
function showKeyWords(value){
	if(value==1){
		//alert($('#keys_100').css('display'));
		$('#keys_100').css('display','block');
		$('#keys_100').dialog({
			title: '关键词TOP100',
			width: 350,
			top:100,
			height: 600,
			closed: false,
			cache: false,
			modal: true
			});
	}else if(value==2){
		$('#city_100').css('display','block');
		$('#city_100').dialog({
			title: '城市TOP100',
			width: 350,
			height: 600,
			top:100,
			closed: false,
			cache: false,
			modal: true
			});
	}
}
</script>
<div class="rightMain" >
	<div class="main-map">
		<div class="container" style="width: 98.5%; margin-left: 25px;">
			<div class="main-map" style="overflow:hidden;zoom:1;position:relative;border: 1px solid black;">
			<div style="width:100%;height:600px;" id="gomekeys"></div>
			<div id="showPanelBtn" style="position:absolute;font-size:14px;top:50%;margin-top:0px;right:0px;width:20px;padding:10px 10px;color:#999;cursor:pointer;text-align:center;height:95px;background:rgba(255,255,255,0.9);-webkit-transition: all 0.5s ease-in-out;transition: all 0.5s ease-in-out;font-family:'微软雅黑';font-weight:bold;">展开面板<br/>&lt;</div>
			<div id="panelWrap" style="width:0px;position:absolute;top:60px;right:0px;height:100%;overflow:auto;-webkit-transition: all 0.5s ease-in-out;transition: all 0.5s ease-in-out;">
				<h4>热门关键词</h4>
				<div style="width:20px;height:200px;margin:-100px 0 0 -225px;color:#999;position:absolute;opacity:0.5;top:50%;left:50%;"><ul id="question"></ul></div>
				<div id="panel" style="position:absolute;"></div>
				</div>
			</div>
		</div>
		
		<dl class="setBox setBoxShow" >
 		<dt class="setHd">
  		<span class="fr">
	  		<a js-triger="show" class="down" href="javascript:void(0)"></a>|
	  		<%--<a js-triger="close" class="close" href="javascript:void(0)"></a>--%>
	  	</span>
  		<i class="ic"></i>
  		操作
 		</dt>
 		<dd class="setTurn">
			<ul id="result">
				<dl>
					<dt>搜索排行榜</dt>
					<dd>
						<a class="btn inRight" data-toggle="modal" data-backdrop="false" href="#" onclick="showKeyWords(1)">关键词 TOP 100</a>
						<a class="btn inRight" data-toggle="modal" data-backdrop="false" href="#" onclick="showKeyWords(2)">CITY TOP 100</a>
					</dd>
				</dl>
			</ul>
 		</dd>
		<dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
	  </dl>
	  
	<dl class="setBox setBoxShow">
		<dt class="setHd">
  		<span class="fr">
	  		<a js-triger="show" class="down" href="javascript:void(0)"></a>|
	  		<%--<a js-triger="close" class="close" href="javascript:void(0)"></a>--%>
	  	</span>
  		<i class="ic"></i>
  		城市 关键词
 		</dt>
 		<dd class="setTurn">
			<ul class="proLists">
				<table width="100%" id="keyTOcity"></table>
			</ul>
 		</dd>
 		<dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
 	</dl>
 	<div id="keys_100" style="display: none"><p id="k_100" style="padding-left: 40px"></p></div>
 	<div id="city_100" style="display: none"><p id="q_100" style="padding-left: 40px"></p></div>
	</div>
</div>
<%@ include file="../footer.jsp" %>
<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=F9628003c8bbe202e250e4d564bb3e46"></script>
<script type="text/javascript" src="http://api.map.baidu.com/library/SearchInfoWindow/1.5/src/SearchInfoWindow_min.js"></script>
<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/bootstrap-modal.js"></script>
<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-busi-map_new.js"></script>
<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-busi-keysBall.js"></script>
	</body>
</html>