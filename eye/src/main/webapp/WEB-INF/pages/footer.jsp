<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page language="java" import="com.gome.totem.sniper.util.Globalkey" %>
<%
String footPath = request.getContextPath();
String socketPath = request.getServerName()+":"+request.getServerPort()+footPath+"/";
String wsPath = Globalkey.WSHOST;
%>
	<input id="hostName" type="hidden" value="<%= socketPath %>/cloud/eye">
</div> 
<script src="${initParam.cdnJsServerUrl}/pages/js/jquery-1.9.1.js"></script>
<script src="${initParam.cdnJsServerUrl}/pages/js/jquery-ui-1.10.2.custom.js"></script>
<script src="${initParam.cdnJsServerUrl}/pages/js/bootstrap.js"></script>
<script src="${initParam.cdnJsServerUrl}/pages/js/eyeIndex.min.js"></script>
<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery.common.js"></script>

<!--easyUI-->

<link rel="stylesheet" type="text/css" href="${initParam.cdnJsServerUrl}/pages/easyui/themes/bootstrap/easyui.css">
<link rel="stylesheet" type="text/css" href="${initParam.cdnJsServerUrl}/pages/easyui/themes/icon.css">
<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/easyui/jquery.easyui.min.js"></script>
<script src="${initParam.cdnJsServerUrl}/pages/easyui/locale/easyui-lang-zh_CN.js"></script>

<script type="text/javascript" language="javascript">
$(document).ajaxStart(function(){
 	$.ajax({
		url : '/cloud/eye/verifyUser',
		type : 'get', timeout : 800000,	cache : false,
		dataType : 'json',
		success : function(data) {
			if (!data) {
				window.parent.location = "/login.jsp";
				return;
			}
		},
		error : function(errorThrown) {
		}
	});
});
/*
function iFrameHeight() {
	var ifm= document.getElementById("iframepage");
	var subWeb = document.frames ? document.frames["iframepage"].document : ifm.contentDocument;
	if(ifm != null && subWeb != null) {
		ifm.height = subWeb.body.scrollHeight;
	}
}
*/
function reinitIframe(){
	var iframe = document.getElementById("iframepage");
	if(iframe==null){
		return;
	}
	if(iframe.contentWindow.document.getElementById("monitorPage")!=null&&iframe.contentWindow.document.getElementById("monitorPage").innerHTML=="true"){
		iframe.height = 650;
		return;
	}
	try{
		var bHeight = iframe.contentWindow.document.body.scrollHeight;
		var dHeight = iframe.contentWindow.document.documentElement.scrollHeight;
		var height = Math.max(bHeight, dHeight);
		if(height<500){
			iframe.height = 500;
			return;
		}
		if(iframe.height != height){
			iframe.height = height;
		}
	}catch(ex){
		
	}
}
window.setInterval("reinitIframe()", 200);

$(document).ready(function(){
	$(".leftBar dd a").click(function(){
		var _this = $(this);
		$("dd a").removeClass("cur");
		_this.addClass("cur");
		var l1 = _this.parent().siblings().html().replace("<i></i><s></s>","");
		var l2 = _this.html().replace("<s></s>","");
		$("#l1 span").html(l1);
		$("#l2 span").html(l2);
	});
	
	var page_tag = $("#page_tag").html();
	$(".nav a").removeClass("cur");
	$("#tg_"+page_tag).addClass("cur");
});
</script> 