<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <title>业务地区关键字品牌</title>
    <script src="${initParam.cdnJsServerUrl}/pages/js/jquery-1.9.1.js"></script>
    <script src="${initParam.cdnJsServerUrl}/pages/js/jquery.cookie.min.js"></script>
    <script src="${initParam.cdnJsServerUrl}/pages/js/jquery.common.js"></script>
    <link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/base.css"/>
    <script src="${initParam.cdnJsServerUrl}/pages/js/jquery.ztree.core-35.js"></script>
    <script src="${initParam.cdnJsServerUrl}/pages/js/cityBrandTree.js"></script>
    <link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/zTree.css"></link>
    <script type="text/javascript" src="http://js.gome.com.cn/js/g/ui/gCity.js"></script>
    <link type="text/css" rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/gCity.min.css" />
    <style type="text/css">
     		.main-left{border:solid 1px blue;position:absolute; top:0; left:0; width:300px; height:100%; background-color:#CCCCCC}
     		.main-right{border: solid 1px red;margin-left:320px; height:100%;}
			.select-hd .gCity{display:none;}
			.cityShow  .gCity{display:block;}
    </style>
  </head>
  <body style="font-size: 14px;">
	<div class="container" style="width: auto;">
		<div class="main-left">
			<ul id="treeDemo" class="ztree"></ul>
		</div>
		<div class="main-right">
			<dl class="setBox setBoxShow">
		    <dt class="setHd">
		    	<span class="fr">
		    		<a href="javascript:void(0)" class="down" js-triger="show"></a>
		    	</span>
		    	<i class="ic"></i>关键字录入</dt>
		    <dd class="setTurn">
		    		<span>关键字：</span>
		    		<input type="text" id="keywords"/>
		    		<input type="button" value="添加" onclick="addKeyWords()" />
	     	 </dd>
		    <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
			</dl>
			<dl class="setBox setBoxShow">
		    <dt class="setHd">
		    	<span class="fr">
		    		<a href="javascript:void(0)" class="down" js-triger="show"></a>
		    	</span>
		    	<i class="ic"></i>品牌选择</dt>
		    <dd class="setTurn">
		    		<span>请输入分类Id：</span>
		    	   <input type="text" id="catIdInput"/>
		    	   <input type="button" value="查询" onclick="addKeyWords()" />
	     	 </dd>
		    <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
			</dl>
			<dl class="setBox setBoxShow">
		    <dt class="setHd">
		    	<span class="fr">
		    		<a href="javascript:void(0)" class="down" js-triger="show"></a>
		    	</span>
		    	<i class="ic"></i>地区选择</dt>
		    <dd class="setTurn">
		    	<div class="layout clearfix">
		    	<span style="height:24px; line-height:24px;padding-left:15px;" class="dt filter-item">请选择地区</span>
		    	<div id="J-address" class="filter-item address">
		      	<div class="select-hd">
			      	<span id="address" class="regon">
			         	<a id="stockaddress" href="javascript:;">请选择</a>
			             <i></i>
			             <span class="space"></span>
			         </span>
		      		<div class="gCity clearfix"></div>
		        </div>
		      	
		      	<div class="store-selector select-bd" style="display: none;">
		        	
		        </div>
		      </div>
	     	 </dd>
		    <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
			</dl>
		</div>
  	</div>
  	<script type="text/javascript" language="javascript">
	/*
	function iFrameHeight() {
		var ifm= document.getElementById("iframepage");
		var subWeb = document.frames ? document.frames["iframepage"].document : ifm.contentDocument;
		if(ifm != null && subWeb != null) {
			ifm.height = subWeb.body.scrollHeight;
		}
	}
	*/
	
	function reinitIframe(){
		var iframe = document.getElementById("iframepage");
		if(iframe==null){
			return;
		}
		try{
			var bHeight = iframe.contentWindow.document.body.scrollHeight;
			var dHeight = iframe.contentWindow.document.documentElement.scrollHeight;
			var height = Math.max(bHeight, dHeight);
			if(height<500){
				iframe.height = 500;
				return;
			}
			if(iframe.height != height){
				iframe.height = height;
			}
		}catch(ex){
			
		}
	}
	window.setInterval("reinitIframe()", 200);
	
	$(document).ready(function(){
		$(".leftBar dd a").click(function(){
			var _this = $(this);
			$("dd a").removeClass("cur");
			_this.addClass("cur");
			var l1 = _this.parent().siblings().html().replace("<i></i><s></s>","");
			var l2 = _this.html().replace("<s></s>","");
			$("#l1 span").html(l1);
			$("#l2 span").html(l2);
		});
		
		var page_tag = $("#page_tag").html();
		$(".nav a").removeClass("cur");
		$("#tg_"+page_tag).addClass("cur");
	});
	</script> 
  </body>
</html>