<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/business.css" />
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/base.css"/>
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/zTree.css"/>
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/glayers.css"/>
<link type="text/css" rel="stylesheet" href="http://css.gome.com.cn//css/n/detail/gCity.min.css" />
		
<div class="rightMain">
	<a href="javascript:void(0)" class="slideBtn" hidefocus="true"></a>
	<div class="main">
		<dl class="setBox setBoxShow">
			<div class="line1"></div>
			<dt class="setHd">
			<span class="fr">
				<a href="javascript:void(0)" class="down" js-triger="show"></a>
			</span>
			<i class="ic"></i>列表页推荐</dt>
			<dd class="setTurn_recommed">
				<table>
					<tbody class="left signin left-signin">
						<tr>
							<td colspan="2"><div class="cate" id="catContent"></div></td>
						</tr>
					</tbody>
					<tbody class="right signin right-signin">
						<tr id="beforeThree">
							<td><center>请先选择三级分类，然后才可设置列表页推荐</center></td>
						</tr>
						<tr id="afterThree" style="display:none;">
							<td>
								<form>
									<table class="business_list">
                           <tbody>
                           	<tr>
                                <th class="col_1">列表页：</th>
                                <td class="col_2"><input value="" readonly="readonly" id="catTitle" ct="" class="zdx_t" >          
                                </td>
                                <td>
									    	<!-- <span style="float: left">推荐地区：</span>
									    	<div id="J-address" class="filter-item address">
									      	<div class="select-hd">
										      	<span id="address" class="regon">
										         	<a id="stockaddress" href="javascript:;">请选择</a>
										             <i></i>
										             <span class="space"></span>
										         </span>
									      		<div class="gCity clearfix"></div>
									        </div>
									      	
									      	<div class="store-selector select-bd" style="display: none;">
									        	
									        </div>
									      </div> -->
                                </td>
                            	</tr>
	                            <tr>
	                                <th class="col_1">列表页推荐：</th>
	                                <td class="col_2">
	                                    <select class="nSelect" id="js_select" name="" onchange="mainbody.selected(this)">
	                                        <option value="0">大数据</option>
	                                        <option value="1">业务推荐</option>
	                                    </select>
	                                </td>
	                                <td><span class="js_s">选择“业务推荐“，可设置推荐商品。</span></td>
	                            </tr>
	                            <tr class="js_gm" style="display: none;">
	                                <th class="col_1">推荐1：</th>
	                                <td class="col_2"><input type="text" readonly="readonly" class="tt zdx_t" pos="1" value="" onfocus="testtt(this)" onblur="testss(this)" data-check="" data-list="zdx" data-back=""></td>
	                                <td class="col_3"><span></span></td>
	                            </tr>
	                            <tr class="js_gm" style="display: none;">
	                                <th class="col_1">推荐2：</th>
	                                <td class="col_2"><input type="text" readonly="readonly" class="tt zdx_t" pos="2" value="" onfocus="testtt(this)" onblur="testss(this)" data-check="" data-list="zdx" data-back=""></td>
	                                <td class="col_3"><span></span></td>
	                            </tr>
	                            <tr class="js_gm" style="display: none;">
	                                <th class="col_1">推荐3：</th>
	                                <td class="col_2"><input type="text" readonly="readonly" class="tt zdx_t" pos="3" value="" onfocus="testtt(this)" onblur="testss(this)" data-check="" data-list="zdx" data-back=""></td>
	                                <td class="col_3"><span></span></td>
	                            </tr>
	                            <tr class="js_gm" style="display: none;">
	                                <th class="col_1">推荐4：</th>
	                                <td class="col_2"><input type="text" readonly="readonly" class="tt zdx_t" pos="4" value="" onfocus="testtt(this)" onblur="testss(this)" data-check="" data-list="zdx" data-back=""></td>
	                                <td class="col_3"><span></span></td>
	                            </tr>
	                            <tr class="js_gm" style="display: none;">
	                                <th class="col_1">推荐5：</th>
	                                <td class="col_2"><input type="text" readonly="readonly" class="tt zdx_t" pos="5" value="" onfocus="testtt(this)" onblur="testss(this)" data-check="" data-list="zdx" data-back=""></td>
	                                <td class="col_3"><span></span></td>
	                            </tr>
	                            <tr class="js_gm" style="display: none;">
	                                <th class="col_1">推荐6：</th>
	                                <td class="col_2"><input type="text" readonly="readonly" class="tt zdx_t" pos="6" value="" onfocus="testtt(this)" onblur="testss(this)" data-check="" data-list="zdx" data-back=""></td>
	                                <td class="col_3"><span></span></td>
	                            </tr>
	                            <tr class="js_gm" style="display: none;">
	                                <th class="col_1">推荐7：</th>
	                                <td class="col_2"><input type="text" readonly="readonly" class="tt zdx_t" pos="7" value="" onfocus="testtt(this)" onblur="testss(this)" data-check="" data-list="zdx" data-back=""></td>
	                                <td class="col_3"><span></span></td>
	                            </tr>
	                            <tr class="js_gm" style="display: none;">
	                                <th class="col_1">推荐8：</th>
	                                <td class="col_2"><input type="text" readonly="readonly" class="tt zdx_t" pos="8" value="" onfocus="testtt(this)" onblur="testss(this)" data-check="" data-list="zdx" data-back=""></td>
	                                <td class="col_3"><span></span></td>
	                            </tr>
	                            <tr class="js_gm" style="display: none;">
	                                <th class="col_1">推荐9：</th>
	                                <td class="col_2"><input type="text" readonly="readonly" class="tt zdx_t" pos="9" value="" onfocus="testtt(this)" onblur="testss(this)" data-check="" data-list="zdx" data-back=""></td>
	                                <td class="col_3""><span></span></td>
	                            </tr>
	                            <tr class="js_gm" style="display: none;">
	                                <th class="col_1">推荐10：</th>
	                                <td class="col_2"><input type="text" readonly="readonly" class="tt zdx_t" pos="10" value="" onfocus="testtt(this)" onblur="testss(this)" data-check="" data-list="zdx" data-back=""></td>
	                                <td class="col_3"><span></span></td>
	                            </tr>
	                            <tr id="js-save" style="display:none">
	                                <td style="text-align:center" colspan="3">
	                                    <a id="saveButt" class="nBtn" href="javascript:void(0)">保存</a>
	                                    <a id="cancelButt" class="nBtn" href="javascript:void(0)">取消</a>
	                                </td>
	                            </tr>
	                            <tr id="js-edit" style="display:none">
	                                <td style="text-align:center" colspan="3">
	                                    <a id="editButt" class="nBtn" href="javascript:void(0)">编辑商品</a>
	                                </td>
	                            </tr>
                        	</tbody>
                        	</table>
								</form>
							</td>
						</tr>
						<tr>
							<td>
								<div class="line1"></div>
							</td>
						</tr>
					</tbody>
				</table>
			</dd>
			<dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
		</dl>
       
		<dl class="setBox setBoxShow">
		    <dt class="setHd">
		    <span class="fr"><a href="javascript:void(0)" class="down" js-triger="show"></a>
		    	<%--|<a js-triger="close" class="close" href="javascript:void(0)"></a>--%>
		     </span><i class="ic"></i>操作手册</dt>
		     <dd class="manual">
		         <h4>操作说明</h4>
		         <p>功能描述：</p>
		         <ol class="nLowLeight">
		             <li>此功能用于设置列表页顶部”热销推荐“所要展示的商品。</li>
		             <li>选择”大数据“，由系统自动推荐商品。</li>
		             <li>选择”业务推荐“，业务可设置推荐的商品。</font></li>
		             <li>当“业务推荐”的商品全部无货时，自动恢复大数据推荐。</li>
		         </ol>
		         <p>列表页推荐，仅可展示三个商品。展示规则如下：</p>
		         <ol class="nLowLeight">
		             <li>根据用户选择的送至区域（省-市-县），展示高优先级且有货的商品。（推荐1优先级最高，推荐10优先级最低）</li>
		             <li>如果推荐前三的商品出现无货的情况时，优先从推荐四-十按顺序补充展示。</li>
		             <li>仅剩三个商品可展示时，仅剩一个商品有货，仍然按业务推荐展示。</li>
		             <li>仅剩三个商品可展示时，三个商品都无货，自动恢复大数据推荐。</li>
		             <li>仅剩三个商品可展示时，如有一个下架，自动恢复大数据推荐。</li>
		         </ol>
		     </dd>
		     <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
		 </dl>

		<div class="busi-hide">
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
				<ul>
					<li jsselect="product">
						<input onclick="zSubmit(1);" type="checkbox"  />
						<img jsvalues="alt:$this.name;src:readSkuImg($this.img, 100)"><span jshtml="$this.name"></span>
					</li>
				</ul>
			</div>
			
		</div>
		<%@ include file="../footer.jsp" %>
		<script src="${initParam.cdnJsServerUrl}/pages/js/jquery-1.9.1.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/json2.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/template.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/util.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/zdxUtil.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery.cookie.js"></script>
    	<script type="text/javascript" src="http://js.gome.com.cn/js/g/ui/gCity.js"></script>
		<script type="text/javascript" src="/pages/js/eye-busi-recommad.js"></script>

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
		
		<div id="imgLoading" style="display: none;">
			<img src="/pages/img/zloading.gif">
		</div>
		
	</div>
</div>
    </div>
</div>
