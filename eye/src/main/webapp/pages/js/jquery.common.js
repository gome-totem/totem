$(function(){
	var globalVar = new globalKey(), pannelShow = true;
	$(".slideBtn").click(function(){
		if(pannelShow){
			pannelShow = false;
			$(".demoMain").attr("style","border-left-width:0");
			$(this).addClass("allScreen");
			$(".slideBtn").removeAttr("style");
		}else{
			pannelShow = true;
			$(".demoMain").attr("style","border-left-width:220px");
			$(this).removeClass("allScreen");
			$(".slideBtn").removeAttr("style");
		}
	});
	
	$("[data='js-tipsBox']").each(function(){
		$(this).mouseenter(function(){
			$(this).addClass("chs")
		}).mouseleave(function(){
			$(this).removeClass("chs")
		})
	});
	
	$(".setBox").each(function(){
		var $this = $(this), triger =$this.find("[js-triger='show']"), closeTriger = $this.find("[js-triger='close']");
		triger.bind("click",function(){
			if($this.hasClass("setBoxShow")){
				$this.removeClass("setBoxShow");
				$(this).removeClass("up");
			}else{
				$this.addClass("setBoxShow");
				$(this).addClass("up");
			}
		});
		closeTriger.bind("click",function(){
		    alert(00)
		});
	});
	
	$(".barBox").each(function(){
		var $this = $(this), triger = $this.find("dt");
		triger.bind("click",function(){
			if($this.hasClass("barBoxShow")){
				$this.removeClass("barBoxShow");
			}else{
				$this.addClass("barBoxShow");
			}
		});
	});
    
	var tc = globalVar.getQStr("tc"), type = 0;
	if(tc == 'sort') type = 4;
	if(tc == 'logg') type = 3;
	if(tc == 'moni') type = 2;
	if(tc == 'data') type = 1;
	
	$(".nav li.links").each(function(cusor){
		if(cusor == type) $(this).find("a").addClass("cur");
    });
})

var globalKey = function(){}
globalKey.prototype = {
	getQStr : function(name) {
		var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
		var r = window.location.search.substr(1).match(reg);
		if (r != null){
			return unescape(r[2]);
		}
		return null;
	}
}
