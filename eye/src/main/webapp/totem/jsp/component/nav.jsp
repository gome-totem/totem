<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>

  <head>
    <base href="<%=basePath%>">
    <title>图腾-导航栏</title>
	 <meta http-equiv="keywords" content="云眼 图腾 TOTEM EYE GOME footer 导航栏">
	 <meta http-equiv="description" content="云眼-图腾框架后台业务管理和配置系统-导航栏">
	 <%@ include file="/totem/jsp/common/css.jspf" %>
	 <style>
	 	.container-fluid{
	 		width: 600px;
	 	}
	 	.row-fluid .offset4:first-child {
    		margin-left: 32.1%;
		}
		.row-fluid .well{
			padding: 0;
		}
		.nav-list .nav-header{
		   margin-left: -15px;
    		margin-right: -15px;
    		text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
    		height: 45px;
    	   line-height: 45px;
    	   color:gray ;
		   border-color: #DDDDDD;
		   border-style:solid;
		   border-spacing: 0;
		   border-width: 0;
		   font-size: 14px;
		}
		.row-fluid .nav-list > li > a {
    		margin-left: -15px;
    		margin-right: -15px;
    		text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
    		height: 45px;
    	   line-height: 45px;
    	   background-color: white ;
    	   color:gray ;
		   border-color: #DDDDDD;
		   border-style:solid;
		   border-spacing: 0;
		   border-width: 0;
		   border-top-width: 1px;
		   font-size: 14px;
		}
		.row-fluid .nav-list > li > a:hover,
		.row-fluid .nav-list > li > a:focus {
		   color: white;
		   text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.2);
		   background-color: #FF0033;
		   margin-left: -16px;
    	   margin-right: -16px;
		}
		.row-fluid .nav-list > .active > a,
		.row-fluid .nav-list > .active > a:hover,
		.row-fluid .nav-list > .active > a:focus {
		   color: white;
		   text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.2);
		   background-color: #FF0033;
		   margin-left: -16px;
    	   margin-right: -16px;
		}
		.container-fluid .row-fluid .span6 {
    		width: 40.936%;
		}
		.icon-sitemap, .icon-money, .icon-home , .icon-tags, .icon-wrench{
			float: right;
		}
	 </style>
  </head>
  <body>
  	
 	<div class="container-fluid">
     <div class="row-fluid">
        <div class="span6 offset4">
          <div class="well sidebar-nav">
            <ul class="nav nav-list">
              <li class="nav-header">ZOOKEEPER WATCH<i class="icon-home icon-2x"></i></li>
              <li class="active"><a href="<%=basePath %>/cool/consume.html" target="mainframe">消费明细<i class="icon-money icon-2x"></i></a></li>
              <li><a href="<%=basePath %>/totem/jsp/common/main.jsp" target="mainframe">DEVELOP<i class="icon-sitemap icon-2x"></i></a></li>
              <li><a href="<%=basePath %>/totem/jsp/common/main.jsp" target="mainframe">TAGS<i class="icon-tags icon-2x"></i></a></li>
              <li><a href="<%=basePath %>/totem/jsp/common/main.jsp" target="mainframe">RULEs<i class="icon-wrench icon-2x"></i></a></li>
              <li><a href="<%=basePath %>/totem/jsp/common/main.jsp" target="mainframe">PRELIVE<i class="icon-sitemap icon-2x"></i></a></li>
              <li><a href="<%=basePath %>/totem/jsp/common/main.jsp" target="mainframe">PRD<i class="icon-sitemap icon-2x"></i></a></li>
            </ul>
          </div>
        </div>
       </div>
   </div>
      
   <%@ include file="/totem/jsp/common/js.jspf" %>
   <script type="text/javascript">
   
   		var navigation = function() {
  				this.navs = $(".nav-list").children() ;
   			}   
   
   		navigation.prototype = {
   			clickAction : function (){
  					this.navs.each(function(){
   						$(this).click(function(){
   								var siblings = $(this).siblings() ;
   									siblings.each(function(){
   									$(this).removeClass("active") ;
   									}) ;
   								$(this).addClass("active") ;
   							}) ;
 					}) ;
   				} 
   			}
  				
   		$(document).ready(function(){
	  			var navigator = new navigation();
	  			navigator.clickAction() ;
   			});
   			
   </script>
  </body>
</html>
