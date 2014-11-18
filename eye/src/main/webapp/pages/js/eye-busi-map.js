var zoomLevel = 5;
var map = new BMap.Map("gomekeys");
var gc = new BMap.Geocoder();
map.centerAndZoom(new BMap.Point(120.404, 38.915), zoomLevel);

//缩放功能
map.addControl(new BMap.NavigationControl());
map.enableScrollWheelZoom();

//设置缩放比例
map.setMinZoom(5);
map.setMaxZoom(15);

//添加监听事件
map.addEventListener("click", function(e){
	var pt = e.point;
	gc.getLocation(pt, function(rs){
		var addComp = rs.addressComponents, city = null;
		//addComp.province，addComp.city，addComp.district，addComp.street，addComp.streetNumber
		city = addComp.city;
	});
});

var MapPoint = {
	showPoint : function(bMapCoder, cityName, icon){ //添加热点城市
		bMapCoder.getPoint(cityName, function(point){
			if (point) {
				var iconMAP = null;
				var marker = new BMap.Marker(point);
				if(icon){
					iconMAP = new BMap.Icon(icon, new BMap.Size(20, 32), {anchor: new BMap.Size(10, 30) });
					marker.setIcon(iconMAP);
				}else{
					marker.setAnimation(BMAP_ANIMATION_BOUNCE); //跳动的动画
				}
				map.addOverlay(marker);
				marker.addEventListener("click", function(){
					var infoWinOpts = {width : 200 ,height : 100}, msg = "", content = "";
					$.ajax({
						url : '/cloud/business/key-map',
						type : 'get', timeout : 800000,	cache : false,
						data : {"city":cityName, ts:new Date().getTime()},
						dataType : 'json',
						success : function(data) {
							var keys = data["value"];
							if(keys == null) msg = "暂无数据！";
							else msg = keys.toString();
							for ( var i = 0; i < keys.length; i++) {
								if((i+1)%3==1){
									content += "<tr><td class='top'>"+keys[i]+"</td>";
								}else if((i+1)%3==2){
									content += "<td class='top'>"+keys[i]+"</td>";
								}else{
									content += "<td class='top'>"+keys[i]+"</td></tr>";
								}
							}
							var infoWin = new BMap.InfoWindow("<ul><li style=' font-size: 15px;'>城市："+cityName+"</li><br><li>关键词："+msg.substring(0,80)+"...</li>",infoWinOpts); //点击显示文字
							marker.openInfoWindow(infoWin);
							map.enableScrollWheelZoom(); 
							$("#keyTOcity").html("");
							$("#keyTOcity").html(content);
						},
						error : function(errorThrown) {
						}
					}); 
				});
			}
		}, cityName);
	}
};

//三个热门城市
//MapPoint.showPoint(gc, "北京市");
//MapPoint.showPoint(gc, "重庆市");
//MapPoint.showPoint(gc, "上海市");
//MapPoint.showPoint(gc, "黔东南州凯里");

var mapInit = {
	searchMapInit : function(){
		$.ajax({
			url : '/cloud/business/key-map',
			type : 'post', timeout : 800000,	cache : false,
			data : {ps:"city",ts:new Date().getTime()},
			dataType : 'json',
			success : function(data) {
				mapInit.cityInit(data);
			},
			error : function(errorThrown) {
			}
		}); 
	},
	dataInit : function(){
		$.ajax({
			url : '/cloud/business/key-map',
			type : 'post', timeout : 800000,	cache : false,
			data : {ps:"keys",ts:new Date().getTime()},
			dataType : 'json',
			success : function(data) {
				mapInit.questionInit(data);
			},
			error : function(errorThrown) {
			}
		}); 
	},
	cityInit : function(values){
		var strStart = "<table><tbody><tr><th>城市</th><th>热度</th></tr>", strEnd = "</tbody></table>", content = "";
		for ( var i = 0; values!=null && i < values.length; i++) {
			var value = values[i], city = value["_id"], icon = '/pages/eye/image/blue_'+ (i + 1) +'.png', num = value["value"];
			if(i < 10){
				MapPoint.showPoint(gc, city, icon);
			}else{
				MapPoint.showPoint(gc, city);
			}
			content += "<tr><td>"+city+"</td><td>"+num+"</td></tr>";
		}
		$("#q_100").append(strStart + content + strEnd);
	},
	questionInit : function(values){
		var strStart = "<table><tbody><tr><th>关键词</th><th>热度</th></tr>", strEnd = "</tbody></table>", content = "";
		for ( var i = 0; values!=null && i < values.length; i++) {
			var value = values[i], question = value["_id"], num = value["value"];
			$("#question").append("<a href='http://www.gome.com.cn/search?question="+ question +"' target='_blank'>"+ question +"</a>");
			content += "<tr><td>"+question+"</td><td>"+num+"</td></tr>";
		}
		var keysBall = new Ball();
		keysBall.ballInit();
		
		$("#k_100").append(strStart + content + strEnd);
	}
};
$(function(){
	mapInit.searchMapInit();
	mapInit.dataInit();
});

var isPanelShow = false;
//显示结果面板动作
$("#showPanelBtn").click(function(){
    if (isPanelShow == false) {
        isPanelShow = true;
        $("#showPanelBtn").css("right","450px");
        $("#panelWrap").css("width","450px");
        $("#gomekeys").css("margin-right","450px");
        $("#showPanelBtn").html("隐藏面板<br/>>");
    } else {
        isPanelShow = false;
        $("#showPanelBtn").css("right","0px");
        $("#panelWrap").css("width","0px");
        $("#gomekeys").css("margin-right","0px");
        $("#showPanelBtn").html("展开面板<br/><");
    }
});
