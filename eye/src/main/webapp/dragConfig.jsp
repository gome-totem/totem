<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>drop table</title>
<link rel="stylesheet"
	href="http://css.gomein.net.cn/search/eye/pages/css/jquery-ui-1.10.2.custom.css">
<script
	src="http://js.gomein.net.cn/search/eye/pages/js/jquery-1.9.1.js"></script>
<script
	src="http://js.gomein.net.cn/search/eye/pages/js/jquery-ui-1.10.2.custom.js"></script>
<script type="text/javascript" src="js/eye-search-config.js"></script>
<style>
div {
	margin: 0px;
	padding: 0px;
}

a:LINK {
	list-style: none;
	text-decoration: none;
}

.boder {
	width: 400px;
	height: 500px;
	float: left;
	margin-left: 40px
}

.boder ol {
	margin: 0;
	padding: 1em 0 1em 1em;
}

table {
	width: 400px;
}

table {
	border-collapse: collapse;
}

td,th {
	border: 1px solid #D1EEEE;
}

.tr_select {
	background-color: #B0E0E6;
}

ol li {
	list-style: none;
}

h1 {
	padding: .2em;
	margin: 0;
}
</style>
</head>
<body>
	<div class="boder" id="config">
		<div class="ui-widget-header">
			<font size="5">配置列表</font>
		</div>
		<div class="ui-widget-content">
			<table>
				<thead align="center">
					<tr id="title">
						<th><div class="prdClass">产品ID</div>
						</th>
						<th><div class="skuClass">SKU ID</div>
						</th>
						<th><div class="nameClass">产品名称</div>
						</th>
						<th><div class="scoreClass">因子得分</div>
						</th>
						<th><a href="#"></a></th>
					</tr>
				</thead>
				<tbody align="center" id="content">
					<tr class="tr_select">
						<td><div class="prdClass">11212322</div>
						</td>
						<td><div class="skuClass">2343rt226</div>
						</td>
						<td><div class="nameClass">三星手机</div>
						</td>
						<td><div class="scoreClass">6</div>
						</td>
						<td id="del23435226"><a href="javascript:removeItem(23435226)">[删除]</a></td>
					</tr>
				</tbody>
			</table>
			<ol>
				<li class="placeholder"><div style="float: left"><font size="2"
					style="font-style: italic;" id="tips">请拖动您要配置的商品到此处...</font></div>
					<div style="float: right"><a href="javascript:saveSearchConfig();">[确定]</a></div>
					<br>
				</li>
			</ol>
		</div>
	</div>
	<div class="boder" id="product">
		<div class="ui-widget-header">
			<font size="5">商品列表</font>
		</div>
		<div class="ui-widget-content">
			<table border="0" cellpadding="3" cellspacing="1">
				<thead align="center">
					<tr>

						<th><div class="prdClass">产品ID</div>
						</th>
						<th><div class="skuClass">SKU ID</div>
						</th>
						<th><div class="nameClass">产品名称</div>
						</th>
						<th><div class="scoreClass">因子得分</div>
						</th>
					</tr>
				</thead>
				<tbody align="center">
					<tr>
						<td><div class="prdClass">11212322</div>
						</td>
						<td><div class="skuClass">23435346</div>
						</td>
						<td><div class="nameClass">三星手机</div>
						</td>
						<td><div class="scoreClass">6</div>
						</td>
					</tr>
					<tr>
						<td><div class="prdClass">35345435</div>
						</td>
						<td><div class="skuClass">34543654</div>
						</td>
						<td><div class="nameClass">苹果手机</div>
						</td>
						<td><div class="scoreClass">5</div>
						</td>
					</tr>
					<tr>
						<td><div class="prdClass">34645645</div>
						</td>
						<td><div class="skuClass">23423653</div>
						</td>
						<td><div class="nameClass">小米手机</div>
						</td>
						<td><div class="scoreClass">4</div>
						</td>
					</tr>
					<tr>
						<td><div class="prdClass">34645645</div>
						</td>
						<td><div class="skuClass">23423652</div>
						</td>
						<td><div class="nameClass">黑莓手机</div>
						</td>
						<td><div class="scoreClass">3</div>
						</td>
					</tr>
					<tr>
						<td><div class="prdClass">34645645</div>
						</td>
						<td><div class="skuClass">23423651</div>
						</td>
						<td><div class="nameClass">锤子手机</div>
						</td>
						<td><div class="scoreClass">2</div>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>

</body>
</html>
