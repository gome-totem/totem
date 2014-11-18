package com.gome.totem.sniper.util;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.z.global.dict.GlobalPage;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class StringUtil {
	
	private static Logger logger = Logger.getLogger(StringUtil.class);
	private static final Random img = new Random();
	
	public static boolean isNullOrEmpty(String str){
		if(null == str || str.trim().length() == 0){
			return true;
		}
		return false;
	}
	
	public static String nullToEmpty(String str){
		return str == null ? "" : str;
	}
	public static String nullToDef(String str,String def){
		return (str == null||str.length() == 0) ? def : str;
	}
	public static Object nullToEmpty(Object str){
		return str == null ? "" : str.equals("")?"":str.equals("null")?"":str;
	}
	
	public static Object nullToEmptybyDef(Object obj,Object def)
	{
		if(obj == null)
			return def;
		return obj;
	}
	
	/**
	 * 字符串转成int
	 * @param str
	 * @return
	 */
	public static int StrToInt(String str){
		try{
			String key=nullToEmpty(str);
			if(isNullOrEmpty(key))
				return 0;
			else
			{
				return Integer.parseInt(key);
			}
		}catch(Exception ex)
		{
			return 0;
		}
	}
	
	public static boolean isEmpty(String v) {
		if (StringUtils.isEmpty(v)) {
			return true;
		}
		if (v.equalsIgnoreCase("null") || v.equalsIgnoreCase("undefined")) {
			return true;
		}
		return false;
	}
	
	public static String arrayToString(Long [] array){
		return arrayToString(array,",");
	}
	
	public static <T> String arrayToString(T[] array,String seperator){
		if(array == null || array.length == 0)
			return null;
		StringBuffer sb = new StringBuffer();
		for(int i = 0 ; i < array.length ; i++){
			sb.append(array[i]);
			if(i != (array.length - 1))
				sb.append(seperator);
		}
		return sb.toString();
	}
	
	public static<T> String collectionToString(Collection<T> collection){
		return collectionToString(collection,",");
	}
	public static<T> String collectionToString(Collection<T> collection,String seperator){
		if(null==collection|| collection.size()<=0){
			return "";
		}
		StringBuilder sb=new StringBuilder();
		for(T id : collection){
			if(sb.length() >0) sb.append(seperator);
			sb.append(id);
		}
		return sb.toString();
	}
	
	public static<T> String MapToString(Map<T,T> mapobj,String seperator){
		if(null==mapobj|| mapobj.size()<=0){
			return "";
		}
		StringBuilder sb=new StringBuilder();
		for(Object key : mapobj.keySet()){
			if(sb.length() >0) sb.append(seperator);
			sb.append(key+"="+mapobj.get(key));
		}
		return sb.toString();
	}
	
	public static List<Long> stringToLongList(String stringList){
		if(null==stringList||stringList.length() == 0){
			return new ArrayList<Long>();
		}
		String[] strList=stringList.split(",");
		List<Long> arrList=new ArrayList<Long>(strList.length);
		for(String id : strList){
			try{
				arrList.add(Long.parseLong(id));
			}catch(Exception ex){
				logger.error("Failed to 'stringToLongList', [id -> '" + id + "']", ex);
			}
		}
		return arrList;
	}
	
	public static List<String> stringToList(String stringList){
		
		if(null==stringList||stringList.length() == 0)
			return new ArrayList<String>();
		
		String[] strList=stringList.split(",");
		List<String> arrList=new ArrayList<String>(strList.length);
		for(String id : strList){
			arrList.add(id);
		}
		return arrList;
	}
	
	
	public static String listToStr(List<String> strList,String seperator){
		return collectionToString(strList,seperator);
	}
	public static String ListToStr(List<String> strList){
		return listToStr(strList,",");
	}
	
	public static String ListToStr2(List<String> strList){
		return listToStr(strList,"_");
	}
	
	
	public static Long[] stringToLongArray(String stringList){
		if(null == stringList || stringList.trim().length() == 0)
			return null;
			
		String[] strList=stringList.split(",");
		Long [] longArray = new Long[strList.length];
		for(int i = 0 ; i < strList.length ; i++){
			try{
				longArray[i] = Long.parseLong(strList[i]);
			}catch(Exception ex){
				logger.error("Failed to 'stringToLongArray', [id -> '" + strList[i] + "']", ex);
			}
		}
		return longArray;
	}
	
	public static String[] stringToStringArray(String stringList){
		if(null == stringList || stringList.trim().length() == 0)
			return null;
		else if(stringList.indexOf(',') < 0)
			return new String[]{stringList};
			
		return stringList.split(",");
	}
	
	public static<T> Collection<String> collectionToStringCollection(Collection<T> longCollection){
		if(null==longCollection||longCollection.size()<=0){
			return new ArrayList<String>();
		}
		Collection<String> stringCollection=new ArrayList<String>(longCollection.size());
		for(T id : longCollection){
			stringCollection.add(String.valueOf(id));
		}
		return stringCollection;
	}
	
	public static Collection<Long> collectionToLongCollection(Collection<String> collection){
		Collection<Long> longCollection=new ArrayList<Long>();
		for(String id : collection){
			try{
			longCollection.add(Long.valueOf(id));
			}catch(Exception ex){
				logger.error("Failed to 'collectionToLongCollection', [id -> '" + id + "']", ex);
			}
		}
		return longCollection;
	}
	
	public static String fileStr(String[] filestr)
	{
		Set<String> setstr=new HashSet<String>();
		for(String str:filestr)
		{
			setstr.add(str.trim());
		}
		String returnstr=setstr.toString();		
		returnstr=returnstr.replaceAll("\\[","").replaceAll("\\]","").trim();

		return returnstr;
	}
	/**
	 * @author zhangnk
	 * @param strlist
	 * @return
	 */
	public static List<String> termStringList(List<String> strlist)
	{		
		Set<String> setstr=new HashSet<String>();
		for(String str:strlist)
		{
			setstr.add(str.trim());
		}
		
		return new ArrayList<String>(setstr);
	}
	public static boolean isXss(String str){
		return (str.contains("'") || str.contains("'") || str.contains("<") || str.contains(">") );
	}
	
	
	public static int getRandomInt(int start , int end )
	{
		Random rd=new Random();
		int num = rd.nextInt(end);
		while(num < start)
			num=rd.nextInt(end);		
		return num;//192.168.10.123     ------------//10.63.200.86
	}
	
	/**
	 * 截取字符串
	 * @param str 需要截取的字符串
	 * @param num 需要截取多长
	 * @param len 超过多长开始截取
	 * @return
	 */
	public static String formatStrByNum(String str,int num,int len)
	{
		if(str.length()>=len){
			char[] list=new char[num];
			str.getChars(0, num, list, 0);		
			return new String(list)+"...";
		}
		return str;
			
	}
	
	/**
	 * 判断字符串是否是有数字组成
	 * @param str
	 * @return
	 */
	public static boolean checkStrIfNumber(String str)
	{
		if(StringUtil.isNullOrEmpty(str))
			return false;
		boolean ok=Pattern.matches("[0-9]*",str);
		return ok;
	}
	
	/** 
     * 验证输入的邮箱格式是否符合 
     * @param email 
     * @return 是否合法 
     */ 
	public static boolean emailFormat(String email) 
    { 
        boolean tag = true; 
        final String pattern1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"; 
        final Pattern pattern = Pattern.compile(pattern1); 
        final Matcher mat = pattern.matcher(email); 
        if (!mat.find()) { 
            tag = false; 
        } 
        return tag; 
    } 
	public static String[] getCheckMem(String type)
	{
		String[] checkmem=new String[]{"custName","custMobile","custEmail","identityType","identityNum","bd"};
		if("ctya".equals(type) || "ctyb".equals(type))
		{
			checkmem=new String[]{"custName","custMobile","custEmail","identityType","identityNum","bd","lvxingshemingcheng","cityid"};
		}else if("zyx".equals(type))
		{
			checkmem=new String[]{"custName","custMobile","custEmail","identityType","identityNum","bd","lvyou","cityid"};
		}else if("cx".equals(type))
			checkmem=new String[]{"custName","custMobile","custEmail","identityNum","bd","postalcode","cityid"};
		return checkmem;
	}

	public static String getCheckType(String checktype,String idt)
	{
		String str="";
		if("custName".equals(checktype) || "lvyou".equals(checktype) || "lvxingshemingcheng".equals(checktype)||"cityid".equals(checktype))
		{
			str="10";
		}else if("custMobile".equals(checktype))
		{
			str="5";
		}else if("custEmail".equals(checktype))
		{
			str="2";
		}else if("identityNum".equals(checktype))
		{
			if("1".equals(idt))
				str="4";
			else
				str="10";
		}else if("bd".equals(checktype))
		{
			str="3";
		}else if("postalcode".equals(checktype))
			str="6";
		return str;
	}
	
	public static String getNameforParam(String param)
	{
		String str="";
		if("custName".equals(param))
		{
			str="姓名";
		}else if("custMobile".equals(param))
		{
			str="手机号";
		}else if("custEmail".equals(param))
		{
			str="邮箱";
		}else if("identityType".equals(param))
		{
			str="证件类型";
		}else if("identityNum".equals(param))
		{
			str="证件号码";
		}else if("bd".equals(param))
		{
			str="出生日期";
		}else if("lvyou".equals(param))
			str="旅游线路";
		else if("lvxingshemingcheng".equals(param))
			str="旅行社名称";
		else if("cityid".equals(param))
			str="省份";
		else if("postalcode".equals(param))
			str="邮编";
		return str;
	}
	
	public static boolean checkPhone(String phone)
	{
		boolean ok=false;
		if(checkStrIfNumber(phone))
		{
			if(!phone.startsWith("13") && !phone.startsWith("15") && !phone.startsWith("18"))
			{
				return ok;
			}else if(phone.length() != 11)
				return ok;
		}else
		{
			return ok;
		}
		return true;
	}
	
	//验证邮编
	public static boolean checkPostCode(String postCode){
			
	        String pattern1 = "[1-9]\\d{5}"; 
	        Pattern p = Pattern.compile(pattern1);
	        Matcher m = p.matcher(postCode);
	        boolean b = m.matches();
	        return b;
	}
	
	public static String decodeIp(String ip){
		if (ip!=null && ip.trim().length()>0) {
			ip = ip.replace("origin_app_server_", "")
					.replace("app_server_", "")
					.replace("origin_router_server_", "")
					.replace("router_server_", "")
					.replace("_", ".");
			return ip ;
		} else {
			return "" ;
		}
	}
	
	public static String getRandomStr() {
		StringBuilder sb = new StringBuilder();
		sb.append(System.currentTimeMillis() + "" + getRandomInt(100, 999));
		return sb.toString();
	}

	public static String operateLog(String ip, String username, String date,
			String url, String referrer) {
		StringBuffer log = new StringBuffer();
		log.append("ip: ").append(ip).append("\n") ;
		log.append("username: ").append(username).append("\n") ;
		log.append("date: ").append(date).append("\n") ;
		log.append("url: ").append(url).append("\n") ;
		log.append("referrer: ").append(referrer).append("\n") ;
		return log.toString() ;
	}
	
	public static  String makeIp(String name) {
		String ip = name;
		ip = ip.replace("origin_app_server_", "")
				.replace("app_server_", "")
				.replace("origin_router_server_", "")
				.replace("router_server_", "").replace("_", ".")
				.replace("-", ":");
		return ip;
	}
	
	/**
	 * 将Bson字符串转换成java对象，存放在application
	 * 
	 * @param dict
	 */
	public static void StoreDictServer(BasicDBList dict, ServletContext servletContext) {
		servletContext.setAttribute(Globalkey.DICT_SERVER, dict);
	}
	
	public static String readChinese(String str) {
		StringBuilder buder = new StringBuilder();
		String regEx = "[\\u4e00-\\u9fa5 A-Za-z0-9]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		while (m.find()) {
			buder.append(m.group(0));
		}
		return buder.toString();
	}
	
	public static void writeToFile(String content, String fileName, String charset) {
		try {
			FileOutputStream fileStream = new FileOutputStream(fileName);
			OutputStreamWriter writer = new OutputStreamWriter(fileStream, charset);
			IOUtils.write(content, writer);
			writer.close();
			fileStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getNickName(String prefix, BasicDBObject oItem){
		return prefix + oItem.getString("ip","").replace(".", "_").replace(":", "-");
	}
	
	public static long decNow(long time) {
		return System.currentTimeMillis() - time;
	}
	
	public static long nowTime(){
		return System.currentTimeMillis();
	}
	
	public static void quickSortByName(final String name, BasicDBList rows) {
		Collections.sort(rows, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				if (o1==null && o2==null) {
					return 0 ;
				}
				if (o1==null) {
					return 1 ;
				}
				if (o2==null) {
					return -1 ;
				}
				String ip1 = ((BasicDBObject)o1).getString(name,"");
				String ip2 = ((BasicDBObject)o2).getString(name,"");
				return ip1.compareTo(ip2) ;
			}

		});
	}
	
	public static String XSSFilter(String source) {
		StringBuilder result = new StringBuilder();
		Pattern pattern = Pattern.compile("[\u4e00-\u9fa5 a-zA-Z0-9]");// 中文,英文,数字

		char[] c = source.toCharArray();
		for (int i = 0; i < c.length; i++) {
			String v = String.valueOf(c[i]);
			boolean match = true;
			Matcher matcher = pattern.matcher(v);
			while (matcher.find()) {
				result.append(matcher.group());
				match = false;
			}
			if (match) {
				result.append(escape(v));
			}
		}
		return result.toString();
	}
	
	public static String escape(String content) {
		if (content == null) {
			return "";
		}
		content = content.replaceAll("\r\n", "\\\\n").replaceAll("\n", "\\\\n");
		StringBuffer sb = new StringBuffer();
		for (int i = 0, len = content.length(); i < len; i++) {
			char c = content.charAt(i);
			switch (c) {
			case ' ':
				sb.append("&nbsp;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case '\'':
				sb.append("&apos;");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String randomImgServerName() {
		int random = img.nextInt(14);
		return "http://img" + (random == 0 ? "" : random) + (GlobalPage.PICTURESERVER).replace("http://img", "");
	}
}