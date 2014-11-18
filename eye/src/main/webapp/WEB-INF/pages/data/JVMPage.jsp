<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<meta http-equiv="refresh"  content= "5" >
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/base.css"/>
<div class="rightMain">
  <a href="javascript:void(0)" class="slideBtn" hidefocus="true"></a>
  <div class="main">
		<dl class="setBox setBoxShow">
			<dt class="setHd">
			<span class="fr">
            <a href="javascript:void(0)" class="down" js-triger="show"></a>
            </span>
			<i class="ic"></i>JVM內存状态</dt>
          <dd class="setTurn">
           <table style="height: 300px;">
         		<tbody>
         			<tr style="background-color: #F3F3F3">
         				<td>JVM总体状态</td>
         				<td colspan="2">Free memory: ${JVM.freeMem}</td>
         				<td colspan="2">Total memory: ${JVM.totalMem}</td>
         				<td colspan="2">Max memory: ${JVM.MaxMem}</td>
         			</tr>
         			<tr>
         				<td colspan="7"></td>
         			</tr>
         			<tr >
         				<td  width="180px"></td>
         				<td width="180px">Memory Pool</td>
         				<td width="180px">Type</td>
         				<td width="180px">Initial</td>
         				<td width="180px">Total</td>
         				<td width="180px">Maximum</td>
         				<td width="180px">Used</td>
         			<tr>
         			<c:forEach var="jvm" items="${JVM.detail}" varStatus="stats">
         				<tr>
         					<td>
         						<c:if test="${stats.index==2}">JVM各代状态</c:if>
         					</td>
         					
         					<td>
         						${jvm.name}
         					</td>
         					<td>
         						${jvm.type}
         					</td>
         					<td>
         						${jvm.init}
         					</td>
         					<td>
         						${jvm.commit}
         					</td>
         					<td>
         						${jvm.max}
         					</td>
         					<td>
         						${jvm.use}
         					</td>
         				</tr>
         			</c:forEach>
					
				</tbody>
           </table>
         </dd>
         <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
	   </dl>
		<%@ include file="../footer.jsp" %>
      </div>
    </div>
