package org.x.server.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

/**
 * es服务器监控类
 * 
 * @author root
 * 
 */
@Controller
@RequestMapping(value = "/cloud/eye")
public class ESMonitorController {	
	protected Logger logger = LoggerFactory.getLogger(ESMonitorController.class);

	HttpClient httpclient;

	Properties prop = new Properties();

	private static final int REQUEST_TIMEOUT = 10 * 1000;// 设置请求超时10秒钟

	@RequestMapping(value = "/esAsc")
	public void esAsc(HttpServletRequest request, HttpServletResponse response){
		String catId=request.getParameter("catId");
		String ip=request.getParameter("ip");
		String port=request.getParameter("port")==null?"9200":request.getParameter("port");
		if(StringUtils.isBlank(ip)){
			Write2PageUtil.write(response, new BasicDBObject("result","error"));
			return;
		}
		String url = createAscUrl(ip, port, catId);
		dealHttpRequest(response,url);
	}
	
	private static String createAscUrl(String ip, String port,String catId) {
		String url="http://" + ip + ":" + port +"/_mstore/sync";
		if(StringUtils.isNotBlank(catId)){
			url+="/"+catId;
		}
		return url;
	}
	
	private void dealHttpRequest(HttpServletResponse response, String url) {
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpClient client = null;
		HttpResponse responseBody=null;
		try {
			client = HttpClients.createDefault();
			responseBody = client.execute(httpGet);
			int status = responseBody.getStatusLine().getStatusCode();
			if(status==200){
				String body = EntityUtils.toString(responseBody.getEntity(), "UTF-8");
				Write2PageUtil.write(response, new BasicDBObject("result",body));
			}else if(status==404){
				Write2PageUtil.write(response, new BasicDBObject("result","404"));
			}else{
				Write2PageUtil.write(response, new BasicDBObject("result","error"));
			}
		}catch(SocketTimeoutException e){
			Write2PageUtil.write(response, new BasicDBObject("result","timeout"));
		}catch(ConnectTimeoutException e){
			Write2PageUtil.write(response, new BasicDBObject("result","timeout"));
		}catch(UnknownHostException e){
			Write2PageUtil.write(response, new BasicDBObject("result","nohost"));
		}catch (Exception e) {
			e.printStackTrace();
			Write2PageUtil.write(response, new BasicDBObject("result","error"));
		}finally{
			if(client!=null){
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	@RequestMapping(value = "/ascCacheData")
	public void ascCacheData(HttpServletRequest request, HttpServletResponse response){
		String url=request.getParameter("url");
		if(StringUtils.isBlank(url) || !url.startsWith("http://")){
			Write2PageUtil.write(response, new BasicDBObject("result","error"));
			return;
		}
		dealHttpRequest(response,url.trim());
	}
	@RequestMapping(value = "/es")
	protected void doES(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text;html;charset=utf-8");

		URL murl = Thread.currentThread().getContextClassLoader()
				.getResource("eyeconfig.properties");

		prop.load(new FileInputStream(murl.getPath()));

		String ss = prop.getProperty("esServers");
		// System.out.println(ss);

		String[] esServers = ss.split(",");
		int n = random.nextInt(esServers.length);

		String ip = esServers[n];
		final String url = "http://" + ip+ ":9200/_cluster/state/nodes,master_node?pretty";

		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

			public String handleResponse(final HttpResponse response)
					throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				String charset = "UTF-8";
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity,
							charset) : null;
				} else {
					return "error " + url + " response status: " + status;

				}
			}

		};

		String responseBody = null;

		try {
			HttpGet httpGet = new HttpGet(url);
			responseBody = httpclient.execute(httpGet, responseHandler);
		} catch (IOException e) {
			logger.error("es ", e);
		}

		Map map = (Map) JSON.parse(responseBody);

		Map health = getESstatus(ip);

		map.put("health", health);
		PrintWriter out = response.getWriter();

		out.print(map);

	}

	public Map getESstatus(String ip) {
		final String url = "http://" + ip + ":9200/_cluster/health";

		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

			public String handleResponse(final HttpResponse response)
					throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				String charset = "UTF-8";
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity,
							charset) : null;
				} else {
					return "error " + url + " response status: " + status;

				}
			}

		};

		String responseBody = null;

		try {
			HttpGet httpGet = new HttpGet(url);
			responseBody = httpclient.execute(httpGet, responseHandler);
			Map map = (Map) JSON.parse(responseBody);

			return map;
		} catch (IOException e) {
			logger.error("es ", e);
		}
		return null;
	}

	Random random;

	public ESMonitorController() {
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
		httpclient = new DefaultHttpClient(httpParams);
		random = new Random();

	}

}
