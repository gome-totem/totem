var spider = function(){
	this.jarSpider = null;
	this.xmlSpider = null;
	this.logSpider = null;
	this.start = false;
};
spider.prototype = {
	startSpider : function(){
		var _this = this;
		_this.dataCheck();
		if(_this.start){
			_this.start = false;
			$.ajax({
				url : '/cloud/business/spider',
				type : 'post', timeout : 800000,	cache : false,
				data : {"jar": _this.jarSpider, "log": _this.logSpider, "xml": _this.xmlSpider},
				dataType : 'json',
				success : function(data) {
					console.log(data);
					$("td[zdx-error=jarSpider]").html("<font color='red'>" + data.error + "</font>");
					$("td[zdx-info=spider]").html(data.info);
				},
				error : function(errorThrown) {
					console.log("error:" + errorThrown);
				}
			}); 
		}
	},
	dataCheck : function(){
		var _this = this;
		$("td[zdx-info=spider]").html("");
		_this.jarSpider = $("input[zdx-type=jarSpider]").val();
		_this.logSpider = $("input[zdx-type=logSpider]").val();
		_this.xmlSpider = $("input[zdx-type=xmlSpider]").val();
		
		if(tool.isIllegal(_this.jarSpider)){
			$("td[zdx-error=jarSpider]").html("<font color='red'>jar路径错误！</font>");
			return _this.start;
		}
		if(tool.isIllegal(_this.logSpider)){
			$("td[zdx-error=logSpider]").html("<font color='red'>log路径错误！</font>");
			return _this.start;
		}
		if(tool.isIllegal(_this.xmlSpider)){
			$("td[zdx-error=xmlSpider]").html("<font color='red'>xml路径错误！</font>");
			return _this.start;
		}
		_this.start = true;
	}
};

var tool = {
	isIllegal : function(e) {
		if (e == '' || e == undefined || e == null)
			return true;
		else
			return false;
	}
};

$("input[zdx-type=jarSpider]").focus(function(){
	$("td[zdx-error=jarSpider]").html("");
});
$("input[zdx-type=logSpider]").focus(function(){
	$("td[zdx-error=logSpider]").html("");
});
$("input[zdx-type=xmlSpider]").focus(function(){
	$("td[zdx-error=xmlSpider]").html("");
});
var myspider = new spider();