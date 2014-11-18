<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../header.jsp" %>
<span id="page_tag" style="display:none;">monitor</span>
	<div class="demoMain clearfix">
		<div class="leftBar">
			 <shiro:hasAnyRoles name="admin,super,monitor,show,dataAnalyse,dev">
			    <dl class="barBox barBoxShow">
			        <dt><i></i><s></s>监控</dt>
			        <dd><a href="/dispacher/monitor" target="iframepage" class="cur"><s></s>云眼</a></dd>
			    </dl>
		    </shiro:hasAnyRoles>
		    <shiro:hasAnyRoles name="super">
			    <dl class="barBox">
			        <dt><i></i><s></s>监控配置</dt>
			        <dd><a href="/dispacher/configPage" target="iframepage"><s></s>Dict配置</a></dd>
			    </dl>
		    </shiro:hasAnyRoles>
		</div>
		
      <div class="rightMainb">
          <a href="javascript:void(0)" class="slideBtn" hidefocus="true"></a>
          <div>
              <ul class="crumbs clearfix">
                  <li><a href="/login.jsp" class="home"></a></li>
                  <li id="l1"><em class="lBg" level="two"></em><span>监控</span></li>
                  <li id="l2"><em class="lBg" level="three"></em><span>云眼</span></li>
              </ul>
              		
				  <iframe id="iframepage" height="1000px" style="overflow-y:hidden;" frameborder="0" scrolling="yes" src="/dispacher/monitor" width="100%" name="iframepage"></iframe>

              <%@ include file="../footer.jsp" %>
          </div>
      </div>
    </div>
</div>
