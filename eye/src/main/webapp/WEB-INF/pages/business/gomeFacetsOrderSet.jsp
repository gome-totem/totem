<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<html>
  <head>
    <title>gome筛选项排序</title>
    <meta charset="utf-8">
    <link href="${initParam.cdnCssServerUrl}/pages/css/business.css" rel="stylesheet"/>
    <link href="${initParam.cdnCssServerUrl}/pages/css/pagination.css" rel="stylesheet"/>
    <link href="${initParam.cdnCssServerUrl}/pages/css/zTree.css" rel="stylesheet"/>
    <link href="${initParam.cdnCssServerUrl}/pages/css/square/green.css" rel="stylesheet">
    <link href="${initParam.cdnCssServerUrl}/pages/css/base.css" rel="stylesheet"/>
	<%@ include file="/pages/eye/jsp/common/commoncss.jspf" %>
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
  </head>
  
  <body style="padding-top: 5px;">
  
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
			
			<tbody class="right signin right-signin" style="margin-left: 10px;font-size: 13px;'">
						<tr>
							<td colspan="2" width="40%">
								<div class="cs_title"><span id="facetsListTitle"></span>筛选项列表:<span id="catIdHide" style="display: none;"></span></div>
							</td>
							<td colspan="2" width="60%">
								<div class="cs_title"><span ></span>筛选项名排序列表:</div>
							</td>
						</tr>
						<tr>
							<td colspan="2" valign="top">
							<div id="facetsGroups" style="float:left;">
								<ul style="list-style: none outside none;">
									<li style="width: 300px;color: green;"><sapn>请选择所对应的三级分类…</sapn>
									</li>
								</ul>
							</div> 
							</br>
							<div id="selFacetsNames"
								style="margin-top: 10px;color: navy;font-weight: bold;"></div></td>
							<td colspan="2" valign="top"><textarea id="categoryOrder" cols ="50" rows = "5" style="width: 400px;"></textarea></td>
						</tr>
					<tr>
						<td colspan="2"></td>
						<td colspan="2" valign="top"><input type="button" class="btn"
							onclick="promotion.setBrandOrder()" value="提交" />
						</td>
					</tr>
					<tr>
						<td colspan="2"></td>
						<td colspan="2" style="height: 100" valign="top">提示信息：<span id="tips">无</span>
						</td>
					</tr>
				</tbody>
		</table>
	</dd>
	<dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
  </dl>
  <dl class="setBox setBoxShow">
	<dt class="setHd">
		<span class="fr">
 		<a js-triger="show" class="down" href="javascript:void(0)"></a>|
 		<a js-triger="close" class="close" href="javascript:void(0)"></a>
		</span>
		<i class="ic"></i>
		操作说明
	</dt>
	<dd class="setTurn">
		<table class="main">
			<tbody>
				<tr>
					<td style="height: 100"><span class="suggest">
					<h6>功能描述：</h6>
									此功能用于三级分类页以及搜索结果页facet具体值进行排序.</br>
					<h6>操作方法：</h6>
							  <div>
							  		<div> 1、选择要排序三级分类分类，系统将在右侧列出所选分类下所有的筛选项集合的列表.<div>
							  		<div> 2、点击选择要排序筛选项集合.<div>  
					        		<div> 3、将所选筛选项下要排序的筛选项名复制进筛选项名排序列表框中(注意：排序列表框中不能含有空格以及回车换行等信息，筛选项名称必须和网站显示名称严格一致，
					        		否则可能导致排序不生效，如只输入了部分筛选项信息，则所输入筛选项将按照输入顺序排在最前面，其它筛选项按照系统默认顺序进行排列).</div>
					        		<div> 4、点击提交.</div>
					        </div>
					</span></td>
				</tr>
			</tbody>
		</table>
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
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery.icheck.min.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/json2.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/template.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/util.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/facetOrderSet.js"></script>
	
  </body>
</html>
