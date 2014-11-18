<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
<script src="${initParam.cdnJsServerUrl}/pages/js/jquery-1.9.1.js"></script>
<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/easyui/jquery.easyui.min.js"></script>
<script src="${initParam.cdnJsServerUrl}/pages/easyui/locale/easyui-lang-zh_CN.js"></script>
<script src="/pages/js/edm-data-config.js"></script>
<link rel="stylesheet" type="text/css" href="${initParam.cdnJsServerUrl}/pages/easyui/themes/bootstrap/easyui.css">
<link rel="stylesheet" type="text/css" href="${initParam.cdnJsServerUrl}/pages/easyui/themes/icon.css">
<style type="text/css">
	table td{
		
	}
</style>
<script type="text/javascript">
	$(function(){
		var w=$('#addPanel').width()-20;
		$('#addPanel').width(w);
		$('#dataList').width(w);
		dictData.showDataList("1");
	});
</script>
</head>
<body >
	<div style="width: 80%">
		<div id="addData" class="easyui-panel" >
			<table style="margin-top: 8px;">
				<tr>
					<td>系统参数为1webpower活动ID：<input type="text" id="campaignId" name="data1" size="10px"/></td>
					<td>系统参数为1webpower邮件ID：<input type="text" id="mailingId" name="data2" size="10px"/></td>
					<td>&nbsp;&nbsp;系统参数为1邮件模板名称：<input type="text" id="template" name="data3" size="10px"/></td>
					<td>
					<a href="#" class="easyui-linkbutton" style="margin-left: 10px" onclick="javascript:dictData.addData(1);">&nbsp;添加&nbsp;</a>
					</td>
				</tr>
				
		
				
				<tr>
					<td>系统参数为2添加邮件ID：<input type="text" id="ldmailingid" name="lddata1" size="10px"/></td>
					<td>系统参数为2添加发送计划ID：<input type="text" id="ldsendplanid" name="lddata2" size="10px"/></td>
					<td>系统参数为2邮件模板名称：<input type="text" id="ldtemplatename" name="lddata3" size="10px"/></td>
					<td>
					<a href="#" class="easyui-linkbutton" style="margin-left: 10px" onclick="javascript:dictData.addData(3);">&nbsp;添加&nbsp;</a>
					</td>
				</tr>
				
			</table>
		</div>
		</div>
		<div style="width: 80%">
					<p>触发条件组合添加</p>
		<div id="addData" class="easyui-panel" >
			<table style="margin-top: 8px;">
		
			<tr>
					<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;组合名称:<input type="text" id="comname" name="comname" size="10px" value=""/></td>
			</tr>
			
			<tr>
					<td>关键词或catid搜索次数:<input type="text" id="clickuptimes" name="clickuptimes" size="10px" value=""/>次</td>
			</tr>
			
			<tr>
					<td>点击列表商品次数:<input type="text" id="clickdowntimes" name="clickdowntimes" size="10px" value=""/>次</td>
			</tr>
			
			<tr>
					<td>总点击次数:<input type="text" id="totalclicktimes" name="totalclicktimes" size="10px" value="0"/>次</td>
			</tr>
			
			<tr>
					<td>&nbsp;&nbsp;时间：<input type="text" id="time" name="time" size="10px" value=""/>分钟</td>
			</tr>
			<tr>
					<td>&nbsp;&nbsp;&nbsp;CatId:<input type="text" id="catid" name="catid" size="10px" value="0"/>(若没有任何限制则设置为0)</td>
			</tr>
				<tr>
					<td>选择时间段：&nbsp;&nbsp;<select class="easyui-combobox" id="timerange" style="width:80px" name="timerange" data-options="multiple:true,multiline:true">
									<option value="0">0-6</option>
									<option value="1">6-12</option>
									<option value="2">12-18</option>
									<option value="3">18-24</option>
									<option value="4">全天</option>
								  </select>
					</td>
				</tr>
			<tr>
					<td>
					<a href="#" class="easyui-linkbutton" style="margin-left: 10px" onclick="javascript:dictData.addData(2);">&nbsp;添加&nbsp;</a>
					</td>
			</tr>
			</table>
		</div>
		</div> 
		<div style="width: 80%">
		<div id="deleteData" class="easyui-panel" >
			<table style="margin-top: 8px;">
				<tr>
					<td>邮件模板：<input class="easyui-combobox" data-options="url:'/eye/data/edmPlat?flag=3&select=1',valueField:'mailingid',textField:'templatename'" id="delete1" name="deletemplate1"/></td>	
				<td>
					<a href="#" class="easyui-linkbutton" style="margin-left: 10px" onclick="javascript:dictData.delData(1);">&nbsp;删除&nbsp;</a>
					</td>
				</tr>
				<tr>
					<td>发送条件：<input class="easyui-combobox" data-options="url:'/eye/data/edmPlat?flag=3&select=2',valueField:'actionname',textField:'actionname'" id="delete2" name="deletemplate2"/></td>	
					<td>
					<a href="#" class="easyui-linkbutton" style="margin-left: 10px" onclick="javascript:dictData.delData(2);">&nbsp;删除&nbsp;</a>
					</td>
				</tr>
				<tr>
					
				</tr>
				
			</table>
		</div>
		
		 
		
		<div id="addPanel" class="easyui-panel" >
			<form id="addForm" action="/eye/data/edmPlat?">
					<input type="hidden" id="flag" name = "flag" value="2"/>
			<table style="margin-top: 8px;">
				<tr>
					<td>系统参数:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" class="easyui-textbox" id="systemPara" name="systemPara" value="1"/></td>
				</tr>
				<tr>
					<td>选择邮件模板：<input class="easyui-combobox" data-options="url:'/eye/data/edmPlat?flag=3&select=1',valueField:'mailingid',textField:'templatename'" id="mailtemplate" name="mailtemplate"/></td>
					<td>设定邮件参数范围:<input type="text" class="easyui-textbox" id="paramrange" name="paramrange" value="1-10"/></td>
				</tr>
				<tr>
					<td>选择发送条件：<input class="easyui-combobox" data-options="url:'/eye/data/edmPlat?flag=3&select=2',valueField:'actionname',textField:'actionname',multiple:true,multiline:true" id="sendcondition" name="sendcondition"/></td>
				</tr>
				<tr>
					<td>选择时间段：&nbsp;&nbsp;<select class="easyui-combobox" id="timerange" style="width:80px" name="timerange" data-options="multiple:true,multiline:true">
									<option value="0">0-6</option>
									<option value="1">6-12</option>
									<option value="2">12-18</option>
									<option value="3">18-24</option>
									<option value="4">全天</option>
								  </select>
					</td>
				</tr>
				<!-- <tr>
					<td>Select4：&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input class="easyui-combobox" data-options="url:'/eye/data/edmPlat?flag=3&select=2',valueField:'value',textField:'text',multiple:true,multiline:true" id="select4" name="select4"/></td>
				</tr>
					-->
				<tr>
					<td>选择性别：<select class="easyui-combobox" id="gender" style="width:80px" name="gender">
									<option value="2">男</option>
									<option value="1">女</option>
									<option value="0">全部</option>
								  </select>
					</td>
					</tr>
					<tr>
					<td>选择年龄段：&nbsp;&nbsp;<select class="easyui-combobox" id="age" style="width:80px" name="age" data-options="multiple:true,multiline:true" >
									<option value="0">1-20</option>
									<option value="1">20-40</option>
									<option value="2">40-60</option>
									<option value="3">60以上</option>
									<option value="4">全部</option>
								  </select>
					</td>
					</tr>
					<tr>
					<td>
					<a href="#" class="easyui-linkbutton" style="margin-left: 50px" onclick="javascript:dictData.add();">&nbsp;提&nbsp;&nbsp;交&nbsp;</a>
					</td>
					</tr>
			</table>
			</form>
		</div>
	</div>
	<div style="width: 80%">
		<div id="listPanel" >
			<table id="dataList" ></table>
		</div>
	</div>
</body>
</html>

