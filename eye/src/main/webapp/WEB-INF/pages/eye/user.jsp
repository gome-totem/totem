<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../header.jsp" %>
    <script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery-1.9.1.js"></script>
    <script type="text/javascript">
    	function pwdChange(){
    		var oldPwd = $("#oldPwd").val();
    		var newPwd = $("#newPwd").val();
    		var newPwd1 = $("#newPwd1").val();
    		
    		if(oldPwd.length<1){
    			$("#pwdChange").html("旧密码不能为空！");
    			return;
    		}else if(newPwd.length<1){
    			$("#pwdChange").html("新密码不能为空！");
    			return;
    		}else if(newPwd1.length<1){
    			$("#pwdChange").html("确认新密码不能为空！");
    			return;
    		}
    		
    		if(newPwd!=newPwd1){
    			$("#pwdChange").html("两次新密码输入不匹配！");
    			return;
    		}
    		
			$.getJSON("/cloud/eye/user",
			{module:"pwdChange",
			 uPwdNew:newPwd,
			 uPwdOld:oldPwd,
			 t:new Date()},
			function(data){
				$("#pwdChange").html("");
				if(data.msg=="success"){
					$("#pwdChange").append("修改成功！下次请用新密码登陆！");
				}else if(data.msg=="unEq"){
					$("#pwdChange").append("新旧密码不匹配！");
				}else if(data.msg=="Error"){
					$("#pwdChange").append("系统错误！");
				}
			});
    	}
    	
    </script>
		<dl class="setBox setBoxShow">
		    <dt class="setHd">
		    	<span class="fr">
		    		<a href="javascript:void(0)" class="down" js-triger="show"></a>
		    	</span>
		    	<i class="ic"></i>
		    	用户密码修改
		    </dt>
		    <dd class="setTurn" style="height: 150px;">
		    		<span style="color: red;">*</span>
		    		<label for="oldPwd">旧密码：</label>
		    		<input type="password" id="oldPwd" />
		    		</br></br>
		    		<span style="color: red;">*</span>
		    		<label for="newPwd">新密码：</label>
		    		<input type="password" id="newPwd" />
		    		</br></br>
		    		<span style="color: red;">*</span>
		    		<label for="newPwd1">确认新密码：</label>
		    		<input type="password" id="newPwd1" />
		    		</br></br>
		    		<input type="button" onclick="pwdChange()" value="提交" />
		    		</br>
					<div>
						<span id="pwdChange" style="color: red;"></span>
					</div>
					</br></br>
		    </dd>
		    <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
		</dl>
		
		<%@ include file="../footer.jsp" %>