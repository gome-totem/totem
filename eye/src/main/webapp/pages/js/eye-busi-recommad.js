var recommad = function(){}, ctool = function(){};
recommad.prototype = {
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
				var $level = $content.level;
				switch($level){
				case 0:
					ct.categoryInfo($content);//分类信息
					break;
				default:
					ct.categoryOtherInfo($content);//二级分类信息
					break;
				}
			},
			error : function(errorThrown) {
				console.log("error:" + errorThrown);
			}
		});
	},
	showLeftTree:function(){
		var _this = this;
		$('#catContent').delegate('a .icoClick','click',function(){
			var self = $(this), $level = 0;
//					var cal = self.siblings("span").attr("class");
//					if(cal.contains("open"))
//						alert(cal);
			
			var domId = self.attr("id");
			var ids = domId.split('_');
			var catId = ids[1], $ul = self.parent().siblings("ul"), $treeTwo = self.parent().parent().siblings().find("ul#treeTwo"),
			$firstParent = self.parent().parent().parent().parent().parent(), $fDiv = self.parent().parent().parent().siblings("div"),
			$parent2li = self.parent().parent().siblings("li");
			$ul.attr("id", "treeTwo");//添加模板ID
			$treeTwo.removeAttr("id");//去除其他的模板节点，2级
			$firstParent.removeAttr("id");//去除其他的模板节点，3级
			$level = $fDiv.attr("fooler");
			if(parseInt($level) == 2){
				_this.fireShow(catId, ids[3]);
			}else{
				_this.fireHide();
				_this.search(catId);
				$parent2li.children("ul").html("");
			}
			self.siblings("span").removeClass("ico_close").addClass("ico_open");
			$parent2li.find("a span").removeClass("ico_open").addClass("ico_close").removeClass("selectClass");
			self.addClass("selectClass");
		});
	},
	fireShow : function(catId, catName){
		catTitle.val(catName);
		catTitle.attr("ct", catId);
		
		$(".business_list input[data-list=zdx]").each(function(e){
			var _this = $(this), nameSpan = _this.parent("td").parent("tr").find("td.col_3 span");
			
			_this.val("");
			_this.attr("data-check","");
			nameSpan.html("");
		});
		
		mainbody.init();//点击三级分类页初始化右侧数据
		
		beforeThree.hide();
		afterThree.show();
		atgRegionPlugn();
	},
	fireHide : function(){
		beforeThree.show();
		afterThree.hide();
	},
};

ctool.prototype = {
	categoryInfo : function(message){
		dataProcess("catContent", "cateTempId", true, message);
	},
	categoryOtherInfo : function(message){
		dataProcess("treeTwo", "cateTempId", true, message);
	}
};
function treeId($this, dom){
	return "treeCate_"+ $this.catId +"_" + dom + "_" + $this.catName;
}

var beforeThree = $("#beforeThree"), afterThree = $("#afterThree"), catTitle = $("#catTitle"), jsedit = $("#js-edit"),
	jssave = $("#js-save"), jsselect = $("#js_select");
var mainBody = function(){
	this.editButt = $("#editButt");
	this.saveButt = $("#saveButt");
	this.cancelButt = $("#cancelButt");
	this.tt = $(".business_list input[data-list=zdx]");
	this.checkerr = $(".business_list input[data-check=error]");		//设置时有问题的input
	this.checkopera = $(".business_list input[data-check=success]");	//设置后成功的input
}
mainBody.prototype = {
	errorNum : function(){
		return $(".business_list input[data-check=error]").length;
	},
	operaNum : function(){
		return $(".business_list input[data-check=success]").length;
	},
	isRepeat : function(ev, pos){
		var tmpId = "";
		$(".business_list input[data-check=success]").each(function(e){
			var skuId = $(this).val(), skuId = skuId.trim();
			if((e + 1) != pos && ev == skuId){
				tmpId = skuId;
			}
		});
		
		if(tmpId.length > 0){
			return true;
		}
		return false;
	},
	clearData : function(){
		$(".business_list input[data-list=zdx]").each(function(e){
			var _this = $(this), nameSpan = _this.parent("td").parent("tr").find("td.col_3 span");
			
			_this.val("");
			_this.attr("data-check","");
			_this.attr("data-back","");
			nameSpan.html("");
		});
	},
	cancelData : function(){
		$(".business_list input[data-list=zdx]").each(function(e){
			var _this = $(this), nameSpan = _this.parent("td").parent("tr").find("td.col_3 span");
			
			_this.val("");
			_this.attr("data-check","");
			_this.attr("data-back","");
			nameSpan.html("");
			zTool.zReadOnly(_this, true);
			_this.attr("class","tt zdx_t");
			$("#saveButt"+ (e + 1)).hide();
		});
	}
}

var mainbody = new mainBody();
//点击编辑
mainbody.editButt.click(function(){
	mainbody.tt.each(function(e){
		var _this = $(this);
		zTool.zReadOnly(_this, false);
		_this.attr("class","tt zdx_s");
		$("#saveButt"+ (e + 1)).show();
	});
	
	//显示和隐藏按钮
	jssave.show();
	jsedit.hide();
});
//点击保存
mainbody.saveButt.click(function(){
	if(mainbody.errorNum() > 0){	//保存时判断是否有错误的项
		errorShow(null, "设置项有问题");
		return;
	}
	if(mainbody.operaNum() < 3){	//保存时判断是否至少配置3个
		errorShow(null, "设置项至少需要3个");
		return;
	}
	var datas = [];
	$(".business_list input[data-check=success]").each(function(e){
		var _this = $(this), skuId = _this.val(), skuId = skuId.trim(), pos = _this.attr("pos"), name = _this.parent("td").parent("tr").find("td.col_3").attr("title"),
		data = {skuId : skuId, name : name, pos : pos};
		datas.push(data);
	});
	pop.layerShow('imgLoading', 100);
	$.ajax({
		url : aUrl,
		type : 'post', timeout : 800000,	cache : false,
		data : {datas : JSON.stringify(datas), catId: catTitle.attr("ct")},
		enctype : "multipart/form-data",
		dataType : 'json',
		success : function(data) {
			pop.closeLayer('imgLoading');
			if(data){
				if(data["msg"] == "ok"){
					mainbody.readOnly();
					
					//显示和隐藏按钮
					jsedit.show();
					jssave.hide();
				}else{
					errorShow(null, "保存失败，请重试，若连续多次不成功，请联系IT服务台。");
				}
			}
		},
		error : function(errorThrown) {
			pop.closeLayer('imgLoading');
			console.log("error:" + errorThrown);
		}
	});  
});
//点击取消
mainbody.cancelButt.click(function(){
	mainbody.clearData();
	
	mainbody.selected(null, 1);
	mainbody.readOnly();

	//显示和隐藏按钮
	jsedit.show();
	jssave.hide();
});
/**
 * 选择列表页推荐功能
 * */
mainBody.prototype.selected = function(ev, num){ 
	var catId = catTitle.attr("ct"), data = {catId: catId, ps: "pq", time: new Date().getTime()}, elValue = 0;

	if(num){
		elValue = Number(num);
	}else{
		elValue = $(ev).val(), elValue = Number(elValue);
	}
	
	if(catId){
		$.ajax({
			url : aUrl,
			type : 'get', timeout : 800000,	cache : false,
			data : data,
			dataType : 'json',
			success : function(data) {
				dataAnalyse(elValue, catId, data);
			},
			error : function(errorThrown) {
				console.log("error:" + errorThrown);
			}
		});  
	}else{
		errorShow(null, "请先选则三级分类");
	}
}
mainBody.prototype.init = function(){
	var catId = catTitle.attr("ct"), data = {catId: catId, ps: "pq", time: new Date().getTime()}, elValue = 2;
	if(catId){
		$.ajax({
			url : aUrl,
			type : 'get', timeout : 800000,	cache : false,
			data : data,
			dataType : 'json',
			success : function(data) {
				dataAnalyse(elValue, catId, data);
			},
			error : function(errorThrown) {
				console.log("error:" + errorThrown);
			}
		});  
	}else{
		errorShow(null, "请先选则三级分类");
	}
}
mainBody.prototype.readOnly = function(){
	$(".business_list input[data-list=zdx]").each(function(e){
		var _this = $(this);
		zTool.zReadOnly(_this, true);
		_this.attr("class","tt zdx_t");
		$("#saveButt"+ (e + 1)).hide();
	});
}
/*****
 * 检测输入的skuId
 * 
 * **/
function testss(ev){
	var thisId = $(ev).val(), thisId = thisId.trim(), iCatId = catTitle.attr("ct"), readO = $(ev).attr("readonly"),
	pos = $(ev).attr("pos"), pos = Number(pos);
	
	if(readO){	//只读操作不做任何处理
		return;
	}
	
	if(thisId.length == 0){	//未输入skuId时不做处理
		$(ev).attr("data-check","");
		$(ev).parent("td").parent("tr").find("td.col_3").html('<span></span>');
		return;
	}
	
	if(thisId == $(ev).attr("data-back")){ //如果输入内容不变则不做任何处理
		return;
	}
	
	if(!zTool.isSkuId(thisId)){	//是否为skuId
		$(ev).attr("data-check","error");
		$(ev).parent("td").parent("tr").find("td.col_3").html('<span class="nRed">SKU ID格式不正确</span>');
		return; 
	}
	
	if(!zTool.isLenCorrect(thisId)){ //skuId长度判断
		$(ev).attr("data-check","error");
		$(ev).parent("td").parent("tr").find("td.col_3").html('<span class="nRed">SKU ID位数不正确</span>');
		return;
	}
	
	if(mainbody.isRepeat(thisId, pos)){ //skuId去重判断
		$(ev).attr("data-check","error");
		$(ev).parent("td").parent("tr").find("td.col_3").html('<span class="nRed">SKU ID不能重复设置</span>');
		return;
	}
	pop.layerShow('imgLoading', 100);
	
    $.ajaxSetup({
		error:function(x,e){
			pop.closeLayer('imgLoading');
			errorShow($(ev), "查不到此商品，确认该商品属于此三级分类，请联系IT服务台。");
			return ;
		}
    });
	
	$.getJSON(aUrl, {skuId: thisId, ps: "ps", time: new Date().getTime()}, function(data){
		pop.closeLayer('imgLoading');
		if(data){
			var product = data.product;
			if(product){
				var cates = product["cates"], name = product["name"], state = product["state"], stock = product["stock"], belongs = false, catIds = null;
				state = Number(state);
				stock = Number(stock);
				
				if(cates){
					for(var i in cates){
						var cat = cates[i], catId = cat["catId"];
						if(catId == iCatId){
							belongs = true;
						}
						
						if(catIds){
							catIds += ",";
							catIds += catId;
						}else{
							catIds = catId;
						}
					}
				}
				if(!belongs){
					errorShow($(ev), "该商品不属于" + iCatId +",属于" + catIds);
					return; 
				}
				var content = zTool.subStr(name, 40) + " (" + (state == 4 ? "已上架，" : "已下架，") + (stock == 1 ? "有货)" : "无货)");
				$(ev).parent("td").parent("tr").find("td.col_3").html("");
				$(ev).parent("td").parent("tr").find("td.col_3").html("<span>" + content + "</span>");
				$(ev).parent("td").parent("tr").find("td.col_3").attr("title", content);
				$(ev).attr("data-check","success");
			}else{
				errorShow($(ev), "查不到此商品");
			}
		}else{
			errorShow($(ev), "查不到此商品");
		}
	});
}
function testtt(ev){
	$(ev).attr("data-back", $(ev).val());
}
function dataAnalyse(elValue, catId, data){
	if(data){
		var msg = data["msg"];
		if(msg){
			var flag = msg["flag"], flag = Number(flag), conf = false, skus = msg["skus"];
			switch(elValue){
			case 0:
				if(flag == elValue){
					mainbody.clearData();
					listHide();
					break;
				}
				if(flag == 1){
					conf = confirm("确定要取消列表页推荐吗？");
					if(conf == true){
						$.getJSON(aUrl, {catId: catId, ps: "pc", time: new Date().getTime()}, function(data){
							if(data["msg"] == "ok"){
								mainbody.clearData();
								listHide();
							}else{
								errorShow(null, "取消失败！请重试，若连续多次不成功，请联系IT服务台。");
								jsselect.get(0).selectedIndex = 1;
							}
						});
					}else{
						jsselect.get(0).selectedIndex = 1;
					}
				}
				break;
			case 1:
				if(flag == elValue && skus){
					for ( var i in skus) {
						var sku = skus[i], pos = sku["pos"], pos = Number(pos), name = sku["name"], skuId = sku["skuId"];
						$(".business_list input[data-list=zdx]").each(function(e){
							var _this = $(this), nameSpan = _this.parent("td").parent("tr").find("td.col_3 span");
							
							if(pos == (e + 1)){
								_this.val(skuId);
								_this.attr("data-check","success");
								nameSpan.html("");
								nameSpan.html("<span>" + name + "</span>");
							}
						});
					}
					jsselect.get(0).selectedIndex = 1;
				}else{
					mainbody.readOnly();
				}
				listShow();
				break;
			case 2:
				if(flag == 1){
					dataAnalyse(1, catId, data);
				}else{
					dataAnalyse(0, catId, data);
					jsselect.get(0).selectedIndex = 0;
				}
				break;
			}
		}else if(elValue == 1){
			jsselect.get(0).selectedIndex = 1;
			mainbody.cancelData();
			listShow();
		}else{
			jsselect.get(0).selectedIndex = 0;
			mainbody.readOnly();
			listHide();
		}
	}
}
function errorShow(_this, msg){
	if(_this){
		_this.attr("data-check","error");
		_this.val("");
	}
	$("#lock_err_one_msg").html("<center>"+msg+"</center>");
	pop.layerShow('lock_err_one', 80);
}
function listShow(){
	$(".js_gm").show();
	$(".js_s").hide();
	jsedit.show();
}
function listHide(){
	$(".js_gm").hide();
	$(".js_s").show();
	jsedit.hide();
	jssave.hide();
}
var re = new recommad(), ct = new ctool(), aUrl = "/cloud/business/recommad";
$(function(){
	re.search();
	re.showLeftTree();
});
var staSite='http://www.gome.com.cn',contextPath='/ec/homeus',cookieDomain='www.gome.com.cn';
function atgRegionPlugn(){
    /*绑定城市选择器*/
    $('#address').gCity({
       gc_ads:'chtm',
       gc_dat:"11011400|北京北京市东城区东城区|11010000|11000000|110114001",
       gc_evt:function(){
           //$.cookie('atgregion',this.xid+"|"+this.chtm+"|"+this.cid+"|"+this.sid+"|"+this.zid,{expires:30,path:'/',domain:cookieDomain});
           //$.cookie('atgMboxCity',this.snam,{expires:30,path:'/',domain:cookieDomain});
    	   console.info($.cookie('atgregion'));
    	   console.info(this.chtm);
       	}
    	});
};
