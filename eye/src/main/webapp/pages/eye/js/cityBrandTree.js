var selNode,zTreeObj,cityId;
var contextPath = "ec/homeus";

/**
 * 设置zTree树
 */
var setting = {
	async:{
		enable:true,
		url: "http://www.mysite.com.cn/cloud/eye/cityKeywordsBrand?module=getChildrens",
		autoParam:["id"]
	},
	check: {enable: true},
	data: {
		simpleData: {
			enable: true
		}
	},
   view: {selectedMulti: false}, 
	callback: {

	}
};
var zNodes =[{ id:"1", pId:"1", name:"关键字根目录"},
             { id:"11", pId:"1", name:"冰箱"},
             { id:"111", pId:"11", name:"海尔"},
             { id:"1111", pId:"111", name:"北京"},
             { id:"1112", pId:"111", name:"上海"},
             { id:"1113", pId:"111", name:"广州"},
             { id:"1114", pId:"111", name:"深圳"},
             { id:"1115", pId:"111", name:"沈阳"},
             { id:"112", pId:"11", name:"松下"},
             { id:"1121", pId:"112", name:"北京"},
             { id:"1122", pId:"112", name:"上海"},
             { id:"1123", pId:"112", name:"广州"},
             { id:"1124", pId:"112", name:"深圳"},
             { id:"1125", pId:"112", name:"沈阳"},
             { id:"113", pId:"11", name:"西门子"},
             { id:"12", pId:"1", name:"电视机"},
             { id:"13", pId:"1", name:"i9500"},
             { id:"131", pId:"13", name:"三星"},
             { id:"1311", pId:"131", name:"沈阳"},
             { id:"14", pId:"1", name:"空调"}
             ];

function getUrlByNodeId(treeId, treeNode){
    return "/cloud/eye/cityKeywordsBrand?module=getChildrens" + treeNode.id;
}

function beforeExpand(treeId, treeNode){
	console.log("beforeExpand");
}

function onAsyncError(event, treeId, treeNode, XMLHttpRequest, textStatus, errorThrown){
	console.log("onAsyncError");
}

function zTreeOnClick(event, treeId, treeNode){
	selNode = treeNode;
}

function onAsyncSuccess(event, treeId, treeNode, msg){
	console.log("onAsyncSuccess");
}

var nodeId=10000;

function treeAddNodes(name,id){
	id = nodeId++;
	if(selNode != undefined){
	   var newNode = {id:id,name:name};
		zTreeObj.addNodes(selNode,newNode);
	}else{
		alert("点击选择节点进行添加！");
	}
}

function addKeyWords(){
	var keyWords = $("#keywords").val();
	treeAddNodes(keyWords, 0)
}

function atgRegionPlugn(){
    /*绑定城市选择器*/
    $('#address').gCity({
    	 gc_utl:'http://www.gome.com.cn/ec/homeus/browse/provinceDroplet.jsp',/* 获取一级城市 */
    	 gc_jnp:'http://www.gome.com.cn/ec/homeus/browse/areaCommonDroplet.jsp',/* 获取二三四级城市 */
       gc_ads:'chtm',
       gc_dat:"11011400|北京北京市东城区东城区|11010000|11000000|110114001",
       gc_evt:function(){
         cityId=this.xid;
          }
    	 });
}

function getBrandsByCatId(){
	var catId = $("#catIdInput").val();
	$.getJSON("",
			{},
		   function(data){
			console.log(data);
		});
}

$(document).ready(function(){
	$.fn.zTree.init($("#treeDemo"), setting, zNodes);//初始化zTree树
	zTreeObj = $.fn.zTree.getZTreeObj("treeDemo");
	atgRegionPlugn();
});

