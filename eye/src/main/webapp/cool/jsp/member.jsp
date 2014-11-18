<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>

<head>
<%@ include file="/cool/jsp/common/css.jspf"%>
<base href="<%=basePath%>">
<title>Search team member</title>
<meta http-equiv="keywords" content="xiaoming zhoudong zhangzhixin zoudexiong jianglijun">
<meta http-equiv="description" content="search team member">
</head>

<body>
	<div class="container">
	
		<%@include file="/cool/jsp/common/header.jsp"%>
		
		<div class="container marketing">
			<div class="row">
				<div class="span4">
					<img class="img-circle" src="<%=basePath%>cloud/eye/pictures/xiaoming.jpg" height="140px" width="140px" style="height: 140px; width: 140px;">
					<h2>肖明</h2>
					<p>
						<span class="">TOTEM系统总架构师。</span> 国美云架构的设计和核心编写者，分布式搜索核心的开发者。追求极致与完美的人。</br>联系电话：13911813380
					</p>
					<p>
						<a class="btn" href="#">View details &raquo;</a>
					</p>
				</div>
				<div class="span4">
					<img class="img-circle" src="<%=basePath%>cloud/eye/pictures/zhangzhixin.jpg" height="140px" width="140px" style="height: 140px; width: 140px;">
					<h2>张志新</h2>
					<p>
						<span class="">TOTEM系统自然语言分析。</span> 自然语言处理NLP的核心撰写者，SOLR索引的核心开发者，负责索引，NLP的技术支持。</br>联系电话：18612624951
					</p>
					<p>
						<a class="btn" href="#">View details &raquo;</a>
					</p>
				</div>
				<div class="span4">
					<img class="img-circle" src="<%=basePath%>cloud/eye/pictures/jianglijun.jpg" height="140px" width="140px" style="height: 140px; width: 140px;">
					<h2>蒋礼俊</h2>
					<p>
						<span class="">TOTEM系统与ATG系统对接。</span> ATG搜索最牛X的人（在国美），TOTEM第三方API接口的核心撰写者。负责ATG与TOTEM云框架的对接。联系电话：18701614182
					</p>
					<p>
						<a class="btn" href="#">View details &raquo;</a>
					</p>
				</div>
				<div class="span4">
					<img class="img-circle" src="<%=basePath%>cloud/eye/pictures/zhoudong.jpg" height="140px" width="140px" style="height: 140px; width: 140px;">
					<h2>周动</h2>
					<p>
						<span class="">TOTEM分布式框架与监控。</span> TOTEM云技术分布式框架的核心撰写者，负责服务器环境，分布式架构，服务器性能优化与监控。</br>联系电话：15901164606
					</p>
					<p>
						<a class="btn" href="#">View details &raquo;</a>
					</p>
				</div>
				<div class="span4">
					<img class="img-circle" src="<%=basePath%>cloud/eye/pictures/zoudexiong.jpg" height="140px" width="140px" style="height: 140px; width: 140px;">
					<h2>邹德雄</h2>
					<p>
						<span class="">TOTEM系统-UI开发。</span> TOTEM系统网站，手机及其他用户界面的编写者，负责所有页面的商业逻辑，脚本运行，用户体验等。联系电话：15699831802
					</p>
					<p>
						<a class="btn" href="#">View details &raquo;</a>
					</p>
				</div>
			</div>
		</div>

		<%@include file="/cool/jsp/common/footer.jsp"%>
		
	</div>

	<%@ include file="/cool/jsp/common/js.jspf"%>
</body>
</html>
