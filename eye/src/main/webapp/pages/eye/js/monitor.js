function showDetail(ip, server){
	var title = server + ": " + ip ;
	
	if(server=="router"){
		var $infoDiv = $("#show-router-info-div");
		$infoDiv.attr("title", "").attr("title", title) ;
		$infoDiv.html("") ;
		$infoDiv.siblings(".ui-dialog-titlebar").children("span").html(title) ;
		$infoDiv.attr("ip",ip).attr("server",server);
		$infoDiv.dialog("open");
	} else if (server=="appserver"){
		var $infoDiv = $("#show-app-info-div");
		$infoDiv.attr("title", "").attr("title", title) ;
		$infoDiv.html("") ;
		$infoDiv.siblings(".ui-dialog-titlebar").children("span").html(title) ;
		$infoDiv.attr("ip",ip).attr("server",server);
		$infoDiv.dialog("open");
	}
}

function detailOfZookeeper(){
	$("#detail-information-of-zookeeper").dialog("open");
}

function afterDrop(draggable, droppable){
	if(null==draggable || undefined==draggable || null==droppable || undefined==droppable){return ;}
	var dropclasses,dragclasses,appClass="",targetRouterClass="",originRouterClass,app,originRouter,appIp,routerIp = null;
	dropclasses = droppable.attr("class").split(" ") ;
	dragclasses = draggable.attr("class").split(" ") ;
	for(var i = 0; i< dragclasses.length; i++){
		if(dragclasses[i].length>11 && dragclasses[i].substring(0,11)=='app_server_'){
			appClass = dragclasses[i] ;
		}
	}
	for(var i = 0; i< dropclasses.length; i++){
		if(dropclasses[i].length>14 && dropclasses[i].substring(0,14)=='router_server_'){
			targetRouterClass = dropclasses[i] ; 
		}
		if(dropclasses[i].indexOf('appRouterId') > -1){
			routerIp = dropclasses[i];
		}
	}
	app = $("li ." + appClass) ; 
	targetRouter = $("li ." + targetRouterClass) ;
	originRouter = app.parent("ul").parent("li") ;
	originRouterClass = originRouter.attr("class") ;
	if(eyetool.trim(targetRouterClass)==eyetool.trim(originRouterClass)){
		showMessage(2,"This App already in this router!") ;
		return ;
	}
	appIp = appClass.substring(11) ;
	operations.changeApp2Router(appIp, routerIp, 8) ;
}

var templ = {
	zooTree : function(data){
		var $zooNav = document.getElementById("zooNav");
		var $zooNodeTmpId = jstGetTemplate('zooNodeTmpId');
		$zooNav.innerHTML = '';
		$zooNav.appendChild($zooNodeTmpId);
		jstProcess(new JsEvalContext(data), $zooNodeTmpId);
	},
	zooDetailInfo : function(data){
		var $detail = document.getElementById('detail-information-of-zookeeper');
		var $zooInfoTmpId = jstGetTemplate('zooInfoTmpId');
		$detail.innerHTML = '';
		$detail.appendChild($zooInfoTmpId);
		jstProcess(new JsEvalContext(data), $zooInfoTmpId);
	}
};

$(document).ready(function(){
	onMapReady();
	eyeMonitor.initZooNode();
	eyeMonitor.initDroppable() ;
});

var drawTable = {
	drawZooNode : function(message){
		if(message==null || message=="" || message== undefined)   return ;
		var data = JSON.parse(message);
		if(data.isOpen==true){
			return ;
		}
		templ.zooTree(data);
		templ.zooDetailInfo(data);
		eyeMonitor.initOrgChart() ;
		eyeMonitor.initDroppable() ;
		eyetool.removeEmtyTable();
	},
	showServerInfo : function(ip, server, data){
		var nickName = server  + "_" + ip.replace(".", "_").replace(".", "_").replace(".", "_") ;
		var id,title,maxMemory,allocatedMemory,freeMemory,totalThread,blockedThread,buttons="",content, jobCount ;
		title = server.replace("_server", "") + " " + ip;
			usedMemory = data.usedMemory ;
	    	allocatedMemory = data.allocatedMemory ;
	    	freeMemory = data.freeMemory ;
	    	totalThread = data.totalThread ;
	    	blockedThread = data.blockedThread ;
	    	jobCount = data.jobCount ;
	    	id = data.id ;
	    	
	    if(server=="router_server"){    		
		    	buttons = "<div class='btn-group' colspan='2'>" + 
		    	"<button onclick='operations.monitorOperation(this.name)' name='" + nickName + "' id='" + id + "' class='btn btn-mini'>Monitor</button>" + 
		    	"<button onclick='operations.offlineOperation(this.name)' name='" + nickName + "' id='" + id + "' class='btn btn-mini'>OffLine</button>" +
		    	"<button onclick='operations.readAppsOperation(this.name)' name='" + nickName + "' id='" + id + "' class='btn btn-mini'>Apps</button></div>"  ;
	    	}
	    if(server=="app_server"){
			buttons = "<div class='btn-group' colspan='2'>" + 
			"<button onclick='operations.monitorOperation(this.name)' name='" + nickName + "' id='" + id + "' class='btn btn-mini'>Monitor</button>" + 
			"<button onclick='operations.offlineOperation(this.name)' name='" + nickName + "' id='" + id + "' class='btn btn-mini'>OffLine</button></div>"; 
		}
		content = "<table class='table table-bordered'><tr><td colspan='2'>" + title + "</td></tr>" +
		 	"<tr><td>maxMem</td><td>" + allocatedMemory + "</td></tr>" + 
		 	"<tr><td>useMem</td><td>" + usedMemory + "</td></tr>" + 
		 	"<tr><td>freeMem</td><td>" + freeMemory + "</td></tr>" + 
		 	"<tr><td>aThread</td><td>" + totalThread + "</td></tr>" + 
		 	"<tr><td>bThread</td><td>" + blockedThread + "</td></tr>" + 
		 	"<tr><td>jobCount</td><td>" + jobCount + "</td></tr>" + 
		 	"</table>" + buttons ;
		 $(".node ." + nickName).html(content) ;
		 this.showError("error");
		 this.showError("warning");
	},
	showError : function(css){
		var errors = $("#zooNav").find("." + css);
		 errors.each(function(e){
			 var errorNames = $(this).attr("class").split(" ");
			 var name = "", values = null, serverType = null, value = null, content = null;
			 for ( var i = 0; i < errorNames.length; i++) {
				 var errorName = errorNames[i];
				 if(errorName.indexOf("_server_")>-1)
					 name = errorName;
			 }
			 values = name.split("_");
			 serverType = values.shift();
			 values.shift();
			 value = values.join(".");
			 
			 if(css == "error"){
				content = value + '<br/><button onclick="operations.onlineOperation(this)" type="'+serverType+'" name="'+ value +'" class="btn"  style="margin-top:10px;">Online</button>';
			 }
			 if(css == "warning"){
				 content = value + "<br/><button onclick='operations.offlineOperation(this.name)' name='" + name + "' class='btn'>OffLine</button>";
			 }
			 $(".node ."+ css + "." + name).html("<h2>" +content + "</h2>") ;
		 });
	}
};
