<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <title>业务手动增量索引</title>
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
				<tbody class="signin">
					<tr>
						<td>
							<div id="default">
								<input class="input-skuId" style="color: rgb(165, 165, 165);" id="idSku" type="text" onfocus="if (value ==this.defaultValue){value =''};this.style.color='#5e5e5e'" onblur="if (value ==''){value=this.defaultValue};this.style.color='#a5a5a5'" value="skuNo" />
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<select class="sele" id="selectId" onchange="return dragon.showMessage();">
								<option value="0">skuNo</option>
								<option value="1">cityId</option>
								<option value="2">question</option>
							</select>
							<span id="idCity" class="eye-hide">
								<input class="input-skuId" style="color: rgb(165, 165, 165);" type="text" onfocus="if (value ==this.defaultValue){value =''};this.style.color='#5e5e5e'" onblur="if (value ==''){value=this.defaultValue};this.style.color='#a5a5a5'" value="CityId" />
								<input type="radio" name="a" checked="checked" value="1">有货
								<input type="radio" name="a" value="0">无货
								<input class="btn btn-ext" type="button" value="提交"  onclick="dragon.watersearch()"/>
							</span>
							<div id="idSearch" class="eye-hide">
								<input class="input-skuId" style="color: rgb(165, 165, 165);width:140px;" type="text" onfocus="if (value ==this.defaultValue){value =''};this.style.color='#5e5e5e'" onblur="if (value ==''){value=this.defaultValue};this.style.color='#a5a5a5'" id="inPutQuestion" value="Question"/>
								<input class="input-skuId" style="color: rgb(165, 165, 165);width:140px;" type="text" onfocus="if (value ==this.defaultValue){value =''};this.style.color='#5e5e5e'" onblur="if (value ==''){value=this.defaultValue};this.style.color='#a5a5a5'" id="inPutCatId"  value="CatId"/>
								<input class="input-skuId" style="color: rgb(165, 165, 165);width:70px;" type="text" onfocus="if (value ==this.defaultValue){value =''};this.style.color='#5e5e5e'" onblur="if (value ==''){value=this.defaultValue};this.style.color='#a5a5a5'" id="inPutPageNum"  value="PageNum"/>
								<input class="input-skuId" style="color: rgb(165, 165, 165);width:70px;" type="text" onfocus="if (value ==this.defaultValue){value =''};this.style.color='#5e5e5e'" onblur="if (value ==''){value=this.defaultValue};this.style.color='#a5a5a5'" id="inPutProductTag"  value="ProductTag"/>
								<input class="btn btn-ext" type="button" value="提交"  onclick="dragon.drgDevTools()"/>
							</div>
							<input class="btn btn-ext" type="button" id="skuIdSearch" value="提交"  onclick="dragon.firesearch()"/>
						</td>
					</tr>
					<tr>
					 <td>
					 <span id="info"></span>
					 </td>
					</tr>
				</tbody>
			</table>
		</dd>
		<dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
	</dl>
  	<%@ include file="../footer.jsp"%>
  	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-busi-dragon-new.js?df=d"></script>
  	</body>
</html>
