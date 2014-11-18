<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>

<head>
<title>MEMBER</title>
<%@include file="/totem/jsp/common/css.jspf"%>
<meta http-equiv="keywords" content="云眼 图腾 TOTEM EYE GOME">
<meta http-equiv="description" content="云眼-图腾框架后台业务管理和配置系统">
</head>

<style type="text/css">
.span11 {
	width: 852px;
}

.thumbnail {
	max-width: 196px;
	width: 196px;
}
</style>
</head>

<body>

	<div id="wrap">

		<%@include file="/totem/jsp/common/header.jsp"%>

		<div id="container">
			<ul class="thumbnails">
				<li class="masonry">
					<div class="thumbnail">
						<img src="<%=basePath%>totem/photo/xiaoming.jpg">
						<h4>肖明</h4>
						<p>
							<span class="">TOTEM系统总架构师。</span> <br>国美云架构的设计和核心编写者，分布式搜索核心的开发者。追求极致与完美的人。</br>联系电话：13911813380
						</p>
					</div>
				</li>
				<li class="masonry">
					<div class="thumbnail">
						<img src="<%=basePath%>totem/photo/zhangzhixin.jpg">
						<h4>张志新</h4>
						<p>
							<span class="">TOTEM系统自然语言分析。</span> <br>自然语言处理NLP的核心撰写者，SOLR索引的核心开发者，负责索引，NLP的技术支持。</br>联系电话：18612624951
						</p>
					</div>
				</li>
				<li class="masonry">
					<div class="thumbnail">
						<img src="<%=basePath%>totem/photo/jianglijun.jpg">
						<h4>蒋礼俊</h4>
						<p>
							<span class="">TOTEM系统与ATG系统对接。</span> <br>ATG搜索最牛X的人（在国美），TOTEM第三方API接口的核心撰写者。负责ATG与TOTEM云框架的对接。 <br>联系电话：18701614182
						</p>
					</div>
				</li>
				<li class="masonry">
					<div class="thumbnail">
						<img src="<%=basePath%>totem/photo/zhoudong.jpg">
						<h4>周动</h4>
						<p>
							<span class="">TOTEM分布式框架与监控。</span> TOTEM云技术分布式框架的核心撰写者，负责服务器环境，分布式架构，服务器性能优化与监控。</br>联系电话：15901164606
						</p>
					</div>
				</li>
				<li class="masonry">
					<div class="thumbnail">
						<img src="<%=basePath%>totem/photo/zoudexiong.jpg" alt="">
						<h4>邹德雄</h4>
						<p>
							<span class="">TOTEM系统-UI开发。</span> TOTEM系统网站，手机及其他用户界面的编写者，负责所有页面的商业逻辑，脚本运行，用户体验等。</br>联系电话：15699831802
						</p>
					</div>
				</li>


				<li class="masonry">
					<div class="thumbnail">
						<img src="<%=basePath%>totem/photo/fangli.JPG" alt="">
						<h4>邹德雄</h4>
						<p>
							<span class="">TOTEM系统-UI开发。</span> TOTEM系统网站，手机及其他用户界面的编写者，负责所有页面的商业逻辑，脚本运行，用户体验等。</br>联系电话：15699831802
						</p>
					</div>
				</li>
				<li class="masonry">
					<div class="thumbnail">
						<img src="<%=basePath%>totem/photo/liuchaoyang.JPG" alt="">
						<h4>邹德雄</h4>
						<p>
							<span class="">TOTEM系统-UI开发。</span> TOTEM系统网站，手机及其他用户界面的编写者，负责所有页面的商业逻辑，脚本运行，用户体验等。</br>联系电话：15699831802
						</p>
					</div>
				</li>
				<li class="masonry">
					<div class="thumbnail">
						<img src="<%=basePath%>totem/photo/zhanglei.jpg" alt="">
						<h4>邹德雄</h4>
						<p>
							<span class="">TOTEM系统-UI开发。</span> TOTEM系统网站，手机及其他用户界面的编写者，负责所有页面的商业逻辑，脚本运行，用户体验等。</br>联系电话：15699831802
						</p>
					</div>
				</li>
				<li class="masonry">
					<div class="thumbnail">
						<img src="<%=basePath%>totem/photo/qiaoshuangshuang.jpg" alt="">
						<h4>邹德雄</h4>
						<p>
							<span class="">TOTEM系统-UI开发。</span> TOTEM系统网站，手机及其他用户界面的编写者，负责所有页面的商业逻辑，脚本运行，用户体验等。</br>联系电话：15699831802
						</p>
					</div>
				</li>
				<li class="masonry">
					<div class="thumbnail">
						<img src="<%=basePath%>totem/photo/pengzhang.jpg" alt="">
						<h4>邹德雄</h4>
						<p>
							<span class="">TOTEM系统-UI开发。</span> TOTEM系统网站，手机及其他用户界面的编写者，负责所有页面的商业逻辑，脚本运行，用户体验等。</br>联系电话：15699831802
						</p>
					</div>
				</li>
				<li class="masonry">
					<div class="thumbnail">
						<img src="<%=basePath%>totem/photo/liuxiangqian.jpg" alt="">
						<h4>邹德雄</h4>
						<p>
							<span class="">TOTEM系统-UI开发。</span> TOTEM系统网站，手机及其他用户界面的编写者，负责所有页面的商业逻辑，脚本运行，用户体验等。</br>联系电话：15699831802
						</p>
					</div>
				</li>
			</ul>
		</div>
	</div>

	<%@include file="/totem/jsp/common/footer.jsp"%>
	<%@include file="/totem/jsp/common/js.jspf"%>
	<script type="text/javascript" src="/totem/js/jquery.masonry.js"></script>

	<script type="text/javascript">
		$(document).ready(function() {
			$('#container').masonry({
				itemSelector : '.masonry'
			});
		});
	</script>
</body>
</html>
