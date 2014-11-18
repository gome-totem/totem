<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>图腾-MAIN</title>
	 <meta http-equiv="keywords" content="云眼 图腾 TOTEM EYE GOME footer main">
	 <meta http-equiv="description" content="云眼-图腾框架后台业务管理和配置系统-main">
	 <link href="/totem/css/style.css" rel="stylesheet" />
	 <%@ include file="/totem/jsp/common/css.jspf" %>
	<style type="text/css">
	.span11{
		width: 852px;
	}
	h1, h2, h3, h4, h5, h6, p {
    	color: black;
	}
	.thumbnail{
		max-width: 196px;
		width: 196px;
	}
	</style>
  </head>
  
  <body>
	<div id="container">
		 <ul class="thumbnails">
    		<li class="masonry">
				<div class="thumbnail">
					<img src="cool/img/33333.jpg" alt="">
					<h4>cool man</h4>
					<p>HE IS FUCKING AWESOME</p>
				</div>
   		 </li>
   		 <li class="masonry">
    			<div class="thumbnail">
					<img src="cool/img/11111.jpg" alt="">
					<h4>pretty girl</h4>
					<p>I like this lovely girl</p>
				</div>
   		 </li>
   		 <li class="masonry">
    			<div class="thumbnail">
					<img src="cool/img/44444.jpg" alt="">
					<h4>universal</h4>
					<p>Beautiful Universal</p>
				</div>
   		 </li>
   			    		<li class="masonry">
				<div class="thumbnail">
					<img src="cool/img/33333.jpg" alt="">
					<h4>cool man</h4>
					<p>HE IS FUCKING AWESOME</p>
				</div>
   		 </li>
   		 <li class="masonry">
    			<div class="thumbnail">
					<img src="cool/img/11111.jpg" alt="">
					<h4>pretty girl</h4>
					<p>I like this lovely girl</p>
				</div>
   		 </li>
   		 <li class="masonry">
    			<div class="thumbnail">
					<img src="cool/img/10173.jpg" alt="">
					<h4>universal</h4>
					<p>Beautiful Universal</p>
				</div>
   		 </li>
   		     		<li class="masonry">
				<div class="thumbnail">
					<img src="cool/img/33333.jpg" alt="">
					<h4>cool man</h4>
					<p>HE IS FUCKING AWESOME</p>
				</div>
   		 </li>
   		 <li class="masonry">
    			<div class="thumbnail">
					<img src="cool/img/11111.jpg" alt="">
					<h4>pretty girl</h4>
					<p>I like this lovely girl</p>
				</div>
   		 </li>
   		 <li class="masonry">
    			<div class="thumbnail">
					<img src="cool/img/10173.jpg" alt="">
					<h4>universal</h4>
					<p>Beautiful Universal</p>
				</div>
   		 </li>
   		     		<li class="masonry">
				<div class="thumbnail">
					<img src="cool/img/33333.jpg" alt="">
					<h4>cool man</h4>
					<p>HE IS FUCKING AWESOME</p>
				</div>
   		 </li>
   		 <li class="masonry">
    			<div class="thumbnail">
					<img src="cool/img/11111.jpg" alt="">
					<h4>pretty girl</h4>
					<p>I like this lovely girl</p>
				</div>
   		 </li>
   		 <li class="masonry">
    			<div class="thumbnail">
					<img src="cool/img/10173.jpg" alt="">
					<h4>universal</h4>
					<p>Beautiful Universal</p>
				</div>
   		 </li>
   		     		<li class="masonry">
				<div class="thumbnail">
					<img src="cool/img/33333.jpg" alt="">
					<h4>cool man</h4>
					<p>HE IS FUCKING AWESOME</p>
				</div>
   		 </li>
   		 <li class="masonry">
    			<div class="thumbnail">
					<img src="cool/img/11111.jpg" alt="">
					<h4>pretty girl</h4>
					<p>I like this lovely girl</p>
				</div>
   		 </li>
   		 <li class="masonry">
    			<div class="thumbnail">
					<img src="cool/img/10173.jpg" alt="">
					<h4>universal</h4>
					<p>Beautiful Universal</p>
				</div>
   		 </li>
   		     		<li class="masonry">
				<div class="thumbnail">
					<img src="cool/img/33333.jpg" alt="">
					<h4>cool man</h4>
					<p>HE IS FUCKING AWESOME</p>
				</div>
   		 </li>
   		 <li class="masonry">
    			<div class="thumbnail">
					<img src="cool/img/11111.jpg" alt="">
					<h4>pretty girl</h4>
					<p>I like this lovely girl</p>
				</div>
   		 </li>
   		 <li class="masonry">
    			<div class="thumbnail">
					<img src="cool/img/44444.jpg" alt="">
					<h4>universal</h4>
					<p>Beautiful Universal</p>
				</div>
   		 </li>
   		     		<li class="masonry">
				<div class="thumbnail">
					<img src="cool/img/33333.jpg" alt="">
					<h4>cool man</h4>
					<p>HE IS FUCKING AWESOME</p>
				</div>
   		 </li>
   		 <li class="masonry">
    			<div class="thumbnail">
					<img src="cool/img/11111.jpg" alt="">
					<h4>pretty girl</h4>
					<p>I like this lovely girl</p>
				</div>
   		 </li>
   		 <li class="masonry">
    			<div class="thumbnail">
					<img src="cool/img/10173.jpg" alt="">
					<h4>universal</h4>
					<p>Beautiful Universal</p>
				</div>
   		 </li>
   		 </li>
	    </ul>
	</div>
	
   <%@ include file="/totem/jsp/common/js.jspf" %>
   <script type="text/javascript" src="/totem/js/jquery.masonry.js"></script>
   <script type="text/javascript">
   		
	$(document).ready(function(){
		$('#container').masonry({
		  itemSelector: '.masonry'
		});
 	});

   		
   </script>
  </body>
</html>
