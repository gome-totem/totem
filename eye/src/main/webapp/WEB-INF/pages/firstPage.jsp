<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    <title>gome totem</title>
  </head>
  <body>
   <%@ include file="header.jsp"%>
	<div class="easyui-panel" style="padding:10px;border: none;">
		<div class="main">
			<ul class="crumbs clearfix">
				<li><a href="/login.jsp" class="home"></a>
				</li>
			</ul>
			<dl class="daysData clearfix">
				<dd class="data">
					<div class="schematic"></div>
					<p class="triger">
						<i>+</i><b>586</b><br />今日增量
					</p>
				</dd>
				<dd class="data">
					<div class="schematic"></div>
					<p class="triger">
						<i>+</i><b>586</b><br />今日增量
					</p>
				</dd>
				<dd class="data">
					<div class="schematic"></div>
					<p class="triger">
						<i>+</i><b>586</b><br />今日增量
					</p>
				</dd>
				<dd class="data">
					<div class="schematic"></div>
					<p class="triger">
						<i>+</i><b>586</b><br />今日增量
					</p>
				</dd>
				<dd class="data">
					<div class="schematic"></div>
					<p class="triger">
						<i>+</i><b>586</b><br />今日增量
					</p>
				</dd>
				<dd class="data">
					<div class="schematic"></div>
					<p class="triger">
						<i>+</i><b>586</b><br />今日增量
					</p>
				</dd>
			</dl>
		</div>
		<ul class="brands clearfix">
			<li><a href="javascript:void(0)"><img
					src="/pages/images/brand_1.png" alt="" />增加品牌</a>
			</li>
			<li><a href="javascript:void(0)"><img
					src="/pages/images/brand_1.png" alt="" />增加品牌</a>
			</li>
			<li><a href="javascript:void(0)"><img
					src="/pages/images/brand_1.png" alt="" />增加品牌</a>
			</li>
			<li><a href="javascript:void(0)"><img
					src="/pages/images/brand_1.png" alt="" />增加品牌</a>
			</li>
			<li><a href="javascript:void(0)"><img
					src="/pages/images/brand_1.png" alt="" />增加品牌</a>
			</li>
			<li><a href="/dispacher/totemMapPage"><img
					src="/pages/images/brand_1.png" alt="" />搜索配置图</a>
			</li>
		</ul>
	</div>
	<%@ include file="footer.jsp"%>
  </body>
</html>
