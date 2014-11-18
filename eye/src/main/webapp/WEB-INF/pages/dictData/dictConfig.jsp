<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page language="java" import="com.gome.totem.sniper.util.Globalkey" %>
<!DOCTYPE html>
<%
String foot = request.getContextPath();
String socket = request.getServerName()+":"+request.getServerPort()+foot+"/";
%>
<html>
<head>
<title>服务参数配置</title>
<script src="${initParam.cdnJsServerUrl}/pages/js/jquery-1.9.1.js"></script>
<link href="${initParam.cdnCssServerUrl}/pages/css/base.css"rel="stylesheet" />
<style type="text/css">
	.datagrid-toolbar table{
		margin-left:70%;
	}
</style>
</head>

<body>
	<div id="ajax"></div>
	<input id="hostName" type="hidden" value="<%= socket %>/cloud/eye">
	<input id="hostDomin" type="hidden" value="<%= Globalkey.detectAddress %>">
	<dl class="setBox setBoxShow" style="width: 90%">
		<dt class="setHd">
			<span class="fr"> <a js-triger="show" class="down"
				href="javascript:void(0)"></a> <%--|<a js-triger="close" class="close" href="javascript:void(0)"></a>--%>
			</span> <i class="ic"></i> 数据字典
		</dt>
		<dd>
			<table width="98%">
				<tbody>
					<tr>
						<td width="15%" class="leftTD" valign="top" style="padding-left: 10px;padding-top: 10px;">
							<div class="easyui-panel" data-options="collapsible:true"
								style="height: 665px;">
								<ul id="dictCategory" class="easyui-tree" data-options="url:'/pages/js/tree_data.json',method:'get',animate:true,"></ul>
							</div></td>
						<td width="85%" class="rightTD">
							<div class="easyui-panel" style="height: 700px;border: none">
								<div style="width: 98%;padding-left: 10px;padding-top: 10px;" id="datagrid">
									<div class="easyui-panel" style="height: 70px;" data-options='collapsible:true' id="queryCondition">
										<br />
										<table class="f12">
											<tbody id="queryServer" style="display: none">
												<tr align="center">
													<td style="padding-left:10px;">筛选条件&nbsp;<select
														class="easyui-combobox" style="width:100px" id="select1">
															<option value="">&nbsp;&nbsp;</option>
															<option value="1">IP地址</option>
															<option value="2">服务Tag</option>
													</select></td>
													<td style="padding-left:10px;">部署环境&nbsp;<select
														class="easyui-combobox" style="width:100px" id="select2">
															<option value="">全部</option>
															<option value="10000">生产环境</option>
															<option value="10001">测试环境</option>
															<option value="10002">开发环境</option>
													</select></td>
													<td style="padding-left:10px;">搜索词&nbsp;<input
														class="easyui-textbox" style="width:200px" id="keywords"></td>
													<td><a class="easyui-linkbutton"
														data-options="iconCls:'icon-search'"
														style="margin-left: 20px;" onclick="dictData.query(1,'server')">查询</a></td>
													<td style="padding-left:10px;" align="right"><a
														class="easyui-linkbutton"
														data-options="iconCls:'icon-add'"
														style="margin-left: 20px;" onclick="dictData.add('server')">新增</a></td>
												</tr>
											</tbody>
											<tbody id="queryBasic" style="display: none">
												<tr align="center">
													<td style="padding-left:10px;">部署环境&nbsp;<select
														class="easyui-combobox" style="width:100px" id="select3">
															<option value="">&nbsp;&nbsp;</option>
															<option value="1">生产环境</option>
															<option value="2">测试环境</option>
															<option value="3">开发环境</option>
													</select></td>
													<td style="padding-left:10px;">KEY值&nbsp;<input
														class="easyui-textbox" style="width:200px"></td>
													<td><a class="easyui-linkbutton"
														data-options="iconCls:'icon-search'"
														style="margin-left: 20px;" onclick="dictData.query('basic')">查询</a></td>
													<td style="padding-left:10px;" align="right"><a
														class="easyui-linkbutton"
														data-options="iconCls:'icon-add'"
														style="margin-left: 20px;" onclick="dictData.add('basic')">新增</a></td>
												</tr>
											</tbody>
										</table>
									</div>
								</div>
								<div style="width: 98%;padding-left:10px; ">
									<table id="dict_data">
									</table>
								</div>
							</div></td>
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
	<div id="topWindow"></div>
	<script src="${initParam.cdnJsServerUrl}/pages/js/dict-data-config.js"></script>
	<%@ include file="../footer.jsp"%>
	<style type="text/css">
	.tree-node-selected{
		background: none repeat scroll 0 0 #CCCCCC;
	}
	</style>
</body>
</html>
