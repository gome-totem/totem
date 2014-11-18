<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <title>每日增量</title>
    <script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery-1.9.1.js"></script>
	 <%@ include file="/pages/eye/jsp/common/commoncss.jspf" %>
	 <link href="${initParam.cdnCssServerUrl}/pages/css/base.css" rel="stylesheet"/>
    <link href="${initParam.cdnCssServerUrl}/pages/css/business.css" rel="stylesheet">
   </head>
<body>
   <dl class="setBox setBoxShow">
  		<dt class="setHd">
	  		<span class="fr">
		  		<a js-triger="show" class="down" href="javascript:void(0)"></a>
		  		<%--|<a js-triger="close" class="close" href="javascript:void(0)"></a>--%>
	  		</span>
	  		<i class="ic"></i>
	  		操作
  		</dt>
  		<dd class="setTurn">
				<table class="main">
					<tbody class="signin">
						<tr><td>
							<div>查看今天增量日志： <input type="text" id="proId" placeholder="输入产品Id">
							<input type="button" onclick="incIndex.fireSearch()" class="btn btn-ext" value="查询" /></div>
						</td></tr>
						<tr>
							<td>
								<div id="find"></div>
							</td>
						</tr>
					</tbody>
				</table>
        </dd>
       <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
   </dl>			
				
	<dl class="setBox setBoxShow">
  		<dt class="setHd">
	  		<span class="fr">
		  		<a js-triger="show" class="down" href="javascript:void(0)"></a>
		  		<%--|<a js-triger="close" class="close" href="javascript:void(0)"></a>--%>
	  		</span>
	  		<i class="ic"></i>
	  		增量日志
  		</dt>
  		<dd class="setTurn">
	  		<table>		
				<tbody class="signin">
					<tr>
						<td class="inLeft"><em id="datF"></em><span id="dayData1"></span></td>
					</tr>
					<tr>
						<td class="inLeft"><em id="datS"></em><span id="dayData2"><span style="margin-left:10px">扫描日志中<img src="/pages/img/loading.gif" ></span></span></td>
					</tr>
					<tr>
						<td class="inLeft"><em id="datT"></em><span id="dayData3"></span></td>
					</tr>
				</tbody>
			</table>
  			
			<div class="main">
				<canvas id="barChartData" height="182px" ></canvas>
			</div>
			<div class="modal hide fade" id="day1">
			    <div class="modal-header">
				    <a class="close" data-dismiss="modal">×</a>
				    <h3>增量日志(<em id="date1"></em>)</h3>
				</div>
				<div class="modal-body">
				    <p id="dayJson1">今日增量日志</p>
				</div>
				<div class="modal-footer">
				    <a href="#" class="btn" data-dismiss="modal">关闭</a>
			    </div>
		    </div>
			<div class="modal hide fade" id="day2">
			    <div class="modal-header">
				    <a class="close" data-dismiss="modal">×</a>
				    <h3>增量日志(<em id="date2"></em>)</h3>
				</div>
				<div class="modal-body">
				    <p id="dayJson2">昨日增量日志</p>
				</div>
				<div class="modal-footer">
				    <a href="#" class="btn" data-dismiss="modal">关闭</a>
			    </div>
		    </div>
			<div class="modal hide fade" id="day3">
			    <div class="modal-header">
				    <a class="close" data-dismiss="modal">×</a>
				    <h3>增量日志(<em id="date3"></em>)</h3>
				</div>
				<div class="modal-body">
				    <p id="dayJson3">前日增量日志</p>
				</div>
				<div class="modal-footer">
				    <a href="#" class="btn" data-dismiss="modal">关闭</a>
			    </div>
		    </div>
			</div>
	   </dd>
      <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
  </dl>		
	<!-- <script type="text/javascript" src="/pages/eye/js/bootstrap-modal.js"></script> -->
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/bootstrap-transition.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-busi-daliyIndex.js"></script>
	<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-error.js"></script>
	<%@ include file="../footer.jsp"%>
</body>
</html>