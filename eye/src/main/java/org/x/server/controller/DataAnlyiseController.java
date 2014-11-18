package org.x.server.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.x.server.service.IDataAnlyise;
import org.x.server.tools.HiveJdbcTools;
import org.z.global.util.StringUtil;

import com.gome.totem.sniper.util.TimeUtil;
import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * 
 * @author doujintong
 *大数据分析模块
 */

@Controller@RequestMapping(value="/cloud")
public class DataAnlyiseController {

	
	private static final int limit = 15;
	private static String catagoryIdTop=null;
	private static BasicDBList cacheGomeTop = null;
	private static String catagoryId=null;
	private static BasicDBList cacheGome = null;
	@Autowired
	private IDataAnlyise dataAnlyise;
	/**
	 * 搜索地图
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value="/business/key-map",method=RequestMethod.POST)
	protected void keyMapPOST(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String ps = req.getParameter("ps");
		if(ps == null || StringUtils.isEmpty(ps)) return;
		
		if(ps.equalsIgnoreCase("city")){
			dataAnlyise.cityMsg(resp);
		}else if(ps.equalsIgnoreCase("keys")){
			dataAnlyise.keysMsg(resp);
		}
	}
	
	@RequestMapping(value="/business/key-map",method=RequestMethod.GET)
	protected void keyMapGET(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String cityName = req.getParameter("city");
		String cityCode = req.getParameter("cityCode");
		StringBuffer city_keys_sql = new StringBuffer("select question,count(1) value from tbl_search where districtcode=")
									.append(cityCode).append(" group by question order by value desc limit 100");
		if(cityCode == null) return;
		BasicDBList cityKeys =(BasicDBList) HiveJdbcTools.city_keys.get(cityCode);
		if(cityKeys == null){
			try {
				cityKeys=HiveJdbcTools.queryForBasicDBList(city_keys_sql.toString());
				HiveJdbcTools.city_keys.put(cityCode, cityKeys);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Write2PageUtil.write(resp, cityKeys);
	}
	
	/**
	 * top100关键字
	 * 
	 * @param req
	 * @param resp
	 */

	@RequestMapping(value="/business/cattop")
	public void top100ByHive(HttpServletRequest req, HttpServletResponse resp) {
		String gomeCatId = req.getParameter("id"),page = req.getParameter("page");
		int pageNumber = page == null ? 1 : Integer.parseInt(page), totalPage = 1;
		int start = (pageNumber - 1) * limit, end = pageNumber * limit - 1, listSize = 0, rest = 0;
		StringBuffer sql = new StringBuffer()
				.append("select question,count(1) cnt from tbl_search where catid='")
				.append(gomeCatId)
				.append("' group by question order by cnt desc limit 100");
		BasicDBList gomeTop = null;
		if (!StringUtil.isEmpty(gomeCatId)) {
			try {
				if(StringUtils.equals(gomeCatId, catagoryIdTop)){
					gomeTop=cacheGomeTop;
				}else{
					if(HiveJdbcTools.allCacheKeys!=null){
						gomeTop = HiveJdbcTools.getCacheGomeTop(HiveJdbcTools.allCacheKeys,gomeCatId,"TOP100");
					}else{
						gomeTop = HiveJdbcTools.queryForBasicDBList(sql.toString());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		listSize =gomeTop==null?0:gomeTop.size();
		//pageinfo(rest,listSize,pageNumber,totalPage,end);
		rest = listSize % limit;
		totalPage = listSize / limit;
		totalPage = rest == 0 ? totalPage: totalPage + 1;
		
		if(pageNumber > totalPage){
			pageNumber = totalPage;
		}
		if(pageNumber < 1){
			pageNumber = 1;
		}
			
		if(pageNumber == totalPage){
			end = (pageNumber - 1) * limit + rest - 1;
		}
		catagoryIdTop=null;cacheGomeTop=null;
		catagoryIdTop=gomeCatId;
		cacheGomeTop=gomeTop;
		BasicDBList results = new BasicDBList();
		if(gomeTop!=null && gomeTop.size()>0){
			for (int i = start; i <= end; i++) {
				results.add(new BasicDBObject().append("gomeKey", ((BasicDBObject) gomeTop.get(i)).getString("question", "")));		
			}
		}

		Write2PageUtil.write(resp, new BasicDBObject().append("oList", results).append("page", pageNumber).append("totalPage", totalPage).append("size", listSize));
	}
	/**
	 * SEM关键字
	 * 
	 * @param req
	 * @param resp
	 */
	@RequestMapping(value="/data/catkey")
	protected void catkey(HttpServletRequest req, HttpServletResponse resp)  {
		String catId = req.getParameter("catId"), page = req.getParameter("page"),catName=req.getParameter("catName");
		int pageNumber = page == null ? 1 : Integer.parseInt(page), totalPage = 1;
		int start = (pageNumber - 1) * limit, end = pageNumber * limit - 1, listSize = 0, rest = 0;
		
		if(catId == null) return ;		
		StringBuffer sql = new StringBuffer()
				.append("select question,count(1) cnt from tbl_search where catid='")
				.append(catId)
				.append("' group by question order by cnt desc");
		BasicDBList gomeTop = null;
		if (!StringUtil.isEmpty(catId)) {
			try {
				if(StringUtils.equals(catId, catagoryId)){
					gomeTop=cacheGome;
				}else{
					if(HiveJdbcTools.allCacheKeys!=null){
						gomeTop = HiveJdbcTools.getCacheGomeTop(HiveJdbcTools.allCacheKeys,catId,"ALL");
					}else{
						gomeTop = HiveJdbcTools.queryForBasicDBList(sql.toString());
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//pageinfo(rest,listSize,pageNumber,totalPage,end);
		listSize =gomeTop==null?0:gomeTop.size();
		rest = listSize % limit;
		totalPage = listSize / limit;
		totalPage = rest == 0 ? totalPage: totalPage + 1;
		
		if(pageNumber > totalPage){
			pageNumber = totalPage;
		}
		if(pageNumber < 1){
			pageNumber = 1;
		}
			
		if(pageNumber == totalPage){
			end = (pageNumber - 1) * limit + rest - 1;
		}
		catagoryId=null;cacheGome=null;
		catagoryId=catId;
		cacheGome=gomeTop;
		BasicDBList results = new BasicDBList();
		if(gomeTop!=null && gomeTop.size()>0){
			for (int i = start; i <= end; i++) {
				results.add(new BasicDBObject()
								.append("gomeKey", ((BasicDBObject) gomeTop.get(i)).getString("question", ""))
								.append("timesKey", ((BasicDBObject) gomeTop.get(i)).getString("cnt", ""))
							);		
			}
		}
		Write2PageUtil.write(resp, new BasicDBObject().append("oList", results).append("page", pageNumber).append("totalPage", totalPage).append("size", listSize));
	}
	
	/**
	 * 无结果搜索
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/business/NoneResultKeywordsServlet")
	public void noneResultKeyWords(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		long startdate = 0L;
		long enddate = 0L;
		int pageNo = 0;
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		PrintWriter out = response.getWriter();
		int pd = 0;
		String pdate = request.getParameter("idate");
		String strPageNo = request.getParameter("pageNo");
		if (StringUtil.isAlphanumeric(strPageNo)) {
			pageNo = Integer.parseInt(strPageNo);
		}
		if (StringUtil.isAlphanumeric(pdate)) {
			pd = Integer.parseInt(pdate);
		}

		startdate = TimeUtil.getLongTimeByToday(-(pd + 1));
		enddate = TimeUtil.getLongTimeByToday(pd);

		int pageNumber = strPageNo == null ? 1 : Integer.parseInt(strPageNo), totalPage = 1;
		int start = (pageNumber - 1) * limit, end = pageNumber * limit - 1, listSize = 0, rest = 0;
		BasicDBList gomeTop = null;
		try {
			if (HiveJdbcTools.noResultCacheKeys != null) {
				gomeTop = HiveJdbcTools.noResultCacheKeys;
			} else {
				gomeTop = HiveJdbcTools.queryForBasicDBList(HiveJdbcTools.nosql.toString());
				HiveJdbcTools.noResultCacheKeys=null;
				HiveJdbcTools.noResultCacheKeys = gomeTop;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//pageinfo(rest,listSize,pageNumber,totalPage,end);
		rest = listSize % limit;
		totalPage = listSize / limit;
		totalPage = rest == 0 ? totalPage: totalPage + 1;
		
		if(pageNumber > totalPage){
			pageNumber = totalPage;
		}
		if(pageNumber < 1){
			pageNumber = 1;
		}
			
		if(pageNumber == totalPage){
			end = (pageNumber - 1) * limit + rest - 1;
		}
		BasicDBList results = new BasicDBList();
		if (gomeTop != null && gomeTop.size()>0) {
			for (int i = start; i <= end; i++) {
				results.add(new BasicDBObject().append(
						"gomeKey",
						((BasicDBObject) gomeTop.get(i)).getString("question",
								"")).append("timesKey",
						((BasicDBObject) gomeTop.get(i)).getString("cnt", "")));
			}
		}
		Write2PageUtil.write(response,new BasicDBObject().append("oList", results).append("pageNo", pageNumber).append("totalPage", totalPage).append("size", listSize));
	}
	/**
	 * 自定义搜粟
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/business/userSearch")
	public void userSearch(HttpServletRequest request,HttpServletResponse response){
		String hql=request.getParameter("hql");
		if(StringUtils.isBlank(hql)){
			return;
		}
		try {
			BasicDBList data=HiveJdbcTools.queryForBasicDBList(hql);
			Write2PageUtil.write(response, data);
		} catch (Exception e) {
			Write2PageUtil.write(response, new BasicDBObject("error",e.getMessage()));
		}
	}
}
