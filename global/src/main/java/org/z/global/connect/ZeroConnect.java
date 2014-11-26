package org.z.global.connect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.dict.CompressMode;
import org.z.global.dict.Device;
import org.z.global.dict.Global;
import org.z.global.dict.Global.HashMode;
import org.z.global.dict.MessageScope;
import org.z.global.dict.MessageType;
import org.z.global.dict.MessageVersion;
import org.z.global.environment.Const;
import org.z.global.object.SecurityObject;
import org.z.global.util.StringUtil;
import org.z.global.util.ZeromqUtil;
import org.z.global.zk.ServerDict;
import org.z.global.zk.ServerDict.NodeType;
import org.z.global.zk.ServiceName;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class ZeroConnect {

	protected static Logger logger = LoggerFactory.getLogger(ZeroConnect.class);

	public static DBObject send(Device device, ServiceName service, MessageScope scope, MessageType type, MessageVersion version, String msgTag, DBObject oReq, String remoteCallModuleName) {
		BasicDBList targets = null;
		BasicDBObject oServer = null;
		switch (scope) {
		case ROUTER:
			oServer = ServerDict.self.hashServer(NodeType.router, service, null, remoteCallModuleName);
			if (oServer != null) {
				targets = new BasicDBList();
				targets.add(oServer);
			}
			break;
		case APP:
				oServer = ServerDict.self.hashServer(NodeType.app, service, null, remoteCallModuleName);
			if (oServer != null) {
				targets = new BasicDBList();
				targets.add(oServer);
			}
			break;
		case ALLROUTER:
		case BROADCAST:
			targets = ServerDict.self.routersByTag(service.value);
			break;
		case ALLAPP:
			targets = ServerDict.self.appsByTag(service.value);
			break;
		}
		if (targets == null) {
			logger.error("Can't find any [{}] [{}] is running.", new Object[] { service.value, scope });
			return null;
		}
		CompressMode compress = null;
		byte[] content = StringUtil.toBytes(oReq.toString());
		byte[] compressContent = content;
		if (content.length >= Global.DataPacketLimit) {
			compress = CompressMode.SNAPPY;
			compressContent = StringUtil.compress(content);
		}
		if (compressContent == content) {
			compress = CompressMode.NONE;
		}
		System.out.println(oServer.getString("ip"));
		if (targets.size() == 1) {
			oServer = (BasicDBObject) targets.get(0);
			return sendBy(oServer.getString("ip"), oServer.getInt("port"), device, service, scope, type, version, msgTag, compress, compressContent, oReq);
		}
		BasicDBList results = new BasicDBList();
		for (int i = 0; i < targets.size(); i++) {
			oServer = (BasicDBObject) targets.get(i);
			results.add(sendBy(oServer.getString("ip"), oServer.getInt("port"), device, service, scope, type, version, msgTag, compress, compressContent, oReq));
		}
		return results;
	}

	public static DBObject sendBy(String ip, int port, Device device, ServiceName service, MessageScope scope, MessageType type, MessageVersion version, String msgTag, CompressMode compress,
			byte[] content, DBObject oReq) {
		long time = System.currentTimeMillis();
		ZMsg msg = new ZMsg();
		msg.add(device.getData());
		msg.add(service.value);
		msg.add(String.valueOf(service.ordinal()));
		msg.add(scope.getData());
		msg.add(type.getData());
		msg.add(version.getData());
		msg.add(ZeromqUtil.getBytes(StringUtil.createUniqueID()));
		msg.add(ZeromqUtil.getBytes(msgTag));
		msg.add(ZeromqUtil.MsgTo);
		msg.add(String.valueOf(time));
		msg.add(compress.getData());
		msg.add(content);
		ZeroSocket oSocket = null;
		content = null;
		DBObject oResult = null;
		String uri = "tcp://" + ip + ":" + port;
		String returnTag = "";
		try {
			if (Const.AppConnectPoolEnable) {
				oSocket = ZeroSocketFactory.get(uri, null, ZMQ.REQ);
			} else {
				oSocket = ZeroSocketFactory.create(uri, null, ZMQ.REQ);
			}
			if (oSocket == null || oSocket.isAlive == false) {
				return new BasicDBObject().append("message", "ZeroSocketFactory can't create socket");
			}

			ZMsg returnMsg = null;
			try {
				msg.send(oSocket.socket, false);
				returnMsg = ZMsg.recvMsg(oSocket.socket);
				oSocket.isAlive = !returnMsg.isEmpty() && returnMsg.getFirst().hasData() == true;
				if (oSocket.isAlive) {
					ZFrame v = returnMsg.pop();
					returnTag = StringUtil.toString(v.getData());
					v.destroy();

					v = returnMsg.pop();
					CompressMode mode = CompressMode.values()[Integer.parseInt(v.toString())];
					v.destroy();

					v = returnMsg.pop();
					byte[] bytes = v.getData();
					v.destroy();

					if (mode == CompressMode.SNAPPY) {
						bytes = StringUtil.uncompress(bytes);
					}
					oResult = (DBObject) JSON.parse(StringUtil.toString(bytes));
				}
			} catch (Exception e) {
				oSocket.isAlive = false;
				logger.error("sendBy[{}],exception:[{}]", new Object[] { uri, e.getLocalizedMessage() });
			} finally {
				ZeromqUtil.free(returnMsg);
			}
			// if (System.currentTimeMillis() - time > (1000 * 5)) {
			// errorSendByMongo.put(((BasicDBObject) oReq).append("ip",
			// ip).append("port", port).append("ServiceName",
			// service.name()).append("tag", msgTag));
			// }
			if (!returnTag.equalsIgnoreCase(msgTag)) {
				logger.warn("Uri=[{}] & MsgTag={} not match ReturnTag={},MsgType=[{}] & ServiceName=[{}]\n Require Content=[{}]",
						new Object[] { oSocket.uri, msgTag, returnTag, type.name(), service.name(), oReq.toString() });
			}
		} finally {
			ZeromqUtil.free(msg);
			if (Const.AppConnectPoolEnable) {
				ZeroSocketFactory.ret(uri, oSocket);
			} else {
				oSocket.destroy();
			}
		}
		return oResult;
	}


	public static DBObject post(ServiceName service, MessageType type, String msgTag, BasicDBObject oReq) {
		String host = "";
		BasicDBObject pageParams = (BasicDBObject) oReq.get("pageParams");
		if (pageParams != null) {
			host = pageParams.getString("host", ":").split(":")[0];
			if (host.startsWith("1")) {
				return sendBy(host, Global.MQAppServerPort, Device.BROWSER, service, MessageScope.APP, type, MessageVersion.MQ, msgTag, CompressMode.SNAPPY, ZeromqUtil.getBytes(oReq.toString()), oReq);
			}
		}
		return send(Device.BROWSER, service, Const.AppConnMode == HashMode.appserver ? MessageScope.APP : MessageScope.ROUTER, type, MessageVersion.MQ, msgTag, oReq, null);
	}

	public static DBObject readHtmlPage(String pageName, BasicDBObject pageParams, BasicDBObject cookies, BasicDBObject oReq) {
		oReq.append("action", "read");
		oReq.append("cookies", cookies);
		oReq.append("pageName", pageName);
		oReq.append("pageParams", pageParams);
		return post(ServiceName.HtmlPage, MessageType.REQUEST, "0", oReq);
	}
	public static DBObject readHtmlPage(String pageName, BasicDBObject oReq) {
		oReq.append("action", "read");
		oReq.append("pageName", pageName);
		return post(ServiceName.HtmlPage, MessageType.REQUEST, "0", oReq);
	}

	public static DBObject updateIndex(ServiceName service, String msgTag, DBObject oDoc) {
		return send(Device.SERVER, service, Global.DevelopMode ? MessageScope.ALLROUTER : MessageScope.BROADCAST, MessageType.UPDATE, MessageVersion.MQ, msgTag, oDoc, null);
	}

	public static DBObject removeIndex(ServiceName service, BasicDBObject oDoc) {
		return send(Device.SERVER, service, Global.DevelopMode ? MessageScope.ALLROUTER : MessageScope.BROADCAST, MessageType.REMOVE, MessageVersion.MQ, "TAG", oDoc, null);
	}

	public static DBObject commitIndex(ServiceName service) {
		return send(Device.SERVER, service, Global.DevelopMode ? MessageScope.ALLROUTER : MessageScope.BROADCAST, MessageType.COMMIT, MessageVersion.MQ, "TAG", ZeromqUtil.EMPEYOBJECT, null);
	}

	public static DBObject syncCatalog(ServiceName service, BasicDBObject oDoc) {
		return send(Device.SERVER, service, Global.AppConnMode == Global.HashMode.appserver ? MessageScope.APP : MessageScope.ROUTER, MessageType.SYNC, MessageVersion.MQ, "catalog", oDoc, null);
	}

	public static DBObject updateCategory(ServiceName service, BasicDBObject oDoc) {
		return send(Device.SERVER, service, Global.AppConnMode == Global.HashMode.appserver ? MessageScope.APP : MessageScope.ROUTER, MessageType.SYNC, MessageVersion.MQ, "category", oDoc, null);
	}

	public static DBObject searchIndex(ServiceName service, String msgTag, BasicDBObject oReq) {
		return send(Device.SERVER, service, Const.AppConnMode == HashMode.appserver ? MessageScope.APP : MessageScope.ROUTER, MessageType.SEARCH, MessageVersion.MQ, msgTag, oReq, null);
	}

	public static DBObject eyeBussinessSend(String msgTag, BasicDBObject req) {
		return send(Device.SERVER, ServiceName.Dict, MessageScope.APP, MessageType.UPDATE, MessageVersion.MQ, msgTag, req, null);
	}
	public static DBObject sendSocket(String server, BasicDBObject oReq) {
		BasicDBObject oServer = ServerDict.self.serverBy(server);
		if (oServer == null)
			return null;
		return sendBy(oServer.getString("ip"), Global.MQAppServerPort, Device.SERVER, ServiceName.Server, MessageScope.APP, MessageType.SOCKET, MessageVersion.MQ, "socket", CompressMode.NONE,
				StringUtil.toBytes(oReq.toString()), oReq);
	}



	public static DBObject remoteCall(String moduleName, String methodName, Object... args) {
		BasicDBObject oReq = new BasicDBObject();
		oReq.put("moduleName", moduleName);
		oReq.put("methodName", methodName);
		oReq.put("args", args);
		return send(Device.SERVER, ServiceName.RemoteCall, Const.AppConnMode == HashMode.appserver ? MessageScope.APP : MessageScope.ROUTER, MessageType.REMOTECALL, MessageVersion.MQ, "remoteCall",
				oReq, moduleName);
	}

	public static DBObject offline(HashMode mode, String ip) {
		return sendBy(ip, mode == HashMode.appserver ? Global.MQAppServerPort : Global.MQRouterPort, Device.BROWSER, ServiceName.Server, mode == HashMode.appserver ? MessageScope.APP
				: MessageScope.ROUTER, MessageType.OFFLINE, MessageVersion.MQ, "", CompressMode.NONE, ZeromqUtil.EMPTY, ZeromqUtil.EMPEYOBJECT);
	}

	public static DBObject online(HashMode mode, String ip) {
		return sendBy(ip, mode == HashMode.appserver ? Global.MQAppServerPort : Global.MQRouterPort, Device.BROWSER, ServiceName.Server, mode == HashMode.appserver ? MessageScope.APP
				: MessageScope.ROUTER, MessageType.ONLINE, MessageVersion.MQ, "", CompressMode.NONE, ZeromqUtil.EMPTY, ZeromqUtil.EMPEYOBJECT);
	}

	public static DBObject changeRouter(String appIP, String routerId) {
		return sendBy(appIP, Global.MQAppServerPort, Device.SERVER, ServiceName.Server, MessageScope.APP, MessageType.UPDATE, MessageVersion.MQ, "", CompressMode.NONE, ZeromqUtil.getBytes(routerId),
				ZeromqUtil.EMPEYOBJECT);
	}

	public static DBObject readAppServers(String routerIP) {
		return sendBy(routerIP, Global.MQRouterPort, Device.BROWSER, ServiceName.Server, MessageScope.ROUTER, MessageType.READ, MessageVersion.MQ, "appservers", CompressMode.NONE, ZeromqUtil.EMPTY,
				ZeromqUtil.EMPEYOBJECT);
	}

	public static DBObject readServerRuntime(HashMode mode, String ip) {
		return sendBy(ip, mode == HashMode.appserver ? Global.MQAppServerPort : Global.MQRouterPort, Device.BROWSER, ServiceName.Server, mode == HashMode.appserver ? MessageScope.APP
				: MessageScope.ROUTER, MessageType.READ, MessageVersion.MQ, "runtime", CompressMode.NONE, ZeromqUtil.EMPTY, ZeromqUtil.EMPEYOBJECT);
	}

	public static DBObject readServerId(HashMode mode, String ip) {
		return sendBy(ip, mode == HashMode.appserver ? Global.MQAppServerPort : Global.MQRouterPort, Device.BROWSER, ServiceName.Server, mode == HashMode.appserver ? MessageScope.APP
				: MessageScope.ROUTER, MessageType.READ, MessageVersion.MQ, "serverId", CompressMode.NONE, ZeromqUtil.EMPTY, ZeromqUtil.EMPEYOBJECT);
	}

	public static DBObject readSkuId(ServiceName service, String msgTag, BasicDBList oReq) {
		return send(Device.SERVER, service, Const.AppConnMode == HashMode.appserver ? MessageScope.APP : MessageScope.ROUTER, MessageType.REQUEST, MessageVersion.MQ, msgTag, oReq, null);

	}

	public static BasicDBObject requestBy(String catalog, String question, int pageSize, int pageNumber) {
		BasicDBObject oReq = new BasicDBObject();
		oReq.append("catalog", catalog);
		oReq.append("pageSize", pageSize);
		oReq.append("pageNumber", pageNumber);
		oReq.append("question", question);
		return oReq;
	}

	public static String[] doSecurity(SecurityObject... objects) {
		BasicDBList items = new BasicDBList();
		for (int i = 0; i < objects.length; i++) {
			SecurityObject security = objects[i];
			BasicDBObject sObject = new BasicDBObject();
			sObject.append("security", security.type.name());
			sObject.append("mode", security.mode.name());
			sObject.append("content", security.content);
			items.add(sObject);
		}
		BasicDBList list = (BasicDBList) send(Device.SERVER, ServiceName.Dict, MessageScope.ROUTER, MessageType.SECURITY, MessageVersion.MQ, "security", items, null);
		if (list == null) {
			logger.error("doSecurity return null");
			return null;
		}
		String[] results = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			results[i] = String.valueOf(list.get(i));
		}
		return results;
	}

	public static BasicDBObject checkCookieId(String cookieId) {
		if (StringUtil.isEmpty(cookieId))
			return null;
		BasicDBObject oReq = new BasicDBObject().append("action", "checkCookie").append("cookieId", cookieId);
		BasicDBObject oResult = (BasicDBObject) send(Device.SERVER, ServiceName.Server, Const.AppConnMode == HashMode.appserver ? MessageScope.APP : MessageScope.ROUTER, MessageType.READ,
				MessageVersion.MQ, "checkCookie", oReq, null);
		return oResult;
	}

	private static class ZeroThread extends Thread {
		protected int index = 0;
		private BasicDBList servers = null;

		public ZeroThread(int index, BasicDBList servers) {
			this.index = index;
			this.servers = servers;
		}

		@Override
		public void run() {
			for (int i = 0; i < servers.size(); i++) {
				BasicDBObject server = (BasicDBObject) servers.get(i);
				String ip = server.getString("ip");
				System.out.println("ip: " + ip + "  index=" + index + ":" + readServerRuntime(HashMode.router, ip));
			}

		}
	}

	private static class searchThread extends Thread {
		protected BasicDBObject oReq = null;
		protected int index = 0;

		public searchThread(int index, BasicDBObject oReq) {
			this.index = index;
			this.oReq = oReq;
		}

		@Override
		public void run() {
			System.out.println(index + ":" + ZeroConnect.searchIndex(ServiceName.ProductIndex, ObjectId.get().toString(), oReq).toString());
		}
	}

	private static class updateThread extends Thread {
		protected BasicDBObject oReq = null;
		protected int index = 0;

		public updateThread(int index, BasicDBObject oReq) {
			this.index = index;
			this.oReq = oReq;
		}

		@Override
		public void run() {
			String content = oReq.toString();
			ZeroConnect.sendBy(Global.ServerIP, Global.MQRouterPort, Device.SERVER, ServiceName.ProductIndex, MessageScope.BROADCAST, MessageType.UPDATE, MessageVersion.MQ, String.valueOf(index),
					CompressMode.NONE, ZeromqUtil.getBytes(content), oReq);
			System.out.println(index);
		}
	}

	public static void main(String[] args) {
		int mode = 10;
		long time = System.currentTimeMillis();
		switch (mode) {
		case 22:
			System.out.println(ZeroConnect.updateIndex(ServiceName.ProductIndex, "promoTag", new BasicDBObject().append("tag", "promoTag")));
			break;
		case 21:
			ZeroConnect.updateIndex(ServiceName.ProductIndex, "remove", new BasicDBObject().append("id", "9129931913"));
			break;

		case 19:
			BasicDBObject oData = new BasicDBObject();
			for (int i = 0; i < 100000; i++) {
				oData.append("content", "xiao" + i);
				updateThread t = new updateThread(i, oData);
				t.start();
			}
			break;
		case 18:
			System.out.println(ZeroConnect.remoteCall("spider", "startSpider", new Object[] { "/bin/sh -c 'export DISPLAY=:1.0;java -jar /server/gomespider.jar /server/zdx.log /server/jd.xml'" }));
			break;
		case 17:
			BasicDBList productIds = new BasicDBList();
			productIds.add("9010000624");
			BasicDBList catIds = new BasicDBList();
			catIds.add("cat10795542");
			System.out.println(ZeroConnect.remoteCall("facet", "getProductFacetByProductId", new Object[] { productIds }));
			System.out.println(ZeroConnect.remoteCall("facet", "getCategoryFacetByCategoryId", new Object[] { catIds }));
			// System.out.println(ZeroConnect.remoteCall("productindex", "test",
			// new Object[] { "catId", 123L, 456 }));
			break;
		case 12:
			String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Do, "xiaoming@yiqihi.com"),
					SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Do, "jason"));
			System.out.println(values[0] + "====" + values[1]);
			break;
		case 11:
			BasicDBList servers = ServerDict.self.routers();
			for (int i = 0; i < 1000000; i++) {
				ZeroThread t = new ZeroThread(i, servers);
				t.start();
			}
			break;
		case 10:
			BasicDBObject obj1 = new BasicDBObject().append("question", "手机").append("pageNumber", 1).append("pageSize", 8).append("docCount", false).append("catId", "cat10000070");
			BasicDBObject obj2 = (BasicDBObject) ZeroConnect.searchIndex(ServiceName.ProductIndex, ObjectId.get().toString(), obj1);
			BasicDBObject oResult2 = (BasicDBObject) obj2.get("response");
			BasicDBObject oResult1 = (BasicDBObject) oResult2.get("results");
			System.out.println(oResult1.toString());
			break;
		case 9:
			System.out.println(offline(HashMode.appserver, "10.57.41.213"));
			System.out.println(online(HashMode.appserver, "10.57.41.213"));
			break;
		case 8:
			System.out.println(readServerRuntime(HashMode.appserver, "10.57.41.213"));
			break;
		case 7:
			System.out.println(readAppServers("10.57.41.213"));
			break;
		case 6:
			System.out.println(readServerRuntime(HashMode.router, "10.57.41.204"));
			break;
		case 5:
			System.out.println(offline(HashMode.router, "10.57.41.213"));
			break;
		case 0:
			BasicDBObject pageParams = new BasicDBObject();
			BasicDBObject cookies = new BasicDBObject();
			String pageName = "index";
			for (int i = 0; i < 10000; i++) {
				System.out.println(i++ + "==" + ZeroConnect.readHtmlPage(pageName, pageParams, cookies, new BasicDBObject()));
			}
			break;
		case 1:
			ZeroConnect.commitIndex(ServiceName.ProductIndex);
			String query = "*";
			BasicDBObject oReq = new BasicDBObject().append("key", query);
			String id = StringUtil.createUniqueID();
			System.out.println(ZeroConnect.searchIndex(ServiceName.ProductIndex, id, oReq));
			break;
		case 2:
			oReq = new BasicDBObject();
			for (int i = 1; i < 10; i++) {
				String msgTag = "search[" + i + "]";
				try {
					System.out.println("waiting " + i);
					System.in.read();
				} catch (Exception e) {
				}
				DBObject oResult = searchIndex(ServiceName.ProductIndex, msgTag, oReq);
				if (oResult != null) {
					System.out.println(msgTag + "=" + oResult.toString());
				}
			}

		case 3:
			File file = new File("/users/user/product.txt");
			int count = 0;
			try {
				FileInputStream _fileStream = new FileInputStream(file);
				InputStreamReader _reader = new InputStreamReader(_fileStream, "utf-8");
				BufferedReader br = new BufferedReader(_reader);
				String line = null;
				oReq = new BasicDBObject();
				while (((line = br.readLine()) != null)) {
					values = line.split("\\t");
					if (values.length < 5) {
						continue;
					}
					int index = 0;
					count++;
					oReq.append("id", values[index++]);
					oReq.append("title", values[index++]);
					String v = values[index++];
					int i = v.indexOf(".");
					if (i < 0) {
						continue;
					}
					v = v.substring(0, i);
					oReq.append("price", StringUtil.intOf(v));
					oReq.append("url", values[index++]);
					oReq.append("snapshot", values[index++]);
					oReq.append("catalog", values[index++]);
					System.out.println("update DocId[" + oReq.getString("id") + "] Title=" + oReq.getString("title"));
					ZeroConnect.updateIndex(ServiceName.ProductIndex, "product", oReq);
				}
				ZeroConnect.commitIndex(ServiceName.ProductIndex);
				_reader.close();
				_fileStream.close();
				System.out.println("add document Count=" + count);
			} catch (Exception E) {
				E.printStackTrace();
			}
			break;
		case 4:
			for (int i = 0; i < 10000000; i++) {
				query = "";
				oReq = new BasicDBObject().append("question", query);
				oReq.append("catalog", "homeStoreCatalog");
				oReq.append("catId", "cat10000078");
				System.out.println(ZeroConnect.searchIndex(ServiceName.ProductIndex, ObjectId.get().toString(), oReq));
			}
			break;
		case 14:
			oReq = new BasicDBObject();
			oReq.append("catalog", "homeStore");
			oReq.append("catId", "cat10000078");
			searchThread s = null;
			for (int i = 0; i < 100; i++) {
				s = new searchThread(i, oReq);
				s.start();
			}
			break;
		case 13:
			oReq = new BasicDBObject();
			oReq.append("price", 7000);
			oReq.append("onSaleStartDate", System.currentTimeMillis() + 70 * 1000);
			oReq.append("onSaleEndDate", System.currentTimeMillis() + 30 * 1000);
			oReq.append("skuId", "9010000918");
			ZeroConnect.updateIndex(ServiceName.ProductIndex, "product", oReq);
			break;
		}
		System.out.println("done!, usedtime=" + (System.currentTimeMillis() - time));

	}

}
