<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <title>gome搜索rule设置</title>
    <meta charset="utf-8">
    <link href="${initParam.cdnCssServerUrl}/pages/css/business.css" rel="stylesheet"/>
    <link href="${initParam.cdnCssServerUrl}/pages/css/pagination.css" rel="stylesheet"/>
	<%@ include file="/pages/eye/jsp/common/commoncss.jspf" %>
  </head>
  <body style="padding-top: 10px">
 	<table class="main">
 		<tbody class="left signin left-signin lRule">
 			<tr><td></td></tr>
 			<tr class="suggest">
 				<td>
 					<h5>操作流程：</h5>
 					<p>1、选择需要设置的类型,facet、category、纠错词</p>
 					<p>2、输入关键词</p>
 					<p>3、提交</p>
 				</td>
 			</tr>
 			<tr class="suggest">
 				<td>
 					<h5>友情提示：</h5>
 					<p>1、设置的类型与关键词匹配</p>
 					<p>2、提交前确认操作无错误</p>
 				</td>
 			</tr>
 		</tbody>
 		<tbody class="right signin right-signin rRule">
 			<tr>
 				<td>
 					<select id="ruleSelect">
 						<option value="addFilter">筛选项</option>
						<option value="addCategory">分类</option>
						<option value="text">纠错词</option>
 					</select>
 				</td>
 			</tr>
 			<tr>
 				<td>
 					<textarea id="question" style="width:200px;" rows="5" cols="50" placeholder="这里输入关键词" ></textarea>
 					<textarea id="searchRule" style="width:200px;" rows="5" cols="50" placeholder="这里输入类型的值"></textarea>
 				</td>
 			</tr>
 			<tr>
 				<td><input type="button" class="btn" value="提交" onclick="tool.confirm()"/></td>	
 			</tr>
 			<tr>
 				<td><br /></td>
 			</tr>
 			<tr>
 				<td>结果提示信息：<em id="tips"></em><span class="eye-hide"><img src="/pages/img/loading.gif" /></span></td>
 			</tr>
 		</tbody>
 	</table>
	<div id="que_err_msg" style="display:none;"></div>
	<div id="val_err_msg" style="display:none;"></div>
	<div id="comit_msg" style="display:none;"></div>
	<%@ include file="../footer.jsp"%>
    <script src="${initParam.cdnJsServerUrl}/pages/js/searchRule.js"></script>
  </body>
</html>
