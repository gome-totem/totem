<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<head>
		<title>gome促销排序</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0" />
		<meta name="description" content="main pape of manager zookeeper" />
		<meta name="author" content="zoudexiong" />

	    <%@ include file="/pages/eye/jsp/common/commoncss.jspf" %>
	    <link href="${initParam.cdnCssServerUrl}/pages/css/business.css" rel="stylesheet"/>
	    <link href="${initParam.cdnCssServerUrl}/pages/css/pagination.css" rel="stylesheet"/>
	    <link href="${initParam.cdnCssServerUrl}/pages/css/zTree.css" rel="stylesheet"/>
	</head>
	<body>
	
	<%@ include file="../header.jsp"%>
	<div class="container" style="width: auto;">
	<div class="title"><h3>gome促销排序</h3></div>
	<table class="main" >
		<tbody class="left signin left-signin">
			<tr>
				<td colspan="2"><div class="cate" id="catContent">选择站点：</div></td>
			</tr>
			<tr class="eye-hide">
				<td width="40%"><div class="cate">按分类搜索：</div></td>
				<td>
					<input id="idCate" type="text" onfocus="if (value ==this.defaultValue){value =''}this.style.color='#5e5e5e'" onblur="if (value ==''){value=this.defaultValue};this.style.color='#a5a5a5'" value="搜索分类" />
					<input id="catIdSearch" class="btn" type="button" value="提交" onclick="promotion.firesearch()"/>
				</td>
			</tr>
			<tr>
				<td colspan="2"><div id="cateId" class="catinfo"><font color="gray"></font></div></td>
			</tr>
			<tr>
				<td colspan="2">
					<div class="suggest">
						<br />
						<div class="tips">排序步骤tips:</div>
						<p>1:搜索需要排序的分类</p>
						<p>2:在该分类中按skuId进行设置promotion值</p>
						<p>3:点击排序查看设置结果</p>
						<br />
						<div class="tips">友情提示：</div>
						<p>1:每个分类设置promotion的值为5 4 3 2</p>
						<p>2:promotion值为5商品每个分类最多设置4个</p>
						<p>3:promotion值为4商品每个分类最多设置32个</p>
						<p>4:promotion值为3商品每个分类最多设置36个</p>
						<p>5:promotion值为2商品每个分类最多设置36个</p>
					</div>
				</td>
			</tr>
			<tr>	
				<td colspan="2">
					<div id=serverInfo></div>
				</td>
			</tr>
		</tbody>
		<tbody class="right signin right-signin">
			<tr>
				<td><div class="cate">按skuId搜索：</div></td>
				<td>
					<div class="wrapLeft">
						<input class="input-skuId" id="idSku" type="text" onfocus="if (value ==this.defaultValue){value =''};this.style.color='#5e5e5e'" onblur="if (value ==''){value=this.defaultValue};this.style.color='#a5a5a5'" value="搜索skuId" />
						<input class="btn btn-ext" type="button" id="skuIdSearch" value="搜索" onclick="promotion.windsearch()"/>
						<span id="skuId_search_err" ></span>
					</div>
				</td>
			</tr>
			<tr>
				<td>商品信息：</td>
				<td><div id="productInChange" class="catinfo"><font color="gray">暂无搜索商品</font></div></td>
			</tr>
			<tr>
				<td>设置商品的promotion值：</td>
				<td>
				<form action="">
					<select id="promotionvalue" class="sele">
						<option value="5">5</option>
						<option value="4">4</option>
						<option value="3">3</option>
						<option value="2">2</option>
						<option value="0">0</option>
					</select>
					<input class="btn btn-ext" type="button" id="promoSubmit" onclick="promotion.setPromoScore()" value="确认" />
				</form>
				</td>
			</tr>
			<tr><td colspan="2"><div id="skuId_setScore_err" class="catinfo"></div></td></tr>
			<tr><td><br /></td><td><br /></td></tr>
			<tr>
				<td>查看设置结果：<input id="sort" class="btn" type="button" value="排序" onclick="promotion.waterSearch()" /></td>
				<td></td>
			</tr>
			<tr>
				<td colspan="2">
					<div class="line1"></div>
					<div class="result" id="result"></div>
				</td>
			</tr>
		</tbody>
	</table>
	
	<div class="busi-hide">
		<table id="prodTempId" class="tmpTable">
			<tbody class="pageNation">
				<tr><th>产品ID</th>	<th>SKU ID</th>	<th>产品名称</th>	<th>因子得分</th></tr>
				<tr jsselect="products">
					<td><div class="prdClass" jscontent="$this.id"></div></td>
					<td><div class="skuClass" jscontent="$this.skuId"></div></td>
					<td><div class="namClass" jscontent="$this.productName"></div></td>
					<td><div class="scoClass" jscontent="$this.promoScore"></div></td>
				</tr>
			</tbody>
		</table>
		
		<div id="cateTempId">
			<div class="fooler" jsselect="level" jsvalues="fooler:$this"></div>
			<ul id="treeCate" class="ztree" style="-moz-user-select: -moz-none;" >
				<li jsselect="childs">
					<span jsvalues="id:treeId($this,'switch')" class="button switch center_line" onclick="" treenode_switch="" ></span>
					<a jsvalues="title:$this.catName;id:treeId($this,'a')" treenode_a="" >
						<span class="button ico_close" treenode_ico="" jsvalues="id:treeId($this,'ico')"></span>
						<span class="icoClick" jscontent="$this.catName" jsvalues="id:treeId($this,'span')"></span>
					</a>
					<ul jsvalues="id:treeId($this,'ul')" class="line"></ul>
				</li>
			</ul>
		</div>
	</div>
	
	<%@ include file="/footer.jsp"%>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/json2.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/template.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/util.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-busi-promo.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery.pagination.js"></script>
	</body>
</html>