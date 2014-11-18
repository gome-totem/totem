package org.x.server.eye;

import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gome.totem.sniper.util.Globalkey;
import com.mongodb.BasicDBObject;

public class DetectAddress implements DetectSubject {

	private static final Logger logger = LoggerFactory.getLogger(DetectAddress.class);
	private static ExecutorService pool = null;
	private static final List<String> defaultIps = new ArrayList<String>();
	private static final int[] defaultPorts = new int[] { 8070, 8080, 8090 };
	private static final Queue<Message> queue = new LinkedBlockingQueue<Message>();
	private static final List<Observer> observers = new ArrayList<DetectSubject.Observer>();
	public static Boolean isAlive = false;
	private List<Address> addresses = null;

	public void registerObserver(Observer observer) {
		observers.add(observer);
	}

	public void removeObserver(Observer observer) {
		observers.remove(observer);
	}

	public void notifyObserver(Message message) {
		for (Observer observer : observers)
			observer.execute(message);
	}

	static {
		String detectAddress=Globalkey.detectAddress;
		if(detectAddress!=null && detectAddress.length()>0){
			String[] ips=StringUtils.split(detectAddress,",");
			for (String ip : ips) {
				defaultIps.add(ip);
			}
		}
	}

	public void executeDefaultSearch(int times, int thread) {
		addresses = new ArrayList<DetectSubject.Address>();
		for (String ip : defaultIps)
			for (int port : defaultPorts)
				addresses.add(new Address(createSearchUrl(ip, port), ip, port));
		execute(times, thread, addresses);
	}
	

	public void executeDefaultCategory(int times, int thread) {
		addresses = new ArrayList<DetectSubject.Address>();
		for (String ip : defaultIps)
			for (int port : defaultPorts)
				addresses.add(new Address(createCategoryUrl(ip, port), ip, port));
		execute(times, thread, addresses);
	}
	
	
	private static String createCategoryUrl(String ip, int port) {
		return "http://" + ip + ":" + port + "/category/cat10000070.html?t=" + System.nanoTime();
	}

	private static String createSearchUrl(String ip, int port) {
		return "http://" + ip + ":" + port + "/search?question=iphone5s&t=" + System.nanoTime();
	}

	public void execute(int times, int thread, List<Address> addresses) {
		synchronized (isAlive) {
			if (isAlive == true)
				return;
		}
		isAlive = true;
		if (addresses == null || addresses.size() == 0)
			return;
		times = times < 1 ? 1 : times;
		thread = thread < 1 ? 1 : thread;
		this.addresses = addresses;
		pool = Executors.newFixedThreadPool(thread);
		for (int i = 0; i < times; i++)
			pool.execute(new Detect());
		pool.shutdown();
		while (pool.isTerminated() == false && queue.peek() == null)
			threadAwaite(pool);
		while (true) {
			if (queue.isEmpty() && pool.isTerminated())
				break;
			Message message = queue.poll();
			if (message == null)
				continue;
			notifyObserver(message);
			if(times==1){
				logger.info("ASCFILTER DATA----{}",new Object[]{message});
			}
		}
		notifyObserver(new Message(new Address("0.0.0.0", "0.0.0.0", 0), 0, 0));
		isAlive = false;
	}

	private static void threadAwaite(ExecutorService pool) {
		try {
			pool.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private class Detect implements Runnable {
		public void run() {
			detectAll();
		}
	}

	private void detectAll() {
		CloseableHttpClient httpclient = null;
		for (int i = 0, j = addresses.size(); i < j; i++) {
			if (i % 10 == 0) {
				closeHttpClient(httpclient);
				httpclient = HttpClients.createDefault();
			}
			detectAddress(addresses.get(i), httpclient);
		}
		closeHttpClient(httpclient);
	}

	private static void closeHttpClient(CloseableHttpClient httpclient) {
		if (httpclient == null)
			return;
		try {
			httpclient.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void detectAddress(Address address, CloseableHttpClient httpclient) {
		long begin = System.currentTimeMillis();
		handlerResponse(address, getResponse(address, httpclient), begin);
	}

	private void handlerResponse(Address address, HttpResponse response, long begin) {
		long intervalTime = System.currentTimeMillis() - begin;
		if(response != null){
			int status = response.getStatusLine().getStatusCode();
			queue.add(new Message(address, status, intervalTime));
		}
	}

	private HttpResponse getResponse(Address address, CloseableHttpClient httpclient) {
		try {
			HttpGet httpGet =new HttpGet(address.getUrl());
			RequestConfig requestConfig = RequestConfig.custom().
					setConnectionRequestTimeout(5000).
					setSocketTimeout(5000).
					setConnectTimeout(5000).
					build();
			httpGet.setConfig(requestConfig);
			return httpclient.execute(httpGet);
		}catch(SocketTimeoutException e){
			queue.add(new Message(address, 888, 0));
			return null;
		}catch(ConnectTimeoutException e){
			queue.add(new Message(address, 888, 0));
			return null;
		}catch (ClientProtocolException e) {
			queue.add(new Message(address, 888, 0));
			return null;
		} catch (IOException e) {
			queue.add(new Message(address, 999, 0));
			return null;
		}
	}

	public static void main(String[] args) throws InterruptedException {
		int loopSize = 1;
		Observer observer = new Observer() {
			public void execute(Message data) {
				if (data.getStatus() == 200)
					print(System.out, data);
				else
					print(System.err, data);
			}

			private void print(PrintStream print, Message data) {
				BasicDBObject result = new BasicDBObject();
				result.append("ip", data.getIp());
				result.append("port", data.getPort());
				result.append("status", data.getStatus());
				result.append("time", data.getInterval());
				print.println(result.toString());
			}
		};
		DetectAddress detectAddress = new DetectAddress();
		detectAddress.registerObserver(observer);
		detectAddress.executeDefaultCategory(loopSize, 10);
	}

	

}