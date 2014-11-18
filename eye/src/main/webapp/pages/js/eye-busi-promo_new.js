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
				var $content = data.response.content;
				if($content){
					var $level = $content.level;
					switch(Number($level)){
					case 0:
						ctool.categoryInfo($content);//分类信息
						break;
					default:
						ctool.categoryOtherInfo($content);//二级分类信息
						break;
					}
				}
				$('#result').html("");
				$(".layout-panel-west  .panel-header .panel-title").html("已配置列表");
				 $("#idCate").val('');
			},
			error : function(errorThrown) {
				$("#lock_err_one_msg").html("<center>网络异常！</center>");
				pop.layerShow('lock_err_one', 80);
			}
		}); 
	},
	firesearch:function(){
		var $catId = $("#idCate").val();
		$.ajax({
			url : '/cloud/business/promotion',
			type : 'get', timeout : 800000,	cache : false,
			data : {"catId":$catId, "ps" : "pf"}, 
			dataType : 'json',
			success : function(data) {
				var $results = data.response.results, isJd = data["isJd"];
				$("#cateId").html(ctool.cateInfo($results));//分类信息
				ctool.productInfo($results, isJd);//产品信息
				$("#result .tmpTable .pageNation").after(ctool.pagination(data));//分页信息
				promotion.showConfig(1);
				promotion.setProductColor();
				$(".layout-panel-west  .panel-header .panel-title").html("已配置列表");
			},
			error : function(errorThrown) {
				$("#lock_err_one_msg").html("<center>网络异常！</center>");
				pop.layerShow('lock_err_one', 80);
			}
		});  
	},
	setProductColor:function(){
		 var stockTD=$(".stockClass");
		 $.each(stockTD, function(i, item){
			 if($(item).text()=='0'){
				 $(item).parent().parent().css('color','red');
			 };
		});
	},
	windsearch:function(){
		zOnchage();
		if(ptool.emptySearch("idCate", "搜索skuId")){
			$("#skuId_setScore_err").html("<font color='red'>请选择三级分类！</font>");
			ptool.searchSuccessed("promoSubmit","fail");//失败，没有选择分类
			return;
		}
		
		if(ptool.emptySearch("idSku", "搜索skuId")){
			$("#skuId_setScore_err").html("<font color='red'>请输入SKUID！</font>");
			ptool.searchSuccessed("promoSubmit","fail");//失败，没有输入SKUID
			return;
		}
		
		var $skuId = $("#idSku").val(), score = $("#promotionvalue").val();
		if(!zTool.isSkuId($skuId)){
			$("#skuId_setScore_err").html('skuId输入不合法！');
			return;
		}
		
		if(score){

		}else{
			$("#skuId_setScore_err").html('请输入分值！');
			return;
		}
		
		$.ajax({
			url : '/cloud/business/promotion',
			type : 'get', timeout : 800000,	cache : false,
			data : {"skuId":$skuId, "score" : score, "catId" : $("#idCate").val(), "ps" : "pc"},
			dataType : 'json',
			success : function(data) {
				if(data){
					var error = Number(data["zError"]);
					
					switch(error){
					case 0:
						dataProcess("sku_info_show", "skuTmpId", true, data);
						pop.layerShow('sku_info', 80);
						break;
					case 1:
						$("#lock_err_one_msg").html("<center>查不到该商品！</center>");
						pop.layerShow('lock_err_one', 80);
						break;
					case 2:
						$("#lock_err_one_msg").html("<center>该分值已经设置，请勿重复设置。</center>");
						pop.layerShow('lock_err_one', 80);
						break;
					case 3:
						$("#lock_err_one_msg").html("<center>输入的分值非法(检查分值是否在3.01～5.99之间)！</center>");
						pop.layerShow('lock_err_one', 80);
						break;
					case 4:
						$("#lock_err_one_msg").html("<center>输入的分值小数位不对,保留两位小数！</center>");
						pop.layerShow('lock_err_one', 80);
						break;
					case 5:
						$("#lock_err_one_msg").html("<center>输入的分值非法(检查分值是否为2，3，4，5)！</center>");
						pop.layerShow('lock_err_one', 80);
						break;
					}
				}
			},
			error : function(errorThrown) {
				$("#lock_err_one_msg").html("查询失败，请检查skuId是否正确，或者网络异常");
				pop.layerShow('lock_err_one', 80);
			}
		}); 
	},
	waterSearch : function(e){
		var $catId = $("#idCate").val(), $sort = "promo", $name = "aftersort";
		this.sortAjax($catId, $sort, $name);
	},
	pageSearch : function(page){
		var $sort = null, $name = "", $catId = $("#idCate").val(), issort = $(".pagination .fr").attr("name");
		if(!ptool.isIllegal(issort) && issort == "aftersort"){
			$sort = "promo", $name = "aftersort";
		}
		this.sortAjax($catId, $sort, $name, page);
	},
	sortAjax:function(catId, sort, name, page){
		var $num = null;
		if(page){
			$num = parseInt(page);
		}
		$.ajax({
			url : '/cloud/business/promotion',
			type : 'get', timeout : 800000,	cache : false,
			data : {"catId":catId, "sort":sort, "page":$num, "ps" : "pf"},
			dataType : 'json',
			success : function(data) {
				var $results = data.response.results, isJd = data["isJd"];
				ctool.productInfo($results, isJd);//产品信息
				$("#result .tmpTable .pageNation").after(ctool.pagination(data));//分页信息
				$(".pagination .fr").attr("name", name);//分页排序
				promotion.setProductColor();
			},
			error : function(errorThrown) {
				$("#lock_err_one_msg").html("<center>网络异常！</center>");
				pop.layerShow('lock_err_one', 80);
			}
		}); 
	},
	setPromoScore : function(){
		$("#skuId_setScore_err").html('<center><img src="/pages/img/loading.gif"/></center>');
		var score = $("#promotionvalue").val(), skuId = $("#idSku").val(), catId = $("#idCate").val();
		if(ptool.isIllegal(skuId)){
			$("#skuId_setScore_err").html('');
			$("#skuId_setScore_err").html('请输入SKUID！');
			return;
		}
		$.ajax({
			url : '/cloud/business/promotion',
			type : 'get', timeout : 800000,	cache : false,
			data : {"score":score, "skuId":skuId, "catId":catId, "ps":"ps", "ts": new Date().getTime()},
			dataType : 'json',
			success : function(data) {
				$("#skuId_setScore_err").html("<font color='red'>" + data.msg + "</font>");
			},
			error : function(errorThrown) {
				$("#lock_err_one_msg").html("<center>网络异常！</center>");
				pop.layerShow('lock_err_one', 80);
			}
		}); 
	},
	showConfig : function(page){
		var catId = $("#idCate").val();
		if(catId==null||catId.length < 1){
			$("#toolMsg").html('请选择确定的三级分类！');
			return;
		}
		$("#configed").html("<tr><th>编号</th><th>skuId</th><th>类型</th><th>操作</th></tr>");
		$.ajax({
			url : '/cloud/business/promotion',
			type : 'get', timeout : 800000,	cache : false,
			data : {"catId":catId, "ps":"getPromoScore", "page":page,"ts": new Date().getTime()},
			dataType : 'json',
			success : function(data) {
				$.each(data["oList"],function(i,item){
					var skuId = item.skuId.replace(" ","<span style='color:red;'>空格</span>");
					$("#configed").append("<tr><td width='20%'>"+((page-1)*15+i+1)+"</td>"+
							"<td class='sku' width='40%'>"+skuId+"</td>" +
									"<td class='score' width='20%'>"+zPromoScore(item.promoScore, Number(data["isJd"]))+"</td>" +
											"<td class='del' width='20%'><div style='font-size:12px'> &nbsp;&nbsp;<a href='javascript:promotion.delPromoScore(\""+skuId+"\",\""+item.catId+"\")' style='color:blue'>删除</a></div></td></tr>");
				});
				if(data["oList"].length>0){
					$("#promo").show();
					$("#configed").append("<tbody class='pagination'><tr><th colspan='4'><div class='fr'>" + pageNav.go(data["page"], data["totalPage"],'config') + "</div></th></tr></tbody>");
				}else{
					$("#promo").hide();
					$("#configed").append("<tbody ><tr><th colspan='4' align='center'><div>---暂无配置商品---</div></th></tr></tbody>");
				}
				$(".layout-panel-west  .panel-header .panel-title").html("已配置列表("+catId+")");
			},
			error : function(errorThrown) {
				$("#lock_err_one_msg").html("<center>网络异常！</center>");
				pop.layerShow('lock_err_one', 80);
			}
		}); 
	},
	showLeftTree:function(){
		$('#catContent').delegate('a .icoClick','click',function(){
			var self = $(this), $level = 0;
//			var cal = self.siblings("span").attr("class");
//			if(cal.contains("open"))
//				alert(cal);
			
			var domId = self.attr("id");
			var ids = domId.split('_');
			var catId = ids[1], $ul = self.parent().siblings("ul"), $treeTwo = self.parent().parent().siblings().find("ul#treeTwo"),
			$firstParent = self.parent().parent().parent().parent().parent(), $fDiv = self.parent().parent().parent().siblings("div"),
			$parent2li = self.parent().parent().siblings("li");
			$ul.attr("id", "treeTwo");//添加模板ID
			$treeTwo.removeAttr("id");//去除其他的模板节点，2级
			$firstParent.removeAttr("id");//去除其他的模板节点，3级
			$level = $fDiv.attr("fooler");
			zOnchage();
			if(parseInt($level) == 2){
				$("#idCate").val(catId);
				promotion.firesearch();
			}else{
				promotion.search(catId);
				$parent2li.children("ul").html("");
			}
			$("#configed").find("tbody").remove();
			//$("#configed").append('<tbody><tr><th>编号</th><th>分类Id</th><th>skuId</th><th>促销类型</th><th>操作</th></tr><tr><td align="center" colspan="4">请点击附加工具“查看设置”</td></tr></tbody>');
			self.siblings("span").removeClass("ico_close").addClass("ico_open");
			$parent2li.find("a span").removeClass("ico_open").addClass("ico_close").removeClass("selectClass");
			self.addClass("selectClass");
		});
	},
	delPromoScore : function(skuId,catId){
		//var ptr = $(_this).parent("tr"), catId = ptr.find(".cat").text(), skuId = ptr.find(".sku").text();
		$.messager.confirm('删除','您确定要删除此项配置?',function(r){
			if (r){
				$.ajax({
					url : '/cloud/business/promotion',
					type : 'get', timeout : 800000,	cache : false,
					data : {"catId":catId, "skuId" : skuId, "ps":"del", "ts": new Date().getTime()},
					dataType : 'json',
					success : function(data) {
						if(Number(data["type"]) == 0){
							promotion.showConfig(1);
						}else{
							$("#lock_err_one_msg").html("<center>删除失败，请重试！</center>");
							pop.layerShow('lock_err_one', 80);
						}
					},
					error : function(errorThrown) {
						$("#lock_err_one_msg").html("<center>网络异常！</center>");
						pop.layerShow('lock_err_one', 80);
					}
				});
			}
		});
		 
	},
	fullPromoProduct:function(){
		var catId = $("#idCate").val();
		if(catId==null||catId.length < 1){
			$("#toolMsg").html('<font color="red">请选择确定的三级分类！</font>');
			return;
		}
 	   	$("#toolMsg").html("数据同步中……");
		$.getJSON("/cloud/eye/indexSM",
			{module:"promoFull",catId:catId,t:new Date()},
			function(data){
				$("#toolMsg").html("");
				if(data.msg="success"){
					if(data.size==0){
						$("#toolMsg").html("暂时没有数据可以同步。");

					}else{
						$("#toolMsg").html("同步成功！同步商品数：<span class='hl' style='color:red'>"+ data.size +"</span>,共用时<span class='hl'>"+data.time+"秒</span>");
					}
				}
		   });
 	},
 	promoSingleProduct:function(skuId,score){	
		if(skuId==null||skuId.length < 1){
			$("#toolMsg").html("skuId不存在！");
			return;
		}
		var stockTD=$(".sku");
		 $.each(stockTD, function(i, item){
			 if($(item).text()==skuId){
				 var brother=$(item).next();
				 score=$(brother).text();
			 };
		});
		$.getJSON("/cloud/eye/indexSM",
			{module:"promoSingle",skuId:skuId,score:score,t:new Date()},
			function(data){
				if(data.msg="success"){
					$("#toolMsg").html("同步成功！商品的skuId为："+skuId+",分数为："+score);
				}else{
					$("#toolMsg").html("同步失败！");
				}
		   });
 	}
};

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
		var $result = "";
		var count = message.totalCount;
		count = parseInt(count);
		if(count == 0 && $level > 0){
			$result = "<font color='red'>该分类下无产品！</font>";
			ptool.searchSuccessed("skuIdSearch","noresult");//失败，分类无结果
		}else if(count > 0){
			$result = "分类名： " + ptool.fontcolor(ctool.fontBefor, $catName, ctool.fontAfter) + 
			" 分类ID： " + ptool.fontcolor(ctool.fontBefor, $catId, ctool.fontAfter) + 
			" 属于第 " + ptool.fontcolor(ctool.fontBefor, $level, ctool.fontAfter) + " 级分类";
			ptool.searchSuccessed("skuIdSearch","success");//成功，可以进行skuId搜索
			$("#skuId_search_err").html("");
		}else{
			$result = "<font color='red'>未找到该分类的信息！</font>";
			ptool.searchSuccessed("skuIdSearch","nocate");//失败，无该分类
		}
		return $result;
	},
	productInfo : function(message, isJd){
		dataProcess("result", "prodTempId", true, message, null, ("jd_" + isJd));
	},
	categoryInfo : function(message){
		dataProcess("catContent", "cateTempId", true, message);
	},
	categoryOtherInfo : function(message){
		dataProcess("treeTwo", "cateTempId", true, message);
	},
	pagination : function(message){
		var pageNumber = message.page, pageSize = message.pageSize, totalCount = message.response.results.totalCount;
		pageNumber = parseInt(pageNumber), pageSize = parseInt(pageSize), totalCount = parseInt(totalCount);
		var totalPage = ptool.totalPage(totalCount, pageSize);
		return "<tbody class='pagination'><tr><th colspan='4'><div class='fr'>" + pageNav.go(pageNumber, totalPage) + "</div></th></tr></tbody>";
	},
	filterData:function(){
		var $productId=$("#filterSearchBtn").val();
		var $catId = $("#idCate").val();
		if($productId=='' && $catId==''){
			$("#result").html("");
			return;
		}
		$.ajax({
				url : '/cloud/business/promotion',
				type : 'get', timeout : 800000,	cache : false,
				data : {"catId":$catId, "ps" : "pf","productId":$productId}, 
				dataType : 'json',
				success : function(data) {
					var $results = data.response.results, isJd = data["isJd"];
					ctool.productInfo($results, isJd);//产品信息
					promotion.setProductColor();
					$("#result .tmpTable .pageNation").after(ctool.pagination(data));//分页信息
				},
				error : function(errorThrown) {
					$("#lock_err_one_msg").html("<center>网络异常！</center>");
					pop.layerShow('lock_err_one', 80);
				}
		});
	}
};

var ptool = {
	fontcolor : function(prefix, v, suffix){
		return prefix + v + suffix;
 	},
 	emptySearch : function(id, str){
 		var $ID = $("#" + id );
 		if($ID.val().length == 0) return true;
 		return false;
 	},
 	removeDisable:function(id){
 		$("#" + id ).removeAttr("disabled");
 	},
 	searchSuccessed : function(id, value){
 		$("#" + id ).attr("dosearch",value);
 	},
 	showSkuIdErrorMsg : function(labelId, errorId){
 		//fail, success, noresult, nocate
 		var $value = $("#" + labelId).attr("dosearch");
 		if($value == "success") return false;
 		if($value == null || $value == undefined || $value == "fail") $("#" + errorId).html("请先搜索分类，您未选择分类！");
 		if($value == "noresult") $("#" + errorId).html("请更换分类，该分类无商品！");
 		if($value == "nocate") $("#" + errorId).html("请重新搜索分类，无该分类！");
 		return true;
 	},
 	showProErrorMsg : function(labelId, errorId){
 		//fail, success, noresult
 		var $value = $("#" + labelId).attr("dosearch");
 		if($value == "success") return false;
 		if($value == null || $value == undefined || $value == "fail") $("#" + errorId).html("请确认需要修改促销值的skuId！");
 		if($value == "noresult") $("#" + errorId).html("未找到该商品！");
 		return true;
 	},
 	totalPage : function(totalCount, pageSize){
		var totalPage = 0;
		if (totalCount % pageSize > 1) {
			totalPage = totalCount / pageSize + 1;
		} else {
			totalPage = totalCount / pageSize;
		}
		return totalPage == 0 ? 1 : Math.floor(totalPage);
 	},
	isIllegal : function(e) {
		if (e == '' || e == undefined || e == null)
			return true;
		else
			return false;
	}
};
function doPageNumSearch(p){
	promotion.pageSearch(p);
}
$("#result").delegate('.pagination .fr a[zdx=nBtn]','click',function(){
	var p = $("#pNum").val();
	p = p == null ? 1 : Number(p);
	promotion.pageSearch(p);
});
function doPageConfigSearch(p){
	promotion.showConfig(p);
}
$("#configed").delegate('.pagination .fr a[zdx=nBtn]','click',function(){
	var p = $("#zNum").val();
	p = p == null ? 1 : Number(p);
	doPageConfigSearch(p);
});
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

$("#result").delegate('.skuClass','dblclick',function(){
	var skuId = $(this).text();
	$("#idSku").val(skuId);
	zOnchage();
});

$(document).ready(function(){
	promotion.search();
	promotion.showLeftTree();
	$(".layout-panel-center .panel-tool input").remove();
	$(".layout-panel-center .panel-tool").prepend("<input id='filterSearchBtn' style='width:200px;height:18px;font-size:12px' type='text' placeholder='请输入productId过滤       ' >");
});

function readSkuImg(img, size){
	return img + "_" + size + ".jpg";
}
function zPromoScore(ev, jd){
	if(Number(jd) == 1){
		return parseFloat(ev).toFixed(2);
	}else{
		return ev;
	}
}
function zSubmit(ev){
	if(ev){
		pop.closeLayer("sku_info");
	}
	$dSubmit.show();
}
function zOnchage(){
	$dSubmit.hide();
	$("#skuId_setScore_err").html("");
}
var $dSubmit = $(".setTurn td.ok a");
