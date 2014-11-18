<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <title>每日增量</title>
    <script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery-1.9.1.js"></script>
	 <%@ include file="/pages/eye/jsp/common/commoncss.jspf" %>
	 <link href="${initParam.cdnCssServerUrl}/pages/css/base.css" rel="stylesheet"/>
    <link href="${initParam.cdnCssServerUrl}/pages/css/business.css" rel="stylesheet">
     <script type="text/javascript">
     		function polyphoneFix(){
     			var facetId = $("#facetId").val();
     			var facetName = $("#name_zh").val();
     			var short = $("#short").val();
     			var full = $("#full").val();
        		$.getJSON("/cloud/eye/dicSM",
        				{facetId:facetId,
        				 facetName:encodeURIComponent(facetName),
        				 short:short,
        				 full:full},
        				function(data){
        					if(data.msg=="success"){
        						$("#msg").html("修改成功！");
        					}else if(data.msg=="valiErr"){
        						$("#msg").html("数据输入不合法！");
        					}else{
        						$("#msg").html("修改失败！");
        					}
        				}
        		);
     		}
     </script>
     <style type="text/css">
	     	.red{color:red}
     </style>
   </head>
<body>
   <dl class="setBox setBoxShow">
  		<dt class="setHd">
	  		<span class="fr">
		  		<a js-triger="show" class="down" href="javascript:void(0)"></a>
	  		</span>
	  		<i class="ic"></i>
	  		操作
  		</dt>
  		<dd class="setTurn">
				<table class="main">
					<tbody class="signin">
					<tr>
						<td>facetId：</td>
						<td>
							<div>
								<input type="text" id="facetId" placeholder="输入FacetId" />
							</div>
						</td>
					</tr>
					<tr>
						<td>筛选项名： </td>
						<td>
							 <div><input type="text" id="name_zh" placeholder="筛选项名" /></div>
						</td>
					</tr>
					<tr>
						<td>拼音缩写：</td>
						<td>
							
								<div><input type="text" id="short" placeholder="拼音缩写" /></div>
							
						</td>
					</tr>
					
					<tr>
						<td>全拼： </td>
						<td>
							<div><input type="text" id="full" placeholder="全拼" /></div>
						</td>
					</tr>
					<tr>
						<td colspan="2"  align="center">
							<input type="button" onclick="polyphoneFix()" class="btn btn-ext" value="提交" />
						</td>
					<tr>
							
					<tr>
							<td colspan="2" align="center">
								<div id="msg" class="red"></div>
							</td>
						</tr>
					</tbody>
				</table>
        </dd>
       <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
   </dl>			
				
	
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/bootstrap-transition.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-error.js"></script>
	<%@ include file="/footer.jsp"%>
</body>
</html>