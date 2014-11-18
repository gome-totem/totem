<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>

<head>
<title>TOTEM-HELP</title>
<%@include file="/totem/jsp/common/css.jspf"%>
<meta http-equiv="keywords" content="stunnel install in windows">
<meta http-equiv="description" content="stunnel install in windows">
<style type="text/css">
.main-body{text-align: center; margin: 0 auto;}
h4{color: red;}
</style>
</head>

<body>

	<div class="navbar navbar-inverse navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">
				<div class="nav-collapse collapse">
					<ul class="nav">
						<li class="active title">欢迎使用国美云架构【图腾】监控管理系统</li>
						<li class="active username">请登录</li>
						<li class="active welcome">您好！</li>
					</ul>
				</div>
			</div>
		</div>
		<div class="navbar-inner totem-header">
			<div class="container">
				<div class="nav-collapse collapse">
					<ul class="nav">
						<li class="span2"><i class="icon-camera-retro icon-large"></i>&nbsp;开发团队</li>
						<!-- 
						<li class="span2"><i class="icon-money icon-large"></i>&nbsp;活动花费</li>
						<li class="span2"><i class="icon-pencil icon-large"></i>&nbsp;搜索设置</li>
						 -->
						<li class="span2"><i class="icon-book icon-large"></i>&nbsp;读书分享</li>
						<li class="span2"><i class="icon-fighter-jet icon-large"></i>&nbsp;大数据分析</li>
						<li class="span2"><i class="icon-bar-chart icon-large"></i>&nbsp;系统监控</li>
						<li class="span2"><i class="icon-calendar icon-large"></i>&nbsp;日志管理</li>
						<li class="span4 description"><i class="icon-eye-open icon-2x"></i>&nbsp;云眼</li>
					</ul>
				</div>
			</div>
		</div>
	</div>

	<div class="begin-body-padding"></div>
	<div id="wrap">

		<div class="span12 main-body">
			<h1>在windows中使用stunnel加密访问代理</h1>
			<br>
		
			<h4>1.下载stunnel安装文件。地址：https://www.stunnel.org/downloads.html</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/01.png">
			</div><br>

			<h4>2.双击下载以后的安装文件，进行安装</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/02.jpg">
			</div><br>

			<h4>3.点击I Agree</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/03.png">
			</div><br>

			<h4>4.点击Next</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/04.png">
			</div><br>

			<h4>5.点击 Install</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/05.png">
			</div><br>

			<h4>6.根据提示输入相应的内容。分别是：国家，省，城市，公司名，部门名，正式域名</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/06.png">
			</div><br>

			<h4>7.stunnel安装完毕，点击Close</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/07.png">
			</div><br>

			<h4>8.进入stunnel的安装位置，我的电脑上是C:\Program Files (x86) ---【可在第5步中看到文件夹地址】</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/08.png">
			</div><br>

			<h4>9.进入stunenl文件夹C:\Program Files (x86)\stunnel</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/09.png">
			</div><br>

			<h4>10.修改stunnel.conf配置文件（如果无法保存，可先把此文件移出这个目录，修改完以后再放回来。）</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/10.png">
			</div><br>

			<h4>11.在配置文件中添加监听端口和向外访问的端口</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/11.png">
			</div><br>

			<h4>12.在桌面或者是快捷方式中找到stunnel按钮，双击</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/12.jpg">
			</div><br>

			<h4>13.确认stunnel开始运行</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/13.png">
			</div><br>

			<h4>14.修改firefox访问方式。在工具中找到【选项】</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/14.jpg">
			</div><br>

			<h4>15.在【网络】中点击【设置(E)】</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/15.jpg">
			</div><br>

			<h4>16.选择手动设置代理，127.0.0.1端口8088</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/16.jpg">
			</div><br>

			<h4>17.在firefox中输入www.youtube.com确认可以访问</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/17.jpg">
			</div><br>

			<h4>PS：如果修改stunnel.conf文件以前stunnel已经运行，在任务管理器中找到stunnel程序，然后结束进程。再重启stunnel</h4>
			<div>
				<img width="570px" alt="stunnel install windows" src="<%=basePath%>totem/image/18.png">
			</div><br>
			
		</div>
	</div>

	</div>
	<%@include file="/totem/jsp/common/footer.jsp"%>
	<%@include file="/totem/jsp/common/js.jspf"%>

	<script type="text/javascript">
		
	</script>

</body>

</html>
