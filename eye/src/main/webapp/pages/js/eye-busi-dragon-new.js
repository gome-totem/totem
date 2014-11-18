var dragon = {
	firesearch : function(){
		var skuno = $("#idSku").val();
		if(tools.isIllegal(skuno)) return;
		
		$.ajax({
			url : '/cloud/business/dragon',
			type : 'get', timeout : 800000,	cache : false,
			data : {"skuNo" : skuno, "method" : "skuNoIncrease"},
			dataType : 'json',
			success : function(data) {
				$("#info").html(data.msg);
			},
			error : function(errorThrown) {
			}
		}); 
	},
	watersearch : function(){
		var skuno = $("#idSku").val();
		if(tools.isIllegal(skuno)) return;
		
		var CityId = $("#idCity .input-skuId").val();
		if(tools.isIllegal(CityId)) return;
		
		var ava = $('input:radio[name="a"]:checked').val();

		$.ajax({
			url : '/cloud/business/dragon',
			type : 'get', timeout : 800000,	cache : false,
			data : {"skuNo" : skuno, "method" : "cityIncrease", "cityId" : CityId, "available":ava},
			dataType : 'json',
			success : function(data) {
				$("#info").html(data.msg);
			},
			error : function(errorThrown) {
			}
		}); 
	},
	drgDevTools : function(){
		var inPutQuestion = $("#inPutQuestion").val();		
		var inPutCatId = $("#inPutCatId").val();
		var inPutPageNum = $("#inPutPageNum").val();
		var inPutProductTag = $("#inPutProductTag").val();
		if(inPutPageNum == "PageNum")inPutPageNum = "";
		if(inPutCatId == "CatId")inPutCatId = "";
		if(inPutQuestion == "Question")inPutQuestion = "";
		if(inPutProductTag == "ProductTag")inPutProductTag = "";
		$.ajax({
			url : '/cloud/business/dragon',
			type : 'get', timeout : 800000,	cache : false,
			data : {"pageNum":inPutPageNum,"method":"question","question":inPutQuestion,"catId":inPutCatId,"productTag":inPutProductTag},
			dataType : 'json',
			success : function(data) {
				alert(data.msg);
			},
			error : function(errorThrown) {
			}
		}); 
	},
	showMessage : function(){
		var selectId = $("#selectId").val();
		if(selectId == 1){
			$("#idCity").show();
			$("#idSearch").hide();
			$("#skuIdSearch").hide();
		}else if(selectId == 2){
			$("#idSearch").show();
			$("#idCity").hide();
			$("#skuIdSearch").hide();
		}else{
			$("#skuIdSearch").show();
			$("#idSearch").hide();
			$("#idCity").hide();
		}
	}
};

var tools = {
	isIllegal : function(e) {
		if (e == '' || e == undefined || e == null)
			return true;
		else
			return false;
	}
}