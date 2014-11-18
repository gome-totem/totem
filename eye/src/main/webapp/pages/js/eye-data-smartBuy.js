//全局变量
var inputNo = 1,gCatId="",count=0;
//促销因子对象
var promotion = {
	search : function(req){
		var $catId = null;
		if(req){
			$catId = req;
		}
		$.ajax({
			url : '/cloud/business/categoryTree',
			type : 'get', timeout : 800000,	cache : false,
			data : {"catId":$catId, "ps":"fireSearch", ts:new Date().getTime()},
			dataType : 'json',
			success : function(data) {
				var $server = data.server;
				var $content = data.response.content;
				var $level = $content.level;
				switch($level){
				case 0:
					ctool.categoryInfo($content);//分类信息
					break;
				default:
					ctool.categoryOtherInfo($content);//二级分类信息
					break;
				}
				$("#suningdataList").html('');$("#jddataList").html('');$("#pageButtons").remove();
			},
			error : function(errorThrown) {
				console.log("error" + errorThrown);
			}
		}); 
	},
	showLeftTree:function(){
		$('#catContent').delegate('a .icoClick','click',function(){
			var self = $(this), $level = 0;
			var cal = self.siblings("span").attr("class");
			
			var domId = self.attr("id");
			var ids = domId.split('_');
			var catId = ids[1], $ul = self.parent().siblings("ul"), $treeTwo = self.parent().parent().siblings().find("ul#treeTwo"),
			$firstParent = self.parent().parent().parent().parent().parent(), $fDiv = self.parent().parent().parent().siblings("div"),
			$parent2li = self.parent().parent().siblings("li");
			$ul.attr("id", "treeTwo");//添加模板ID
			$treeTwo.removeAttr("id");//去除其他的模板节点，2级
			$firstParent.removeAttr("id");//去除其他的模板节点，3级
			$level = $fDiv.attr("fooler");
			$("#categoryListTitle").html(self.html()+"("+catId+")<span id='parentCategory' style='display:none;'>"+catId+"</span>");
			if(parseInt($level) == 2){
				$("#idCate").val(catId);
			}else{
				promotion.search(catId);
				$parent2li.children("ul").html("");
			}
			
			if(parseInt($level)==0){
				$("#cat1").html(self.html());
				gCatId = "";
			}else if(parseInt($level)==1){
				$("#cat2").html(" > " + self.html());
				gCatId = "";
			}else if(parseInt($level)==2){
				$("#cat3").html(" > " + self.html()+"("+catId+")");
				gCatId = catId;
				ptool.getData(1, 10);
			}

			self.siblings("span").removeClass("ico_close").addClass("ico_open");
			$parent2li.find("a span").removeClass("ico_open").addClass("ico_close").removeClass("selectClass");
			self.addClass("selectClass");
		});
	}
}

var ctool = {
	fontBefor : "<font color='blue'>",
	fontAfter : "</font>",
	serverInfo : function(message){
		return "server: " + ptool.fontcolor(ctool.fontBefor, message.server, ctool.fontAfter) + 
				" from: " + ptool.fontcolor(ctool.fontBefor, message["@from"], ctool.fontAfter) + 
				" time: " + ptool.fontcolor(ctool.fontBefor, message["@time"], ctool.fontAfter) + "ms";
	},
	cateInfo : function(message){
		var $categories = message.categories;
		var $catId = "" , $catName = "", $level = 0;
		for ( var i = 0; $categories != null && i < $categories.length; i++) {
			var $category = $categories[i];
			var $isCatone = $category.isDefault;
			if($isCatone){
				$catId = $category.id, $catName = $category.name, $level = 1;
				var $categoriesTwo = $category.childs;
				for ( var j = 0; $categoriesTwo != null && j < $categoriesTwo.length; j++) {
					var $categoryTwo = $categoriesTwo[j];
					var $isCattwo = $categoryTwo.isDefault;
					if($isCattwo){
						$catId = $categoryTwo.id, $catName = $categoryTwo.name, $level = 2;
						var $categoriesThree = $categoryTwo.childs;
						for ( var k = 0; k < $categoriesThree.length; k++) {
							var $categoryThree = $categoriesThree[k];
							var $isCatthree = $categoryThree.isDefault;
							if($isCatthree){
								$catId = $categoryThree.id, $catName = $categoryThree.name, $level = 3;
							}
						}
					}
				}
			}
		}
		return $result;
	},
	categoryInfo : function(message){
		var setTempId = document.getElementById('catContent');
		var jsTemp = jstGetTemplate('cateTempId');
		setTempId.innerHTML = '';
		setTempId.appendChild(jsTemp);
		jstProcess(new JsEvalContext(message), jsTemp);
	},
	categoryOtherInfo : function(message){
		var setTempId = document.getElementById('treeTwo');
		var jsTemp = jstGetTemplate('cateTempId');
		setTempId.innerHTML = '';
		setTempId.appendChild(jsTemp);
		jstProcess(new JsEvalContext(message), jsTemp);
	},
	pagination : function(pageMsg){
		var pageNumber = pageMsg.pageNum, pageSize = pageMsg.pageSize, totalCount = pageMsg.totalCount;
		pageNumber = parseInt(pageNumber), pageSize = parseInt(pageSize), totalCount = parseInt(totalCount);
		var totalPage = ptool.totalPage(totalCount, pageSize);
		return "<div id='pageButtons' class='pagination'><div class='fr'>" + pageNav.go(pageNumber, totalPage) + "</div></div>";
	}
}

var ptool = {
	 totalPage : function(totalCount, pageSize){
		var totalPage = 0;
		if (totalCount % pageSize > 1) {
			totalPage = totalCount / pageSize + 1;
		}else{
			totalPage = totalCount / pageSize;
		}
		return totalPage == 0 ? 1 : Math.floor(totalPage);
	 },
	fontcolor:function(prefix, v, suffix){
		return prefix + v + suffix;
 	},
 	emptySearch:function(id, str){
 		var $ID = $("#" + id );
 		if(str == $ID.val()) return true;inputNo
 		return false;
 	},
	isIllegal:function(e) {
		if (e == '' || e == undefined || e == null)
			return true;
		else
			return false;
	},
	getData:function(pageNum,pageSize){
		$("#suningdataList").html('');$("#jddataList").html('');$("#pageButtons").remove();
		var skuId = $("#gomeSkuId").val();
		skuId = skuId.trim();
		var status = $("#selStatus").val();
		var mallType = $("#mallType").val();
		if(!skuId){
			skuId = "-1"
		}
		if(!status){
			status = "-1"
		}
		var domData=null;mallName="";
		if(mallType=='JD'){
			domData=$("#jddataList");
			mallName="京东商城";
		}else if(mallType='SUNING'){
			domData=$("#suningdataList");
			mallName="苏宁易购";
		}
		domData.html("<tr><td width=\"100\">国美SKUID</td><td width=\"300\">"+mallName+"对应商品的链接</td><td width=\"80\">匹配方式</td><td width=\"150\">匹配时间</td><td width=\"100\">操作</td></tr>");
		if(gCatId.length < 1){
			alert("请选择对应的三级分类！");
			return;
		}
		$.getJSON(
				"/cloud/eye/smartBuy",
				{pageNum:pageNum,
				 pageSize:pageSize,
				 skuId:skuId,
				 status:status,
				 catId:gCatId,
				 mallType:mallType,
				 module:"findList"},
				function(data){
					if(data.msg=="success"){
						$.each(data.result,function(i,item){
							var links = "";
							var arrId =  eval(item.jdSkuId);
							for(var j=0;j<arrId.length;j++){
								
								if(mallType=='JD'){
									var tempLink="http://item.jd.com/"+arrId[j]+".html";
									links += "<span><a href='"+tempLink+"' target='_blank'>"+tempLink+"</a></span><br>";
								}else if(mallType=='SUNING'){
									var tempLink1="http://product.suning.com/"+arrId[j]+".html";
									links += "<span><a href='"+tempLink1+"' target='_blank'>"+tempLink1+"</a></span><br>";
								}
								
							}
							var action= "<a href='javascript:void(0)' onclick='ptool.showUpdateAlert(\""+ item.skuId +"\")'> 修改 </a>" +
											"<a href='javascript:void(0)' onclick='ptool.showDelAlert(\""+ item.skuId +"\")'> 撤销匹配 </a>";
							if(item.status==2||item.status==4){
								action= "<a href='javascript:void(0)' onclick='ptool.showUpdateAlert(\""+ item.skuId +"\")'> 修改 </a>";
							}
							var htmlItem = "<tr>" +
													"<td>"+item.skuId+"</td>" +
													"<td>"+links+"</td>" +
													"<td>"+item.statusDes+"</td>" +
													"<td>"+item.lastTime+"</td>" +
													"<td>" +
													action +
													"</td>" +
												"</tr>";
							domData.append(htmlItem);
						});
						$("#pageButtons").remove();
						domData.after(ctool.pagination(data.pageMsg));
						$("#pageButtons").delegate('.pagination .fr a[zdx=nBtn]','click',function(){
							var p = $("#pNum").val();
							p = p == null ? 1 : Number(p);
							ctool.pageSearch(p);
						});
					}else{
						alert("系统出错！");
					}
				}
		);
	},
	checkSku:function(self){
		var skuInput = $(self);
		var skuId = $.trim(skuInput.val());
		var mallType = $("#mallType").val();
		if(skuId.length==0){
			return;
		}
		var tagNum = skuInput.attr("tag").replace("inSkuId","");
		$.post("/cloud/eye/smartBuy",
				{skuId:skuId,
				module:"checkSku",
				mallType:mallType
				},
				function(data){
					if(data.msg=="success"){
						var clearFlag = false;
						if(data.result==1){
							if(!confirm("skuId:"+skuId+"已配置，要覆盖吗？")){
								clearFlag = true;
							}
						}else if(data.result==2){
							alert("skuId:"+skuId+"不存在，请检查输入是否正确，或已下架！");
							clearFlag = true;
						}else if(data.result==3){
							alert("未配置，校验存在性时超时！");
						}
						if(data.result==0 || data.result==1){
							$("#inName" + tagNum).val(data.name);
							$("#inBrand" + tagNum).val(data.brand);
						}
						if(clearFlag){
							skuInput.val("");
							$("#inName" + tagNum).val("");
							$("#inBrand" + tagNum).val("");
							$("#inIsGome" + tagNum).val("");
							$("#inJdLink" + tagNum).val("");
							$("#inProdId" + tagNum).val("");
						}
						
					}else{
						alert("检验失败");
					}
		},"json");
	},
	addInputItem:function(){
		if($("tr[tag='inputItem']").size()>=5){
			alert("最多可同时录入5条！");
			return;
		}
		var inputItem = "<tr tag=\"inputItem\"><td><input type=\"text\" id=\"inProdId"+ inputNo +"\" style='height:30px' onblur='ptool.checkIsGome(this)'></td>" +
				"<td><input type=\"text\" id=\"inIsGome"+ inputNo +"\" style='height:30px'></td>" +
						"<td><input type=\"text\" onblur=\"ptool.checkSku(this)\" tag=\"inSkuId"+ inputNo +"\" style='height:30px'></td>" +
								"<td><input type=\"text\" id=\"inName"+ inputNo +"\" style='height:30px'></td>" +
										"<td><input type=\"text\" id=\"inBrand"+ inputNo +"\" style='height:30px'></td>" +
												"<td><textarea id=\"inJdLink"+ inputNo +"\" cols='40' rows='1'></textarea></td>" +
														"<td><a class='easyui-linkbutton' data-options=\"iconCls:'icon-save'\" onclick=\"ptool.removeInputItem(this)\"><img src='/pages/easyui/themes/icons/cancel.png'></a></td></tr>";
		$("#inputTab").append(inputItem);
		inputNo ++;
	},
	removeInputItem:function($this){
		$($this).parent().parent().remove();
	},
	signIn:function(){
		var skuIdInput = $("input[tag^='inSkuId']");
		var skuIds = "",jdLinks = "",productIds="",nameDatas="",brandDatas="",isGomes="";
		$.each(skuIdInput,function(i,inputItem){
			var skuInput = $(inputItem);
			var tagNum = skuInput.attr("tag").replace("inSkuId","");
			var skuId = $.trim(skuInput.val());
			var jdLink = $.trim($("#inJdLink" + tagNum).val());
			var productId = $.trim($("#inProdId" + tagNum).val());
			var nameData = $.trim($("#inName" + tagNum).val());
			var brandData = $.trim($("#inBrand" + tagNum).val());
			var isGome = $.trim($("#inIsGome" + tagNum).val());
			if(skuId.length>0&&jdLink.length>0){
				skuIds.length<1?skuIds+=skuId:skuIds+="|"+skuId;
				jdLinks.length<1?jdLinks+=jdLink:jdLinks+="|"+jdLink;
				productIds.length<1?productIds+=productId:productIds+="|"+productId;
				nameDatas.length<1?nameDatas+=nameData:nameDatas+="|"+nameData;
				brandDatas.length<1?brandDatas+=brandData:brandDatas+="|"+brandData;
				isGomes.length<1?isGomes+=isGome:isGomes+="|"+isGome;
			}
		});
		if(skuIds.length<1||jdLinks.length<1){
			alert("请检查国美sku和链接是否为空！");
			return;
		}
		if(nameDatas.length<1||brandDatas.length<1){
			alert("请检查商品名称或品牌是否为空！");
			return;
		}
		if(gCatId.length<1){
			alert("请先选择要操作的分类！");
			return;
		}
		var mallType = $("#mallType").val();
		$.post("/cloud/eye/smartBuy",
				{jdLinks:jdLinks,
			 	 skuIds:skuIds,
			 	 catId:gCatId,
			 	 productIds:productIds,
			 	 nameDatas:nameDatas,
			 	 brandDatas:brandDatas,
			 	 isGomes:isGomes,
			 	 module:"add",
			 	 mallType:mallType},
				function(data){
					if(data.msg=="success"){
					  alert("新建成功");
					}else{
					  alert(data.msg);
					}
				},"json");
	},
	clearSign:function(){
		var skuIdInput = $("input[tag^='inSkuId']");
		$.each(skuIdInput,function(i,inputItem){
			var skuInput = $(inputItem);
			var tagNum = skuInput.attr("tag").replace("inSkuId","");
			skuInput.val("");
			$("#inJdLink" + tagNum).val("");
			$("#inProdId" + tagNum).val("");
			$("#inName" + tagNum).val("");
			$("#inBrand" + tagNum).val("");
			$("#inIsGome" + tagNum).val("");
		});
	},
	closeAlert:function(winId){
		$("#"+winId).hide();
	},
	delMapping:function(){
		var skuId = $("#delSkuId").html();
		if(skuId.length==0){
			return;
		}
		var mallType = $("#mallType").val();
		$.post("/cloud/eye/smartBuy",
				{skuId:skuId,
			 	 module:"del",
			 	mallType:mallType},
				function(data){
					if(data.msg=="success"){
					  alert("撤销成功！");
					  ptool.closeAlert('delMapping');
					  ptool.getData(1,10);
					}else{
					  alert(data.msg);
					  ptool.closeAlert('delMapping');
					}
				},"json");
	},
	updateMapping:function(){
		var skuId = $("#updateSkuId").html();
		if(skuId.length==0){
			return;
		}
		var jdLinks = $("#updateJDLinks").val();
		if(jdLinks.length<1){
			alert("请输入链接！");
			return;
		}
		var mallType = $("#mallType").val();
		var mode = $("input[name='mode']:checked").val();
		$.post("/cloud/eye/smartBuy",
				{skuId:skuId,
				 jdLinks:jdLinks,
				 mode:mode,
			 	 module:"updateByParam",
			 	 mallType:mallType},
				function(data){
					if(data.msg=="success"){
					  alert("修改成功！");
					  ptool.closeAlert('updateMapping');
					  ptool.getData(1,10);
					}else{
					  alert(data.msg);
					  ptool.closeAlert('updateMapping');
					}
				},"json");
	},
	showDelAlert:function(skuId){
		var targetWin = $("#delMapping");
		targetWin.css("position","fixed").css("left","40%").css("top","180px");
		$("#delSkuIdInfo").html("(skuId:" + skuId+")");
		$("#delSkuId").html(skuId);
		targetWin.show();
	},
	showUpdateAlert:function(skuId){
		var targetWin = $("#updateMapping");
		$("#modeNone").attr("checked",true);
		targetWin.css("position","fixed").css("left","40%").css("top","180px");
		$("#updateSkuIdInfo").html(skuId);
		$("#updateSkuId").html(skuId);
		targetWin.show();
	},
	checkIsGome:function($this){
		if($this==null || $($this).val()==""){
			$($this).parent().next().children().val("");
			return;
		}
		if($($this).val().indexOf('A')==0){
			
			$($this).parent().next().children().val("第三方联营");
		}else{
			$($this).parent().next().children().val("国美自营");
		}
	}
}

function doPageNumSearch(p){
	ptool.getData(p,10);
}

function treeId($this, dom){
	return "treeCate_"+ $this.catId +"_" + dom;
}
function fooler($this){
	return parseInt($this) + 1;
}
function icoClass($this){
	if($this ==3)
		return "ico_docu";
}
$(function(){
	promotion.search();
	promotion.showLeftTree();
	$("#jd").click(function(){
		$(this).css("border-bottom-color","#3D3D3D").css("opacity",1);
		$("#suning").css("border-bottom-color","##777\9").css("opacity",0.5);
		$("#mallType").val("JD");
		$("span[name='mall']").html("京东商城");
		if(count!=0){
			ptool.getData();
		}
		count++;
	});
	$("#suning").click(function(){
		$(this).css("border-bottom-color","#3D3D3D").css("opacity",1);
		$("#jd").css("border-bottom-color","#777\9").css("opacity",0.5);
		$("#mallType").val("SUNING");
		$("span[name='mall']").html("苏宁易购");
		ptool.getData();
	});
	$("#checkData").click(function(){
		ptool.getData();
	});
	$("#jd").click();
});

