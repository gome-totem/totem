<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <title>业务手动增量索引</title>
    <script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery-1.9.1.js"></script>
	 <%@ include file="/pages/eye/jsp/common/commoncss.jspf" %>
    <link href="${initParam.cdnCssServerUrl}/pages/css/base.css" rel="stylesheet"/>
    <link href="${initParam.cdnCssServerUrl}/pages/css/business.css" rel="stylesheet">
    <style type="text/css">
	.datagrid-toolbar table{
		margin-left:70%;
	}
	.setTurn td{
		padding:0px;
	}
    </style>
    <script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-busi-incrementIndex.js"></script>
  </head>
  	<body style="font-size: 14px;">
  	
  	<dl class="setBox setBoxShow">
  		<dt class="setHd">
	  		<span class="fr">
		  		<a js-triger="show" class="down" href="javascript:void(0)"></a>
		  		<%--|<a js-triger="close" class="close" href="javascript:void(0)"></a>--%>
	  		</span>
	  		<i class="ic"></i>
	  		操作
  		</dt>
  		<dd class="setTurn">
  			
	  		<br/>
	  		<textarea rows="1" cols="50" id="id"></textarea>
	  		<select name="type" id="type">
  			<option value="category">category</option>
  			<option value="product" selected="selected">9码</option>
  			<option value="sku">sku</option>
  			<option value="merchant">merchant</option>
	  		</select>
	  		<input type="button" class="btn" value="添加增量" onclick="addIncrementIndex()" style="margin-left: 10px;">
	  		<input type="button" class="btn" value="执行增量" onclick="executeIncrementIndex()" style="margin-left: 10px;">
	  		<br/><br/>
	  		<table id="incrementIndexLogs"  >
	  			
	  		</table>
	  		<>
  		</dd>
  		<dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
  	</dl>
  	
  	  	<dl class="setBox setBoxShow">
  		<dt class="setHd">
	  		<span class="fr">
		  		<a js-triger="show" class="down" href="javascript:void(0)"></a>
		  		<%--|<a js-triger="close" class="close" href="javascript:void(0)"></a>--%>
	  		</span>
	  		<i class="ic"></i>
	  		操作说明
  		</dt>
  		<dd class="setTurn">
  			<div class="suggest">
		  		功能：调价后，如果列表页的价格和sku页的价格不一致，请使用进行修复。商品下架不成功，页面依旧显现。</br>
		  		步骤：<br>1.选择9码 <br>2.输入9码，多个9码用英文逗号分开 <br>3.点击添加增量 <br>4.点击执行增量.待状态返回已成功后，2分钟到页面进行验证
  			</div>
  		</dd>
  		<dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
  	</dl>
  	
  	<%@ include file="../footer.jsp"%>
  </body>
</html>
