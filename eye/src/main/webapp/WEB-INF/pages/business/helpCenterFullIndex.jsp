<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>帮助中心</title>
<script src="${initParam.cdnJsServerUrl}/pages/js/jquery-1.9.1.js"></script>
<link href="${initParam.cdnCssServerUrl}/pages/css/base.css" rel="stylesheet"/>
<script type="text/javascript">
	function startFullIndex() {
		$.ajax({
			type : "post",
			url : "/cloud/business/helpcenter",
			data : {
				"module" : "ccfullIndex",
			},
			dataType:"json",
			success:function(data) {
				if(data.msg=="success"){
					$("#logs").html("全量已经开始！");
				}else{
					$("#logs").html("系统出错！");
				}
			}
		});
	}
	
</script>
</head>

<body>
  <dl class="setBox setBoxShow">
		<dt class="setHd">
			<span class="fr">
	 		<a js-triger="show" class="down" href="javascript:void(0)"></a>
			</span>
			<i class="ic"></i>
			帮助中心搜索全量索引
		</dt>
		<dd class="setTurn">
			点击按钮开始
			</br>			
			<input type="button" value="开始" onclick="startFullIndex()" />
			<div id="logs"></div>
		</dd>
		<dd class="bottomLine">
			<div class="line"><div class="line"></div></div>
		</dd>
	</dl>
	<%@ include file="../footer.jsp"%>
</body>
</html>
