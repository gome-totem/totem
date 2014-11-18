var topcat = {
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
					ctool.categoryInfo($content);//分类信息
					break;
				default:
					ctool.categoryOtherInfo($content);//二级分类信息
					break;
				}
			},
			error : function(errorThrown) {
				console.log("error:" + errorThrown);
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
			if(parseInt($level) == 2){
				$("#result").html("");
				$("#result").attr("zdx", catId);
				topcat.pagesearch(catId,1);
			}else{
				topcat.search(catId);
				$parent2li.children("ul").html("");
			}
			self.siblings("span").removeClass("ico_close").addClass("ico_open");
			$parent2li.find("a span").removeClass("ico_open").addClass("ico_close").removeClass("selectClass");
			self.addClass("selectClass");
		});
	},
	firesearch : function(catName, catId){
		if(ptool.isIllegal(catName) || ptool.isIllegal(catId)) return;
		$.ajax({
			url : '/cloud/business/cattop',
			type : 'post', timeout : 800000,	cache : false,
			data : {"name":catName,"id":catId, ts:new Date().getTime()},
			dataType : 'json',
			success : function(data) {
				ctool.topList(data);
			},
			error : function(errorThrown) {
				console.log("error:" + errorThrown);
			}
		});
	},
	pagesearch : function(catId, page){
		if(ptool.isIllegal(catId)) return;
		$.ajax({
			url : '/cloud/business/cattop',
			type : 'post', timeout : 800000,	cache : false,
			data : {"page":page,"id":catId, ts:new Date().getTime()},
			dataType : 'json',
			success : function(data) {
				ctool.topList(data);
				ctool.pagination(data);
			},
			error : function(errorThrown) {
				console.log("error:" + errorThrown);
			}
		});
	}
};

var ctool = {
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
	topList : function(message){
		var setTempId = document.getElementById('result');
		var jsTemp = jstGetTemplate('topTempId');
		setTempId.innerHTML = '';
		setTempId.appendChild(jsTemp);
		jstProcess(new JsEvalContext(message), jsTemp);
	},
	pagination : function(message){
		$("#result table").append("<tbody class='pagination'><tr><th colspan='5'><div class='fr'>" + pageNav.go(message['page'], message['totalPage']) + "</div></th></tr></tbody>");
	}
};

$("#result").delegate('.pagination .fr a[zdx=nBtn]','click',function(){
	topcat.pagesearch($("#result").attr("zdx") , $("#pNum").val());
});

function doPageNumSearch(p){
	topcat.pagesearch($("#result").attr("zdx") , p);
}
var ptool = {
	isIllegal : function(e) {
		if (e == '' || e == undefined || e == null)
			return true;
		else
			return false;
	}
};
function treeId($this, dom){
	return "treeCate_"+ $this.catId +"_" + dom + "_" + $this.catName;
}
function fooler($this){
	return parseInt($this) + 1;
}
function icoClass($this){
	if($this ==3)
		return "ico_docu";
}
function catData(name){
	if(ptool.isIllegal(name))
		return "";
	return name;
}
function indexUp($index){
	$index = parseInt($index);
	if($index < 9)
		return "00" + ($index + 1);
	if($index < 99)
		return "0" + ($index + 1);
	
	return $index + 1;
}
$(document).ready(function(){
	topcat.search();
	topcat.showLeftTree();
});