package org.x.server.eye;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x.server.service.MobileIntf;

import com.gome.totem.sniper.util.Globalkey;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

/**
 * es服务器监控类
 * 
 * @author root
 * 
 */
public class ESMonitorTask extends TimerTask {

	protected Logger logger = LoggerFactory.getLogger(ESMonitorTask.class);

	HttpClient httpclient;

	private static final int REQUEST_TIMEOUT = 10 * 1000;// 设置请求超时10秒钟
	private static final int SO_TIMEOUT = 10 * 1000; // 设置等待数据超时时间10秒钟

	Random random;

	public ESMonitorTask() {
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
		httpclient = new DefaultHttpClient(httpParams);
		random = new Random();

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void doRequest() {
		boolean esSend = Globalkey.esSend;
		if (!esSend) {
			return;
		}
		String[] esServers = Globalkey.esServers.split(",");
		int n = random.nextInt(esServers.length);

		String ip = esServers[n];
//		final String url = "http://" + ip+ ":9200//_cluster/state/nodes,master_node?pretty";
		final String url = "http://" + ip+ ":9200/_cluster/health";
		

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
		String msg = "ES ";
		boolean send = false;
		try {
			HttpGet httpGet = new HttpGet(url);
			responseBody = httpclient.execute(httpGet, responseHandler);
		} catch (IOException e) {
			msg += "IOException " + url + " " + e.getMessage();
			send = true;
		}

		if (!send && responseBody.startsWith("error")) {
			msg += responseBody;
			send = true;
		} else if (!send) {
			Map map = (Map) JSON.parse(responseBody);
		
			String status=map.get("status")+"";
			int unassigned_shards=Integer.parseInt( map.get("unassigned_shards")+"");
			
			int active_shards=Integer.parseInt( map.get("active_shards")+"");
			
			if(!status.equals("green")){
				msg += "status:" + status;
				msg+=" active_shards:"+active_shards;
				msg+=" unassigned_shards:"+unassigned_shards;
				send = true;
			}
			
			
		}
		if (send) {
			logger.info(msg);
			msgSend(msg);
		}

	}

	private void msgSend(String massage) {
		BasicDBObject msg = new BasicDBObject();
		BasicDBObject content = new BasicDBObject();
		BasicDBList info = new BasicDBList();

		String[] esDevelopers = Globalkey.esDevelopers.split(",");

		for (String phnumber : esDevelopers) {
			long number = Long.parseLong(phnumber);
			info.add(new BasicDBObject().append("phoneNumber", number).append(
					"context", massage));

		}

		content.append("info", info);
		msg.append("content", content);
		MobileIntf mmmIntf = new MobileHandler();
		mmmIntf.sendHtmlMessage(msg);
	}

	@Override
	public void run() {
		doRequest();

	}
	
/*	public static void main(String[] args) {
		ESMonitorTask task=new ESMonitorTask();
		task.doRequest();
	}*/

}
