<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="eye" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
  <head>
    <title>各App索引监控</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/base.css"/>
    <script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery-1.9.1.js"></script>
    <script type="text/javascript">
    	function fullPromoProduct(){
    	   $("#fullPromoProduct").html("数据同步中……");
			$.getJSON("/cloud/eye/indexSM",
			{module:"promoFull",
			 t:new Date()},
			function(data){
				$("#fullPromoProduct").html("");
				if(data.msg="success"){
					$("#fullPromoProduct").append("同步成功！</br>同步商品数：<span class='hl'>"+ data.size +"</span><br>共用<span class='hl'>"+data.time+"秒</span>");
				}
			});
    	}
    	
    	function fullPromoProductWeight(){
     	   $("#fullPromoProductWeight").html("数据同步中……");
 			$.getJSON("/cloud/eye/indexSM",
 			{module:"promoFullWeight",
 			 t:new Date()},
 			function(data){
 				$("#fullPromoProductWeight").html("");
				if(data.msg="success"){
					$("#fullPromoProductWeight").append("请求已提交，正在同步！</br><span class='hl'>请十分钟后查看效果</span>");
				}
 			});
     	}
    	
    	//window.setInterval("getIndexDocCache()", 5000);
    </script>
    <style type="text/css">
    	li{border: 1px solid;margin: 2px;padding: 0 2px 0 5px;color: black;border-color: silver;width: 300px;}
    	li span{color: #FF0000;font-weight:bold; padding-right: 10px;}
    	#pageNoTag{color: #FF0000;font-weight:bold;font-size:16px;}
    	.db ul{float: left;}
    	.db{border-width: 1px; margin: 5px;}
    	#num{color:#5B5B5B;font-weight: bold;}
    	.hl{color:red;font-weight:bold;}
    	.fb{color:green;font-weight:bold;}
    </style>
  </head>
  <body>
		<dl class="setBox setBoxShow">
		    <dt class="setHd">
		    	<span class="fr">
		    		<a href="javascript:void(0)" class="down" js-triger="show"></a>
		    	</span>
		    	<i class="ic"></i>促销商品全量——因子全量</dt>
		    <dd class="setTurn" style="height: 150px;">
		    		<input type="button" onclick="fullPromoProduct()" value="开始同步">
		    		</br></br></br>
					<div>
						<span id="fullPromoProduct"></span>
					</div>
		    </dd>
		    <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
		</dl>
		
		<dl class="setBox setBoxShow">
		    <dt class="setHd">
		    	<span class="fr">
		    		<a href="javascript:void(0)" class="down" js-triger="show"></a>
		    	</span>
		    	<i class="ic"></i>促销商品全量——权重全量</dt>
		    <dd class="setTurn" style="height: 150px;">
		    		<input type="button" onclick="fullPromoProductWeight()" value="开始同步">
		    		</br></br></br>
					<div>
						<span id="fullPromoProductWeight"></span>
					</div>
		    </dd>
		    <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
		</dl>
		<%@ include file="../footer.jsp" %>
  </body>
</html>
