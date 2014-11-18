<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <title>信息群发页面</title>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <meta name="description" content="mobile message send"/>
    <meta name="author" content="xiong1989win@126.com"/>
    <script type="text/javascript" src="/pages/js/jquery-1.9.1.js"></script>
    <link href="${initParam.cdnCssServerUrl}/pages/css/base.css" rel="stylesheet"/>
    <link href="${initParam.cdnCssServerUrl}/pages/css/business.css" rel="stylesheet">
    <style type="text/css">
    	body{font-size: 14px;}
    </style>
  </head>
  <body>
  <div>
  <dl class="setBox setBoxShow">
  		<dt class="setHd">
  			<span class="fr">
		  		<a js-triger="show" class="down" href="javascript:void(0)"></a>
	  		</span>
	  		<i class="ic"></i>
	  		组内群发
  		</dt>

  		<dd class="setTurn">
				<table class="main" style="width: 50%">
					<tbody class="signin">
					<tr>
						<td><div style="text-align: left;margin-left: 10px;">
								<div style="text-align: left;font-weight: bold;color:gray;">组内短信群发</div>
								</br>
								<div id="right_head"></div>
								<textarea rows="5" cols="60" data-toggle-type="area"
									placeholder="输入信息内容"></textarea>
								</br>
								</br>
								<input type="button" data-toggle-type="but" value="发送" />
							</div>
						</td>
					</tr>
					</tbody>
				</table>
				<table class="main" style="width: 50%">
					<tbody class="signin">
					<tr>
						 <td><div style="text-align: left;margin-left: 10px;">
								<div style="text-align: left;font-weight: bold;color:gray;">Totem
									APP Router掉线监控短信控制</div>
								</br> 状态：<span id="sendStatus"></span> </br>
								</br> <input type="radio" name="enableTag" id="enableTag1"
									value="true" /> <label for="enableTag1">开始发送</label> <input
									type="radio" name="enableTag" id="enableTag2" checked="checked"
									value="false" /> <label for="enableTag2">暂停发送</label> </br> </br> <input
									type="button" onclick="totemMonitorMsgSend()" value="提交" /> </br> </br>
								<div id="msgEnable" style="color: red"></div>
							</div>
						</td>
					</tr>
					</tbody>
				</table>
        </dd>
       
       <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
   </dl>
   </div>
		<script type="text/javascript">
			var text = $("textarea[data-toggle-type=area]"),send = $("input[data-toggle-type=but]");
			
			text.focus(function() {
				$("#right_head").html("");
			}).blur(function() {
			});
			
			send.click(function(){
				var msg = text.val();
				if(msg =='' || msg == null || msg == undefined){
					$("#right_head").html("");
					$("#right_head").append("<font color='red'>请输入短信内容！</font>");
					return;
				}
				
				$.ajax({
		        	type: "get",
		        	url: "/cloud/eye/msg-send?ts=" + new Date().getTime(),
		        	data:{msg:msg},
			      success: function (data) {
			      	data = JSON.parse(data);
			        	$("#right_head").html(data["msg"]);
			        },
			      error: function (XMLHttpRequest, textStatus, errorThrown) {
			        	$("#right_head").html("发送失败" + errorThrown);
			        }
				});
			});
			
			function totemMonitorMsgSend(){
				var msgEnable = $("input[name=enableTag]:checked").val();
				$("#msgEnable").html("");
				$.ajax({
		        	type: "get",
		        	url: "/cloud/eye/msg-send?ts=" + new Date().getTime(),
		        	data:{msgSend:msgEnable},
		        	async:true,
			      success: function (data) {
			    	  data = JSON.parse(data);
			    	  $("#msgEnable").html(data.msg);
			    	  $("#sendStatus").html(msgEnable)
			        },
			      error: function (XMLHttpRequest, textStatus, errorThrown) {
			        	
			        }
				});
			}
			
			$(document).ready(function() {
				$.ajax({
		        	type: "post",
		        	url: "/cloud/eye/msg-send?ts=" + new Date().getTime(),
		        	async:true,
			      success: function (data) {
			    	  data = JSON.parse(data);
			    	  var html = "false";
			    	  if(data.status){
			    	  	html = "true";
			    	  }
			    	  $("#sendStatus").html(html);
			        },
			      error: function (XMLHttpRequest, textStatus, errorThrown) {
			        	
			        }
				});
			});
		</script>
  </body>
</html>