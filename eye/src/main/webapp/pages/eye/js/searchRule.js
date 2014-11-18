var sr = {
	querySearch:function(){
		var question = $("#searchBox").val();
		$.getJSON("/cloud/business/searchRule",
				{query:question,
			    module:"querySearch"},
				function(data){
					if(data.msg=='success'){
						$("#categoryTree").html("");
						if(data.categorys.length>0){
							$.each(data.categorys,function(i,item){
								var parentNode = "<div class='parentNode' id='"+item.id+"'>【"+item.count+"】"+item.name+"("+item.id+")"+"</div>";
								$("#categoryTree").append(parentNode);
								if(item.childs.length>0){
									$.each(item.childs,function(j,jitem){
										var childNode = "<div cid='c"+item.id+"' style='display:none;font-weight:normal;'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/....【"+jitem.count+"】"+jitem.name+"("+jitem.id+")"+"</div>";
										$("#categoryTree").append(childNode);
									});
								}
							});
							$(".parentNode").each(function(){
								var self = $(this); 
								self.click(function(){
									$("[cid='c"+self.attr("id")+"']").toggle() ;
								});
							});
						}
					}else{
						$("#tips").html("搜索失败！");
					}
		});
	},
	setCategoryOrder:function(){
		   var question = $("#searchBox").val();
		   if(question.length==0){
			   $("#tips").html("请输入搜索条件，根据搜索出的结果输入分类排序列表！");
		   	return false;
		   }
		   var categoryOrder = $("#categoryOrder").val();
		   if(categoryOrder.length==0){
			   $("#tips").html("请输入排序的分类列表！");
			   return;
		   }
		   
		   $.getJSON("/cloud/business/searchRule",
				{query:question,
			   categoryOrder:categoryOrder,
			    module:"searchCategoryOrder"},
				function(data){
					if(data.msg=='success'){
						$("#tips").html("提交成功！");
					}else{
						$("#tips").html("提交失败！");
					}
			});
	}
};

var rule = {
	fireSearch : function(){
		var module = $("#ruleSelect").val(), question = $("#question").val(), value = $("#searchRule").val();
		var load = $("#tips").siblings("span");
		$("#tips").html("");
		load.show();
		$.ajax({
			type: "get",
			url: "/cloud/business/searchRule" + "?ts=" + new Date().getTime(),
			data: {"action":module, "question": question, "value":value},
			contentType: "application/json; charset=utf-8",  
			success: function (data) {
				data = JSON.parse(data);
				load.hide();
				$("#tips").html(data.msg);
			},
			error: function (XMLHttpRequest, textStatus, errorThrown) {
				var showData = "Server error!";
				load.hide();
				$("#tips").html(showData);
			}
		});
		
	}
};

var tool = {
	confirm : function(){
		var module = $("#ruleSelect").val(), question = $("#question").val(), value = $("#searchRule").val();
		if(tool.isIllegal(module)){
			return;
		}
		if(tool.isIllegal(question)){
			$("#que_err_msg").html("<center>请输入关键词</center>");
			$("#que_err_msg").dialog("open");
			return;
		}
		if(tool.isIllegal(value)){
			$("#val_err_msg").html("<center>请输入修改对象的值</center>");
			$("#val_err_msg").dialog("open");
			return;
		}
		$("#comit_msg").html("<center>内容已修改,是否提交！</center>");
		$("#comit_msg").dialog("open");
	},
	isIllegal : function(e) {
		if (e == '' || e == undefined || e == null)
			return true;
		else
			return false;
	},
	encode : function(key) {
		return encodeURIComponent(key);
	}
};

$("#que_err_msg").dialog({
	autoOpen: false,
	resizable: true,
	draggable: true,
	title:"错误提示！",
	width: "315",
	height:"145",
	modal: true,
	buttons: {
		"取消": function() {
			$(this).dialog("close");
		}
	},
	close: function() {
		$(this).dialog("close");
	}
});
$("#val_err_msg").dialog({
	autoOpen: false,
	resizable: true,
	draggable: true,
	title:"错误提示！",
	width: "315",
	height:"145",
	modal: true,
	buttons: {
		"取消": function() {
			$(this).dialog("close");
		}
	},
	close: function() {
		$(this).dialog("close");
	}
});
$("#comit_msg").dialog({
	autoOpen: false,
	resizable: true,
	draggable: true,
	title:"确认信息！",
	modal: true,
	buttons: {
		"取消": function() {
			$(this).dialog("close");
		},
		"确认": function() {
			$(this).dialog("close");
			rule.fireSearch();
		}
	},
	close: function() {
		$(this).dialog("close");
	}
});