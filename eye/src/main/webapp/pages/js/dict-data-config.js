var dictData = {
		showServerList:function(value){
			$("#queryCondition").show();
			var width=$('#datagrid').width()-530;
			var queryType=$('#select1').combobox('getValue');
			var envCode=$('#select2').combobox('getValue');
			var keyword=$('#keywords').val();
		    $('#dict_data').datagrid({
		        url:'/eye/data/dictConfig',
		        queryParams: {
		    		dataType: 'server',
		    		queryType:queryType,
		    		envCode:envCode,
		    		keyword:keyword
		    	},
		        height:600,
		        rownumbers: true,
		        idField: 'ip_address', 
		        singleSelect:true,
		        pagination:true,
		        columns:[[
		        {field:'ip_address',title:'IP地址',width:100,align:'center'},
		        {field:'service',title:'服务列表',width:width,align:'center',formatter:formatService},
		        {field:'msgStatus',title:'短信状态',width:50,align:'center',formatter:formatStatus},
		        {field:'update_time',title:'操作时间',width:120,align:'center'},
		        {field:'env_code',title:'部署环境',width:100,align:'center',formatter:formatEnv},
		        {field:'remark',title:'操作',width:100,align:'center',formatter:formatOperation}
		        ]]
		     });
		    $("#queryBasic").hide();$("#queryServer").show();	$(".datagrid-toolbar").hide();
		},
		showKeywordsFilterList:function(value){
			$("#queryCondition").hide();
			var width=$('#datagrid').width()-530;
			 var toolbar = [
			    {text:'新增',
				 iconCls:'icon-add',
				 handler:function(){filter.addItem()}
				 },'-',
				{text:'保存',
				  iconCls:'icon-ok',
				  handler:function(){filter.saveItem()}
			     },'-',
//			    {text:'初始化数据',
//				 iconCls:'icon-tip',
//				 handler:function(){filter.initItem()}
//				 },'-',
				{text:'同步数据',
				iconCls:'icon-reload',
				handler:function(){filter.ascItem()}
				}];
		    $('#dict_data').datagrid({
		        url:'/eye/data/dictConfig',
		        queryParams: {
		    		dataType: 'keywordsFilter',
		    	},
		        height:600,
		        rownumbers: true,
		        idField: 'filterWord', 
		        toolbar:toolbar,
		        singleSelect:true,
		        pagination:true,
		        pageList:[20,50,100],
		        loadFilter:pagerFilter,
		        columns:[[
		  		    {field:'filterWord',title:'关键词',width:200,align:'center',editor:'text'},
		  		    {field:'url',title:'跳转地址',width:width,align:'center',editor:'text'},
		  		    {field:'update_time',title:'操作时间',width:150,align:'center'},
		  		    {field:'remark',title:'操作',width:100,align:'center',formatter:formatOperation}
		  		 ]]
		     });
			$(".datagrid-toolbar").show();
		},
		showBasicList:function(value){
			$("#queryCondition").show();
			var width=$('#datagrid').width()-530;
		    $('#dict_data').datagrid({
		        url:'/eye/data/dictConfig',
		        queryParams: {
		    		dataType: 'basic'
		    	},
		        height:600,
		        rownumbers: true,
		        idField: 'key', 
		        singleSelect:true,
		        pagination:true,
		        columns:[[
		        {field:'key',title:'key值',width:100,align:'center'},
		        {field:'value',title:'value值',width:width,align:'center'},
		        {field:'status',title:'状态',width:50,align:'center'},
		        {field:'update_time',title:'操作时间',width:120,align:'center'},
		        {field:'env_code',title:'部署环境',width:100,align:'center'},
		        {field:'remark',title:'操作',width:100,align:'center',formatter:formatOperation}
		        ]]
		     });
			$("#queryBasic").show();$("#queryServer").hide();	$(".datagrid-toolbar").hide();
		},
		add:function(value){
			var url="";
			if(value=='server'){
				url="/dispacher/addServer";
			}else if(value=='basic'){
				url="/dispacher/addBasic";
			}
			$('#topWindow').dialog({
			   title: '新增',
			   width: 450,
			   height: 300,
			   closed: false,
			   cache: false,
			   href: url,
			   modal: true
			});
		},
		addEecute:function(value){
			
		   $('#ff').form('submit', {
		      success: function(val){
		    	  var dat= eval('(' + val + ')');
		    	  if(dat.data=='success'){
		    		  $.messager.alert('tips', '添加成功', 'info');
		    	  }else{
		    		  $.messager.alert('tips', '添加失败', 'info');
		    	  }
		       }
		    });
		   $('#dict_data').datagrid('reload');
		   $('#ff').form('clear');
		},
		query:function(id,value){
			if(value=='server'){
				dictData.showServerList(value);
			}else if(value=='basic'){

			}
		},
		update:function(id,value){
			var url="";
			if(value=='server'){
				url="/dispacher/updateServer";
			}else if(value=='basic'){
				url="/dispacher/updateBasic";
			}
			$('#topWindow').dialog({
			   title: '编辑',
			   width: 450,
			   height: 300,
			   closed: false,
			   cache: false,
			   href: url+"?ip_address="+id,
			   modal: true
			});
		},
		updateEecute:function(id,value){
			$('#ff').form('submit', {
			      success: function(val){
			    	  var dat= eval('(' + val + ')');
			    	  if(dat.data=='success'){
			    		  $.messager.alert('tips', '更新成功', 'info');
			    		  $('#dict_data').datagrid('reload');
			    	  }else{
			    		  $.messager.alert('tips', '更新失败', 'info');
			    	  }
			       }
			});
			 
		},
		show:function(ip,value,type){
			var url="";
			if(type=='server'){
				url="/eye/data/showServer";
			}else if(type=='basic'){
				url="/eye/data/showServer";
			}
			$('#topWindow').dialog({
			   title: '详细',
			   width: 650,
			   height: 300,
			   closed: false,
			   cache: false,
			   href: url+"?ip_address="+ip+"&tag="+value,
			   modal: true
			});
		},
		del:function(id,value){
			$.messager.confirm('删除','您确定要删除此项?',function(r){
				if (r){
					if(value=='server'){
						$.ajax({
							   type: "POST",
							   url: "/eye/data/delServer",
							   data: {"ip_address":id},
							   success: function(msg){
								   var dat= eval('(' + msg + ')');
								   if(dat.data=='success'){
							    		  //$.messager.alert('tips', '删除成功', 'info');
							    	  }else{
							    		  //$.messager.alert('tips', '删除失败', 'info');
							    	}
								   $('#dict_data').datagrid('reload');
							   }
						});
					}else if(value=='basic'){
					}
				}
			});
		}
};
var editIndex = undefined;
function endEditing(){
	return true;
}
var filter={
	addItem:function(){
		var time = new Date().Format("yyyy-MM-dd hh:mm:ss");
		 if (endEditing()){
			 $('#dict_data').datagrid('appendRow',{update_time:time,remark:'请保存'});
			 editIndex = $('#dict_data').datagrid('getRows').length-1;
			 $('#dict_data').datagrid('selectRow', editIndex).datagrid('beginEdit', editIndex);
		}
	},
	saveItem:function(){
		var data =$('#dict_data').datagrid('getData').originalRows;
		if (endEditing()){
			var rows = $('#dict_data').datagrid('getChanges');
			$('#dict_data').datagrid('acceptChanges');
			var filter="";
			if(rows!=null && rows!=""){
				for ( var r in rows) {
						if(rows[r].filterWord=="" || rows[r].url==""){
							break;
						}else{
							filter+=rows[r].filterWord+"@"+rows[r].url+"#";
						}
						
				}
			}
			if(filter==""){
				$('#dict_data').datagrid('reload');
				$.messager.alert('tips', '请填写有效数据', 'info');
				return;
			}
			$.ajax({
				   type: "POST",
				   url: "/eye/data/addFilter",
				   data: {"data":filter},
				   success: function(msg){
					   var dat= eval('(' + msg + ')');
					   if(dat.data=='success'){
				    		  $.messager.alert('tips', '添加成功', 'info');
				    	  }else{
				    		  $.messager.alert('tips', '添加失败，请重新添加', 'info');
				    	}
					   $('#dict_data').datagrid('reload');
				   }
			});
		}
	},
	initItem:function(){
		$.messager.confirm('初始化数据','初始化会恢复默认数据，请谨慎操作！',function(r){
			if (r){
				$.ajax({
					   type: "POST",
					   url: "/eye/data/initFilter",
					   success: function(msg){
						   var dat= eval('(' + msg + ')');
						   if(dat.data=='success'){
					    		  $.messager.alert('tips', '初始化成功', 'info');
					    	  }else{
					    		  $.messager.alert('tips', '初始化失败，请重试', 'info');
					    	}
						   $('#dict_data').datagrid('reload');
					   }
				});
			}
		})
	},
	delItem:function(value){
		$.messager.confirm('刪除','您確定要刪除此項？',function(r){
			if (r){
				$.ajax({
					   type: "POST",
					   url: "/eye/data/delFilter",
					   data:"keyword="+value,
					   success: function(msg){
						   var dat= eval('(' + msg + ')');
						   if(dat.data=='success'){
					    		  //$.messager.alert('tips', '刪除成功', 'info');
					    	  }else{
					    		  //$.messager.alert('tips', '刪除失败，请重试', 'info');
					    	}
						   $('#dict_data').datagrid('reload');
					   }
				});
			}
		});
	},
	ascItem:function(){	
		$.messager.confirm('同步数据','您需要同步数据？',function(r){
			if (r){
				 $('#topWindow').dialog({
					   title: '同步数据进程',
					   width: 320,
					   height: 450,
					   closed: false,
					   cache: false,
					   modal: true
				 });
				 $('#topWindow .panel-body').html('<div id="load" align="center"><img src="/pages/images/load.gif" style="margin-top: 30px;"></div>');
				$.ajax({
					   type: "POST",
					   url: "/eye/data/ascFilter",
					   success: function(data){
						  var temp=eval("("+data+")");
						  var msgs=temp.msg;
						  $('#topWindow .panel-body').html("");
						  console.info(msgs.length);
						  if(msgs.length>0){
							  for(var i in msgs){
								  dealData(msgs[i]);
							  }
						  }	   
					   }
				});
			}
		});	
	}
};
$(function(){
	setTimeout(function(){
		$("#tree11").click();
		showContent(11);
	}, 400);
});
function showContent(value){
	if(value==11){
		dictData.showServerList(value);
	}else if(value==12){
		dictData.showBasicList(value);
	}else if(value=13){
		dictData.showKeywordsFilterList(value);
	}
}
function formatOperation(value,row,index){
	if(row.remark=="请保存"){
		return "<font color='red'>请保存</font>";
	}
	if(row.filterWord!=null && row.filterWord!="" && row.filterWord!="undefined"){
		return "&nbsp;&nbsp<a href='javascript:filter.delItem(\""+row.filterWord+"\",\"filter\")'>删除</a>";
	}
	return "<a href='javascript:dictData.update(\""+row.ip_address+"\",\"server\");'>编辑</a>&nbsp;&nbsp<a href='javascript:dictData.del(\""+row.ip_address+"\",\"server\")'>删除</a>";
}

function formatService(value,row,index){
	var result="";
	for ( var service in row.service) {
		result+="<a href='javascript:dictData.show(\""+row.ip_address+"\",\""+row.service[service]+"\",\"server\");'>"+row.service[service]+"</a>&nbsp;&nbsp;";
	}
	return result;
}

function formatStatus(value,row,index){
	if(row.msgStatus=='1'){
		return "启用";
	}else if(row.msgStatus=='0'){
		return "禁用";
	}
}
function formatEnv(value,row,index){
	var data=row.env_code.split("_")[0];
	var result="";
	if(data=='10000'){
		result="生产环境_"+row.env_code.split("_")[1];
	}else if(data=='10001'){
		result="测试环境_"+row.env_code.split("_")[1];
	}else if(data=='10002'){
		result="开发环境_"+row.env_code.split("_")[1];
	}
	return result;
}

function checkData(value){
	var str=$("input[name='ip_address']").val();
	if(str!=""){
		$.ajax({
			url: "/eye/data/checkData",
			data: "data="+str+"&type=ip",
			success: function(msg){
				 var dat= eval('(' + msg + ')');
				 if(dat.data=='success'){
					$.messager.alert('tips', 'ip地址已配置', 'error');
					$("input[name='ip_address']").val("");
				}
			}
		});
	}
	
}
function dealData(oMsg){
	var ip=oMsg["ip"],port=oMsg["port"],status=oMsg["status"],result=null;
	if(status!=200){
		result="<font color='red'>失败</font>";
	}else {
		result="成功";
	}
	
	if(oMsg["ip"]=='0.0.0.0'){
		$('#topWindow .panel-body').append("<div style='margin-top=15px;'>同步完成</div>");
	}else{
		$('#topWindow .panel-body').append("<div style='margin-top=10px;'><span>同步IP:"+ip+"</span><span>&nbsp;&nbsp;&nbsp;&nbsp;端口:"+port+"</span><span>&nbsp;&nbsp;&nbsp;&nbsp;结果:"+result+"</span>");
	}
	
}
Date.prototype.Format = function(fmt) 
{
		var o = { 
				"M+" : this.getMonth()+1,                 // 月份
				"d+" : this.getDate(),                    // 日
				"h+" : this.getHours(),                   // 小时
				"m+" : this.getMinutes(),                 // 分
				"s+" : this.getSeconds(),                 // 秒
				"q+" : Math.floor((this.getMonth()+3)/3), // 季度
				"S"  : this.getMilliseconds()             // 毫秒
		}; 
		if(/(y+)/.test(fmt)) 
			fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length)); 
		for(var k in o) 
			if(new RegExp("("+ k +")").test(fmt)) 
				fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length))); 
		return fmt; 
};

	function pagerFilter(data){
		if (typeof data.length == 'number' && typeof data.splice == 'function'){
			data = {
					total: data.length,
					rows: data
			}
		}
		var dg = $(this);
		var opts = dg.datagrid('options');
		var pager = dg.datagrid('getPager');
		pager.pagination({
			onSelectPage:function(pageNum, pageSize){
				opts.pageNumber = pageNum;
				opts.pageSize = pageSize;
				pager.pagination('refresh',{
					pageNumber:pageNum,
					pageSize:pageSize
				});
				dg.datagrid('loadData',data);
			}
		});
		if (!data.originalRows){
			data.originalRows = (data.rows);
		}
		var start = (opts.pageNumber-1)*parseInt(opts.pageSize);
		var end = start + parseInt(opts.pageSize);
		data.rows = (data.originalRows.slice(start, end));
		return data;
	}