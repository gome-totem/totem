/*******************************************************************************
 * 弹出层 * layerShow:弹出弹出层 closeLayer:关闭弹出层
 ******************************************************************************/
var pop = {
	layerShow : function(layerName, ev) {
		if($("input:first"))$("input:first").focus().blur();
		if (!$("#Overlay").length > 0) {
			$("body").append("<div id='Overlay' style='background:#000;cursor: pointer;display: block;filter:alpha(opacity=15);opacity: 0.15;height:100%;width:100%; position: fixed; left: 0;top: 0;z-index:9999998'></div>");
		}
		/* 实现弹出 */
		$("#Overlay").show();
		var od = $("#" + layerName);

		var itop = (document.documentElement.clientHeight - od.height()) / 2;
		var ileft = (document.documentElement.clientWidth - od.width()) / 2;
		if(ev){
			itop = ev;
		}
		od.css("top", itop).css("left", ileft).css("position", "fixed").css("z-index", "9999999").show();
	},
	closeLayer : function(layerName) {
		$("#" + layerName).hide();
		$("#Overlay").hide();
	}
};
var zTool = {
	zReadOnly : function (el, is){
		if(is){
			el.attr("readOnly","readOnly");
		}else{
			el.removeAttr("readonly");
		}
	},
	isSkuId : function(skuId){
		skuId = skuId.trim();
		if(skuId.length>5){
			var nSkuId = skuId.toLowerCase();
			var left2 = nSkuId.substr(0,2), left3 = nSkuId.substr(0,3), left1 = nSkuId.substr(0,1);
			
			if(left2=="10"||left2=="11"||left1=="n"||left3=="pop"||left3=="sku"){
				return true;
			}
		}
		return false;
	},
	isLenCorrect : function(skuId){
		skuId = skuId.trim();
		if(skuId.length>5){
			var nSkuId = skuId.toLowerCase();
			var left2 = nSkuId.substr(0,2), left3 = nSkuId.substr(0,3), left1 = nSkuId.substr(0,1);
			
			if((left2=="10" || left2=="11") && skuId.length == 10){
				return true;
			}
			if((left3=="pop"||left3=="sku") && skuId.length == 13){
				return true;
			}
			if(left1=="n" && skuId.length == 11){
				return true;
			}
		}
		return false;
	},
	subStr : function(str, len){
		if(str.length > len){
			return str.substring(0, len) + "...";
		}
		return str;
	}
};
function dataProcess(html_id, temp_id, isClear, data, html, variable){
	var tmpProess = null, htmlId = null, pCtx = null;
	htmlId = domGetElementById(document, html_id);
	
	if(htmlId) {
		tmpProess = jstGetTemplate(temp_id);
		
		if(tmpProess) {
			if(isClear){
				htmlId.innerHTML = '';
			}
			if(html){
				htmlId.innerHTML = html;
			}
			htmlId.appendChild(tmpProess);
			pCtx = new JsEvalContext(data);
			
			if(variable){
				var vars = variable.split("_");
				pCtx.setVariable(vars[0], vars[1]);
			}
			jstProcess(pCtx, tmpProess);
			
		} else {
			console.log(temp_id + " is not found!")
		}
	} else {
		console.log(html_id + " is not found!")
	}
}

function ajaxFun(msg){
	$.ajax({
		url : msg.url,
		type : 'post', timeout : 800000,	cache : false,
		data : msg.data,
		dataType : 'json',
		success : function(data) {
			callBack(data);
		},
		error : function(errorThrown) {
			console.log("error:" + errorThrown);
		}
	});  
}