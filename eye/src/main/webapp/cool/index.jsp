<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>

<head>
<title>COOL</title>
<%@ include file="/cool/jsp/common/css.jspf"%>
<meta http-equiv="keywords" content="index page">
<meta http-equiv="description" content="index page description">
</head>

<body>

	<div class="container">

		<%@include file="/cool/jsp/common/header.jsp"%>

		<div class="jumbotron">
			<h1>Gome Search</h1>
			<p class="lead">Our core product is TOTEM framework which in a flexible and powerful platform. Any module or service can plug into this platform. Search module is only one of the service in
				totem. Welcome to join our system.</p>
			<a class="btn btn-large" href="#">sign system</a>
		</div>

		<hr>

		<div class="row-fluid">

			<div class="span4">
				<h2>Zookeeper</h2>
				<p>Zookeeper cloud is the congress of the totem system. Kinds of information were stored in Zookeeper cloud. Client set watchers to zookeeper, so client can get the lastest informaions of the
					totem system. Stable of zookeeper cloud is very important.</p>
				<p>
					<a class="btn" href="#">View details &raquo;</a>
				</p>
			</div>
			<div class="span4">
				<h2>Router</h2>
				<p>Take the charge of dispatch or synchronize the information of message. Appserver should register into router, mean while appserver should watcher the routers action. In Router cloud, we
					selcet the leader of router for the purpose the manger cloud.</p>
				<p>
					<a class="btn" href="justified-nav_router.html">View details &raquo;</a>
				</p>
			</div>
			<div class="span4">
				<h2>Appserver</h2>
				<p>Appserver in the container of services or modules. In this section people could view the configurations of appservers. Aslo this section show the details of the apps information. Be aware:
					bussiness functions, modules and services should plug into appserver.</p>
				<p>
					<a class="btn" href="#">View details &raquo;</a>
				</p>
			</div>

		</div>

		<%@include file="/cool/jsp/common/footer.jsp"%>

	</div>

	<%@ include file="/cool/jsp/common/js.jspf"%>

</body>

</html>
