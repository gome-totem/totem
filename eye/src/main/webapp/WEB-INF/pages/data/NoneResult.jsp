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
    	function getNoneResultKeywords(q){
    		var date = $("#time").val();
    		$("div ul").html("");
    		var next,pageNo=0;
    		if(q=='first'){
    			next = false;
    		}else if(q=='next'){
    			next = true;
    			pageNo = $("#pageNo").html();
    		}
    		$("div ul").append("<li><span>每日首次需整理数据，故运行较慢(约需五分钟)，请耐心等待！<span></li>");
    		$.getJSON("/cloud/business/NoneResultKeywordsServlet",
    				{idate:date,
    				 next:next,
    				 pageNo:parseInt(pageNo) + 1},
    				function(data){
    					$("#pageNo").html(data.pageNo);
    					$("#pageNoTag").html(data.pageNo);
    					$("div ul").html("<li><span>结果如下：<span></li>");
    					$.each(data.oList,function(i,item){
    						$("div ul").append("<li><span>" + (i+1) + ":</span>"+item.gomeKey + "   <span id='num'>" + item.timesKey +"<span></li>");
    					});
    				}
    		);
    	}
    </script>
    <style type="text/css">
    	li{border: 1px solid;float: left;margin: 2px;padding: 0 2px 0 5px;color: black;border-color: silver;}
    	li span{color: #FF0000;font-weight:bold; padding-right: 10px;}
    	#pageNoTag{color: #FF0000;font-weight:bold;font-size:16px;}
    	.db ul{float: left;}
    	.db{border-width: 1px; margin: 5px;}
    	#num{color:#5B5B5B;font-weight: bold;}
    </style>
  </head>
  <body>
		<dl class="setBox setBoxShow">
		    <dt class="setHd">
		    	<span class="fr">
		    		<a href="javascript:void(0)" class="down" js-triger="show"></a>
		    	</span>
		    	<i class="ic"></i>无结果关键字</dt>
		    <dd class="setTurn">
		    		<span id="pageNo" style="display:none;"></span>
					<table>
						<tr>
							<td>
								<span>
									日期：
									<select id = "time">
										<option value="0" selected="selected">昨日</option>
									</select>
								</span>
								<span>
									<input type="button" onclick="getNoneResultKeywords('first')" value="查询"/>
									<input type="button" onclick="getNoneResultKeywords('next')" value="下一页"/>
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
		    		</ul>
		    		<div>第<span id="pageNoTag">0</span>页</div>
		    	</div>
		    </dd>
		    <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
		</dl>
		<%@ include file="../footer.jsp" %>
  </body>
</html>
