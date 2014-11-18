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
    	function fullPromoCity(){
    	   $("#fullPromoCity").html("数据同步中……");
		   $.ajax({
			url : '/cloud/eye/indexCity',
			type : 'get', timeout : 800000,	cache : false,
			dataType : 'json',
			success : function(data) {
				$("#fullPromoCity").html("");
				if(data.msg="success"){
					$("#fullPromoCity").append("同步成功！</br>同步地区数：<span class='hl'>"+ data.size +"</span><br>共用<span class='hl'>"+data.time+"秒</span>");
				}
			},
			error : function(errorThrown) {
				$("#fullPromoCity").html("<span class='hl'>网络异常！</span>");
			}
		});
    	}
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
		    	<i class="ic"></i>—地区数据同步</dt>
		    <dd class="setTurn" style="height: 150px;">
		    		<input type="button" onclick="fullPromoCity()" value="开始同步">
		    		</br></br></br>
					<div>
						<span id="fullPromoCity"></span>
					</div>
		    </dd>
		    <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
		</dl>
		<%@ include file="../footer.jsp" %>
  </body>
</html>
