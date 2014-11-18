var appLogIndex = 0, routerIndex = 0 ;

$.WebClient = function() {
	var hostName = $("#hostName").val();
	var ws = null;
	var isOpen = false;
	var self = this;
	this.isLive = function() {
		return isOpen;
	} ;
	this.connect = function() {
		if (ws != null && isOpen) {
			return;
		}
		var uri = "ws://" + hostName + "/web.socket";
		ws = new WebSocket(uri);
		ws.onopen = function() {
			self.hello();
		};
		ws.onmessage = function(e) {
			var oMsg = JSON.parse(e.data);
			var serverType = oMsg["serverType"];
			var msgType = oMsg["msgType"];		
			switch (serverType) {
			case "router":
				switch (msgType) {
				case "runtime":
					drawTable.showServerInfo(oMsg["ip"],"router_server", oMsg) ;
					break;
				case "log":
					if(routerIndex>300){
						$("#show-router-info-div").html("");
						routerIndex = 0 ;
					}
					var message = "Time: " + oMsg.time + ", action: " + oMsg.action + ", message: " + JSON.stringify(oMsg.message) ;
					$("#show-router-info-div").prepend(message + "</br>");
					routerIndex ++ ;
					break;
				}
				break;
			case "appserver":
				switch (msgType) {
				case "runtime":
					drawTable.showServerInfo(oMsg["ip"],"app_server", oMsg) ;
					break;
				case "log":
					if(appLogIndex>300){
						$("#show-app-info-div").html("") ;	
						appLogIndex =  0 ;
					}
					var message = "Time: " + oMsg.time + ", action: " + oMsg.action + ", message: " + JSON.stringify(oMsg.message) ;
					$("#show-app-info-div").prepend(message + "</br>");
					appLogIndex ++ ;
					break;
				}
				break;
			case "zookeeper":
				switch (msgType) {
				case "runtime":
//					eyeMonitor.initZooNode();
					 history.go(0);
					break;
				}
				break;
			}
		} ;
		ws.onerror = function(event) {
			console.log("onerror") ;
			isOpen = false;
		};
	} ;

	this.send = function(action, oReq) {
		oReq.action = action;
		ws.send(JSON.stringify(oReq));
	};

	this.hello = function() {
		var oReq = {};
		oReq.action = "hello";
		ws.send(JSON.stringify(oReq));
	} ;
} ;
var client =null ;
function onMapReady(){
   client = new $.WebClient();
   client.connect();
}


$("#btnOpen").click(function(e) {
	var oReq = {};
	oReq.ip = "10.57.41.213";
	oReq.type = "router";
	client.send("open", oReq);
}) ;
