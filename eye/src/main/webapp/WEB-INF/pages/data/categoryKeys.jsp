<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/base.css"/>
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/business.css" />
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/pagination.css"/>
<link rel="stylesheet" href="${initParam.cdnCssServerUrl}/pages/css/zTree.css"/>
<div class="rightMain">
  <a href="javascript:void(0)" class="slideBtn" hidefocus="true"></a>
  <div class="main">
		<dl class="setBox setBoxShow">
			<div class="line1"></div>
			<dt class="setHd">
			<span class="fr">
            <a href="javascript:void(0)" class="down" js-triger="show"></a>
            <%--|<a js-triger="close" class="close" href="javascript:void(0)"></a>--%>
            </span>
            <i class="ic"></i>排序设置</dt>
            <dd class="setTurn">
           <table>
         		<tbody class="left signin left-signin">
					<tr>
						<td colspan="2"><div class="cate" id="catContent"></div></td>
					</tr>
				</tbody>
				<tbody class="right signin right-signin-top">
					<tr>
						<td class="top">
							<dl class="setBox topBox setBoxShow">
		                  <dt class="setHd"><span class="fr"><a href="javascript:void(0)" class="down" js-triger="show"></a>
		                  <%--|<a href="javascript:void(0)" class="close" js-triger="close"></a>--%>
		                  </span><i class="ic"></i>分类下关键词</dt>
		                  <dd class="proLists" id="result" zdx="">
		                  </dd>
		               <dd class="bottomLine"><div class="line"><div class="line"></div></div></dd>
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
               <p class="nLowLeight">此功能用查看三级分类下的所有关键词</p>
               <p>操作方法:</p>
               <ol class="nLowLeight">
	               <li>选择要查看的分类的上级分类，系统将在右侧列出所选分类所有的子集分类列表</li>
	               <li>点击三级分类右侧出现该分类下所有关键词</li>
	               <li>点击提交</li>
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
				<table id="topTempId" width="100%">
					<tbody>
						<tr align="center"><th></th><th>关键词</th><th>搜索次数</th></tr>
						<tr jsselect="oList" align="center">
							<td class="top topright" width="4%"		jshtml="indexUp($index)"></td>
							<td class="top topright" width="44%"	jshtml="catData($this.gomeKey)"></td>
							<td class="top topright" width="34%"	jshtml="catData($this.timesKey)"></td>
						</tr>
					</tbody>
				</table>
			</div>

		<%@ include file="../footer.jsp" %>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/json2.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/template.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/util.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/eye-data-keycat_new.js"></script>
		<script type="text/javascript" src="${initParam.cdnJsServerUrl}/pages/js/jquery.pagination.js"></script>
      </div>
    </div>
  </div>
</div>
