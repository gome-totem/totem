<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>My JSP 'addDictConfig.jsp' starting page</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
  </head>
  
  <body>
  	<form id="ff" method="post" action="/eye/data/addServer">
    	<table>
    		<thead>
    			<tr>
    				<td colspan="2" style="padding-top: 10px;"></td>
    			</tr>
    		</thead>
    		<tbody>
	    		<tr>
	    			<td align="right">
	    				<div class="addServer">IP地址:</div>
	    			</td>
	    			<td ><div class="addServer">
	    					<input type="text" class="easyui-validatebox" name="ip_address" data-options="missingMessage:'请输入正确的IP地址',required:true" style="width: 300px" onblur="checkData()">
	    				</div>
	    			</td>
	    		</tr>
	    		<tr>
	    			<td align="right">
	    				<div class="addServer">服务:</div>
	    			</td>
	    			<td ><div class="addServer">
	    			<select class="easyui-combobox" name="service" data-options="url:'/eye/data/getServices',valueField:'value',textField:'text',multiple:true,multiline:true,required:true,missingMessage:'请选择服务，可多选'" style="width:300px;height:40px">
	    			</select></div></td>
	    		</tr>
	    		<tr>
	    			<td align="right">
	    				<div class="addServer">部署环境:</div>
	    			</td>
	    			<td><div class="addServer"><select type="text" class="easyui-combobox" name="codeMap1" style="width: 150px" data-options="required:true,missingMessage:'请选择环境'">
	    					<option value="10000">生产环境</option>
	    					<option value="10001">测试环境</option>
	    					<option value="10002">开发环境</option>
	    			</select>
	    			<select type="text" class="easyui-combobox" name="codeMap2" style="width: 150px">
	    					<option value="prd">prd</option>
	    					<option value="pre">pre</option>
	    					<option value="sit">sit</option>
	    					<option value="uat">uat</option>
	    			</select>
	    			</div></td>
	    		</tr>
	    		<tr>
	    			<td align="right">
	    				<div class="addServer">短信状态:</div>
	    			</td>
	    			<td><div class="addServer">
	    				<input type="radio" value="1" name="msgStatus" checked="checked">启用
	    				<input type="radio" value="0" name="msgStatus" style="margin-left: 40px;">禁用
	    			</div></td>
	    		</tr>
	    		<tr >
	    			<td align="right" style="padding-top: 30px;"><div class="addServer"></div></td>
	    			<td align="center" style="padding-top: 30px;"><div class="addServer">
	    			<input type="button" class="easyui-linkbutton" value="确定" style="width: 80px"  onclick="dictData.addEecute('server')">
	    			<input type="button" class="easyui-linkbutton" style="margin-left: 50px;width: 80px" value="取消" onclick="$('#topWindow').dialog('close')"></div>
	    			</td>
	    		</tr>
    		</tbody>
    	</table>
    </form>
  <script type="text/javascript" src="/pages/js/dict-data-config.js"></script>
  <style type="text/css">
	.addServer{
		padding-left: 20px;
		padding-top: 15px;
	}
	</style>
  </body>
</html>
