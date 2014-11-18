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
    	function getIndexDoc(){
    	   $("#indexDoc").html("数据查询中……");
			$.getJSON("/cloud/eye/indexSM",
			{module:"docCount",
			 t:new Date()},
			function(data){
				$("#indexDoc").html("");
			   var maxCount = 0; 
				$.each(data,function(i,item){
					if(maxCount < item.docCount) maxCount = item.docCount;
				});
				$.each(data,function(i,item){
					var num = maxCount - item.docCount;
					if(num != 0){
						$("#indexDoc").append("<li>服务器：<span class='fb'>"+item.ip+"</span>索引Doc数为：<span class='hl'>"+item.docCount+"</span></br>与最大索引个数相差：<span class='hl'>"+ num +"</span></li>");
					}else{
						$("#indexDoc").append("<li><span class='hl'>☆</span>服务器：<span class='fb'>"+item.ip+"</span>索引Doc数为：<span class='hl'>"+item.docCount+"</span></br>与最大索引个数相差：<span class='hl'>"+ num +"</span></li>");
					}
				});
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
		    	<i class="ic"></i>各App索引Doc数监控</dt>
		    <dd class="setTurn" style="height: 500px;">
		    		<input type="button" onclick="getIndexDoc()" value="获取AppDoc数">
		    		</br></br></br>
					<div>
						<ul id="indexDoc"></ul>
					</div>
		    </dd>
		    <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
		</dl>
		<%@ include file="../footer.jsp" %>
  </body>
</html>
