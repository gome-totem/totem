<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<style>
<!--
table td{
	 border: 1px solid #E6E6E6;
	 font-size: 14px;
}
-->
</style>
<!DOCTYPE html>
<html>
  <head>
    <title>聪明购设置</title>
    <meta charset="utf-8">
	<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/business.css" />
	<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/base.css"/>
	<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/pagination.css"/>
	<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/zTree.css"/>
	<style type="text/css">
body {
	font-size: 12px;
}

td {
	border-collapse: collapse;
	border-spacing: 0;
	border: 1px solid;
}

.setTurn td {
	padding: 2px;
}

.tabTitle {
	
}

.btn {
	padding: 0 10px;
	font-weight: bold;
}

.altWin {
	padding: 15px;
	border: 1px solid green;
	display: none;
	box-shadow: 0 0 40px rgba(0, 0, 0, 0.19);
	background: #F0F0F0;
}

.cls {
	padding: 3px;
	font-size: 22px;
	font-weight: bold;
	cursor: pointer;
	z-index: 10;
}

.altWinTitle {
	text-align: right;
	width: 100%;
}

#updateMapping label {
	display: inline;
}

#delMapping {
	width: 200px;
}

#updateMapping {
	width: 450px;
}

#tabs {
	overflow: hidden;
	width: 100%;
	margin: 0;
	padding: 0;
	list-style: none;
}

#tabs li {
	float: left;
	margin: 0 -15px 0 0;
}

#tabs a {
	float: left;
	position: relative;
	padding: 0 40px;
	height: 0;
	line-height: 30px;
	text-transform: uppercase;
	text-decoration: none;
	color: #fff;
	border-right: 30px solid transparent;
	border-bottom: 30px solid #3D3D3D;
	border-bottom-color: #777\9;
	opacity: .3;
	filter: alpha(opacity = 30);
}

#tabs a:focus {
	outline: 0;
}

#tabs #current {
	z-index: 3;
	border-bottom-color: #3d3d3d;
	opacity: 1;
	filter: alpha(opacity = 100);
}
</style>
	
	<%@ include file="/pages/eye/jsp/common/commoncss.jspf" %>
	<script type="text/javascript">
		function checkIsExist(){
			var isSuccess=true;
			var n = [];
			var len=$("#inputTab tr").length-1;
			for ( var i = 0; i < len; i++) {
				var pid=$("#inProdId"+i).val(),skuId=$("input[tag=inSkuId"+i+"]").val();
				var data=pid+"_"+skuId;
				if (n.indexOf(data) == -1){
					n.push(data);
				} else{
					isSuccess=false;
					break;
				}
			}
			console.info(n);
			return isSuccess;
		}
		function checkIsIn(){
			var istrue=checkIsExist();
			console.info(istrue);
			if(istrue){
				ptool.signIn();
			}else{
				alert("您配置的数据无效或有重复，请查看");
			}	
		}
	</script>
  </head>
<body style="padding-top: 10px">
	<ul id="tabs">
		<li><a href="#" name="#tab1" id="jd">京东商城</a>
		</li>
		<li><a href="#" name="#tab2" id="suning">苏宁易购</a>
		</li>
	</ul>
	<input type="hidden" id="mallType" name="mallType">
	<div id="content">
		<div id="tab1">
						<dl class="setBox setBoxShow">
				<dt class="setHd">
					<span class="fr"> <a js-triger="show" class="down"
						href="javascript:void(0)"></a> </span> <i class="ic"></i> 商品匹配信息录入
				</dt>
				<dd class="setTurn">
					<table id="inputTab">
						<tr class="tabTitle">
							<td width="50" class="leftTD" align="center">国美ProductID</td>
							<td width="50" class="leftTD" align="center">商品类别</td>
							<td width="50" class="leftTD" align="center">国美SKUID</td>
							<td width="100" class="leftTD" align="center">商品名称</td>
							<td width="80" class="leftTD" align="center">商品品牌</td>
							<td width="300" class="leftTD" align="center" colspan="2"><span name="mall"></span>对应商品的链接(多个链接以英文逗号分割)</td>
							 
						</tr>
						<tr tag="inputItem">
							<td class="leftTD"><input type="text" id="inProdId0"
								onblur="ptool.checkIsGome(this)" style="height:30px" /></td>
							<td class="leftTD"><input type="text" id="inIsGome0"
								style="height:30px" /></td>
							<td class="leftTD"><input type="text"
								onblur="ptool.checkSku(this)" tag="inSkuId0" id="inSkuId0"
								style="height:30px" /></td>
							<td class="leftTD"><input type="text" id="inName0"
								style="height:30px" /></td>
							<td class="leftTD" style="border-left: none"><input type="text" id="inBrand0"
								style="height:30px" /></td>
							<td class="leftTD" colspan="2"><textarea id="inJdLink0" cols='40' rows='1'></textarea>
							</td>
						</tr>
					</table>
					<br /> <a href="javascript:void(0);" onclick="ptool.addInputItem();">+
						再加一条 （最多可同时录入5条）</a> &nbsp;&nbsp;<input type="button" class="btn"
						onclick="checkIsIn();" value="录入">&nbsp;&nbsp; <input
						type="button" class="btn" onclick="ptool.clearSign();" value="清空">
				</dd>
				<dd class="bottomLine">
					<div class="line">
						<div class="line"></div>
					</div>
				</dd>
			</dl>
			<dl class="setBox setBoxShow">
				<dt class="setHd">
					<span class="fr"> <a js-triger="show" class="down"
						href="javascript:void(0)"></a> </span> <i class="ic"></i> 商品匹配信息列表
				</dt>
				<dd class="setTurn">
					<table class="main">
						<tbody class="left signin left-signin">
							<tr>
								<td colspan="2" class="leftTD"><div id="catContent">选择站点：</div>
								</td>
							</tr>
							<tr class="eye-hide">
								<td class="leftTD"><div>按分类搜索：</div>
								</td>
								<td class="leftTD"><input id="idCate" style="height:30px"
									type="text"
									onfocus="if (value ==this.defaultValue){value =''}this.style.color='#5e5e5e'"
									onblur="if (value ==''){value=this.defaultValue};this.style.color='#a5a5a5'"
									value="搜索分类" /></td>
							</tr>
							<tr>
								<td colspan="2" class="leftTD">
									<div id=serverInfo></div></td>
							</tr>
						</tbody>
						<tbody class="right signin right-signin">
							<tr>
								<td class="leftTD"><span id="cat1"></span><span id="cat2"></span><span
									id="cat3"></span>
								</td>
							</tr>
							<tr>
								<td class="leftTD">国美SKUID: <input type="text" id="gomeSkuId" />&nbsp;&nbsp;
									匹配方式: <select name="status" id="selStatus">
										<option value="0">全部方式</option>
										<option value="1">系统匹配</option>
										<option value="2">人工匹配</option>
										<option value="3">已撤销</option>
								</select> &nbsp;&nbsp;&nbsp;&nbsp; <input id="checkData" type="button" class="btn"
									value="检索"  /> <input type="button"
									class="btn" value="清空" onclick="reset();" /></td>
							</tr>
							<tr>
								<td class="leftTD">
									<table id="suningdataList">
										<tr>
											<td width="100" class="leftTD">国美SKUID</td>
											<td width="500" class="leftTD"><span name="mall"></span>对应商品的链接</td>
											<td width="80" class="leftTD">匹配方式</td>
											<td width="150" class="leftTD">匹配时间</td>
											<td width="100" class="leftTD">操作</td>
										</tr>
									</table>
									<table id="jddataList">
										<tr>
											<td width="100" class="leftTD">国美SKUID</td>
											<td width="500" class="leftTD"><span name="mall"></span>对应商品的链接</td>
											<td width="80" class="leftTD">匹配方式</td>
											<td width="150" class="leftTD">匹配时间</td>
											<td width="100" class="leftTD">操作</td>
										</tr>
									</table>
									</td>
							</tr>
						</tbody>
					</table>
				</dd>
				<dd class="bottomLine">
					<div class="line">
						<div class="line"></div>
					</div>
				</dd>
			</dl>
			<dl class="setBox setBoxShow">
				<dt class="setHd">
					<span class="fr"> <a js-triger="show" class="down"
						href="javascript:void(0)"></a> </span> <i class="ic"></i> 操作说明
				</dt>
				<dd class="manual">
					<h4>操作说明</h4>
					<p>功能描述：</p>
					<ol class="nLowLeight">
						<li>
							<p>此功能为聪明购的辅助后台，功能目的为人工修正机器智能匹配国美与<span name="mall"></span>商品的对应关系。其中需要说明的有：</p>
							<p>1.匹配目标为国美sku与<span name="mall"></span>商品链接的对应关系</p>
							<p>2.允许业务同事补全系统未匹配成功的的sku数据</p>
							<p>2.允许业务同事撤销或者修正系统匹配不正确数据</p></li>
					</ol>
					<p>操作方法：</p>
					<ol class="nLowLeight">
						<li>sku录入：录入国美商品productId,skuId,<span name="mall"></span>商品链接，【注意】必须先选择三级类目，点击录入，其中国美skuId输入完成后，失去焦点时会进行sku验证，如果SkuId已经配置，或skuId不存在下架等会给出相应提示。
							每次录入最多可以一次性录入5条，点击再加一条即可增加输入条目，当有五个输入条目时，也可以部分添加，其余条目为空，但是不允许在同一条目内productId,skuId,<span name="mall"></span>链接输入不完全的情况。</li>
						<li>查询数据，允许业务单独根据skuId,匹配方式进行检索，点击分类查询分类下匹配的商品</li>
					</ol>
				</dd>
				<dd class="bottomLine">
					<div class="line">
						<div class="line"></div>
					</div>
				</dd>
			</dl>
			<div class="busi-hide">
				<div id="cateTempId">
					<div class="fooler" jsselect="level" jsvalues="fooler:$this"></div>
					<ul id="treeCate" class="ztree" style="-moz-user-select: -moz-none;">
						<li jsselect="childs"><span jsvalues="id:treeId($this,'switch')"
							class="button switch center_line" onclick="" treenode_switch=""></span>
							<a jsvalues="title:$this.catName;id:treeId($this,'a')" treenode_a="">
								<span class="button ico_close" treenode_ico=""
								jsvalues="id:treeId($this,'ico')"></span> <span class="icoClick"
								jscontent="$this.catName" jsvalues="id:treeId($this,'span')"></span>
						</a>
							<ul jsvalues="id:treeId($this,'ul')" class="line"></ul></li>
					</ul>
				</div>
			</div>
			<!-- 撤销匹配弹层 -->
			<div id="delMapping" class="altWin">
				<div class="altWinTitle">
					<span class="cls" onclick="ptool.closeAlert('delMapping');">×</span>
				</div>
				<div>
					是否要撤销匹配<span id="delSkuIdInfo"></span>？
				</div>
				<br>
				<div style="text-align:center;">
					<input type="button" class="btn" onclick="ptool.delMapping()"
						value="确认" /> &nbsp;&nbsp;&nbsp;&nbsp; <input type="button"
						class="btn" onclick="ptool.closeAlert('delMapping');" value="取消" /> <span
						style="display:none" id="delSkuId"></span>
				</div>
			</div>
			<!-- 修改弹层 -->
			<div id="updateMapping" class="altWin">
				<div class="altWinTitle">
					<span class="cls" onclick="ptool.closeAlert('updateMapping');">×</span>
				</div>
				<div>
					国美SKUID：<span id="updateSkuIdInfo" style="font-weight:bold;"></span>
				</div>
				<div>
					<span name="mall"></span>对应商品的链接：
					<textarea id="updateJDLinks" cols='30' rows='1'></textarea>
				</div>
				<div>
					匹配方式优先原则： <input type="radio" id="modeNone" name="mode" value="-1" />
					<label for="modeNone">不修改</label> <input type="radio" id="machine"
						name="mode" value="1" /> <label for="machine">机器匹配</label> <input
						type="radio" id="manual" name="mode" value="2" /> <label for="manual">人工匹配</label>
				</div>
				<br>
				<div style="text-align:center;">
					<input type="button" class="btn" onclick="ptool.updateMapping();"
						value="确认" /> &nbsp;&nbsp;&nbsp;&nbsp; <input type="button"
						class="btn" onclick="ptool.closeAlert('updateMapping');" value="取消" />
					<span style="display:none" id="updateSkuId"></span>
				</div>
			</div>
		</div>
	</div>
	<%@ include file="../footer.jsp"%>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/json2.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/template.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/util.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-data-smartBuy.js?d=f"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery.pagination.js"></script>
</body>
</html>
