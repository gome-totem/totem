<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="eye" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
  <head>
    <title>爬虫--网络爬虫设置页面</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/base.css"/>
  </head>
  <body>
		<dl class="setBox setBoxShow">
		    <dt class="setHd"><span class="fr"><a href="javascript:void(0)" class="down" js-triger="show"></a></span><i class="ic"></i>spider配置</dt>
		    <dd class="setTurn">
		    	<table>
		    		<tbody>
		    			<tr><th>jar路径：</th><td><input zdx-type="jarSpider" type="text" style="width:227px;" class="nText" placeholder="spider jar 路径" ></td><td zdx-error="jarSpider"></td></tr>
		    			<tr><th>log路径：</th><td><input zdx-type="logSpider" type="text" style="width:227px;" class="nText" placeholder="日志路径" ></td><td zdx-error="logSpider"></td ></tr>
		    			<tr><th>xml路径：</th><td><input zdx-type="xmlSpider" type="text" style="width:227px;" class="nText" placeholder="xml路径" ></td><td zdx-error="xmlSpider"></td></tr>
		    			<tr><th></th><td class="ok"><a class="nBtn nBtn-r" onclick="myspider.startSpider()" href="javascript:void(0)"><b>确认</b></a></td><td zdx-info="spider"></td></tr>
		    		</tbody>
		    	</table>
		    </dd>
		    
		    <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
		</dl>
		<%@ include file="../footer.jsp" %>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-data-spider.js"></script>
  </body>
</html>
