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
				   promotion.categorySetRender($content);
					break;
				}
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
				$("#categoryListContet").html("<span style='width: 300px;color: green;'>所选分类为三级分类无子分类</span>");
			}else{
				promotion.search(catId);
				$parent2li.children("ul").html("");
			}
			self.siblings("span").removeClass("ico_close").addClass("ico_open");
			$parent2li.find("a span").removeClass("ico_open").addClass("ico_close").removeClass("selectClass");
			self.addClass("selectClass");
		});
	},
	categorySetRender:function(content){
		$("#categoryListContet").html("<ul></ul>");
		$.each(content.childs,function(i,item){
			$("#categoryListContet ul").append("<li><span>"+item.catName+"("+item.catId+")"+"</span></li>");
		});
	},
	setCategoryOrder:function(){
	   var parentCat = $("#parentCategory");
	   if(parentCat==undefined){
	      $("#tips").html("您还没有要排序的父级分类！");
		   return;
	   }
	   var parentCatId = parentCat.html();
	   var categoryOrder = $("#categoryOrder").val();
	   if(categoryOrder.length==0){
		   $("#tips").html("排序筛选项名不能为空！");
	   	return false;
	   }
	   categoryOrder = encodeURIComponent(categoryOrder);
	   $.getJSON("/cloud/business/CategoryFacetsSet",
			{parentCatId:parentCatId,
		    categoryOrder:categoryOrder,
		    module:"catgoryOrder"},
			function(data){
				if(data.msg=='success'){
					$("#tips").html("提交成功！");
				}else{
					$("#tips").html("修改失败！");
				}
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
	}
}

var ptool = {
	fontcolor : function(prefix, v, suffix){
		return prefix + v + suffix;
 	},
 	emptySearch : function(id, str){
 		var $ID = $("#" + id );
 		if(str == $ID.val()) return true;
 		return false;
 	},
	isIllegal : function(e) {
		if (e == '' || e == undefined || e == null)
			return true;
		else
			return false;
	}
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

$(document).ready(function(){
	promotion.search();
	promotion.showLeftTree();

//	$.fn.zTree.init($("#treeCate"), setting, zNodes);
});