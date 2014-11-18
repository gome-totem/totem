package com.gome.totem.sniper.util;

import javax.servlet.http.HttpServletRequest;

/**
 * 获取访问者的IP，并判断IP是否为真实IP
 * @author Dong Zhou
 * @version 1.0
 * @since JDK1.7
 */
public class IpUtils {

	/** 获取访问者的IP，并判断IP是否为真实IP */
	public static String getIp(HttpServletRequest request) {
		
		String ip = getIpAddr(request) ;
		if (ip.indexOf(",")>0&&ip.split(",")!=null) {
			if (ip.split(",")[0]!=null&&!"".equals(ip.split(",")[0])) {
				ip = getIpAddr(request).split(",")[0]; // 获取客户端的IP地址，建议：编写获取客户端IP地址的程序
			}else {
				ip = getIpAddr(request); // 获取客户端的IP地址，建议：编写获取客户端IP地址的程序
			}
		}else {
			ip = getIpAddr(request); // 获取客户端的IP地址，建议：编写获取客户端IP地址的程序
		}
		return ip ;
	}
	
	private static String getIpAddr(HttpServletRequest request) {
	       
		   String ip = request.getHeader("x-forwarded-for");
	       if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	           ip = request.getHeader("Proxy-Client-IP");
	       }
	       if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	           ip = request.getHeader("WL-Proxy-Client-IP");
	       }
	       if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	           ip = request.getRemoteAddr();
	       }
	       return ip;
	}
}
