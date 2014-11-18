var eyeMonitor = {
	initOrgChart : function(){
		$("#zooTree").html("") ;
		$("#zooNav").orgChart({container: $("#zooTree"), interactive: false, fade: true, speed: 'slow'});
	},
	initZooNode : function(){ //初始化zooNode
		$.ajax({
	        type: "get",
	        url: "/cloud/eye/zookeeper/ajax-info?ts=" + new Date().getTime(),
	        success: function (data) {
	        	drawTable.drawZooNode(data) ;
	        },
	        error: function (XMLHttpRequest, textStatus, errorThrown) {
	        	showMessage(3,"Init zooNode error!");
	        }
		});
	},
	initDroppable : function() {
		var $offLine = $("#off-line"), $level1=$("#zooTree .level1"),$level2=$("#zooTree .level2");
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
				afterDrop(ui.draggable, $(this)) ;
			}
		});
		$offLine.droppable({
			activeClass: "ui-state-highlight",
			hoverClass: "ui-state-active",
			drop: function( event, ui ) {
				console.log($(this).attr("class")) ;
			}
		});
	}
};

var operations = {
	changeApp2Router : function(appIp, routerIp, actType){
		$.ajax({
	        type: "get",
	        url: "/cloud/eye/server/handle",
	        data:{act:actType,ip:appIp,routerIp:routerIp, server:"app",tm:ts=new Date().getTime()},
	        contentType: "application/json; charset=utf-8",  
	        success: function (data) {
	        },
	        error: function (XMLHttpRequest, textStatus, errorThrown) {
	        	showMessage(3,"Sorry, You are Unauthorized! (ip: " + appIp + ")");
	        }
		});
	},
	offlineOperation : function(nm){
		var ip = "", server = "", names = nm.split("_");
		if(names.length === 6){
			server = names.shift();	
			names.shift(); //去除数组的第一个，剩下ip子段
			ip = names.join(".");
			$.ajax({
			        type: "get",
			        url: "/cloud/eye/server/handle?ts=" + new Date().getTime(),
			        data: {act:14, ip: ip,server:server},
			        contentType: "application/json; charset=utf-8",  
			        success: function (data) {
			        	$(".node ." + nm).css("background-color","#00FFFF") ;
			        },
			        error: function (XMLHttpRequest, textStatus, errorThrown) {
			        	showMessage(3,"Sorry, You are Unauthorized! (ip: " + ip + ")");
			        }
			});
		}
	},
	readAppsOperation : function(nm){
		var ip = "", server = "", names = nm.split("_");
		if(names.length === 6){
			server = names.shift();	
			names.shift(); //去除数组的第一个，剩下ip子段
			ip = names.join(".");
		
			$.ajax({
		        type: "get",
		        url: "/cloud/eye/server/handle?ts=" + new Date().getTime(),
		        data: {act:20, ip: ip, server : server},
		        contentType: "application/json; charset=utf-8",  
		        success: function (data) {
		        	if(data!=null && data!=undefined && data!=""){
		        		data = eval('(' + data + ')');  
		        		var title = "Router: " + ip ;
		        		var table = "<table class='table table-bordered'>" ;
		        		table += "<tr><td>Ip</td><td>Tag</td></tr>" ;
		        		for(var i =0; i<data.length; i++){
		        			table += "<tr><td>" + data[i].ip + "</td><td>" + data[i].tags + "</td></tr>" ;
		        		}
		        		table += "</table>" ;
		        		$("#apps_in_router").html(table) ;
		        		$("#apps_in_router").siblings(".ui-dialog-titlebar").children("span").html(title) ;
		        		$("#apps_in_router").dialog("open");
		        	}
		        },
		        error: function (XMLHttpRequest, textStatus, errorThrown) {
		        	showMessage(3,"Sorry, You are Unauthorized! (ip: " + ip + ")");
		        }
			});
		}
	},
	monitorOperation : function(nm){
		var ip = "", server = "", names = nm.split("_");
		
		if(names.length === 6){
			server = names.shift();	
			if(server == "app") server = "appserver";
			
			names.shift(); //去除数组的第一个，剩下ip子段
			ip = names.join(".");

			var oReq = {};
			oReq.ip = ip;
			oReq.type = server ;
			
			client.send("open",oReq);
			showDetail(ip, server) ;
		}
	},
	onlineOperation : function(info){
		var $server = $(info).attr("type"),$ip = $(info).attr("name");
		if (!eyetool.isIllegal($server) && !eyetool.isIllegal($ip)){
			$.ajax({
				type: "get",
				url: "/cloud/eye/server/handle" + "?ts=" + new Date().getTime(),
				data: {act:13, ip: $ip, server:$server},
				contentType: "application/json; charset=utf-8",  
				success: function (data) {
					if(eyetool.isIllegal(data)){
						showMessage(3,"Add server failed, please check is this router alive?") ;
						return;
					}
//					data = eval('(' + data + ')');  
					if(data.state==false){
						showMessage(3,"Add server failed, please check is this router alive?") ;
					}
				},
				error: function (XMLHttpRequest, textStatus, errorThrown) {
					showMessage(3,"Sorry, You are Unauthorized! (ip: " + $ip + ")");
				}
			});
		}
	}
};

var eyetool = {
	isIllegal : function(e) {
		if (e == '' || e == undefined || e == null)
			return true;
		else
			return false;
	},
	removeEmtyTable : function(){
		$(".eye-sli-hide").siblings().addClass("eye-hide");
	},
	trim : function(obj){
		if(obj==null || obj==undefined || obj==""){
			return "" ;
		}
		return obj;
	}
};

$("#detail-information-of-zookeeper").dialog({
	autoOpen: false,
	width: "450",
	height:"350",
	title:"Detail Information of Zookeeper",
	modal: true,
	buttons: {
		"取消": function() {
			$(this).dialog("close");
		}
	},
	close: function() {
	}
});	
$("#apps_in_router").dialog({
	autoOpen: false,
	resizable: true,
	width: "400",
	height:"350",
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
$("#show-router-info-div").dialog({
	autoOpen: false,
	resizable: true,
	draggable: true,
	width: "500",
	height:"200",
	position:'center',
	buttons: {
		"取消": function() {
			$(this).dialog("close");
		}
	},
	close: function() {
		routerIndex = 0 ;
		monitorRouterIp = null ;
		$("#show-router-info-div").html("") ;
		var ip=$("#show-router-info-div").attr("ip");
		var server=$("#show-router-info-div").attr("server");
		var oReq = {};
		oReq.ip = ip;
		oReq.type = server ;
		client.send("close",oReq);
		$("#show-router-info-div").attr("ip","");
	}
});	
$("#show-app-info-div").dialog({
	autoOpen: false,
	resizable: true,
	draggable: true,
	width: "1000",
	height:"500",
	buttons: {
		"取消": function() {
			$(this).dialog("close");
		}
	},
	close: function() {
		appLogIndex = 0 ;
		$("#show-app-info-div").html("") ;
		var ip=$("#show-app-info-div").attr("ip");
		var server=$("#show-app-info-div").attr("server");
		var oReq = {};
		oReq.ip = ip;
		oReq.type = server ;
		client.send("close",oReq);
		$("#show-app-info-div").attr("ip","");
	}
});

function readTitle($this){
	var json = { name : $this.name, id : $this.id, ip : $this.ip, role : $this.role, timestamp : $this.timestamp
			, tags : $this.tags};
	return JSON.stringify(json);
}
function cla($this){
	var status = $this.state;
	if("1"==status){
		return "error " + $this.nickName;
	}
	if("2"==status)
		return "warning " + $this.nickName;
	var $id = $this.id;
	$id = $id.replace(" ", "_");
	$id = "appRouterId" + $id;
	return  $this.nickName + " " + $id;
}
function routerId($this){
	var $id = $this.id;
	$id = $id.replace(" ", "_");
	$id = "appRouterId" + $id;
	return  $id;
}
function claApp($this){
	var status = $this.state;
	if("1"==status) return "error " + $this.nickName;
	if("2"==status) return "warning " + $this.nickName;
	return $this.nickName;
}
function showDetailFiler($this){
	var status = $this.state;
	if("1"==status) return false;
	return true;
}
