var inputIpCSS = $('input.input-medium[data-toggle-name=add-server-ip]');//ip输入地址栏
var $routerClick = $('div.btn-group button[data-toggle-type=router]');		//router button
var $appClick = $('div.btn-group button[data-toggle-type=app]');				//app button
var $addClick = $('div.span9 button[data-toggle-name=add-router-app-btn]');	//app commit
var $finishClick = $("#add-router-app-finish-btn");		//submit
var $backWardConf = $("#init-as-last-config");		//上一次配置

/**
 * 处理router节点
 * */
$routerClick.click(function(){
	var inputIp = inputIpCSS.val(),serverType = $(this).attr("data-toggle-type");
	if(!conftool.checkIp(inputIp)){
		showMessage(2,"Illegal Ip!");
		return;
	}
	inputIp = conftool.formatIp(inputIp);
	if(conftool.isIllegal(inputIp)){
		showMessage(2,"Empty router!");
		return;
	}
	var content = serverType+ "_" + inputIp;
	if(serverNodes.contains(content)){
		showMessage(2,"This router exists!");
		return;
	}else{
		serverNodes.push(content);
		routerNode.value(inputIp);
		routerNode.drawed = false;//每次修改完touter，设置为false：router节点发生变化
		appNode.clear();
	}
});

/**
 * 处理app节点
 * */
$appClick.click(function(){
	var inputIp = inputIpCSS.val(),serverType = $(this).attr("data-toggle-type");
	if(!conftool.checkIp(inputIp)){
		showMessage(2,"Illegal Ip!");
		return;
	}
	inputIp = conftool.formatIp(inputIp);
	if(conftool.isIllegal(inputIp)){
		showMessage(2, "Empty app!");
		return;
	}
	var content = serverType+ "_" + inputIp;
	if(serverNodes.contains(content)){
		showMessage(2, "This app exists!");
		return;
	}else{
		serverNodes.push(content);
		appNode.value(inputIp);
	}
});

/**
 * 画出新的树形结构图
 * */
$addClick.click(function(){
	var aTreeNode = "", $aNodes = appNode.nodes,rTreeNode = "", $rNode =  routerNode.node;
	if(conftool.isIllegal(inputIpCSS.val())){
		showMessage(2, "No Nodes!");
		return;
	}
	//每一次把所有的app nodes画出来
	for ( var i = 0; i < $aNodes.length; i++) {
		aTreeNode += '<li id="a_'+ $aNodes[i] +'" class="a_'+ $aNodes[i] +'">'+ conftool.unFormatIp($aNodes[i]) +'</li>';
	}
	
	//drawed 为false router节点发生变化,无变化则不进行router节点的追加
	if($rNode && routerNode.drawed == false){
		rTreeNode += '<li id="'+ $rNode +'" class="r_'+ $rNode + '">'+ conftool.unFormatIp($rNode) +'<ul>'+ aTreeNode +'</ul></li>';
		$("#zooNav-init li ul:first").append(rTreeNode);
	}else{
		$("#"+$rNode + " ul").html("");
		$("#"+$rNode + " ul").append(aTreeNode);
	}
	eyeconfig.zooInfInit();
	$("#add-router-app-finish-btn").removeAttr("disabled");
	routerNode.drawed = true;
});

/**
 * 启动上一次配置
 * */
$backWardConf.click(function(){
	$.ajax({
        type: "get",
        url: "/cloud/eye/config" + "?op=r&ts=" + new Date().getTime(),
        success: function (data) {
        	conftool.drawLogDict(data);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
        	showMessage(3,"read log error!");
        }
	});
});

/***
 * 遍历树形节点，生成dict配置树
 * **/
$finishClick.click(function(){
	window.location.href = "/cloud/eye/config?op=w&params=" + conftool.getJsonNodes();
});

/***
 * 配置页主对象
 * */
var eyeconfig = {
	zooInfInit : function(){
		$("#zooTree-init").html("");
		$("#zooNav-init").orgChart({container: $("#zooTree-init"), interactive: false, fade: true, speed: 'slow'});
		this.drangAble();
	},
	drangAble : function(){
		var $level1=$("#zooTree-init .level1"),$level2=$("#zooTree-init .level2");
		$level1.draggable({
			revert: "invalid",
			containment: "document",
			helper: "clone",
			cursor: "move"
		});
		$level2.draggable({
			revert: "invalid", 
			containment: "document",
			helper: "clone",
			cursor: "move"
		});
		$level1.droppable({
			accept: $level2,
			drop: function( event, ui ) {
				eyeconfig.initAfterDrop(ui.draggable, $(this)) ;
			}
		});
	},
	initAfterDrop : function(draggable, dropgable){
		if(conftool.isIllegal(draggable) || conftool.isIllegal(dropgable)) return;
		var originSer = conftool.serverClass(draggable, "a_"), targetSer = conftool.serverClass(dropgable, "r_");
		var originIp = conftool.ip(originSer), targetIp = conftool.ip(targetSer);
		//从源节点移除该字节点
		var originRouter = $("#a_" + originIp).parent("ul").parent("li");
		originRouter.children("ul").children("li #a_" + originIp).remove();
		
		//将该节点添加至目标节点
		var aTreeNode = '<li id="a_'+ originIp +'" class="a_'+ originIp +'">'+ conftool.unFormatIp(originIp) +'</li>';
		$("#"+targetIp +" ul").append(aTreeNode);
		
		//如果目的节点的ip为最后一次配置的routerIp，则添加至appNodes，否则,从appNodes移除
		if (targetIp == routerNode.node) {
			appNode.value(originIp);	//add
		}else{
			appNode.remove(originIp);	//remove
		}
		eyeconfig.zooInfInit();
	}
};

/**
 * 配置页工具类
 * */
var conftool = {
	isIllegal : function(e) {
		if (e == '' || e == undefined || e == null)
			return true;
		else
			return false;
	},
	ip : function(server){
		var values = server.split("_");
		values.shift();
		return values.join("_");
	},
	formatIp : function(value){
		var values = value.split(".");
		return values.join("_");
	},
	unFormatIp : function(value){
		var values = value.split("_");
		return values.join(".");
	},
	checkIp : function(value){
		var illegal = true, values = value.split(".");
		if(values.length != 4) return false;
		for ( var i = 0; i < values.length; i++) {
			if(0 > values[i] || values[i] > 255)
				illegal = false;
		}
		return illegal;
	},
	realIp : function(server){
		var values = server.split("_");
		values.shift();
		return values.join(".");
	},
	serverClass : function(item, prefix){
		var $cla = item.attr("class"), serverCla = "";
		var values = $cla.split(" ");
		for ( var i = 0; i < values.length; i++) {
			if(values[i].indexOf(prefix)>-1)
				serverCla = values[i];
		}
		return serverCla;
	},
	isNum : function(val){
		if (val != null) {
			var r, re;
			re = /\d*/i; // \d表示数字,*表示匹配多个数字
			r = val.match(re);
			return (r == val) ? true : false;
		}
		return false;
	},
	getJsonNodes : function(){
		var $routers = $("#zooNav-init").children("li").children("ul").children("li") ;
		if($routers==undefined || $routers==null || $routers.length<1){
			showMessage(13,"AT least config one router!") ;
			return ;
		}
		var routers = '[' ;
		$routers.each(function(i){
			var routerIp = conftool.realIp($(this).attr("class"));
			if(i==0){
				routers += '{"ip":"' + routerIp + '" ,"status":"false"}';
			} else {
				routers += ',{"ip":"' + routerIp + '" ,"status":"false"}';
			}
			$apps = $(this).children("ul").children("li") ;
			if($apps!==undefined && $apps!=null && $apps.length>0){
				$apps.each(function(j){
					var appIp = conftool.realIp($(this).attr("class"));
					routers += ',{"ip":"' + appIp + '" ,"status":"false" ,"routerId":"'+ routerIp +'"}';
				});
			}
		});
		routers += ']' ;
		return routers ;
	},
	drawLogDict : function(data){
		if(this.isIllegal(data)) return;
		var tree = "";
		data = JSON.parse(data);
		for ( var i = 0; i < data.length; i++) {
			var router = data[i];
			var routerId = router.routerId, ip = router.ip, id = conftool.formatIp(ip);
			if(conftool.isIllegal(routerId)){
				tree += '<li id="'+ id +'" class="r_'+ id + '">'+ conftool.unFormatIp(id) +'<ul>';
					for ( var j = 0; j < data.length; j++) {
						var app = data[j];
						var routerIdInApp = app.routerId;
						if(routerIdInApp == ip){
							var aIp = app.ip, aId = conftool.formatIp(aIp);
							tree += '<li id="a_'+ aId +'" class="a_'+ aId +'">'+ conftool.unFormatIp(aId) +'</li>';
						}
					}
				tree += '</ul></li>';
			}
		}
		$("#zooNav-init li ul:first").append(tree);
		eyeconfig.zooInfInit();
	}
};

/**
 * document.ready()
 * */
$(function(){
	eyeconfig.zooInfInit();
	$("#add-router-app-finish-btn").attr("disabled", "disabled");
});

/*************************************/
/**				用于去重				**/
/*************************************/
var serverNodes = [];
serverNodes.contains = function(name){
	var content = JSON.stringify(serverNodes);
	if(content.indexOf(name)>-1) return true;
	return false;
};

/*************************************/
/**		routerNode 有且仅有一个		**/
/**		appNode 有多个,均添入进来		**/
/**		当routerNode 发生变化时		**/
/**	appNode 便重置,app属于新的router  **/
/*************************************/
var routerNode = {
	node : null,
	drawed : false,
	value : function(value){
		this.node = value;
	}
};
var appNode = {
	nodes : new Array(),
	value : function(value){
		this.nodes.push(value);
	},
	remove : function(value){
		var values = this.nodes;
		this.clear();
		for ( var i = 0; i < values.length; i++) {
			if(value != values[i])
				this.value(values[i]);
		}
	},
	clear : function(){
		this.nodes = new Array();
	}
};

$("#error_msg").dialog({
	autoOpen: false,
	resizable: true,
	width: "400",
	height:"350",
	title:"友情提示",
	modal: true,
	buttons: {
		"取消": function() {
			$(this).dialog("close");
		}
	},
	close: function() {
		$("#apps_in_router").html("") ;
	}
});	
