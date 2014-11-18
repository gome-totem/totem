$(function(){
	$('input').iCheck({
	    checkboxClass: 'icheckbox_square',
	    radioClass: 'iradio_square-green',
	    increaseArea: '20%' // optional
	});
});

var tom = {
	restart : function(){
		var $host = $("#tomId").val(), $port = $('input:radio[name="port"]:checked').val();
		
		if(!tomtool.checkIp($host)) {
			showMessage(2,"Illegal Ip!");
			return;
		}
		$("#right_head").html("");
		$("#right_head").append('<img src="/pages/img/loading.gif" />');
		$.ajax({
			url : '/cloud/business/ssh-tom',
			type : 'get', timeout : 800000,	cache : false,
			data : {"host" : $host, "port" : $port, ts:new Date().getTime()},
			dataType : 'text',
			success : function(data) {
				data = JSON.parse(data);
				$("#right_head").html("");
				if(data['shutdown'] == 0 && data['startup'] == 0){
					$("#right_head").append($host + " tomcat" + $port + " 重启成功！");
				}else{
					$("#right_head").append($host + " tomcat" + $port + " 重启失败！");
				}
			},
			error : function(errorThrown) {
				showMessage(3,errorThrown);
			}
		}); 
	}	
};

var tomtool = {
	checkIp : function(value){
		var illegal = true, values = value.split(".");
		if(values.length != 4) return false;
		for ( var i = 0; i < values.length; i++) {
			if(0 > values[i] || values[i] > 255)
				illegal = false;
		}
		return illegal;
	}
};