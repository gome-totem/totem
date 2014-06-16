package org.z.global.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.z.global.ip.IPSeeker;
import org.z.global.util.StringUtil;
import org.z.global.util.XPathTool;

@SuppressWarnings("deprecation")
public class NetTool {
	protected static HashMap<String, String> tableAddr = new HashMap<String, String>();
	public static IPSeeker IPSeeeker = null;
	protected static Logger logger = LoggerFactory.getLogger(NetTool.class);

	public static String getUrlContent(String url) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
				@Override
				public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
					if (!request.containsHeader("Accept-Encoding")) {
						request.addHeader("Accept-Encoding", "gzip");
					}
				}
			});

			httpclient.addResponseInterceptor(new HttpResponseInterceptor() {

				public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						Header ceheader = entity.getContentEncoding();
						if (ceheader != null) {
							HeaderElement[] codecs = ceheader.getElements();
							for (int i = 0; i < codecs.length; i++) {
								if (codecs[i].getName().equalsIgnoreCase("gzip")) {
									response.setEntity(new GzipDecompressingEntity(response.getEntity()));
									return;
								}
							}
						}
					}
				}

			});

			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity);
		} catch (Exception e) {

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
		return null;
	}

	@SuppressWarnings("unused")
	public static String readRedirectUrl(String uri) {
		try {
			final java.net.URL url = new java.net.URL(uri);
			final java.net.HttpURLConnection httpURLConnection = (java.net.HttpURLConnection) url.openConnection();
			httpURLConnection.setInstanceFollowRedirects(false);
			httpURLConnection.connect();
			final int responseCode = httpURLConnection.getResponseCode();
			final java.lang.String header = httpURLConnection.getHeaderField("Location");
			return header;
		} catch (Exception e) {
			logger.error("readRedirectUrl", e);
			return null;
		}
	}

	public static String get(String url, String charset) {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, charset);
		params.setBooleanParameter("http.protocol.expect-continue", false);
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
		HttpClient httpclient = new DefaultHttpClient(manager, params);
		httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		HttpGet httpget = new HttpGet(url);
		String html = null;
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity httpEntity = response.getEntity();
			if (httpEntity != null) {
				byte[] bytes = EntityUtils.toByteArray(httpEntity);
				httpEntity.consumeContent();
				html = new String(bytes, charset);
			}
		} catch (Exception e) {
			logger.error("get", e);
		}
		return html;
	}

	public static String post(String url, List<NameValuePair> nvps) {
		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		HttpPost httppost = new HttpPost(url);
		String html = null;
		try {
			httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity httpEntity = response.getEntity();

			if (httpEntity != null) {
				html = EntityUtils.toString(httpEntity);
				httpEntity.consumeContent();
			}
		} catch (Exception e) {
			logger.error("post", e);
		}
		return html;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String post(String url, Map map) {
		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		List<NameValuePair> nvps = new ArrayList();
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			NameValuePair nvp = new BasicNameValuePair(key, value);
			nvps.add(nvp);
		}
		String html = null;
		try {
			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity httpEntity = response.getEntity();

			if (httpEntity != null) {
				html = EntityUtils.toString(httpEntity);
				httpEntity.consumeContent();
			}
		} catch (Exception e) {
			logger.error("post", e);

		}
		return html;
	}

	public static byte[] getImage(String url) {
		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		byte[] b = null;
		try {
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity httpEntity = response.getEntity();
			b = EntityUtils.toByteArray(httpEntity);
			if (httpEntity != null) {
				httpEntity.consumeContent();
			}
		} catch (Exception e) {
			logger.error("getImage", e);
		}
		return b;
	}

	@SuppressWarnings("unused")
	public static String searchTaobao(String q, String cityName) {
		String query = q;
		String city = "";
		try {
			query = java.net.URLEncoder.encode(q, "gbk");
			if (!cityName.equalsIgnoreCase("全国")) {
				city = "&loc=" + java.net.URLEncoder.encode(cityName, "gbk");
			}
		} catch (Exception e) {
			logger.error("searchTaobao", e);
			query = q;
		}
		String url = "http://s.taobao.com/search?q=" + query + "&style=list";
		return get(url, "gbk");
	}

	public static double[] getLocation(String address) {
		String encodeAddr = address;
		try {
			encodeAddr = java.net.URLEncoder.encode(address, "UTF-8");
		} catch (Exception e) {
			encodeAddr = address;
		}
		String output = "csv";
		String key = "ABQIAAAA8KLvoJDfw7YieIih4Ur5IBQKnzkbsgq4LQvXpURFr6lnkPPSgRTviliReQgP7cLY7Sipyq3ZAC_hpg";
		String url = "http://maps.google.com/maps/geo?q=" + encodeAddr + "&output=" + output + "&key=" + key + "&sensor=false&oe=utf8";
		String content = get(url, "utf-8");
		if (content == null)
			return null;
		String[] tmpArray = content.split(",");
		if (tmpArray.length < 4)
			return null;
		double[] results = new double[2];
		results[0] = Double.parseDouble(tmpArray[2]);
		results[1] = Double.parseDouble(tmpArray[3]);
		return results;
	}

	@SuppressWarnings("unused")
	public static boolean downloadImage(String url, String fileName) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		InputStream reader = null;
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				reader = entity.getContent();
				ByteArrayOutputStream outstream = new ByteArrayOutputStream(4096);
				byte[] buffer = new byte[4096];
				int total = 0;
				int len;
				while ((len = reader.read(buffer)) > 0) {
					total += len;
					outstream.write(buffer, 0, len);
				}
				OutputStream file = new FileOutputStream(fileName);
				outstream.writeTo(file);
				outstream.close();
				file.close();
				return true;
			}
		} catch (Exception ex) {
			logger.error("downloadImage", ex);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				httpclient.getConnectionManager().shutdown();
			} catch (IOException e) {
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	public static byte[] downloadBytes(String url) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		InputStream reader = null;
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				reader = entity.getContent();
				ByteArrayOutputStream outstream = new ByteArrayOutputStream(4096);
				byte[] buffer = new byte[4096];
				int total = 0;
				int len;
				while ((len = reader.read(buffer)) > 0) {
					total += len;
					outstream.write(buffer, 0, len);
				}
				return outstream.toByteArray();
			}
		} catch (Exception ex) {
			logger.error("downloadBytes", ex);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				httpclient.getConnectionManager().shutdown();
			} catch (IOException e) {
			}
		}
		return null;
	}

	@SuppressWarnings("unused")
	protected static byte[] convertToBytes(InputStream stream) {
		try {
			ByteArrayOutputStream outstream = new ByteArrayOutputStream(4096);
			byte[] buffer = new byte[4096];
			int total = 0;
			int len;
			while ((len = stream.read(buffer)) > 0) {
				total += len;
				outstream.write(buffer, 0, len);
			}
			outstream.close();
			return outstream.toByteArray();
		} catch (Exception e) {
			logger.error("convertToBytes", e);
			return null;
		}
	}

	public static String getCharset(String contentType) {
		contentType = contentType.toLowerCase();
		int pos = contentType.indexOf("charset=");
		if (pos < 0)
			return null;
		String sub = contentType.substring(pos + 8);
		int pos2 = sub.indexOf(';');
		if (pos2 < 0)
			return sub.trim();
		else
			return sub.substring(0, pos2).trim();
	}

	public static String downloadContent(String url) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = "";
		try {
			responseBody = httpclient.execute(httpget, responseHandler);
		} catch (Exception e) {
			logger.error("downloadContent", e);
		}
		httpclient.getConnectionManager().shutdown();
		return responseBody;
	}

	public static String postContent(String url) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpget = new HttpPost(url);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = "";
		try {
			responseBody = httpclient.execute(httpget, responseHandler);
		} catch (Exception e) {
			logger.error("postContent", e);
		}
		httpclient.getConnectionManager().shutdown();
		return responseBody;
	}

	public static String getLatlng(String address) {
		if (tableAddr.containsKey(address)) {
			return tableAddr.get(address);
		}
		String ret = "";
		try {
			address = URLEncoder.encode(address, "UTF-8");// 进行这一步是为了避免乱码
		} catch (UnsupportedEncodingException e1) {
		}
		Object[] arr = new String[4];
		arr[0] = address;
		arr[1] = "csv";
		arr[2] = "true";
		arr[3] = "AIzaSyCDgJL_AR29u2g5lsZsFwdYk4BWhla9PKw";
		String url = MessageFormat.format("http://maps.google.com/maps/geo?q={0}&output={1}&sensor={2}&key={3}", arr);
		URL urlmy = null;
		try {
			urlmy = new URL(url);
			HttpURLConnection con = (HttpURLConnection) urlmy.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			con.setInstanceFollowRedirects(false);
			con.connect();
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			String s = "";
			StringBuffer sb = new StringBuffer("");
			while ((s = br.readLine()) != null) {
				sb.append(s + "\r\n");
			}
			ret = "" + sb;
			ret = StringUtil.trim(ret);
			tableAddr.put(address, ret);
		} catch (Exception e) {
			logger.error("getLatlng", e);
		}
		return ret;
	}

	public static String getCoordinate(String address) {
		try {
			String content = downloadContent("http://maps.google.com/maps/geo?" + "q=" + address + "&output=xml" + "&sensor=false&key=abcdefg");
			Document doc = XPathTool.parseContent(content);
			Element root = doc.getDocumentElement();
			Node n = XPathTool.selectSingleNode(root, "//coordinates");
			String coordinate = n.getTextContent();
			return coordinate;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getLocalIP() {
		Enumeration<NetworkInterface> netInterfaces = null;
		String ip = "127.0.0.1";
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					InetAddress ia = (InetAddress) ips.nextElement();
					if (ia instanceof Inet4Address) {
						if (ia.getHostAddress().toString().startsWith("127")) {
							continue;
						} else {
							return ia.getHostAddress();
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("getLocalIP", e);
		}
		return ip;
	}

	public static String getLocalName() {
		try {
			Runtime run = Runtime.getRuntime();
			Process proc = run.exec("hostname");
			StringWriter writer = new StringWriter();
			IOUtils.copy(proc.getInputStream(), writer, "utf-8");
			return StringUtil.trim(writer.toString());
		} catch (Exception e) {
			return "unknow";
		}
	}

	public static float UsdToCny() {
		String url = "http://www.google.com/ig/calculator?hl=en&q=1USD%3D%3FRMB";
		String content = "6.3";
		try {
			content = NetTool.get(url, "utf-8");
			if (content.length() == 0) {
				return (float) 6.3;
			}
			int start = content.indexOf("rhs: \"");
			int stop = content.indexOf("\",error:");
			content = content.substring(start + 6, stop);
			String[] values = content.split("\\ ");
			content = values[0];
		} catch (Exception e) {
			return (float) 6.3;
		}
		return Float.parseFloat(content);
	}

	public static float EurToCny() {
		String url = "http://www.google.com/ig/calculator?hl=en&q=1EUR%3D%3FRMB";
		String content = "8.26";
		try {
			content = NetTool.get(url, "utf-8");
			if (content.length() == 0) {
				return (float) 8.26;
			}
			int start = content.indexOf("rhs: \"");
			int stop = content.indexOf("\",error:");
			content = content.substring(start + 6, stop);
			String[] values = content.split("\\ ");
			content = values[0];
		} catch (Exception e) {
			return (float) 8.26;
		}
		return Float.parseFloat(content);
	}

	public static void main(String[] args) {
		NetTool.postContent("http://127.0.0.1:8080?id=jason");
		downloadImage("http://www.autoimg.cn/logo/brand/75/129302239162870000.jpg", "d:/1.jpg");
		System.out.println(downloadContent("http://www.baidu.com"));
		System.out
				.println(downloadContent("http://dsc06.taobao.com/i6/411/ea0/419ea535be0e41f9ccd98d149bdcdbf8/T18eVyXhdvXXXXXXXX.desc%7Cvar%5Edesc;sign%5Ee64cca94ed35e555cf5b8f37bd6787ca;lang%5Egbk;t%5E1272093592"));
		System.out.println(NetTool.getLocalIP());
	}
}
