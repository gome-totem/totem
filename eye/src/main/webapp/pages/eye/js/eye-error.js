function showMessage(type, content){
	var typeName, backgroundColor, fontColor, message ;
	switch(type){
	case 1:
		typeName = "Info" ;
		backgroundColor = "#99FF00" ;
		fontColor = "black" ;
		break;
	case 2:
		typeName = "Warning" ;
		backgroundColor = "#FFFF00" ;
		fontColor = "#C09853" ;
		break;
	case 3:
		typeName = "Error" ;
		backgroundColor = "#FF0000" ;
		fontColor = "black" ;
		break ;
	default:
		typeName = "Warning" ;
		backgroundColor = "#FFFF00" ;
		fontColor = "#C09853" ;
		break ;
	}
	$("#right_head").html("") ;
	message = '<div class="alert"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>' + typeName + '!</strong> '  + content + ' </div>';
	$("#right_head").html(message);
	$(".alert").css({"background-color":backgroundColor,"color":fontColor}) ;
	setTimeout("$('#right_head .alert').hide();", 2000);
}

function sendData(){
	var $routers = $("#zooNav-init").children("li").children("ul").children("li") ;
	
	if($routers==undefined || $routers==null || $routers.length<1){
		showMessage(13,"AT least config one router!") ;
		return ;
	}
	window.location.href = "/cloud/eye/config?params=" + createJsonDict() ;
}

