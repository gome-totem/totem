<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>索引创建</title>
<script src="${initParam.cdnJsServerUrl}/pages/js/jquery-1.9.1.js"></script>
<link href="${initParam.cdnCssServerUrl}/pages/css/base.css" rel="stylesheet"/>
<script type="text/javascript">
	var timer;
	function submit() {
		var opp = $("#opp").val();
		var type = $("#type").val();
		var env = $("#env").val();
		$.ajax({
			type : "post",
			url : "/cloud/eye/fullindex",
			data : {
				"opp" : opp,
				"type" : type,
				"env":env
			},
			success : function(result) {

			}
		});
	}
	function getLog() {
		$.ajax({
			type : "get",
			url : "/cloud/eye/fullindex",
			success : function(result) {
				if(result!=null&result!=""){
					$("#logs").html(result);
				}
			}
		});
	}

	function stopLog() {
		clearInterval(timer);
		$.ajax({
			type : "get",
			url : "/cloud/eye/fullindex",
			data : {
				"opp" : "stop"
			},
			success : function(result) {
				if(result!=null&result!=""){
					$("#logs").html(result);
				}
			}
		});
	}

	function startLog() {
		$.ajax({
			type : "get",
			url : "/cloud/eye/fullindex",
			data : {
				"opp" : "start"
			},
			success : function(result) {
				timer = setInterval("getLog()", 1000);
				$("#logs").html(result);
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
	 		<%--|<a js-triger="close" class="close" href="javascript:void(0)"></a>--%>
			</span>
			<i class="ic"></i>
			操作
		</dt>
		<dd class="setTurn">
			<select name="env" id="env">
				<option value="sit">sit</option>
				<option value="uat">uat</option>
				<option value="pre">pre</option>
				<option value="live" selected="selected">live</option>
			</select>
			<select name="opp" id="opp">
				<option value="start">start</option>
				<option value="stop">stop</option>
			</select>
			<select name="type" id="type">
				<option value="category">Category</option>
				<option value="product">Product</option>
			</select>
			<a href="#"  onclick="submit()" class="easyui-linkbutton" >&nbsp;提交&nbsp;</a>
			<a href="#"  onclick="startLog()" class="easyui-linkbutton" >获取日志</a>
			<a href="#"  onclick="stopLog()" class="easyui-linkbutton" >停止日志</a>
			<div id="logs"></div>
		</dd>
		<dd class="bottomLine">
			<div class="line"><div class="line"></div></div>
		</dd>
	</dl>
	<%@ include file="../footer.jsp"%>
</body>
</html>
