package org.x.server.service;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.x.server.tools.HiveJdbcTools;

import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBList;
@Service
public class IDataAnlyiseImpl implements IDataAnlyise{

	/**
	 * 搜索城市前100
	 */
	public void cityMsg(HttpServletResponse resp){
		BasicDBList cityMap=null;
		try {
			if(HiveJdbcTools.city_100!=null){
				cityMap = HiveJdbcTools.city_100;
			}else{
				cityMap=HiveJdbcTools.ConvertData(HiveJdbcTools.queryForBasicDBList(HiveJdbcTools.city_sql.toString()));
				HiveJdbcTools.city_100=cityMap;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Write2PageUtil.write(resp, cityMap);
	}
	/**
	 * 搜索关键字前100
	 */
	public void keysMsg(HttpServletResponse resp){
		BasicDBList keysMap=null;
		try {
			if(HiveJdbcTools.keys_100!=null){
				keysMap = HiveJdbcTools.keys_100;
			}else{
				keysMap=HiveJdbcTools.queryForBasicDBList(HiveJdbcTools.keys_sql.toString());
				HiveJdbcTools.keys_100=keysMap;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Write2PageUtil.write(resp, keysMap);
	}
}
