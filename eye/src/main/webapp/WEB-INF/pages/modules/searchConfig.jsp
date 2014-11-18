<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../header.jsp" %>
<span id="page_tag" style="display:none;">searchConfig</span>
    <div class="demoMain clearfix">
		<div class="leftBar">
			    <dl class="barBox barBoxShow">
			        <dt><i></i><s></s>排序设置</dt>
					  <shiro:hasAnyRoles name="admin,super,busi,show,prom">
			        		<dd><a href="/dispacher/searchSortPage" target="iframepage" class="cur"><s></s>促销排序</a></dd>
			        </shiro:hasAnyRoles>
			        <shiro:hasAnyRoles name="admin,super,busi,show,recommend">
				        <dd><a href="/dispacher/gomeCategoryOrderSetPage" target="iframepage"><s></s>分类排序</a></dd>
				        <dd><a href="/dispacher/gomeFacetsOrderSetPage" target="iframepage"><s></s>筛选项排序</a></dd>
				        <dd><a href="/dispacher/recommadPage" target="iframepage"><s></s>列表页推荐</a></dd>
			        </shiro:hasAnyRoles>
			    </dl>
			<shiro:hasAnyRoles name="super,admin,busi">
		    <dl class="barBox">
		        <dt><i></i><s></s>搜索设置</dt>
		        <shiro:hasAnyRoles name="super,admin">
		        		<dd><a href="/dispacher/searchRulePage" target="iframepage"><s></s>搜索Rule</a></dd>
		        </shiro:hasAnyRoles>
		        <shiro:hasAnyRoles name="super,admin,busi">
		        		<dd><a href="/dispacher/smartBuySetPage" target="iframepage"><s></s>聪明购设置</a></dd>
		        </shiro:hasAnyRoles>
		    </dl>
			</shiro:hasAnyRoles>
		</div>
      
      <div class="rightMainb">
      	<div class="easyui-panel"style="border: none;height: 800px; " data-options="closable:true,collapsible:true">
          <a href="javascript:void(0)" class="slideBtn" hidefocus="true"></a>
          <div style="margin-left: 4px;" >
              <ul class="crumbs clearfix">
                  <li><a href="/login.jsp" class="home"></a></li>
                  <li id="l1"><em class="lBg" level="two"></em><span>排序设置</span></li>
                  <li id="l2"><em class="lBg" level="three"></em><span>促销排序</span></li>
              </ul>
				  <iframe id="iframepage" frameborder="0" scrolling="no" src="/dispacher/searchSortPage" width="100%" name="iframepage"></iframe>
					
              <%@ include file="../footer.jsp" %>
          </div>
          </div>
        </div>
    </div>
</div>
