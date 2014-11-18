<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../header.jsp" %>
<span id="page_tag" style="display:none;">logManage</span>
    <div class="demoMain clearfix">
		<div class="leftBar">
		    <dl class="barBox barBoxShow">
		    	  <shiro:hasAnyRoles name="super,admin,busi,pubbusi,bz_dev">
		        		<dt><i></i><s></s>搜索数据同步</dt>
		        </shiro:hasAnyRoles>
		        <shiro:hasAnyRoles name="super,admin,busi,pubbusi">
		        		<dd><a href="/dispacher/incrementIndexPage" target="iframepage" class="cur"><s></s>手动增量</a></dd>
		        </shiro:hasAnyRoles>
		 		  <shiro:hasAnyRoles name="super,admin">       
			        <dd><a href="/dispacher/fullIndexPage" target="iframepage"><s></s>全量索引</a></dd>
			        <dd><a href="/dispacher/dragonPage" target="iframepage"><s></s>dragon增量</a></dd>
		        </shiro:hasAnyRoles>
		        <shiro:hasAnyRoles name="super,admin,bz_dev">
			        <dd><a href="/dispacher/helpCenterFullIndexPage" target="iframepage"><s></s>帮助中心索引</a></dd>
			     </shiro:hasAnyRoles>
		    </dl>
		    <shiro:hasAnyRoles name="admin,super,monitor,show,dataAnalyse,busi">
			    <dl class="barBox">
			        <dt><i></i><s></s>增量监控</dt>
			        <dd><a href="/dispacher/incIndexDaliyPage" target="iframepage"><s></s>每日增量</a></dd>
			    </dl>
		    </shiro:hasAnyRoles>
		    <shiro:hasAnyRoles name="super,dataAnalyse">
			    <dl class="barBox">
			        <dt><i></i><s></s>服务监控</dt>
			        <dd><a href="/dispacher/detectPage" target="iframepage"><s></s>Tomcat监控</a></dd>  
			    </dl>
		    </shiro:hasAnyRoles>
		</div>
      
      <div class="rightMainb">
          <a href="javascript:void(0)" class="slideBtn" hidefocus="true"></a>
          <div>
              <ul class="crumbs clearfix">
                  <li><a href="/login.jsp" class="home"></a></li>
                  <li id="l1"><em class="lBg" level="two"></em><span>搜索数据同步</span></li>
                  <li id="l2"><em class="lBg" level="three"></em><span>手动增量</span></li>
              </ul>
              <shiro:hasAnyRoles name="super,admin,busi,pubbusi">
				  		<iframe id="iframepage" frameborder="0" scrolling="no" src="/dispacher/incrementIndexPage" width="100%" name="iframepage"></iframe>
				  </shiro:hasAnyRoles>
				  
				  <shiro:hasAnyRoles name="dataAnalyse,show">
				  		<iframe id="iframepage" frameborder="0" scrolling="no" src="/dispacher/incIndexDaliyPage" width="100%" name="iframepage"></iframe>
				  </shiro:hasAnyRoles>
				  
				  <shiro:hasAnyRoles name="bz_dev">
				  		<iframe id="iframepage" frameborder="0" scrolling="no" src="/dispacher/helpCenterFullIndexPage" width="100%" name="iframepage"></iframe>
				  </shiro:hasAnyRoles>
				  
              <%@ include file="../footer.jsp" %>
          </div>
        </div>
    </div>
</div>
