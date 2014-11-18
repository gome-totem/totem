<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<html>
  <head>
    <title>gome分类排序</title>
    <meta charset="utf-8">
	<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/business.css" />
	<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/base.css"/>
	<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/pagination.css"/>
	<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/zTree.css"/>
	<%@ include file="/pages/eye/jsp/common/commoncss.jspf" %>
  </head>
  
  <body style="padding-top: 5px;">
  <dl class="setBox setBoxShow" >
		<dt class="setHd">

			<i class="ic"></i>
			操作
		</dt>
		<dd class="setTurn">
				<table class="main" >
					<tbody class="left signin left-signin">
						<tr>
							<td colspan="2"><div class="cate" id="catContent">选择站点：</div></td>
						</tr>
						<tr class="eye-hide">
							<td width="40%"><div class="cate">按分类搜索：</div></td>
							<td>
								<input id="idCate" type="text" onfocus="if (value ==this.defaultValue){value =''}this.style.color='#5e5e5e'" onblur="if (value ==''){value=this.defaultValue};this.style.color='#a5a5a5'" value="搜索分类" />
								<input id="catIdSearch" class="btn btn-primary" type="button" value="提交" onclick="brandSet.treeTarget(111)"/>
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
							<td colspan="2" width="40%">
								<div class="cs_title"><span id="categoryListTitle"></span>子集分类列表:</div>
							</td>
							<td colspan="2" width="60%">
								<div class="cs_title"><span id="categoryListTitle"></span>分类排序列表:</div>
							</td>
						</tr>
						<tr>
							<td colspan="2" id="categoryListContet" valign="top">
							  <span style="width: 300px;color: green;">请选择所对应的分类---</span>
							</td>
							<td colspan="2" valign="top"><textarea id="categoryOrder" cols ="50" rows = "5" style="width: 400px;"></textarea></td>
						</tr>
						<tr>
							<td colspan="2"></td>
							<td colspan="2" valign="top"><input type="button" class="btn" onclick="promotion.setCategoryOrder()" value="提交"/></td>
						</tr>
						<tr>
							<td colspan="2"></td>
							<td colspan="2" style="height: 100" valign="top">提示信息：<span id="tips">无</span></td>
						</tr>
					</tbody>
				</table>
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
			<dd class="manual">
				<h4>操作说明</h4>
				<p>功能描述：</p>
				<ol class="nLowLeight">
					<li>此功能用于三级分类页左边分类列表排序，可针对二级或者三级进行排序.</li>
				</ol>
				<p>操作方法：</p>
				<ol class="nLowLeight">
					<li>选择要排序的分类的上级分类，系统将在右侧列出所选分类所有的子集分类列表.</li>
					<li>复制所要排序分类的id信息，填入分类排序列表中，以英文逗号分割(注意：排序列表框中不能含有空格以及回车换行等信息，如只输入了部分分类信息，则所输入分类将按照输入顺序排在最前面，其它分类按照系统默认顺序进行排列).</li>
					<li>点击提交.</li>
				</ol>
			</dd>
		<dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
	</dl>
  	<div class="busi-hide">
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
	
	<%@ include file="../footer.jsp"%>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/json2.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/template.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/util.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/categoryOrderSet.js"></script>
  </body>
</html>
