<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <title>ES同步缓存</title>
	 <%@ include file="/pages/eye/jsp/common/commoncss.jspf" %>
    <link href="${initParam.cdnCssServerUrl}/pages/css/business.css" rel="stylesheet"/>
    <link href="${initParam.cdnCssServerUrl}/pages/css/base.css" rel="stylesheet"/>
  </head>
  	<body style="font-size: 14px;">
	<div class="container" style="width: auto;">
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
			<table class="main">
				<tbody class="signin" style="margin-left: 30px;" title="ES同步缓存">
					<tr>
						<td>
							<div >
								ES同步地址：<input id="ascIp" class="easyui-combobox" style="width: 150px;"/>
							</div>
						</td>
						<td>
							<div>
								&nbsp;&nbsp;ES同步端口：<input id="ascPort" class="easyui-numberbox" value="9200" data-options="min:0,precision:0" style="width: 65px;"/>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<div>
								ES同步类型：<select id="ascCate" class="easyui-combobox" style="width: 150px;">
												<option value="all">全国数据同步</option>
												<option value="catid">分类数据同步</option>
											</select>
							</div>
						</td>
						<td>
							<div id="catid" style="display: none">
								&nbsp;&nbsp;分类ID：<input id="ascCatd" class="easyui-validatebox" value="" style="width: 100px;"/>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<div>
								<input class="btn btn-ext" type="button" id="ascButton" value="开始同步" />
								<input  type="hidden" id="esServer" value='${esServer}' />
							</div>
						</td>
						<td></td>
					</tr>
				</tbody>
				<tbody class="signin" style="margin-left: 30px;" title="同步缓存">
					<tr>
						<td>
							<div >
								<font > URL操作:</font>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<div>
								URL地址：<textarea  id="ascUrl" style="width: 240px;height:75px">
								</textarea>
								<input class="btn btn-ext" type="button" id="ascData" value="开始执行" />
							</div>
						</td>
					</tr>
				</tbody>
			</table>
		</dd>
		<dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
	</dl>
	<div id="topWindow"></div>
  	<%@ include file="../footer.jsp"%>
  	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-es-asc.js"></script>
  	</body>
</html>


