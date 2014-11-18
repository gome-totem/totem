/* 详情页主信息私有命名空间 */
;
var prdInfoMain = function(){
		/* 通用g域名 */
		this.dynUrl=dynSite+contextPath;
		/* 通用www域名 */
		this.staUrl=staSite+contextPath;
		/* 登录状态：用于需要异步请求并且判断登录的操作 true未登录 false已登录*/
		this.lgStatus=true;
		/* 弹出层对象 */
		this.dgObj=$("#innerBox");
		/* 商品价格 */
		this.price="暂无售价";
		/* 一级城市名称 */
		this.city="北京";
		/* 促销语 */
		this.desc="";
		/* 检测预售状态 默认为0  说明 0-未查询预售信息  1已查询且非预售商品 2已查询且是预售商品 */
		this.chkPre=0;
		/* 预售ID */
		this.itemId=0;
	};
	prdInfoMain.prototype = {
		
		/* 初始化请求事件、绑定事件 */
		bind:function(){
			var _this = this;
			_this.getInfo();
			_this.getStock();
			_this.getGroup();
			_this.getPrdStats();
			
			/* 欢迎纠错 */
			$(".j-corr").click(function(){
				_this.login(function(){
					if(!_this.lgStatus)window.open(_this.staUrl+"/n/product/browse/productErroradd.jsp?productId="+prdInfo.prdId);
				});
			});
			
			/* 多少人评价跳转至评价模块 */
			$("#goappr").click(function(){
				var _h = $("#prd_tbox").offset().top-20;
				$("#prd_tbox").children().eq(2).click();
				$('html,body').animate({scrollTop:_h+'px'},200);
			});
		},
		/* 初始化请求事件、绑定事件结束 */
		
		/*商品状态 上架与下架操作  1上架 2下架*/
		getPrdStats:function(){
			var _this = this;
			if(prdInfo.prdType==1){
				/* 降价通知、到货通知 */
				$("#reduce,#arrival,#anotic").click(function(){
					var _id = $(this).attr("id");
					_this.login(function(){
						if(!_this.lgStatus){
							_this.dialog({inner:"#dialogTZ",cssname:"dialogBox TZ",callback:function(){
								if(_id=="reduce"){
									_this.dgObj.find(".tit").html("降价通知");
									_this.dgObj.find(".txt i").html("降价");
								}
								_this.dgObj.find(".redlink").click(function(){
									_this.addNotic(_id);
								});
								_this.dgObj.find("#mail").focus();
								_this.dgObj.find("#mail,#tell").keydown(function(e){
									if(e.keyCode==13)_this.addNotic(_id);
								})
							}});
						}
					});
				});
				/* 收藏商品 */
				$("#collect").click(function(){_this.collect();});
			}
			if(prdInfo.prdType==2){
				//下架事件在无货 stockUbind 中处理
			}
		},
		
		/* 获取商品促销信息 */
		getInfo:function(){
			var _this = this,_url=_this.staUrl+"/n/product/browse/getProductDetail.jsp",
				_dats = {productId:prdInfo.prdId,skuId:prdInfo.sku,outOfstock:false};
				_this.getAjax({Jname:"fndetail",Jurl:_url,Jdat:_dats,Jcall:_this.getInfoDom});
		},
		getInfoDom:function(){
			if(this.data!=undefined){
				var _dt = this.data,_this = this.obj;
				
				/* 显示促销语 */
				if(_dt.des)$(".prdtitcx").html(_dt.des).show();
				_this.desc=_dt.des;
				
				/* 好评度 好评人数 */
				if(_dt.comments){
					if(_dt.comments>0)$("#pincnt").html(_dt.comments);$("#haocnt").html((_dt.goodCommentPercent*100)+"%");
					if(_dt.comments==0)$("#goappr,#haocnt,.haopin").hide();
					
					/* 同步用户评价条数 只做请求 */
					$.ajax({
						type:"get",url: staSite+"/p/json?module=infPLS&productId="+prdInfo.prdId+"&pls="+_dt.comments,
						dataType:"html",success:function(data){}
					});
					/* 同步用户评价条数 结束 */
				}
				
				/*红券、蓝券、直降、返券等促销信息*/
				var _pcx = $(".prdcxtxt");
				
				if(_dt.promotion.flag=="true"){
					var _pm = _dt.promotion.proms,_tx = "";
					for(var i=0; i<_pm.length; i++){
						var _tp = _pm[i].type;
						var _ds = _pm[i].desc;
						if(_tp!=undefined && _ds!=undefined){
							if(_tp == "直降")_ds='已优惠<b>'+_ds+'</b>元';
							_tx += '<div class="prdcxbox"><span class="prdcxtit">'+ _tp +'</span><span>'+_ds+'</span></div>';
						}
					}
					_pcx.append(_tx).show();
				}
				
				/*赠品信息*/
				if(_dt.gift && _dt.gift.length>0){
					var _gtxt = '<div class="prdcxbox"><span class="prdcxtit">赠品</span>';
					$(_dt.gift).each(function(i){
						var _sty = '';
						if(i>0)_sty = ' class="pedcxbr"';
						_gtxt+='<span'+_sty+'>'+this+'</span>';
					});
					_pcx.append(_gtxt+'</div>').show();
				}
			}
		},
		
		
		/* 获取库存信息 */
		getStock:function(){
			/* 读取cookie信息 */
			var _this = this,_ck = $.cookie('atgregion'),_ct=$.cookie('atgMboxCity');
			/*默认三级海淀区的区号*/
			if(!_ck)_ck = "11010500|北京北京市海淀区(五环里)|11010000|11000000";
			_this.city = _ct||"北京";
			var _arry =_ck.split("|"),_zId="11010000";
				if(_arry[2]){_zId=_arry[2];}
			var _sdat = {"goodsNo":prdInfo.skuNo,"city":_arry[0],"siteId_p":prdInfo.siteId,"skuType_p":prdInfo.skuType,"shelfCtgy3":prdInfo.shelf,"zoneId":_zId,"sid":prdInfo.sku},
				_url=_this.dynUrl+"/browse/exactMethod.jsp";
				$("#stockaddress").html(_arry[1]);
				/* 2是预售商品 */
				if(_this.chkPre==2)return false;
				if(_this.chkPre==0)_sdat["checkPresell"]=true;
				_this.getAjax({Jname:"exact",Jurl:_url,Jdat:_sdat,Jcall:_this.getStockDom});
		},
		getStockDom:function(){
			if(this.data!=undefined){
				var _dat = this.data,_this=this.obj;
					_this.price=_dat.price;
					$("#prdPrice,#miniPrice").html("¥"+_dat.price);
					/* 判断预售商品 */
					if(_this.chkPre==0)_this.chkPre=1;
					if(_dat.preflag && (_dat.preflag=="true" || _dat.preflag==true)){
						_this.chkPre=2;
						_this.preSellDom(_dat);
						return false;
					}
					/* 判断预售商品  结束 */
					if(prdInfo.prdType==1){
						/*有货*/
						if(_dat.result=="Y"){
							if(_this.price>=300 && prdInfo.skuDat!="0元购机"){$("#stages").show();}
							_this.stockBind();
						};
						/*无货*/
						if(_dat.result=="N"){_this.stockUbind();$("#stages").hide();}
					}
					/* 下架 */
					if(prdInfo.prdType==2){_this.stockUbind();}
			}
		},
		/* 只在有货时绑定的事件 */
		stockBind:function(){
			var _this = this;
			/* 负卖状态 */
			if(prdInfo.selltype=="2"){
				
			}
			$(".prdstock").html("有货");
			if(prdInfo.skuDat!="0元购机"){$("#enterQty").attr("disabled",false);$("#btnCount").children().removeClass("disab");}
			if(prdInfo.skuDat=="0元购机"){$("#enterQty").attr("disabled",true);$("#btnCount").children().addClass("disab");$("#addCartLink").html("立即购买");}
			$("#btnLink").removeClass("disab");
			$("#txtChoose").removeClass("nostock");
			$("#txtNostock,#arrival,#similarDom,.prdstocktxt").hide();
			createMbox(1,_this.city,_this.price,_this.desc);
			$("#mboxImported-default-Product_Top_Center-0").hide();
			
			/* 绑定购物车事件 */
			_this.bindCart();
			
			/*分期付款*/
			$("#stages").click(function(){
				_this.dialog({inner:"#dialogFQ",unlock:true,cssname:"dialogBox FQ",callback:function(){
					_this.dgObj.find(".redlink").click(function(){_this.addCart();});
				}});
			});
		},
		/* 无货时需要移除的事件 此处需要判断是否为下架 */
		stockUbind:function(){
			var _stk = "无货";
			if(prdInfo.prdType==2){
				_stk="下架";
				$("#txtNostock").html("该商品已下架，欢迎选购其他类似商品。");
				$("#arrival").html("商品下架");
				$(".prdstocktxt").html("该商品已下架");
				$("#btnLink").addClass("off");
			}
			$(".prdstock").html(_stk);
			$("#btnCount").children().addClass("disab");
			$("#btnLink").addClass("disab");
			$("#txtChoose").addClass("nostock");
			$("#enterQty").attr("disabled",true);
			
			$("#addCart,#addCartLink,#stages").unbind("click");
			$("#addCartLink").unbind("hover").addClass("nostock");
			
			/* 八叉乐-其他类似商品 */
			if($("#mboxImported-default-Product_Top_Center-0").length==0){
				mboxDefine('similarDom','Product_Top_Center','entity.id='+this.city+prdInfo.prdId,'entity.categoryId='+prdInfo.catId);
				mboxUpdate('Product_Top_Center');
			}
			createMbox(0,this.city,this.price,this.desc);
			$("#mboxImported-default-Product_Top_Center-0").show();
			$("#txtNostock,#arrival,#similarDom,.prdstocktxt").show();
		},
		bindCart:function(){
			var _this = this;
			
			/*添加购物车*/
			$("#addCart,#addCartLink").click(function(){
				_this.addCart();
			}).removeClass("nostock").show();
			
			/* 浮动小购物车 */
			var _tim;
			$("#addCartLink,#floatToCart").hover(function(){
				window.clearTimeout(_tim);
				$("#floatToCart").show();
			},function(){
				_tim = window.setTimeout(function(){$("#floatToCart").hide();},200);
			});
		},
		
		
		
		
		/* 获取团抢购信息 */
		getGroup:function(){
			var _this = this,_url=_this.dynUrl+"/n/product/browse/getProductLimitbuyAndGrouponJsonData.jsp";
				_this.getAjax({Jname:"test",Jurl:_url,Jdat:{'productId':prdInfo.prdId},Jcall:_this.getGroupDom});
		},
		getGroupDom:function(){
			if(this.data != undefined){
				var _dt = this.data,_tx = "团购";
				if(_dt.type!=""){
					if(_dt.type==2)_tx="抢购";
					$(".prdtq").html('该商品'+_tx+'中，先到先得...<a href="'+_dt.href+'" target="_blank" rel="nofollow">立即参加</a>'+_tx+'价：<b>¥'+_dt.price+'</b>').show();
				}
			}
		},
		
		
		
		/* 加入购物车 */
		addCart:function(){
			if(this.chkPre==2){
				window.location.href=staSite+"/yushou/"+this.itemId+".html";
				return false;
			}
			var _cl = window.location.href,_action="/ec/homeus",_yuan="",_only="false";
			if(_cl.indexOf("/ec/storesale")>0 || _cl.indexOf("/ec/giftcard")>0 || prdInfo.skuDat=="0元购机"){
				if(_cl.indexOf("/ec/storesale")>0)_action="/ec/storesale";
				if(_cl.indexOf("/ec/giftcard")>0)_action="/ec/giftcard";
				if(prdInfo.skuDat=="0元购机"){
					var _this = this;
					_this.login(function(){
						if(!_this.lgStatus){_action="/ec/contract";_yuan="sj0yg";_only="true";_this.addCartForm(_action,_yuan,_only);}
					});
				}
				if(prdInfo.skuDat!="0元购机"){this.addCartForm(_action,_yuan,_only);}
				return false;
			}
			var _this = this;
			_this.dialog({inner:"#dialogEr",cssname:"dialogBox Er",errIco:"load",errMsg:"正在添加商品到购物车，请稍后...",callback:function(){
				var _url = _this.dynUrl+"/n/product/gadgets/purchase/cartQtyAndVarietyCheck.jsp",
					_dat = {'productId':prdInfo.prdId,'catalogRefId':prdInfo.sku,'quantity':$("#enterQty").val()};
					_this.getAjax({Jname:"chkShopCart",Jurl:_url,Jdat:_dat,Jcall:_this.addCartChk,Param:_dat});
			}});
		},
		addCartChk:function(){
			/* 检测购物车添加的满足条件 */
			if(this.data != undefined){
				var _this = this.obj,_url = _this.dynUrl+"/cart/add.jsp",_dat=this.data;
				if(_dat.errorType == "quantity"){
					var _htm = '<div class="errorTxt">您购物车中的相同商品购买数量<span>不能大于<b>'+_dat.rightNum+'</b>件</span></div>\
								<div class="errorTxt">请您通过<a href="'+_this.staUrl+'/myaccount/customer/customer.jsp">企业客户通道</a>购买！</div>';
					_this.dialog({inner:"#dialogEr",cssname:"dialogBox Er",errIco:"warn",errMsg:_htm});
				}if(_dat.errorType=="sku"){
					_this.dialog({inner:"#dialogAdd",callback:function(){
						_this.dgObj.find("#dgMsg").html('添加失败！');
						_this.dgObj.find("#dgTxt").html('购物车内的商品种数已达上限(50种)！');
						_this.dgObj.find("#dgIcon").addClass("error");
					}});
				}if(_dat.success=="yes"){
					_this.getAjax({Jname:"addCart",Jurl:_url,Jdat:this.param,Jcall:_this.addCartDom});
				}
			}
		},
		addCartDom:function(){
			if(this.data!=undefined){
				var _this = this.obj,_dat=this.data;
				_this.dialog({inner:"#dialogAdd",callback:function(){
					if(_dat.result){
						_this.dgObj.find("#dgTxt").html('购物车共有<strong>'+_dat.result.totalQuantity+'</strong>件商品，商品总价：<strong>¥'+_dat.result.orderAmount+'</strong>');
						/* 八叉乐-购买了还购买了 */
						mboxDefine('buyMbox','Product_Center_Recs','entity.id='+_this.city+prdInfo.prdId,'entity.categoryId='+prdInfo.catId);
						mboxUpdate('Product_Center_Recs');
						_this.dgObj.find("#buyMbox").show();
					}
				}});
			}
		},
		/* 添加购物车：直接提交到购物车页面 */
		addCartForm:function(_action,_yuan,_only){
			var _div=$('#i_tep');
			if(_div.size()<1){_div=$('<div id="i_tep" style="display:none"></div>');_div.appendTo('body');}
			var _fm=_div.find('form');
			if(_fm.size()<1){
			var inp = '<input type="hidden" name="productId" /><input type="hidden" name="skuId" /><input type="hidden" name="num" /><input type="hidden" name="bizType" />\
						<input type="hidden" name="onlyOneSKU" /><input type="hidden" name="roundTim" />';
				_fm=$('<form name="addcar" target="_blank" method="get" action="'+dynSite+_action+'/global/gadgets/comPrdAddShoppingCart.jsp">'+inp+'</form>');_div.append(_fm);
			}
			_fm.find('[name="productId"]').val(prdInfo.prdId).end()
				.find('[name="skuId"]').val(prdInfo.sku).end()
				.find('[name="num"]').val($("#enterQty").val()).end()
				.find('[name="bizType"]').val(_yuan).end()
				.find('[name="onlyOneSKU"]').val(_only).end()
				.find('[name="roundTim"]').val(new Date().getTime());
			_fm.submit();
		},
		
		
		
		/* 添加到货通知、降价通知 */
		addNotic:function(_id){
			var _this = this,
				_eml = _this.dgObj.find("#mail"),
				_tel = _this.dgObj.find("#tell"),
				_rml = _this.dgObj.find("#errorMail"),
				_rel = _this.dgObj.find("#errorTell"),
				_evl = $.trim(_eml.val()),
				_tvl = $.trim(_tel.val());
			var ckEml = /^([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/;
				if(_evl==""){
					_eml.focus();
					_rml.html("<i></i>请输入您的邮箱！").show();
					return false;
				}
				if(!ckEml.test(_evl)){
					_eml.focus();
					_rml.html("<i></i>邮件地址格式有误！").show();
					return false;
				}
				if(_tvl==""){
					_rml.hide();
					_tel.focus();
					_rel.html("<i></i>请输入您的手机号码！").show();
					return false;
				}
				if(isNaN(_tvl) || _tvl.length!=11){
					_rml.hide();
					_tel.focus();
					_rel.html("<i></i>手机号码格式有误！").show();
					return false;
				}
				var isSyn = _this.dgObj.find("#chk").attr("checked");
				var _dat = {'productId':prdInfo.prdId,'skuId':prdInfo.sku,'oldPrice':_this.price,"mail":_evl,"tell":_tvl,"isSyn":isSyn};
					_rml.hide();
					_rel.hide();
				if(_id=="reduce"){
					/* 降价通知 */
					var _url = _this.dynUrl+"/browse/priceNoticeVerify.jsp";
						_this.getAjax({Jname:"notice",Jurl:_url,Jdat:_dat,Jcall:_this.addNoticDom});
				}
				if(_id=="arrival" || _id=="anotic"){
					/* 到货通知 */
					_dat["city"]="11000000";
					var _url = _this.dynUrl+"/browse/goodsNoticeVerify.jsp";
						_this.getAjax({Jname:"notice",Jurl:_url,Jdat:_dat,Jcall:_this.addNoticDom});
				}
		},
		addNoticDom:function(){
			if(this.data!=undefined){
				var _this=this.obj,_dat=this.data;
				if(_dat.succ=="false"){
					_this.dialog({inner:"#dialogEr",cssname:"dialogBox Er",errIco:"error",errMsg:_dat.msg});
				}
				if(_dat.succ=="true"){
					_this.dgObj.html('<div class="noticOk"><i class="dgIcon"></i>提交成功！</div>');
					window.setTimeout(function(){_this.dialogClose();},3000);
				}
			}
		},
		
		
		/* 收藏商品 */
		collect:function(){
			var _this = this;
			_this.login(function(){
				if(!_this.lgStatus){
					var _url = _this.dynUrl+"/global/login/verifyAddToWishlist.jsp",_dat={"productId":prdInfo.prdId,"skuId":prdInfo.sku,"siteId":"homeSite"};
						_this.getAjax({Jname:"wishlist",Jurl:_url,Jdat:_dat,Jcall:_this.collectDom});
				}
			});
		},
		collectDom:function(){
			if(this.data!=undefined){
				var _this = this.obj,_dat = this.data.errotType,_dbj = this.data;
				if(_dat=="isNoLogin"){
					_this.dialog({inner:"#dialogEr",cssname:"dialogBox Er",errIco:"error",errMsg:"请登录后再添加商品到收藏夹！"});
				}
				if(_dat=="isError"){
					_this.dialog({inner:"#dialogEr",cssname:"dialogBox Er",errIco:"error",errMsg:this.data.message});
				}
				if(_dat=="isCollect"){
					_this.dialog({inner:"#dialogAdd",callback:function(){
						_this.dgObj.find("#dgTxt").addClass("have").html('您已收藏过该商品！');
					}});
				}
				if(_dat=="isSuccess"){
					_this.dialog({inner:"#dialogAdd",callback:function(){
						_this.dgObj.find("#dgMsg").html('收藏成功！');
						_this.dgObj.find("#dgTxt").html('您已经成功收藏了<b>'+(_dbj.totality||1)+'</b>个商品');
					}});
				}
				if(_dat=="isCollect" || _dat=="isSuccess"){
					_this.dgObj.find("#dgMsg,.redlink").hide();
					_this.dgObj.find(".arrange").html("查看收藏夹&nbsp;>");
					_this.dgObj.find(".tit").html("收藏了此商品的用户还收藏了：");
					/* 八叉乐收藏了还收藏了 */
					mboxDefine('buyMbox','Product_Center_Recs','entity.id='+_this.city+prdInfo.prdId,'entity.categoryId='+prdInfo.catId);
					mboxUpdate('Product_Center_Recs');
					_this.dgObj.find("#buyMbox").show();
				}
			}
		},
		
		
		/* 预售信息 */
		preSellDom:function(data){
			var _preDom = "<div class=\"tip\">该商品正在预售中...<span>预售价:</span><span class=\"pp\">¥"+data.price+"</span><span>预付定金:</span><span class=\"pp\">¥"+data.deposit+"</span></div>"
			$("#preinfo").html(_preDom);
			$("#txtNostock").hide();
			$("#stockTxt").text("预售");
			$(".prdstocktxt").hide();
			$("#addCart,#addCartLink").html("立即预定");
			this.itemId=data.itemId;
			this.bindCart();
		},
		
		/* 主信息弹出层 logData:{inner:"弹出层模型",unlock:"是否锁屏",errIco:"提示图标样式",errMsg:"提示内容",callback:"弹出时回调函数",closeCall:"关闭时回调函数",closeAuto:定时关闭提示层}} */
		dialog:function(logData){
			if($.browser.msie && parseInt($.browser.version)<7)$(".dialog").css("position","static");
			this.dgObj.html($(logData.inner).html());
			if(logData.errIco)this.dgObj.find(".dgIcon").addClass(logData.errIco);
			if(logData.errMsg)this.dgObj.find(".errorBox").html(logData.errMsg);
			$("#dialogBox").removeAttr("class").addClass(logData.cssname||"dialogBox").show();
			if(logData.callback)logData.callback();
			var _c={},_t=this;if(logData.closeCall)_c.closeCall = logData.closeCall;
			$("#close").click(function(){_t.dialogClose.apply(_c);});
			_t.dgObj.find(".stages").click(function(){_t.dialogClose();});
			if(!logData.unlock)$("#dialogBox").gPop({lockBgColor:'#000',opacity:0.15,isColseBtn:false});
			if(logData.closeAuto){window.setTimeout(function(){_t.dialogClose();},3000);}
		},
		dialogClose:function(){
			$("#dialogBox,#popLock").hide();
			if(this.closeCall)this.closeCall();
		},
		
		
		/* loginstatus为头部公共模块请求获取的登录状态 */
		getHeadLogin:function(){
			if(loginstatus){
				if(loginstatus=="true" || loginstatus==true){
					this.lgStatus=true;
				}
				if(loginstatus=="false" || loginstatus==false){
					this.lgStatus=false;
				}
			}
		},
		/* 检测登录状态  返回的状态 isTransient  true未登录 false已登录 getHeadLogin获取公共头部返回的登录状态 */
		login:function(callback){
			this.getHeadLogin();
			if(this.lgStatus){
				var _url = this.dynUrl+"/global/login/loginstatus.jsp";
				this.getAjax({Jname:"a",Jurl:_url,Jcall:this.loginStatus,Jback:callback});
			}
			if(!this.lgStatus){
				if(callback)callback();
			}
		},
		loginStatus:function(){
			if(this.data != undefined){
				var _st = this.data.isTransient;
				if(_st || _st=="true"){/*未登录操作*/
					pop.layerShow('nobj','popLogin-dytscBir');
					$("#popLogin-ifrWindow").attr("src",this.obj.dynUrl.replace("http","https")+"/poplogin/popLogin.jsp?toSite=commerceDetail");
				}
				/*已登录状态*/
				if(!_st || _st=="false")this.obj.lgStatus=false;
				if(this.callback)this.callback();
			}
		},

		/*阻止元素默认行为*/
		stopLink:function(e){
			//W3C浏览器下
			if(e && e.preventDefault){e.preventDefault();}
			//IE下
			else{window.event.returnValue = false;}
		},
		
		/* 截取指定长度字符，多余显示省略号 */
		spltStr:function suolve(str,lth){
			var temp1 = str.replace(/[^\x00-\xff]/g,"**");/*将汉字或全拼字符截成两个字节*/
			var temp2 = temp1.substring(0,lth);   
			/*找出有多少个*/
			var x_length = temp2.split("\*").length - 1;
			var hanzi_num = x_length/2;
			lth = lth - hanzi_num ;/*实际需要sub的长度是总长度-汉字长度*/
			var res = str.substring(0,lth);
			var _tmp = "";
			if(lth < str.length){_tmp =res+"...";}
			else{_tmp = res;}
			return _tmp;
		},
		
		/* 主信息板块通用ajax事件 _d:{data:"响应的信息",callback:"回调设定的函数",param:"回调函数需要继承的参数值"} */
		getAjax:function(dat){
			var _d={};_d.data=undefined,_t=this;
			try{
				$.ajax({
					type:'get',
					url:dat.Jurl,
					data:dat.Jdat||{},
					dataType:'jsonp',
					jsonpName:dat.Jname,
					success:function(data){
						_d.data=data,_d.obj=_t;
						if(dat.Param)_d.param=dat.Param;
						if(dat.Jback)_d.callback=dat.Jback;
						dat.Jcall.apply(_d);
					},
					error:function(){
						/*_t.dialog({inner:"#dialogEr",cssname:"dialogBox Er",errIco:"warn",errMsg:"抱歉，当前系统繁忙，请稍后再试！",closeAuto:true});*/
					}
				});
			}catch(ex){};
		}
		/* ajax结束 */
	}
	/* 事件初始化 */
var prdMain = new prdInfoMain();
	prdMain.bind();