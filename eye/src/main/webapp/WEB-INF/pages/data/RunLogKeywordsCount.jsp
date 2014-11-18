<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="eye" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
  <head>
    <title>关键字无结果</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/base.css"/>
    <script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery-1.9.1.js"></script>
    <script type="text/javascript">
    	function getCount(){
    		var date = $("#date").val();
    		var kw = $("#keywords").val();
    		var app = $("input[name='app'][type='radio']:checked").val();
    		$("div ul").html("");
    		//var data = {"10.58.22.1":["14","21","17","0"],"10.58.22.2":["29","14","18","0"],"10.58.22.3":["21","12","19","12"],"10.58.22.10":["17","13","25","0"],"10.58.22.12":["13","17","23","0"],"10.58.22.16":["19","26","19","0"],"10.58.50.24":["41","39","42","0"],"10.58.50.25":["33","27","25","0"],"10.58.50.26":["27","32","29","0"],"10.58.50.53":["28","31","26","0"],"10.58.50.54":["32","31","33","0"],"10.58.50.55":["27","23","28","0"],"All":{"tomcat":891,"app":12}};
    		$("div ul").append("<li><span>数据分析中，请耐心等待！<span></li>");
    		
    		$.getJSON("/cloud/eye/runLogAnsi",
    				{idate:date,
    				 keywords:kw,
    				 app:app},
    				function(data){
    					$("div ul").html("");
    		    		for(var i in data){
    		    			$("div ul").append("<li><span>服务器" + i + ": " + JSON.stringify(data[i]) +"<span></li>");
    		    		}
    				}
    		);
    	}
    </script>
    <style type="text/css">
    	li{margin: 2px;padding: 0 2px 0 5px;color: black;border-color: silver;}
    	li span{color: #FF0000;font-weight:bold; padding-right: 10px;}
    	#pageNoTag{color: #FF0000;font-weight:bold;font-size:16px;}
    	#num{color:#5B5B5B;font-weight: bold;}
    </style>
  </head>
  <body>
		<dl class="setBox setBoxShow">
		    <dt class="setHd">
		    	<span class="fr">
		    		<a href="javascript:void(0)" class="down" js-triger="show"></a>
		    	</span>
		    	<i class="ic"></i>日志关键字计数</dt>
		    <dd class="setTurn">
		    		<span id="pageNo" style="display:none;"></span>
					<table class="main" style="width: 50%">
						<tr>
						<td>日期：</td>
						<td>
							<div>
								<input type="text" id="date" placeholder="格式:2013-11-13" />
							</div>
						</td>
						<td>关键字：</td>
						<td>
							<div>
								<input type="text" id="keywords" placeholder="关键字" />
							</div>
						</td>
						<td>服务：</td>
						<td>
							<div>
								<input type="radio" name="app" id="app1" value="app"/>
									<label for="app1">App</label>
									<input type="radio" name="app" id="app2" value="tomcat"/>
									<label for="app2">Tomcat</label>
									<input type="radio" name="app" id="app3" value="all" checked="checked"/>
									<label for="app3">App and Tomcat</label>
							</div>
						</td>
						<td  align="center">
								<span>
									<input type="button" onclick="getCount()" value="提交"/>
								</span>
							</td>
						</tr>
					</table>
		    </dd>
		    
		    <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
		</dl>
		
		
	   <dl class="setBox setBoxShow">
		    <dt class="setHd">
		    	<span class="fr">
		    		<a href="javascript:void(0)" class="down" js-triger="show"></a>
		    	</span>
		    	<i class="ic"></i>查询结果</dt>
		    <dd class="setTurn">
		    	<div class="db">
		    		<ul>
		    			<li>请点击查询</li>
		    			<br>
		    		</ul>
		    	</div>
		    </dd>
		    <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
		</dl>
		<%@ include file="../footer.jsp" %>
  </body>
</html>
