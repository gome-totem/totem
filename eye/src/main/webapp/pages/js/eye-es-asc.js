var esOperation={
		esAscCache:function(ip,port,catId){
			$.ajax({
				url:"/cloud/eye/esAsc",
				data:"ip="+ip+"&port="+port+"&catId="+catId,
				dataType:'json',
				beforeSend :function(){
					$.messager.progress({
						text:'同步数据中...',
					});
				},
				success:function(data){
					$.messager.progress('close');
					if(data.result=="error"){
						$.messager.alert('tips', '同步程序内部出现异常！请联系管理员', 'error');
					}else if(data.result=="timeout"){
						$.messager.alert('tips', '同步地址连接超时，请确保es地址正确！', 'error');	
					}else if(data.result=="404"){
						$.messager.alert('tips', '同步地址连接不存在！请检查地址或端口', 'error');
					}else if(data.result=='nohost'){
						$.messager.alert('tips', '同步IP主机不存在！请检查地址或端口', 'error');
					}
					else{
						esOperation.dealResultData(data.result);
					}
				}
			});
		},
		dealResultData:function(result){
			var resultJson=eval("("+result+")");
			var errorArray=new Array();
			var data=resultJson.data;
			for ( var i = 0,len1=data.length; i < len1; i++) {
				var tempArray=data[i];
				for(var j=0,len2=tempArray.length;j<len2;j++){
					var tempObj=tempArray[j];
					if(tempObj.status!="successful"){
						errorArray.push(tempObj.nodeAddr);
					}
				}
			}
			if(errorArray.length>0){
				var error=errorArray.toString();
				$.messager.alert('tips', '<font color="red">同步失败,es服务地址为：'+error+'  ,请重新同步</font>', 'error');
			}else{
				$.messager.alert('tips', '同步成功！', 'info');
			}
		} 
}

$(function(){
		var esData=eval("("+$("#esServer").val()+")"); 
		$("#ascIp").combobox({
			valueField:'key',
		textField:'value',
		data:esData
		});
		$('#ascIp').combobox().next('span').find('input').focus(function(){
			if($(this).val()=='请选择或输入ip'){
				$(this).val('');
			}
		});
		$('#ascIp').combobox().next('span').find('input').blur(function(){
			if($(this).val()==''){
				$(this).val('请选择或输入ip');
			}
			
		});
		$("#ascIp").combobox("setText","请选择或输入ip");
		$("#combo-text validatebox-text").focus(function(){
			$(this).val('');
		});
		$("#ascCate").combobox({
			onSelect:function(record){
				if(record.value=="catid"){
					$("#catid").show();
				}else{
					$("#catid").hide();
				}
				$("#ascCatd").val('');
			}
		});
		$("#ascButton").click(function(){
			var ip=$("#ascIp").combobox("getText");
			var port=$("#ascPort").val();
			var catid=$("#ascCatd").val();
			if(ip==null || ip=="" || ip=="请选择或输入ip"){
				$.messager.alert('tips', '同步地址不能为空！', 'error');
				return;
			}
			esOperation.esAscCache(ip, port, catid);
		});
		$("#ascData").click(function(){
			var url=$("#ascUrl").val();
			url=url.trim();
			if(url=="" || url.indexOf("http://")!=0){
				$.messager.alert('tips', '请检查您的URL，必须包含http头信息！', 'error');
				return;
			}
			$.ajax({
				url:'/cloud/eye/ascCacheData',
				data:'url='+url,
				dataType:'json',
				beforeSend :function(){
					$.messager.progress({
						text:'同步数据中...',
					});
				},
				success:function(data){
					$.messager.progress('close');
					if(data.result=="error"){
						$.messager.alert('tips', '程序内部出现异常！请联系管理员', 'error');
					}else if(data.result=="timeout"){
						$.messager.alert('tips', 'URL地址连接超时，请确保地址正确！', 'error');	
					}else if(data.result=="404"){
						$.messager.alert('tips', 'URL地址连接不存在404！请检查地址', 'error');
					}else if(data.result=='nohost'){
						$.messager.alert('tips', 'URL主机域名不存在！请检查地址', 'error');
					}else{
						$('#topWindow').dialog({
							   title: '同步结果',
							   width: 550,
							   height: 400,
							   closed: false,
							   cache: false,
							   modal: true
						});
						$('#topWindow .panel-body').html(data.result);
					}
				}
			});
		});
	});