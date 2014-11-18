var appLogIndex = 0, routerIndex = 0;
$.WebClient = function() {
	var hostName = $("#hostName").val();
	var ws = null;
	var isOpen = false;
	var self = this;
	this.isLive = function() {
		return isOpen;
	};
	this.connect = function() {
		if (ws != null && isOpen) {
			return;
		}
		var uri = "ws://" + hostName + "/detect.socket";
		ws = new WebSocket(uri);
		ws.onopen = function() {
			self.hello();
		};
		ws.onmessage = function(e) {
			var oMsg = JSON.parse(e.data);
			showData(oMsg);
		};
		ws.onerror = function(event) {
			console.log("onerror");
			isOpen = false;
		};
	};

	this.send = function(action, oReq) {
		oReq.action = action;
		ws.send(JSON.stringify(oReq));
	};

	this.hello = function() {
		var oReq = {};
		oReq.action = "category";
		ws.send(JSON.stringify(oReq));
	};
};
var client = null;
function onMapReady() {
	client = new $.WebClient();
	client.connect();
}

onMapReady();

var i=0;
var erro500=0;
var erro404=0;
var erro200=0;
var erro999=0;
var erro888=0;
var totalCount=0;
var data404="";
var data999="";
var data500="";
var data888="";
function showData(oMsg){
	$("#result").children().remove();
	$("#detectButton").css('display','none');
	i++;
	totalCount++;
	if(oMsg["status"]==999){
		if(data999.indexOf(oMsg["ip"]+":"+oMsg["port"])<0){
			data999+=oMsg["ip"]+":"+oMsg["port"]+"|";
		}
		erro999++;
	}
	if(oMsg["status"]==888){
		if(data888.indexOf(oMsg["ip"]+":"+oMsg["port"])<0){
			data888+=oMsg["ip"]+":"+oMsg["port"]+"|";
		}
		erro888++;
	}
	if(oMsg["status"]==200){
		erro200++;
	}
	if(oMsg["status"]==404){
		if(data404.indexOf(oMsg["ip"]+":"+oMsg["port"])<0){
			data404+=oMsg["ip"]+":"+oMsg["port"]+"|";
		}
		erro404++;
	}
	if(oMsg["status"]==500){
		if(data500.indexOf(oMsg["ip"]+":"+oMsg["port"])<0){
			data500+=oMsg["ip"]+":"+oMsg["port"]+"|";
		}
		erro500++;
	}
	var style="";
	if(oMsg["status"]==500){
		style="style='color:yellow'";
	}else if(oMsg["status"]==404){
		style="style='color:orange'";
	}else if(oMsg["status"]==999){
		style="style='color:red'";
	}else if(oMsg["status"]==888){
		style="style='color:red'";
	}
	if(oMsg["status"]==888){
		oMsg["status"]='timeout';
	}else if(oMsg["status"]==999){
		oMsg["status"]='error';
	}
	var log="<tr "+ style +"><td class='rightTD' style='font-weight:bold;'>"+oMsg["ip"]+"</td><td class='rightTD' style='font-weight:bold;'>"+oMsg["port"]+"</td><td class='rightTD' style='font-weight:bold;'>"+oMsg["status"]+"</td></tr>"
	if(oMsg["ip"]=='0.0.0.0'){
		$("#load").css('display','none');
		//var percent=Math.round(((erro888+erro999+erro500+erro404)) / totalCount * 10000 / 3) / 100.00;
		var percent=Math.round(erro404 / totalCount * 10000 / 3) / 100.00;
		var notifyPeople='';
		if(percent>30){
			notifyPeople="紧急联系人：<br>丁宏波-13426290206，蒋礼俊-18701614182";
		}
		$("#result").prepend("<br><div>请求监控服务连接共计：<font color='red' style='font-weight:bold;'>"+ (totalCount-1)+"</font>次</div><br><div>请求服务连接正常为：<font color='red' style='font-weight:bold;'>"+erro200+"</font>次</div>" +
		"<br><div>请求服务连接不存在404为：<font color='red' style='font-weight:bold;'>"+erro404+"</font>次</div><br><div>请求服务连接异常500为：<font color='red' style='font-weight:bold;'>"+erro500+"</font>次</div><br><div>请求服务连接超时为：" +
		"<font color='red' style='font-weight:bold;'>"+erro888+"</font>次</div><br><div>请求服务连接错误为：<font color='red' style='font-weight:bold;'>"+erro999+"</font>次</div>" +
		"<br><div >请求服务连接不存在404所占比例为：<font color='red' style='font-weight:bold;'>"+percent+"%</font></div>"+
		"<br><div ><font color='red' style='font-weight:bold;'>"+notifyPeople+"</font></div>");
		sendMail(data404,data500,data999,data888,percent);
		$("#detectButton").css('display','block');
		
	}else{
		$("#logs").prepend(log);
	}
	
	
}

$("#btnOpen").click(function(e) {
	var oReq = {};
	oReq.ip = "10.57.41.213";
	oReq.type = "router";
	client.send("open", oReq);
});

function sendMail(data404,data500,data999,data888,percent){
	if((erro999+erro500+erro404)>0){
		$.ajax({
			url : '/cloud/eye/mailSend',
			type : 'post', timeout : 800000,	cache : false,
			data : {"data404":data404,"data500":data500,"data999":data999,"data888":data888,"percent":percent},
			success : function(dat) {
					$("#result").append('<br><div><font color="red">监控通知邮件已发送成功。</font></div>');
			},
			error : function(errorThrown) {
				console.log("error:" + errorThrown);
			}
		});
	}
}
