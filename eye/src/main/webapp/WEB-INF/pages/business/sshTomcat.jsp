<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<head>
		<title>远程启动tomcat</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0" />
		<meta name="description" content="main pape of manager zookeeper" />
		<meta name="author" content="zoudexiong" />

	    <%@ include file="/pages/eye/jsp/common/commoncss.jspf" %>
	    <link href="${initParam.cdnCssServerUrl}/pages/css/business.css" rel="stylesheet"/>
	    <link href="${initParam.cdnCssServerUrl}/pages/css/square/green.css" rel="stylesheet"/>
	    <link href="${initParam.cdnCssServerUrl}/pages/css/base.css" rel="stylesheet"/>
	</head>
	<body>
	
	<div class="container" style="width: auto;">
	  	<dl class="setBox setBoxShow">
  		<dt class="setHd">
	  		<span class="fr">
		  		<a js-triger="show" class="down" href="javascript:void(0)"></a>|
		  		<a js-triger="close" class="close" href="javascript:void(0)"></a>
	  		</span>
	  		<i class="ic"></i>
	  		操作
  		</dt>
  		<dd class="setTurn">
			<table class="main">
				<tbody class="signin">
					<tr>
						<td>
							<div class="port">
								<input class="input-skuId" id="tomId" type="text" placeholder="这里输入tomcat的ip" />
							</div>
							<div class="port">
								<ul style="list-style: none outside none;margin-left:50px;">
									<li class="eye-hide">
										<input type='radio' id='port' name='port' value='0' />
										<label for='port'>0</label>
									</li>
									<li>
										<input type='radio' id='port1' name='port' value='1' />
										<label for='port1'>1</label>
									</li>
									<li>
										<input type='radio' id='port2' name='port' value='2' />
										<label for='port2'>2</label>
									</li>
									<li>
										<input type='radio' id='port3' name='port' value='3' />
										<label for='port3'>3</label>
									</li>
								</ul>
							</div>
							<div class="port">
								<input class="btn" type="button" value="重启" onclick="tom.restart()"/>
							</div>
						</td>
					</tr>
					<tr><td><div class="span5" id="right_head"></div></td></tr>
				</tbody>
			</table>
		</dd>
 		<dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
 	</dl>
	
	<%@ include file="../footer.jsp"%>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery.icheck.min.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-busi-tomcat.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-error.js"></script>
	</body>
</html>