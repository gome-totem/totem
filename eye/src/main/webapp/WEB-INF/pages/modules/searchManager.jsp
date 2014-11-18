<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../header.jsp" %>
<span id="page_tag" style="display:none;">searchManager</span>
    <div class="demoMain clearfix">
		<div class="leftBar">
		    <shiro:hasAnyRoles name="super">
		    	<dl class="barBox barBoxShow">
	        		<dt><i></i><s></s>DIC相关</dt>
	        		<dd><a href="/dispacher/polyphoneFixPage" target="iframepage" class="cur"><s></s>多音字修正</a></dd>
		    	</dl>
				<dl class="barBox barBoxShow">
				    <dt><i></i><s></s>短信</dt>
				    <dd><a href="/dispacher/msgPage" target="iframepage"><s></s>组内群发</a></dd>
				    <dd><a href="/cloud/business/edmServlet" target="iframepage"><s></s>EDM投票</a></dd>
				    <dd><a href="/dispacher/edmPlat" target="iframepage"><s></s>EDM配置</a></dd>
				</dl>
				<dl class="barBox barBoxShow">
				    <dt><i></i><s></s>服务器</dt>
				    <dd><a href="/dispacher/jvm" target="iframepage"><s></s>JVM内存状态</a></dd>
			        <dd><a href="/dispacher/dictConfigPage" target="iframepage"><s></s>数据字典</a></dd>
			       <dd><a href="/dispacher/sshTomcatPage" target="iframepage"><s></s>Tomcat远程</a></dd>
				    <dd><a href="/dispacher/RunLogKeywordsCountPage" target="iframepage"><s></s>日志错误计数</a></dd>
				    <dd><a href="http://qb.ds.gome.com.cn:8810" target="_blank"><s></s>QB发版</a></dd>
				</dl>
				<dl class="barBox barBoxShow">
				    <dt><i></i><s></s>索引相关</dt>
				    <dd><a href="/dispacher/esAscPage" target="iframepage"><s></s>ES/URL操作</a></dd>
				    <dd><a href="/dispacher/promoProductDataPage" target="iframepage"><s></s>促销数据同步</a></dd>
				    <dd><a href="/dispacher/cityCodeDataPage" target="iframepage"><s></s>地区数据同步</a></dd>
				    <dd><a href="/dispacher/appsIndexDocCountPage" target="iframepage"><s></s>索引DOC数</a></dd>
				    <dd><a href="/dispacher/beansTalkMonitorPage" target="iframepage"><s></s>BeansTalk监控</a></dd>
		
				</dl>
			</shiro:hasAnyRoles>
		</div>
      
      <div class="rightMain">
          <a href="javascript:void(0)" class="slideBtn" hidefocus="true"></a>
          <div>
              <ul class="crumbs clearfix">
                  <li><a href="/login.jsp" class="home"></a></li>
                  <li id="l1"><em class="lBg" level="two"></em><span>DIC相关</span></li>
                  <li id="l2"><em class="lBg" level="three"></em><span>多因字修正</span></li>
              </ul>
				  
				  <iframe id="iframepage" frameborder="0" scrolling="no" src="/dispacher/polyphoneFixPage" width="100%" name="iframepage"></iframe>
				  
              <%@ include file="../footer.jsp" %>
          </div>
        </div>
    </div>
</div>
