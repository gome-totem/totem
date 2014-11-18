<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../header.jsp" %>
<link href="${initParam.cdnCssServerUrl}/pages/css/team.css" rel="stylesheet" />
<span id="page_tag" style="display:none;">dataAnalyse</span>
    <div class="demoMain clearfix">
    	  <div class="leftBar">
			    <dl class="barBox barBoxShow">
			        <dt><i></i><s></s>关键字</dt>
    	  	 	  	  <shiro:hasAnyRoles name="admin,super,busi,monitor,show,dataAnalyse">
			        		<dd><a target="iframepage" href="/dispacher/topSearchMapPage" class="cur"><s></s>搜索地图</a></dd>
			        		<dd><a target="iframepage" href="/dispacher/categorytophundredPage"><s></s>分类TOP100</a></dd>
			        		<dd><a target="iframepage" href="/dispacher/categoryKeysPage"><s></s>SEM关键词</a></dd>
			        		<dd><a target="iframepage" href="/dispacher/userSearchPage"><s></s>自定义搜索</a></dd>
		    	  	  </shiro:hasAnyRoles>
			     	  <shiro:hasAnyRoles name="super,test">
			        		<dd><a target="iframepage" href="/dispacher/NoneResultPage"><s></s>无结果请求</a></dd>
			     	  </shiro:hasAnyRoles>
			    </dl>
		    <shiro:hasAnyRoles name="super">
				<dl class="barBox barBoxShow">
					<dt><i></i><s></s>网络爬虫</dt>
					<dd><a href="/dispacher/spiderPage" target="iframepage" ><s></s>爬虫</a></dd>
				</dl>
		    </shiro:hasAnyRoles>
		  </div>
        <div class="rightMainb">
        		<a class="slideBtn" hidefocus="true" href="javascript:void(0)"></a>
            <div>
	            <ul class="crumbs clearfix">
	                <li><a href="/login.jsp" class="home"></a></li>
	                <li id="l1"><em class="lBg" level="two"></em><span>关键字</span></li>
	                <li id="l2"><em class="lBg" level="three"></em><span>搜索地图</span></li>
	            </ul>
					<iframe id="iframepage" frameborder="0" scrolling="no" src="/dispacher/topSearchMapPage" width="100%" name="iframepage" ></iframe>
               <%@ include file="../footer.jsp" %>
            </div>
        </div>
     </div>
  </body>
</html>