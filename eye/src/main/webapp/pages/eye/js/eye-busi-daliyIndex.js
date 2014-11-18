var incIndex = {
	logSearch : function(){
		$.ajax({
			url : '/cloud/business/daliylog',
			type : 'get', timeout : 800000,	cache : false,
			data : {ts:dayOne},
			dataType : 'json',
			success : function(data) {
				showInfo.showDayLog("dayData1", data.today);
				showInfo.showDayLog("dayData2", data.yesterday);
				showInfo.showDayLog("dayData3", data.todayBeforYesterday);
				
				showInfo.showDayDetailLog("dayJson1", data.today);
				showInfo.showDayDetailLog("dayJson2", data.yesterday);
				showInfo.showDayDetailLog("dayJson3", data.todayBeforYesterday);
			},
			error : function(errorThrown) {
				showInfo.showDayLog("dayData2");
				
				showInfo.showDayDetailLog("dayJson1");
				showInfo.showDayDetailLog("dayJson2");
				showInfo.showDayDetailLog("dayJson3");
			}
		}); 
	},
	fireSearch : function(){
		var input = $("#proId").val();
		$.ajax({
			url : '/cloud/business/daliylog',
			type : 'get', timeout : 800000,	cache : false,
			data : {id:input, ts:dayOne},
			dataType : 'json',
			success : function(data) {
				var result = data.searchResult;
				$("#find").html(JSON.stringify(result));
			},
			error : function(errorThrown) {
				showMessage(3,errorThrown);
			}
		}); 
	}
};

var showInfo = {
	logInfo : "网络连接异常，未获取到日志信息！",
	showDayLog : function(id, message){
		var str = showInfo.logInfo;
		if(message){
			str = "product 的个数： " + (message.productSize == undefined ? 0 : message.productSize) + "个,  sku 的个数： " +
			(message.skuSize == undefined ? 0 : message.skuSize) + " 个,  gome商品： " +
			(message.gomeSize == undefined ? 0 : message.gomeSize) + " 个,  coo8商品： " + 
			(message.coo8Size == undefined ? 0 : message.coo8Size) + " 个";
		}
		$("#" + id).html(str);
	},
	showDayDetailLog : function(id, message){
		var str = showInfo.logInfo;
		if(message){
			str = JSON.stringify(message.logData);
		}
		$("#" + id).html(str);
	},
	showDate : function(){
		 $("#datF").html("今日增量统计： ");
		 $("#datS").html(yesterday + "增量统计： ");
		 $("#datT").html(todayBeforYesterday + "增量统计： ");
		 $("#daySec").html(dayTwo);
		 $("#dayThi").html(dayThree);
	},
	showDialogDate : function(){
		 $("#date1").html(today);
		 $("#date2").html(yesterday);
		 $("#date3").html(todayBeforYesterday);
	},
	log3 :function(){
		$("#day3").dialog("open");
	}
};

var tools = {
	getDateStr : function(day){
		var dd = new Date();
		dd.setDate(dd.getDate()+day);//获取AddDayCount天后的日期
		var y = dd.getFullYear();
		var m = dd.getMonth()+1;//获取当前月份的日期
		var d = dd.getDate();
		return y+"-"+m+"-"+d;
	},
	getDayStr : function(day){
		var dd = new Date();
		dd.setDate(dd.getDate()+day);//获取AddDayCount天后的日期
		var d = dd.getDate();
		return d;
	}
};

var today = tools.getDateStr(0), yesterday = tools.getDateStr(-1), todayBeforYesterday = tools.getDateStr(-2);
var dayOne = tools.getDayStr(0), dayTwo = tools.getDayStr(-1), dayThree = tools.getDayStr(-2);
$(document).ready(function(){
	showInfo.showDate();
	incIndex.logSearch();
	showInfo.showDialogDate();
});