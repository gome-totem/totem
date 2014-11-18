package org.x.server.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.x.server.eye.Eye;
import org.x.server.service.IJVMDetect;
import org.z.global.environment.Const;
import org.z.store.mongdb.DataCollection;

import com.gome.totem.sniper.util.Globalkey;
import com.gome.totem.sniper.util.StringUtil;
import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * 分发页面请求
 * 
 * 
 */
@Controller
@RequestMapping(value = "/dispacher")
public class DispacherController{
	@Autowired
	private IJVMDetect ijvmDetect;
	
	@RequestMapping(value = "/searchManager")
    public ModelAndView searchManager(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/modules/searchManager.jsp");
	}
	@RequestMapping(value = "/searchConfig")
    public ModelAndView searchConfig(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/modules/searchConfig.jsp");
	}
	
	@RequestMapping(value = "/logManager")
    public ModelAndView logManager(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/modules/logManage.jsp");
	}
	
	@RequestMapping(value = "monitorPage")
    public ModelAndView monitorPage(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/modules/monitor.jsp");
	}
	
	@RequestMapping(value = "/monitor")
    public ModelAndView monitor(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/monitor/monitor.jsp");
	}
	
	@RequestMapping(value = "/configPage")
    public ModelAndView config(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/monitor/config.jsp");
	}
	
	@RequestMapping(value = "/dataAnalyse")
    public ModelAndView dataAnalyse(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/modules/dataAnalyse.jsp");
	}
	
	@RequestMapping(value = "/team")
    public ModelAndView team(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/modules/team.jsp");
	}
	
	@RequestMapping(value = "/cityQuestionBrandPage")
    public ModelAndView cityQuestionBrand(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/cityQuestionBrand.jsp");
	}
	
	@RequestMapping(value = "/dragonPage")
    public ModelAndView dragon(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/dragon.jsp");
	}
	@RequestMapping(value = "/esAscPage")
    public ModelAndView esAscPage(HttpServletRequest req,HttpServletResponse resp) {
		String esServer=Globalkey.esServers;
		String[] esServers=StringUtils.split(esServer,",");
		BasicDBList esList=new BasicDBList();
		for (int i = 0; i < esServers.length; i++) {
			esList.add(new BasicDBObject("key",esServers[i]).append("value",esServers[i]));
		}
		return new ModelAndView("/business/esAsc.jsp","esServer",esList.toString());
	}
	
	@RequestMapping(value = "/fullIndexPage")
    public ModelAndView fullIndex(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/fullIndex.jsp");
	}
	
	@RequestMapping(value = "/gomeCategoryOrderSetPage")
    public ModelAndView gomeCategoryOrderSet(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/gomeCategoryOrderSet.jsp");
	}
	@RequestMapping(value = "/gomeFacetsOrderSetPage")
    public ModelAndView gomeFacetsOrderSet(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/gomeFacetsOrderSet.jsp");
	}
	@RequestMapping(value = "/incIndexDaliyPage")
    public ModelAndView incIndexDaliy(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/incIndexDaliy.jsp");
	}
	@RequestMapping(value = "/helpCenterFullIndexPage")
    public ModelAndView helpCenterFullIndex(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/helpCenterFullIndex.jsp");
	}
	@RequestMapping(value = "/incrementIndexPage")
    public ModelAndView incrementIndex(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/incrementIndex.jsp");
	}
	@RequestMapping(value = "/keysMapPage")
    public ModelAndView keysMap(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/keysMap.jsp");
	}
	@RequestMapping(value = "/polyphoneFixPage")
    public ModelAndView polyphoneFix(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/polyphoneFix.jsp");
	}
	@RequestMapping(value = "/recommadPage")
    public ModelAndView recommad(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/recommad.jsp");
	}
	@RequestMapping(value = "/searchRulePage")
    public ModelAndView searchRule(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/searchRule.jsp");
	}
	@RequestMapping(value = "/searchSortPage")
    public ModelAndView searchSort(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/searchSort.jsp");
	}
	@RequestMapping(value = "/searchvotePage")
    public ModelAndView searchvote(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/searchvote.jsp");
	}
	@RequestMapping(value = "/skuSearchPage")
    public ModelAndView skuSearch(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/skuSearch.jsp");
	}
	@RequestMapping(value = "/smartBuySetPage")
    public ModelAndView smartBuySet(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/smartBuySet.jsp");
	}
	@RequestMapping(value = "/sshTomcatPage")
    public ModelAndView sshTomcat(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/sshTomcat.jsp");
	}
	
	@RequestMapping(value = "/topSearchMapPage")
    public ModelAndView topSearchMap(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/topSearchMap.jsp");
	}
	
	@RequestMapping(value = "/votefinishPage")
    public ModelAndView votefinish(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/votefinish.jsp");
	}
	@RequestMapping(value = "/msgPage")
    public ModelAndView msg(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/eye/message.jsp");
	}
	@RequestMapping(value = "/totemMapPage")
    public ModelAndView totemMap(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/eye/totem-map.jsp");
	}
	@RequestMapping(value = "/teamPage")
    public ModelAndView teamPage(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/eye/team.jsp");
	}
	@RequestMapping(value = "/userPage")
    public ModelAndView user(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/eye/user.jsp");
	}
	@RequestMapping(value = "/appsIndexDocCountPage")
    public ModelAndView appsIndexDocCount(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/data/appsIndexDocCount.jsp");
	}
	
	@RequestMapping(value = "/beansTalkMonitorPage")
    public ModelAndView beansTalkMonitor(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/data/beansTalkMonitor.jsp");
	}
	@RequestMapping(value = "/categoryKeysPage")
    public ModelAndView categoryKeys(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/data/categoryKeys.jsp");
	}
	@RequestMapping(value = "/categorytophundredPage")
    public ModelAndView categorytophundred(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/data/categorytophundred.jsp");
	}
	@RequestMapping(value = "/cityCodeDataPage")
    public ModelAndView cityCodeData(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/data/cityCodeData.jsp");
	}
	@RequestMapping(value = "/NoneResultPage")
    public ModelAndView NoneResult(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/data/NoneResult.jsp");
	}
	
	@RequestMapping(value = "/promoProductDataPage")
    public ModelAndView promoProductData(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/data/promoProductData.jsp");
	}
	
	@RequestMapping(value = "/RunLogKeywordsCountPage")
    public ModelAndView RunLogKeywordsCount(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/data/RunLogKeywordsCount.jsp");
	}
	
	@RequestMapping(value = "/spiderPage")
    public ModelAndView spider(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/data/spider.jsp");
	}
	
	@RequestMapping(value = "/detectPage")
    public ModelAndView detect(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/detect.jsp");
	}
	@RequestMapping(value = "/edmPlat")
    public ModelAndView edmPlat(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/business/edmPlatform.jsp");
	}
	

	@RequestMapping(value = "/dictConfigPage")
    public ModelAndView dictConfig(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/dictData/dictConfig.jsp");
	}
	@RequestMapping(value = "/addServer")
    public ModelAndView addServer(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/dictData/addServerConfig.jsp");
	}
	@RequestMapping(value = "/addBasic")
    public ModelAndView addBasic(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/dictData/addDictConfig.jsp");
	}
	@RequestMapping(value = "/updateServer")
    public ModelAndView updateServer(HttpServletRequest req,HttpServletResponse resp) {
		String ip=req.getParameter("ip_address");
		DBObject obj=DataCollection.findOne(Const.defaultDictMongo, Const.defaultDictMongoDB, "t_dict_data", new BasicDBObject("data.server.ip_address",ip));
		DBObject obj1=(DBObject)((DBObject) obj.get("data")).get("server");
		Map map=obj1.toMap();
		return new ModelAndView("/dictData/updateServerConfig.jsp",map);
	}
	@RequestMapping(value = "/updateBasic")
    public ModelAndView updateBasic(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/dictData/updateDictConfig.jsp");
	}
	
	@RequestMapping(value = "/userSearchPage")
    public ModelAndView userSearchPage(HttpServletRequest req,HttpServletResponse resp) {
		return new ModelAndView("/data/userSearchPage.jsp");
	}
	
	@RequestMapping(value = "/jvm")
    public ModelAndView jvmDetect(HttpServletRequest req,HttpServletResponse resp) {
		BasicDBObject o=ijvmDetect.JVMDetect();
		return new ModelAndView("/data/JVMPage.jsp","JVM",o);
	}
	
	@RequestMapping(value = "/categoryAPI")
    public void categoryAPI(HttpServletRequest req,HttpServletResponse resp){
		BasicDBObject oReq = new BasicDBObject();
		oReq.append("id", "category");
		oReq.append("values", "homeStoreRootCategory");
		String catId = req.getParameter("catId");
		String from =req.getParameter("from");
		if(!StringUtil.isNullOrEmpty(catId)){
			oReq.append("values", catId);
		}else{
			catId = "homeStoreRootCategory";
		}
		DBObject categories=Eye.sIntf.loadCategories(oReq);
		BasicDBObject response=categories==null?null:(BasicDBObject)categories.get("response");
		BasicDBObject result=response==null?null:(BasicDBObject)response.get("content");
		if(result!=null && result.get("childs")!=null){
			BasicDBList childs=(BasicDBList)result.get("childs");
			for (int i = 0; i < childs.size(); i++) {
				BasicDBObject obj=(BasicDBObject)childs.get(i);
				obj.remove("url");
				obj.remove("icon");
			}
			result.remove("url");
			result.remove("catalog");
			result.remove("facets");
		}else{
			result=new BasicDBObject();
		}
		if(StringUtils.equals("remote", from)){
			Write2PageUtil.write(resp, "callbackCategories("+result+");");
		}else{
			Write2PageUtil.write(resp, result);
		}
	}
	
	@RequestMapping(value = "/readEdmDoc")
    public void readEdmDoc(HttpServletRequest req,HttpServletResponse resp){
		String tempName = req.getParameter("mailId");
		String param = req.getParameter("paramRange");
		if(tempName == null || param == null)
			return;
		if(param.split("-")[0] == null || param.split("-")[1]== null )
			return;
		int iparam1 = Integer.parseInt(param.split("-")[0]);
		int iparam2 = Integer.parseInt(param.split("-")[1]);
		BasicDBObject oresp = new BasicDBObject();
		for(int i = iparam1; i<= iparam2;i++){
			oresp.append("p"+i, "qss test"+i);
		}
		Write2PageUtil.write(resp, oresp);	

	}
	
	
}
