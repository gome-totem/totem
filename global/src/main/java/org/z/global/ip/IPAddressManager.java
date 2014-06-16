package org.z.global.ip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

/**
 * IP段和城市数据管理
 * @author xj_xiaocheng
 *
 */
public class IPAddressManager {
	private static List<IPAddressRange> ipRangeList = new ArrayList<IPAddressRange>();
	private static Map<String, Long> ipMapCache = new HashMap<String, Long>();				//用于缓存未找到记录的IP
	private DataSource catxDateSource;			
	private String ipStr = "218.241.170.226";	//测试数据;
	
	/**
	 * 根据IP开始段进行排序
	 */
	class ComparatorIpRange implements Comparator<IPAddressRange> {
		@Override
		public int compare(IPAddressRange o1, IPAddressRange o2) {
			if(o1.getIpStartAddress() < o2.getIpStartAddress())
				return -1;
			else if(o1.getIpStartAddress() > o2.getIpStartAddress())
				return 1;
			else
				return 0;
		}
	}

	public static List<IPAddressRange> getIpRangeList() {
		return ipRangeList;
	}
	
	public static void setIpRangeList(List<IPAddressRange> ipRangeList) {
		IPAddressManager.ipRangeList = ipRangeList;
	}

	public static Map<String, Long> getIpMapCache() {
		return ipMapCache;
	}

	public static void setIpMapCache(Map<String, Long> ipMapCache) {
		IPAddressManager.ipMapCache = ipMapCache;
	}

	public String getIpStr() {
		return ipStr;
	}

	public void setIpStr(String ipStr) {
		this.ipStr = ipStr;
	}

	public DataSource getCatxDateSource() {
		return catxDateSource;
	}

	public void setCatxDateSource(DataSource catxDateSource) {
		this.catxDateSource = catxDateSource;
	}

	/**
	 * 根据IP地址查询对应的城市
	 * @param strIP
	 * @return
	 */
	public static String findCityCode(String strIP) {
		
		String cityCode = findIPAddressRange(strIP).getCityCode();
		if(cityCode == null)
			return null;
		else
			return cityCode.toString();
	}
	
	/**
	 * 根据IP地址查询对应的IP段和城市信息
	 * @param strIP
	 * @return
	 */
	public static IPAddressRange findIPAddressRange(String strIP)
    {
		Long lIP = null;
		if(StringUtils.isNotBlank(strIP))
			lIP = IPAddressManager.parseIpToLong(strIP);
		
		IPAddressRange oRange = new IPAddressRange();
		
		if (getIpRangeList() != null && lIP != null)
        {
			//使用二分搜索方式查找
            int low = 0, high = getIpRangeList().size()-1, mid;
            while (low <= high)
            {
                mid = (low + high) / 2;
                oRange = getIpRangeList().get(mid);
                if (lIP < oRange.getIpStartAddress())
                {
                    high = mid - 1;
                }
                else if (lIP > oRange.getIpEndAddress())
                {
                    low = mid + 1;
                }
                else
                {
                    return oRange;
                }
            }
            
            putInCached(strIP, lIP, oRange);
        }
		oRange = new IPAddressRange();
        return oRange;
    }

	/**
	 * 把没找到的IP,放入缓存中
	 * @param strIP
	 * @param lIP
	 * @param oRange
	 */
	private static void putInCached(String strIP, Long lIP,	IPAddressRange oRange) {
		if(oRange == null) {
			getIpMapCache().put(strIP, lIP);
		}
	}
	
	/**
	 * 往列表里插入新的IP段
	 * @param oRange
	 */
	public static void insert(IPAddressRange oRange)
    {
        int i = getIpRangeList().size() - 1;
        for (; i >= 0; i--)
        {
            IPAddressRange oTmpRange = getIpRangeList().get(i);
            if (oRange.getIpEndAddress() < oTmpRange.getIpStartAddress())
            {
                continue;
            }
            else if (oRange.getIpStartAddress() > oTmpRange.getIpEndAddress())
            {
                break;
            }
            
            if (isGreaterThan(oTmpRange, oRange))
            {
                insert(getIpRangeList().remove(i));
            }
            else if (isGreaterThan(oTmpRange, oRange))
            {
                insert(oRange);
                return;
            }
            else
            {
                System.out.println("INSERT Address ERROR"+oRange.getIpStartAddress()+"   "+oRange.getIpEndAddress()+
                		"  "+oTmpRange.getIpStartAddress()+"    "+oTmpRange.getIpEndAddress());
            }
        }
        getIpRangeList().add(i + 1, oRange);
    }
	
	private static boolean isGreaterThan(IPAddressRange ipAddressRange, IPAddressRange oRange)
    {
		boolean a = ipAddressRange.getIpStartAddress() <= oRange.getIpStartAddress();
		boolean b = (ipAddressRange.getIpEndAddress() - ipAddressRange.getIpStartAddress()) > (oRange.getIpEndAddress() - oRange.getIpStartAddress());
        return  a && b;
    }
	
	/**
	 * 将218.241.170.226形式的ip转成long值
	 * @param ipstr
	 * @return
	 */
	public static Long parseIpToLong(String ipstr){
	    Long ip = null;	    
	    String arr[] = ipstr.split("\\.");
	    try{
	    	ip = Long.parseLong(arr[0]) * 256 * 256 * 256
	                + Long.parseLong(arr[1]) * 256 * 256
	                + Long.parseLong(arr[2]) * 256 + Integer.parseInt(arr[3]);
	    }catch(Exception e){
	    	//ip不符合规则
	        e.printStackTrace();
	    }
	    return ip;
	}

	/**
	 * 将map结构数据转换为list集合，放入内存中
	 * @param ipMaps
	 */
	private static void setIpRangeList(Map<String, String> ipMaps) {
		//清空原来的数据
		setIpRangeList(new ArrayList<IPAddressRange>());
		
		if(ipMaps == null || ipMaps.size() <= 0)
			return;
		
		for (String ipRange : ipMaps.keySet()) {
			String[] ipStr = ipRange.split("-");
			Long ipStartLong = Long.parseLong(ipStr[0]);
			Long ipEndLong = Long.parseLong(ipStr[1]);
			String cityCode = ipMaps.get(ipRange);
			
			IPAddressRange range = new IPAddressRange();
			range.setIpStartAddress(ipStartLong);
			range.setIpEndAddress(ipEndLong);
			range.setCityCode(cityCode);
			//将新数据放入list中
			getIpRangeList().add(range);
		}
		//使用内部类排序
		Collections.sort(getIpRangeList(), new IPAddressManager().new ComparatorIpRange());
	}
	
	/**
	 * 从数据库中,加载IP段和城市关系数据
	 * @param userIpMaps
	 */
	@SuppressWarnings("null")
	private void loadDatabase(Map<String, String> userIpMaps) {
		DBCursor csr = null;//DataCollection.findAll(Const.defaultDictServer, Const.defaultDictDB, "ipRegionData");
		while(csr.hasNext()){
			BasicDBObject item = (BasicDBObject)csr.next();
			String ipstart = item.getString("ipstart","0");
			String ipend = item.getString("ipend","0");
			String cityCode = item.getString("cityid","0");
			userIpMaps.put(ipstart + "-" + ipend, cityCode);
		}
	}
	
	/**
	 * 加载IP段和城市关系数据，存入list集合中
	 */
	public void load() {	
		Map<String, String> userIpMaps = new HashMap<String, String>();
		loadDatabase(userIpMaps);
		if(userIpMaps != null && userIpMaps.size() > 0) {
			setIpRangeList(userIpMaps);
		}
	}
	
	/**
	 * 测试查询方法
	 */
	public static void main(String[] args) {
		//IPAddressManager.load();		
		IPAddressManager _this = new IPAddressManager();
		_this.load();
		IPAddressRange range = IPAddressManager.findIPAddressRange(_this.ipStr);
		System.out.println("根据IP查询到的结果：" + range + "\t" + findCityCode(_this.ipStr));
		System.out.println("range:"+range.toString()+" data-size:"+IPAddressManager.ipRangeList.size());
	}

}
