<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>国美投票结果和意见收集</title>

<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">

<style type="text/css">
<!--
.STYLE1 {
	font-family: "新宋体";
	font-size: 36px;
	font-weight: bold;
	color: #FFFFFF;
}

.STYLE2 {
	color: #CC0000
}

.STYLE3 {
	color: #FFFFFF
}
-->
</style>
</head>
<script type="text/javascript"
	src="http://www.webskys.com/skin/tomato/js/jquery.js"></script>
<style type="text/css">
* {
	margin: 0;
	padding: 0;
	list-style-type: none;
}

a,img {
	border: 0;
}

a {
	color: #333;
	text-decoration: none;
}
/*head*/
.votehead {
	margin: 20px auto;
	width: 800px;
	padding: 30px 0;
	background: #CC0000;
	height: 80px;
}

/* voteend */
.voteend {
	margin: 20px auto;
	width: 800px;
	height: 50px auto;
}

/* foot */国美搜索组感谢您的评价
.votefoot {
	margin: 70px auto;
	width: 800px;
	padding: 3px 0;
	background: #CC0000;
	height: 10px;
}

/* votebox */
.votebox {
	margin: 20px auto;
	width: 800px;
	border: solid 1px #ddd;
	padding: 30px 0;
}

.votebox h2 {
	font-size: 14px;
	color: #05345E;
	margin: 0;
	text-align: center;
}

.votebox ul {
	height: 18px;
	margin: 20px 0 0 200px;
}

.votebox li {
	height: 18px;
	line-height: 18px;
	overflow: hidden;
	float: left;
	margin: 0 20px 0 0;
	font-size: 12px;
}

.votebox li span {
	display: inline-block;
	float: left;
	width: 10px;
	height: 10px;
	overflow: hidden;
	background: url(../../pages/images/barbg.gif) repeat-x;
	margin: 3px 3px 0 0;
}

.votebox li span.barline-01 {
	border: 1px solid #C2142C;
	background-position: 0 0;
}

.votebox li span.barline-02 {
	border: 1px solid #EFA804;
	background-position: 0 -10px;
}

.votebox li span.barline-03 {
	border: 1px solid #0A83E6;
	background-position: 0 -20px;
}

/* barbox */
.barbox {
	height: 18px;
	line-height: 18px;
	overflow: hidden;
	padding: 20px 0 0 120px;
}

.barbox dt {
	float: left;
	font-size: 14px;
	width: 180px;
	text-align: right;
}

.barbox dt a {
	color: #0048CC;
}

.barbox dd {
	float: left;
}

.barbox dd.last {
	color: #999;
}

.barbox dd.barline {
	width: 160px;
	background: #E3E3E3;
	height: 12px;
	overflow: hidden;
	margin: 3px 10px 0 10px;
	display: inline;
	border-bottom: solid 1px #F4F4F4;
	border-right: solid 1px #F4F4F4;
}

.barbox dd.barline div.charts {
	height: 10px;
	overflow: hidden;
	background:url(../../pages/images/barbg.gif) repeat-x;
}

.barbox dd.barline div.charts.barred {
	background-position: 0 0;
	border: 1px solid #C2142C;
}

.barbox dd.barline div.charts.baryellow {
	background-position: 0 -10px;
	border: 1px solid #EFA804;
}

.barbox dd.barline div.charts.barblue {
	background-position: 0 -20px;
	border: 1px solid #0A83E6;
}

.STYLE4 {
	font-size: small
}
</style>

</head>

<body>
<div class="votehead">
  <p><a title="国美在线" href="http://www.gome.com.cn/">
    <img alt="国美在线" src="http://app.gomein.net.cn/images/ui/gomelogo.png">
    </a></p>
  <p align="center" class="STYLE1">国美搜索质量调查问卷 </p>
</div>

<div class="votebox">
	<h2>您怎么看国美在线的搜索质量？</h2>
	<ul>
		<li><span class="barline-01"></span>最大值</li>
		<li><span class="barline-02"></span>一般值</li>
		<li><span class="barline-03"></span>最小值</li>
	</ul>
	<dl class="barbox">
		<dt>满意，价格实在太高</dt>
  <dd class="barline">
			<div divindex="0" id="chartSlide_0" w="${percent1}" style="width:0px;" class="charts"></div>
	  </dd>
		<dd class="last">${percent1}%(${count1})</dd>
	</dl>
	<dl class="barbox">
		<dt>准确，第一页不是我想要的</dt>
  <dd class="barline">
			<div divindex="1" id="chartSlide_1" w="${percent2}" style="width:0px;" class="charts"></div>
	  </dd>
		<dd class="last">${percent2}%(${count2})</dd>

	</dl>
	
	<dl class="barbox">
		<dt>结果太少</dt>
  <dd class="barline">
			<div divindex="2" id="chartSlide_2" w="${percent3}" style="width:0px;" class="charts"></div>
	  </dd>
		<dd class="last">${percent3}%(${count3})</dd>
	</dl>
	
	<dl class="barbox">
		<dt>非常不准确</dt>
  <dd class="barline">
			<div divindex="3" id="chartSlide_2" w="${percent4}" style="width:0px;" class="charts"></div>
	  </dd>
		<dd class="last">${percent4}%(${count4})</dd>
	</dl>

	<dl class="barbox">
		<dt>简直是垃圾</dt>
  <dd class="barline">
			<div divindex="3" id="chartSlide_3" w="${percent5}" style="width:0px;" class="charts"></div>
	  </dd>
		<dd class="last">${percent5}%(${count5})</dd>
	</dl>
	
	
	
</div><!--votebox end-->
<script language="javascript">
function animate(){
	var max="barred";
	var middle="baryellow";
	var min="barblue";	
	
	var maxValue=0;
	var minValue=0;
	
	var maxIndex=0;
	var minIndex=0;
	
	$(".charts").each(function(i,item){
		var a=parseInt($(item).attr("w"));
	
		if(i==0){
			minValue=a;
			minIndex=i;
		}
	
		if(a>maxValue){
			maxValue=a;
			maxIndex=i;
		}else if(a<minValue){
			minValue=a;
			minIndex=i;
		}
	
	});
	
	$(".charts").each(function(i,item){
		var addStyle="";
		var divindex=parseInt($(item).attr("divindex"));
		if(divindex==maxIndex){
			addStyle=max;
		}else if(divindex==minIndex){
			addStyle=min;
		}else{
			addStyle=middle;
		}
	
		$(item).addClass(addStyle);
		var a=$(item).attr("w");
		$(item).animate({
			width: a+"%"
		},1000);
	});
	
}
animate();
</script>
<form name = "suggest" action="/cloud/business/edmServlet" method = "post">
<input type = "hidden" name = "showvote" value = "${vote}"/>
<input type = "hidden"   name = "uid" value = "${uuid}"/>
<input type = "hidden"  name = "login" value = "${loginname}"/>
<input  type = "hidden" name = "showquery" value = "${question}"/>
<h1 class="STYLE2" style="font-size:20px; text-align:center;">亲爱的国美会员,针对您的搜索结果请提供您宝贵的意见和建议</h1>
<div class="voteend">
<textarea name="newcontext" cols="55" rows="5" style="width: 799px; height: 154px;">  </textarea>
</div>
<p align = "center">注：请您尽量详细的描述您的搜索体验，比如搜索哪些关键词? 出现哪些问题?～</p>

<div align="center">
<input type = "Submit" value = "提交"  style="background-color: #CC0000 ;border: 1px solid #A80000;font-family: 微软雅黑;
    font-size: 18px; 
    height: 40px;
    width: 94px;background-position: 0 0;
    background-repeat: repeat repeat;" type="button"><span class="STYLE3">提交</span></button>
</div>
</form>

  <div align="center" class="STYLE3 STYLE4"><strong>copyright@国美搜索</strong></div>
	
</body>
</html>
