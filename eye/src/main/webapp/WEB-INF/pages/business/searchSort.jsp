<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/business.css" />
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/base.css"/>
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/pagination.css"/>
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/zTree.css"/>
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/glayers.css"/>
<style>
<!--
#configed td{
   padding: 0px;
   }
-->
</style>
<div class="rightMain">
	<a href="javascript:void(0)" class="slideBtn" hidefocus="true"></a>
	<div class="main" style="margin-top: 1px">
		<dl class="setBox setBoxShow" >
			<dt class="setHd">
				<span class="fr"><a js-triger="show" class="down up" href="javascript:void(0)"></a></span><i class="ic"></i>排序设置</dt>
				<dd class="setTurn">
				<table width="98%">
					<tbody>
						<tr>
							<td width="15%" class="leftTD" valign="top" style="padding-left: 2px;">
								<div class="easyui-panel" data-options="collapsible:true" style="border: none;">
									<div class="cate" id="catContent"></div>
								</div>
							</td>
							<td width="85%" class="rightTD" style="padding-left: 10px;padding-top: 0px;">
								<div class="easyui-panel" data-options="collapsible:true" style="border: none;">
								<dl style="border: 1px solid #E6E6E6;width: 98%;">
								<table class="f12">
									<tbody>
												<tr>
													<th>skuId</th>
													<td><input type="text" id="idSku"
														onchange="zOnchage();" placeholder="选中分类后双击skuId"
														class="nText" style="width:150px;background:#FFF" /> <input
														class="eyehide" id="idCate" /></td>
													<th>促销分值</th>
													<td><input class="nText"
														style="width:70px;background:#FFF" id="promotionvalue" />
														<a href="javascript:void(0)"
														onclick="javascript:promotion.windsearch()"
														class="nBtn easyui-linkbutton"
														data-options="iconCls:'icon-search'">&nbsp;&nbsp;查询&nbsp;&nbsp;</a>
													</td>
													<td class="ok">&nbsp;&nbsp;<a
														href="javascript:void(0)" style="display:none;"
														onclick="javascript:promotion.setPromoScore()"
														class="nBtn nBtn-r easyui-linkbutton"><b>&nbsp;&nbsp;确认&nbsp;&nbsp;</b>
													</a></td>
													<td colspan="2">&nbsp;&nbsp;<a id="sort" class="nBtn easyui-linkbutton"
														type="button" href="javascript:void(0);"
														onclick="promotion.waterSearch()">查看效果</a>
														&nbsp;&nbsp;<!-- <a id="sort" class="nBtn easyui-linkbutton"
														type="button" href="javascript:void(0);"
														onclick="promotion.showConfig()">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;查看设置&nbsp;&nbsp;&nbsp;&nbsp;</a> -->
														
														<a id="promo" class="nBtn easyui-linkbutton"
														type="button" href="javascript:void(0);" style="display: none"
														onclick="promotion.fullPromoProduct()">批量同步数据</a>
													</td>
												</tr>
												<tr>
													<th>附加工具</th>
													<td>
														<input type="text" id="singleSkuId" placeholder="输入需要同步的skuId"
														class="nText" style="width:180px;background:#FFF" />
														
													</td>
													<td colspan="5" align="left"><a href="javascript:void(0)"
														onclick="javascript:promotion.promoSingleProduct($('#singleSkuId').val(),0)"
														class="nBtn easyui-linkbutton"
														data-options="">&nbsp;&nbsp;执行同步&nbsp;&nbsp;&nbsp;&nbsp;</a>
														<span id="toolMsg" class="catinfo" style="margin-left: 20px;"></span>
														<span id="skuId_setScore_err" class="catinfo"></span>
													</td>
												</tr>
											</tbody>
								</table>
								</dl>
								
								<dl style="margin-top: 3px;border: none" >
									<dd>
										<div id="cc" class="easyui-layout" style="width:98%;height:630px;" >
											<div data-options="region:'west',title:'已配置列表'" maxWidth="340px">
												<div class="proLists easyui-panel" title=""
													style="border: none"
													data-options="collapsible:true">
													<table id="configed" class="tmpTable f12"
														style="margin-left: 5px;width: 96%;padding:0px;">
														<tbody>
															<tr>
																<td colspan='4' align='center'><div>---请选择确定的三级分类---</div>
																</td>
															</tr>
														</tbody>
													</table>
												</div>
											</div>
											<div data-options="region:'center',title:'产品列表',tools:[{iconCls:'icon-search',handler:function(){ctool.filterData();}}]" >
												<div class="proLists easyui-panel" title="" style="border: none;" data-options="collapsible:true,minWidth:'600'" id="result">
												</div>
											</div>
						 					</div>
									</dd>
								</dl>	
							</div>
							</td>
						</tr>
					</tbody>
				</table>
				</dd>
		    <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
		</dl>	
		<dl class="setBox setBoxShow">
		   <dt class="setHd">
		   <span class="fr"><a href="javascript:void(0)" class="down" js-triger="down up"></a>
		    </span><i class="ic"></i>操作手册</dt>
		    <dd class="manual">
		        <h4>操作说明</h4>
		        <p>排序步骤tips:</p>
		        <ol class="nLowLeight">
		            <li>选择配置促销因子分值的三级分类;</li>
		            <li>双击产品列表上skuId栏或者直接输入skuId, 和促销分值，进行sku查询；</li>
		            <li>展示的商品为需要设置的商品，勾中复选框，并点击确认进行设置， 否则点击X取消</li>
		            <li>若需要取消某sku的分值，点击“查看设置”，找到该skuId进行删除</li>
		            <li>点击“查看效果” 可以查看索引是否已经生效</li>
		        </ol>
		        <p>友情提示：</p>
		        <ol class="nLowLeight">
		        	 <li>批量同步数据功能用途：同步所选三级分类下的所有已配置的数据</li>
		        	 <li>提交同步功能用途：单独同步输入skuId的数据，若该skud已配置，则同步相应的分数，若没有配置，则默认的分数为0</li>
		        	 <li>产品列表筛选用途：若选中三级分类，则在该三级分类下产品中筛选，否则在所有产品中筛选</li>
		        	 <li>产品列表中红色部分代表该产品无货</li>
		            <li>非家电业务分类设置promotion的值为5 4 3 2， 家电业务分类设置promotion的值范围 3.01～5.99</li>
		            <li>非家电业务分类promotion值为5商品每个分类最多设置8个；4分40个；3分48个；2分48个</li>
		            <li>家电业务分类promotion值为5.01~5.99商品每个分类最多设置8个；4.01~4.99分40个；3.01~3.99分48个,共96个</li>
		        </ol>
		    </dd>
		    <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
		</dl>	
		
		<div class="busi-hide">
			<table id="prodTempId" class="tmpTable f12" style="margin-left: 5px;padding-top:0px;width: 98%" >
				<tbody class="pageNation">
					<tr><th align="center">产品ID</th>	<th align="center">SKU ID</th>	<th align="center">产品名称</th>	<th align="center">分数</th></tr>
					<tr jsselect="products" style="padding: 0px;">
						<td width="13%" style="padding: 0px;" align="center"><div class="prdClass" jscontent="$this.id" align="left"></div></td>
						<td width="16%" style="padding: 0px;" align="center"><div class="skuClass" jscontent="$this.skuId" align="left"></div></td>
						<td width="56%" style="padding: 0px;" align="center"><div class="namClass" jscontent="$this.name" jsvalues="title:$this.name" align="left"></div></td>
						<td width="10%" style="padding: 0px;" align="center"><div class="scoClass" jscontent="zPromoScore($this.promoScore, jd)" align="left"></div></td>
						<td width="5%" style="padding: 0px;display: none"><div class="stockClass" jscontent="$($this.skus)[0].stock"></div></td>
					</tr>
				</tbody>
			</table>
			
			<div id="cateTempId">
				<div class="fooler" jsselect="level" jsvalues="fooler:$this"></div>
				<ul id="treeCate" class="ztree" style="-moz-user-select: -moz-none;" >
					<li jsselect="childs">
						<span jsvalues="id:treeId($this,'switch')" class="button switch center_line" onclick="" treenode_switch="" ></span>
						<a jsvalues="title:$this.catName;id:treeId($this,'a')" treenode_a="" >
							<span class="button ico_close" treenode_ico="" jsvalues="id:treeId($this,'ico')"></span>
							<span class="icoClick" jscontent="$this.catName" jsvalues="id:treeId($this,'span')"></span>
						</a>
						<ul jsvalues="id:treeId($this,'ul')" class="line"></ul>
					</li>
				</ul>
			</div>
			
			<div id="skuTmpId">
				<ul class="alert_item">
					<li jsselect="product">
						<input onclick="zSubmit(1);" type="checkbox"  />
						<img jsvalues="alt:$this.name;src:readSkuImg($this.img, 100)"><span jshtml="$this.name"></span>
					</li>
				</ul>
			</div>
			
		</div>
	
		<%@ include file="../footer.jsp" %>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/json2.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/template.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/util.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/zdxUtil.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-busi-promo_new.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery.pagination-new.js"></script>
		
		<div id="sku_info" style="display: none;">
			<div class="alert-pop">
		  	<div class="alert-cont">
		    	<a onclick="pop.closeLayer('sku_info')" href="javascript:;" class="close"></a>
		      <div class="txt err cart">
		      	<s></s>
	      		<p id="sku_info_show">
	      				
	      		</p>
		      </div>
		    </div>
		  </div>
		</div>
		
		<div id="lock_err_one" style="display: none;">
			<div class="alert-pop">
		  	<div class="alert-cont">
		    	<a onclick="pop.closeLayer('lock_err_one')" href="javascript:;" class="close"></a>
		      <div class="txt err cart">
		      	<s></s>
		      		<p id="lock_err_one_msg"></p>
		      </div>
		    </div>
		  </div>
		</div>
		
	</div>
</div>
    </div>
</div>
