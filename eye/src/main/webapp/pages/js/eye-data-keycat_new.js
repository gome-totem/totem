var keycat = function(){}, ctool = function(){};
keycat.prototype = {
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
//				var cal = self.siblings("span").attr("class");
//				if(cal.contains("open"))
//					alert(cal);
			
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
				$("#result").attr("zdx", catId);
				_this.firesearch(catId, 1);
			}else{
				_this.search(catId);
				$("#result").removeAttr("zdx");
				$parent2li.children("ul").html("");
			}
			self.siblings("span").removeClass("ico_close").addClass("ico_open");
			$parent2li.find("a span").removeClass("ico_open").addClass("ico_close").removeClass("selectClass");
			self.addClass("selectClass");
		});
	},
	firesearch : function(catId, page){
		if(catId){
			$.ajax({
				url : '/cloud/data/catkey',
				type : 'get', timeout : 800000,	cache : false,
				data : {"catId":catId, "page":page, ts:new Date().getTime()},
				dataType : 'json',
				success : function(data) {
					ct.topList(data);
					ct.pagination(data);//分页信息
				},
				error : function(errorThrown) {
					console.log("error:" + errorThrown);
				}
			});
		}
	}
};

ctool.prototype = {
	categoryInfo : function(message){
		dataProcess("catContent", "cateTempId", true, message);
	},
	categoryOtherInfo : function(message){
		dataProcess("treeTwo", "cateTempId", true, message);
	},
	topList : function(message){
		var setTempId = document.getElementById('result');
		var jsTemp = jstGetTemplate('topTempId');
		setTempId.innerHTML = '';
		setTempId.appendChild(jsTemp);
		jstProcess(new JsEvalContext(message), jsTemp);
//		var list = message.oList, content = "<table class='tmpTable' width='100%'><tbody class='pageNation'>",
//		page = message["page"], size = message["size"], mZero = null;
//		if(page){ page = Number(page); }else{ page = 1;	}
//		if(size){ size = Number(size); }else{ size = 0; }
//		
//		mZero = maxZero(size);
//		for ( var i = 0, len = list.length; i < len; i++) {
//			var oValue = list[i], key = oValue["key"];
//			if(i % 4 == 0){
//				content += "<tr><td class='top topright' width='24%'>(" + indexInc(mZero, page, i) + ")" + key +"</td>";
//			}else if(i % 4 == 3){
//				content += "<td class='top' width='24%'>(" + indexInc(mZero, page, i) + ")" + key +"</td></tr>";
//			}else{
//				content += "<td class='top topright' width='24%'>(" + indexInc(mZero, page, i) + ")" + key +"</td>";
//			}
//		}
//		content += "</tbody></table>";
//		$("#result").html("");
//		$("#result").append(content);
	},
	pagination : function(message){
		$("#result table").append("<tbody class='pagination'><tr><th colspan='4'><div class='fr'>" + pageNav.go(message["page"], message["totalPage"]) + "</div></th></tr></tbody>");
	}
};

$("#result").delegate('.pagination .fr a[zdx=nBtn]','click',function(){
	ck.firesearch($("#result").attr("zdx") , $("#pNum").val());
});

function doPageNumSearch(p){
	ck.firesearch($("#result").attr("zdx") , p);
}
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
function indexUp($index){
	$index = parseInt($index);
	if($index < 9)
		return "00" + ($index + 1);
	if($index < 99)
		return "0" + ($index + 1);
	
	return $index + 1;
}
function indexInc(mZero, page, i){
	i = Number(i);
	mZero = Number(mZero);
	var n = (page -1) * 100 + i + 1;
	var str = "", total = 0;
	if(n < 9)		{total = mZero		 }
	if(n < 99)		{total = mZero - 1 }
	if(n < 999)		{total = mZero - 2 }
	if(n < 9999)	{total = mZero - 3 }
	if(n < 99999)	{total = mZero - 4 }
	if(n < 999999)	{total = mZero - 5 }
	if(n < 9999999){total = mZero - 6 }
	
	for ( var ix = 0; ix < total; ix++) {
		str += "0";
	}
	return str + n;
}
function maxZero(num){
	if(num < 10) 		return 1;
	if(num < 100)		return 2;
	if(num < 1000)		return 3;
	if(num < 10000)	return 4;
	if(num < 100000)	return 5;
	if(num < 1000000)	return 6;
	if(num < 10000000)return 7;
	return 0;
}
var ct = new ctool(), ck = new keycat();
$(function(){
	ck.search();
	ck.showLeftTree();
});
function dataProcess(html_id, temp_id, isClear, data, html){
	var tmpProess = null, htmlId = null, pCtx = null;
	htmlId = domGetElementById(document, html_id);
	
	if(htmlId) {
		tmpProess = jstGetTemplate(temp_id);
		
		if(tmpProess) {
			
			if(isClear){
				htmlId.innerHTML = '';
			}
			if(html){
				htmlId.innerHTML = html;
			}
			htmlId.appendChild(tmpProess);
			pCtx = new JsEvalContext(data);
			jstProcess(pCtx, tmpProess);
			
		} else {
			console.log(temp_id + " is not found!");
		}
	} else {
		console.log(html_id + " is not found!");
	}
}

var ptool = {
		isIllegal : function(e) {
			if (e == '' || e == undefined || e == null)
				return true;
			else
				return false;
		}
};

function catData(name){
	if(ptool.isIllegal(name))
		return "";
	return name;
}
