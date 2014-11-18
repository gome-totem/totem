 	var page =1;
   	var timeout;
   	var width=1000;
   	$(function(){
   		width=$("#incrementIndexLogs").parent().parent().width()*0.7;
   		searchIndexLog();
    });
    
   function searchIndexLog(){
		    $('#incrementIndexLogs').datagrid({
		        url:'/cloud/business/incrementindex',
		        rownumbers: true,
		        width:width,
		        height:320,
		        method:'get',
		        pagination:true,
		        pageList:[10,20,30],
		        idField: 'id', 
		        singleSelect:true,
		        columns:[[
		        {field:'type',title:'类型',width:150,align:'center'},
		        {field:'id',title:'id',width:150,align:'center'},
		        {field:'addTime',title:'添加时间',width:200,align:'center'},
		        {field:'executeTime',title:'执行时间',width:200,align:'center'},
		        {field:'successTime',title:'成功时间',width:200,align:'center'},
		        {field:'status',title:'状态',width:130,align:'center',formatter:formatStatus}
		        ]]
		     });
		    $(".datagrid").width(width+5);
   }
   function formatStatus(status){
    		if(status==0){
			  	return "<font color=\"red\">已添加</font>";
			}else if(status==1){
			  	return "<font color=\"blue\">已执行</font>";
			}else if(status==2){
			  	return "<font color=\"green\">已成功</font>";
		   }
    }
   function formatTime(time){
	   if(time==null || time==''){
		   return "";
	   }
		return time.substring(0,19);
   }
    function executeIncrementIndex(){
    		$.ajax({
			  type: "POST",
			  url: "/cloud/business/incrementindex",
			  data: {"method":"executeIncrementIndex"},
			  success: function(result){
			  	var data =eval ("(" + result + ")");
			  	if(data.status){
			  	$('#incrementIndexLogs').datagrid("reload");
			  	}else{
			  		alert(data.msg);
			  	}
			  },
			  error:function(result){
				  alert("程序内部异常，请联系管理员");
			  }
			});
    }
    
    function addIncrementIndex(){
    		var type = $("#type").val();
    		var id = $("#id").val();
    		if(id==null||id==""){
    			alert("id不能够为空");
    			return;
    		}
    		$.ajax({
			  type: "POST",
			  url: "/cloud/business/incrementindex",
			  data: {"type":type,"id":id,"method":"addIncrementIndex"},
			  success: function(result){
				  $('#incrementIndexLogs').datagrid("reload");
			  },
			  error:function(result){
				  alert("程序内部异常，请联系管理员");
			  }
			});
			$("#id").val("");
    }

