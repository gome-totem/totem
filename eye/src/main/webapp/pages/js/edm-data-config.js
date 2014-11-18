


$(function(){
	$('#sendcondition').combobox({
		formatter: function(row){
			//console.info(row);
			return "触发类型为："+row.actionname+"--用户在"+row.time+"分钟--点击总次数为"+row.totalclicktimes+"次--点击关键词或cat次数为"+row.clickuptimes+"次--点击列表次数为"+row.clickdowntimes+"--分类id为"+row.catid;
		}
	});
});
var dictData = {
		showDataList:function(value){
			var width=($('#addPanel').width()-50)/8;
		    $('#dataList').datagrid({
		        url:'/eye/data/edmPlat?flag=4',
		        title:'信息列表',
		        height:600,
		        rownumbers: true,
		        idField: 'text1', 
		        singleSelect:true,
		        columns:[[
		        {field:'systemPara',title:'系统参数',width:width,align:'center'},
		        {field:'mailtemplate',title:'邮件模板',width:width,align:'center',formatter:formatTemplate},
		        {field:'paramrange',title:'邮件参数',width:width,align:'center'},
		        {field:'sendcondition',title:'发送条件',width:width,align:'center',formatter:formatCondition},
		        {field:'timerange',title:'时间段',width:width,align:'center',formatter:formatTime},
		        {field:'gender',title:'性别',width:width,align:'center',formatter:formatGender},
		        {field:'age',title:'年龄段',width:width,align:'center',formatter:formatAge},
		        {field:'select9',title:'操作',width:200,align:'center',formatter:formatOperation}
		        ]]
		     });
		},
		update:function(){
			//alert("更新操作");
			
			$('#dataList').datagrid('reload');
		},
		del:function(id){
			 var param="flag=5&delId="+id;
			 this.excuteOpt(param);
			$('#dataList').datagrid('reload');
			$('#delete1').combobox('reload');
			$('#delete2').combobox('reload');
		},
		
		delData:function(select){
			var delId = null;
			var flag = null;
			if(select=="1"){
				 delId = $('#delete1').combobox("getValues");
				 flag = 61;
			}
			if(select=="2"){
				 delId = $('#delete2').combobox("getValues");
				
				 flag = 62;

			}
			
			 var param="flag="+flag+"&delId="+delId;
			 this.excuteOpt(param);
			 setTimeout(function(){
					$('#dataList').datagrid('reload');
					$('#delete1').combobox('reload');
					$('#delete2').combobox('reload');
					$('#delete1').combobox('select','');
					$('#delete2').combobox('select','');
			 }, 1000);
		
		},
		add:function(){
			var systemPara = $('#systemPara').val();
			var mailtemplate = $('#mailtemplate').combobox("getValue"); 
			var paramrange = $('#paramrange').val();
			var sendcondition = $('#sendcondition').combobox("getValues"); 
		   var timerange = $('#timerange').combobox("getValues"); 
			var gender = $('#gender').val();
			var age = $('#age').combobox("getValues"); 
			if(systemPara =="" || mailtemplate==""|| paramrange==""
				|| sendcondition==""|| timerange==""
				|| gender==""||age==""){
				alert("均不能为空");
				return;
			}
			$('#addForm').form('submit', {
			   success: function(val){
				   var dat=eval('('+val+')');
				   if(dat.result=="success"){
					   alert("添加数据成功");
				   }
				   if(dat.result=="error"){
					   	//console.info(result);
					   alert("失败,数据重复，请重新添加！");
				   }
				  }   
			 });

			$('#dataList').datagrid('reload');
		},
		addData:function(value){
			switch (value) {
			case 1:
				var cid = $('#campaignId').val();
				var mailingid= $('#mailingId').val();
				var templatename= $('#template').val();
				if(cid !="" && mailingid !="" && templatename !=""){
					 var param="cid="+cid+"&mailingid="+mailingid+"&templatename="+templatename+"&flag=11";
					 this.excuteOpt(param);
				}else{
					alert("均不能为空");
				}
				break;
			case 2:
				var comname = $('#comname').val();
				var clickuptimes = $('#clickuptimes').val();
				var clickdowntimes = $('#clickdowntimes').val();
				var totalclicktimes = $('#totalclicktimes').val();
				var time = $('#time').val();
				var catid = $('#catid').val();
				if(comname !=""&&totalclicktimes !=""&&time !=""&&catid !=""&&clickuptimes!=""&&clickdowntimes!=""){
					var param ="flag=12" +"&comname="+comname+"&totalclicktimes="+totalclicktimes+"&clickuptimes="+clickuptimes+"&clickdowntimes="+clickdowntimes+"&time="+time+"&catid="+catid;
					this.excuteOpt(param);
				}
				else{
					alert("均不能为空");
				}

				break;
			case 3:
				var ldmailingid = $('#ldmailingid').val();
				var ldsendplanid= $('#ldsendplanid').val();
				var ldtemplatename= $('#ldtemplatename').val();
				if(ldmailingid !="" && ldsendplanid !="" && ldtemplatename !=""){
					 var param="ldmailingid="+ldmailingid+"&ldsendplanid="+ldsendplanid+"&ldtemplatename="+ldtemplatename+"&flag=13";
					 this.excuteOpt(param);
				}else{
					alert("均不能为空");
				}
				break;
			default:
				break;
			}
			setTimeout(function(){
				$(".easyui-combobox").combobox("reload");
				$('#delete1').combobox('reload');
				$('#delete2').combobox('reload');
				$('#delete1').combobox('select','');
				$('#delete2').combobox('select','');
			}, 1000);
			
		},
		excuteOpt:function(param){
			$.ajax({
				url:'/eye/data/edmPlat',
				data:param,
				success:function(data){
					var dat=eval('('+data+')');
					if(dat.result=="error"){
					   	alert("失败,数据重复，请重新添加！");
				   }else if(dat.result=="success"){
						alert("success");
					}else{
						alert("失败，data已经存在，请重新添加！");
					}					
				}	
			});
		},
	
};

function formatTime(value,row,index){
	if(value=='0'){
		return "0-6";
	}
	if(value=="1"){
		return "6-12";
	}
	if(value=="2"){
		return "12-18";
	}
	if(value=="3"){
		return "18-24";
	}
	if(value=="4"){
		return "全天";
	}
}

function formatTemplate(value,row,index){
	
	if(value == null)
		return;
	var result = null;
	 $.ajax({
		type: 'GET',
		url: '/eye/data/edmPlat?flag=7&key='+value,
		dataType: 'json',
		async:false, 
		success: function(data) {
			result = data.templatename; 
		}
	});
	return result;
}

function formatCondition(value,row,index){
	if(value == null)
		return;
	var result = null;
	 $.ajax({
		type: 'GET',
		url: '/eye/data/edmPlat?flag=8&key='+value,
		dataType: 'json',
		async:false, 
		success: function(data) {
			result ="触发类型为："+ data.actionname+"--"+data.time+"分钟--点击总次数："+data.totalclicktimes+"--关键词点击次数:"+data.clickuptimes+"--列表点击次数:"+data.clickdowntimes+"次--catid："+data.catid; 
		}
	});
	return result;
	
}

function formatAge(value,row,index){
	if(value=='0'){
		return "0-20";
	}
	if(value=="1"){
		return "20-40";
	}
	if(value=="2"){
		return "40-60";
	}
	if(value=="3"){
		return "60以上";
	}
	if(value=="4"){
		return "全部";
	}
}

function formatGender(value,row,index){
	if(value=='0'){
		return "男";
	}
	if(value=="1"){
		return "女";
	}
	if(value=="2"){
		return "全部";
	}
}

function formatOperation(value,row,index){
	var v=row._id.$oid;
	return "<a href=\"javascript:dictData.del('"+v+"');\">删除</a>";
}