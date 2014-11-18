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
    	function getBeansTalkData(){
    	   $("#beanstalkMonitor").html("数据查询中……");
			$.getJSON("/cloud/eye/totemSM",
			{module:"beansTalkMonitor",
			 t:new Date()},
			function(data){
				 if(data.msg=="success"){
					 var result = data.val;
					$("#beanstalkMonitor").html("");
					$("#beanstalkMonitor").append("<div>total command：<span class='hl'>"+result.b_tol+"</span></div>");
					$("#beanstalkMonitor").append("<div>total deleted：<span class='hl'>"+result.b_del+"</span></div>");
					$("#beanstalkMonitor").append("<div>after 1s total command：<span class='hl'>"+result.e_tol+"</span></div>");
					$("#beanstalkMonitor").append("<div>after 1s total deleted：<span class='hl'>"+result.e_del+"</span></div>");
					$("#beanstalkMonitor").append("<div>1s exec command：<span class='fb'>"+result.dif_tal+"</span></div>");
					$("#beanstalkMonitor").append("<div>1s exec delete：<span class='fb'>"+result.dif_del+"</span></div>");
				 }else if(data.msg="running"){
					 $("#beanstalkMonitor").html("请求处理中，请稍后！");
				 }else{
					 $("#beanstalkMonitor").html("处理失败！");
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
		    	<i class="ic"></i>BeansTalk</dt>
		    <dd class="setTurn" style="height: 500px;">
		    		<input type="button" onclick="getBeansTalkData()" value="获取BeansTalk数据">
		    		</br></br></br>
					<div>
						<span id="beanstalkMonitor"></span>
					</div>
		    </dd>
		    <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
		</dl>
		<%@ include file="../footer.jsp" %>
  </body>
</html>
