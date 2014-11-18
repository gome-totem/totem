<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../header.jsp" %>
<span id="page_tag" style="display:none;">team</span>
    <div class="demoMain clearfix">
    		<div class="leftBar">
		    <dl class="barBox barBoxShow">
		        <dt><i></i><s></s>技术人员</dt>
		        <dd><a href="/dispacher/teamPage" target="iframepage" class="cur"><s></s>技术人员简介</a></dd>
		    </dl>
		  </div>
		  
		   <div class="rightMain">
          <a href="javascript:void(0)" class="slideBtn" hidefocus="true"></a>
          <div>
              <ul class="crumbs clearfix">
                  <li><a href="/login.jsp" class="home"></a></li>
                  <li id="l1"><em class="lBg" level="two"></em><span>技术人员</span></li>
                  <li id="l2"><em class="lBg" level="three"></em><span>技术人员简介</span></li>
              </ul>
				  <iframe id="iframepage" frameborder="0" scrolling="no" src="/dispacher/teamPage" width="100%" name="iframepage"></iframe>
              <%@ include file="../footer.jsp" %>
          </div>
        </div>
    </div>
  </body>
</html>