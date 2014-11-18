<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/base.css"/>
<div class="rightMain">
  <a href="javascript:void(0)" class="slideBtn" hidefocus="true"></a>
  <div class="main">
		<dl class="setBox setBoxShow">
			<dt class="setHd">
			<span class="fr">
            <a href="javascript:void(0)" class="down" js-triger="show"></a>
            </span>
			<i class="ic"></i>自定义搜索</dt>
          <dd class="setTurn">
           <table style="height: 300px;">
         		<tbody>
					<tr>
						<td height="40px;">搜索规则：</td><td><textarea cols="100" rows="2" id="hql"></textarea></td><td><a id="userSearchButton" class="easyui-linkbutton" href="#" style="margin-left: 20px;" data-options="size:'large'">&nbsp;&nbsp;执行搜索&nbsp;&nbsp;</a></td>
					</tr>
				</tbody>
				<tbody >
					<tr>
						<td id="content" colspan="5">
							
						</td>
					</tr>
					<tr><td><div class="line1"></div></td></tr>
				</tbody>
           </table>
         </dd>
         <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
	   </dl>
				
		<dl class="setBox setBoxShow">
         <dt class="setHd"><span class="fr"><a js-triger="show" class="down" href="javascript:void(0)"></a>|<a js-triger="close" class="close" href="javascript:void(0)"></a></span><i class="ic"></i>操作手册</dt>
            <dd class="manual">
               <h4>操作说明</h4>
               <p>功能描述:</p>
               <p class="nLowLeight">此功能需用户制定相关的搜索业务规则，查询需要的数据信息，业务规则需输入相应的HQL语句</p>
           </dd>
           <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
       </dl>
		<%@ include file="../footer.jsp" %>
		<script src="${initParam.cdnJsServerUrl}/pages/js/eye-data-JsonFormat.js" type="text/javascript"></script>
      </div>
    </div>
