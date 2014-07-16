package org.z.core.common;

import java.util.Random;

import org.bson.BSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.common.joor.Reflect;
import org.z.core.app.ModuleZeromq;
import org.z.core.interfaces.ServiceIntf;
import org.z.core.module.ModuleProduct;
import org.z.core.queue.ModuleMsg;
import org.z.global.dict.Global.LogLevel;
import org.z.global.dict.Global.ModuleMessageType;
import org.z.global.dict.Global.SearchMode;
import org.z.global.dict.MessageScope;
import org.z.global.dict.MessageType;
import org.z.global.environment.Business.ClassName;
import org.z.global.factory.ModuleFactory;
import org.z.global.interfaces.IndexServiceIntf;
import org.z.global.interfaces.ModuleIntf;
import org.z.global.interfaces.ModuleProcessorIntf;
import org.z.global.interfaces.ModuleQueueIntf;
import org.z.global.object.LogMessage;
import org.z.global.util.DBObjectUtil;
import org.z.global.zk.ServiceName;
import org.z.store.redis.RedisPool;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;


public class ModuleProcessor implements ModuleProcessorIntf {
	protected static Logger logger = LoggerFactory.getLogger(ModuleProcessor.class);
	protected ModuleZeromq mq = null;
	@SuppressWarnings("unused")
	private ModuleQueueIntf queue = null;
	protected Random rdm = new Random();

	@Override
	public void afterCreate(Object[] params) {
	}

	@Override
	public boolean init(boolean isReload) {
		queue = (ModuleQueueIntf) ModuleFactory.queue();
		mq = (ModuleZeromq) ModuleFactory.moduleInstanceBy("mq");
		return true;
	}

	public void handleHtmlPage(BasicDBObject oRequest, BasicDBObject oResult) {
		Context ctx = new Context();
		ctx.initRequest(ModuleFactory.htmlPage(), oRequest);
		ctx.initUser();
		ctx.initHtmlPage();
		oResult.append("message", ctx.renderPage());
		oResult.append("cityName", ctx.cityName);
		oResult.append("xeach", true);
	}

	public void handleServlet(BasicDBObject oRequest, BasicDBObject oResult) {
		int classIndex = oRequest.getInt("classIndex");
		ClassName className = ClassName.values()[classIndex];
		try {
			ServiceIntf service = ServiceFactory.byClass(className, (BasicDBList) oRequest.get("classParams"));
			if (service == null) {
				oResult.append("message", "Module没有启动,请检查");
				return;
			}
			Context ctx = new Context();
			ctx.initRequest(ModuleFactory.htmlPage(), oRequest);
			ctx.initUser();
			BasicDBObject oReturn = (BasicDBObject) service.handle(ctx, oRequest);
			DBObjectUtil.merge(oResult, oReturn);
		} catch (Exception e) {
			oResult.append("message", e.getLocalizedMessage());
			logger.error("handle Servlet ClassName[{}] & Error[{}] ", new Object[] { className.name(), e.getLocalizedMessage() });
		}
	}

	public void handleRemoteCall(BasicDBObject oReq, BasicDBObject oResult) {
		String moduleName = oReq.getString("moduleName");
		ModuleIntf module = ModuleFactory.moduleInstanceBy(moduleName);
		if (module == null) {
			oResult.append("success", false);
			oResult.append("msg", moduleName + "没有被注册");
		} else {
			String methodName = oReq.getString("methodName");
			BasicDBList args = (BasicDBList) oReq.get("args");
			Object[] methodArgs = new Object[args.size()];
			for (int i = 0; i < args.size(); i++) {
				methodArgs[i] = args.get(i);
			}
			try {
				Object returnValue = Reflect.on(module).call(methodName, methodArgs).get();
				if (returnValue == module) {
					oResult.append("returnValue", "void");
				} else {
					oResult.append("returnValue", returnValue);
				}
				oResult.append("success", true);
			} catch (Exception e) {
				oResult.append("success", false);
				oResult.append("msg", e.getMessage());
			}
		}
	}

//	public void handleUploadPicture(BasicDBObject oReq, BasicDBObject oResult) {
//		ArrayList<MagickImage> destroies = new ArrayList<MagickImage>();
//		byte[] bytes = null;
//		BASE64Decoder decoder = new BASE64Decoder();
//		String fileName = oReq.getString("fileName");
//		try {
//			bytes = decoder.decodeBuffer(oReq.getString("stream"));
//			MagickImage image = ImageUtil.readMagickImage(bytes, destroies);
//			Dimension dim = image.getDimension();
//			if (dim == null || dim.width <= 360 || dim.height <= 360) {
//				oResult.append("xeach", false).append("status", -1).append("info", "图片宽度和高度都必须大于360像素");
//				return;
//			}
//			boolean receipt = oReq.getBoolean("receipt", false);
//			String[] values = null;
//			if (receipt) {
//				values = writeReceiptFile(oReq, oReq.getString("fileExt"), fileName, image, oReq.getBoolean("watermark"), destroies);
//			} else {
//				values = writePictureFile(oReq, oReq.getString("fileExt"), fileName, image, oReq.getBoolean("watermark"), destroies);
//			}
//			int pos = fileName.lastIndexOf(".");
//			String title = fileName;
//			if (pos > 0) {
//				title = title.substring(0, pos);
//			}
//			oResult.append("xeach", true);
//			oResult.append("title", title);
//			oResult.append("name", fileName);
//			if (receipt) {
//				oResult.append("receipt", values[0]);
//			} else {
//				oResult.append("shot", values[0]);
//				oResult.append("picture", values[1]);
//			}
//		} catch (Exception e) {
//			oResult.append("xeach", false).append("fileName", fileName).append("status", -1).append("info", "图片错误:" + e.getLocalizedMessage());
//		} finally {
//			ImageUtil.free(destroies);
//		}
//	}

//	public String[] writeReceiptFile(BasicDBObject oUser, String contentType, String fileName, MagickImage source, boolean watermark,
//			ArrayList<MagickImage> destroies) {
//		long userId = oUser.getLong("userId");
//		source = ImageUtil.resize(ImageType.Picture, source, destroies);
//		if (watermark) {
//			ImageUtil.watermark(source, rdm.nextInt(50), rdm.nextInt(130));
//		}
//
//		BasicDBList roles = new BasicDBList();
//		roles.add(new BasicDBObject().append("value", userId).append("type", 1));
//		roles.add(new BasicDBObject().append("value", new UserRole().set(Role.Root)).append("type", 2));
//		roles.add(new BasicDBObject().append("value", new UserRole().set(Role.Accounter)).append("type", 2));
//		String roleUserIdStr = oUser.getString("roleUserIds");
//		if (EmptyUtil.notEmpty(roleUserIdStr)) {
//			String[] userIds = roleUserIdStr.split(",");
//			for (String str : userIds) {
//				roles.add(new BasicDBObject().append("value", str).append("type", 1));
//			}
//		}
//		BasicDBObject meta = new BasicDBObject().append("userId", userId).append("lastModified", System.currentTimeMillis()).append("roles", roles);
//		String receiptId = ModuleAppDict.self.receipts.saveFile(ImageUtil.readBytes(source), userId + "~" + fileName, contentType, meta);
//		meta.append("receiptId", receiptId);
//		String tableName = "i" + userId;
//		BasicDBObject oRecord = new BasicDBObject();
//		oRecord.append("fileName", fileName);
//		int pos = fileName.lastIndexOf(".");
//		String title = fileName;
//		if (pos > 0) {
//			title = title.substring(0, pos);
//		}
//		oRecord.append("title", title).append("tag", "").append("state", RecordState.New.ordinal());
//		oRecord.append("lastModified", System.currentTimeMillis());
//		oRecord.append("contentType", contentType);
//		oRecord.append("receipt", receiptId);
//		oRecord.append("source", "upload");
//		DataCollection.insert(oUser.getString("server"), "receipts", tableName, oRecord);
//		return new String[] { receiptId };
//	}
//
//	public String[] writePictureFile(BasicDBObject oUser, String contentType, String fileName, MagickImage source, boolean watermark,
//			ArrayList<MagickImage> destroies) {
//		long userId = oUser.getLong("userId");
//		MagickImage shotImage = ImageUtil.resize(ImageType.Snapshot, source, destroies);
//		source = ImageUtil.resize(ImageType.Picture, source, destroies);
//		if (watermark) {
//			ImageUtil.watermark(source, rdm.nextInt(50), rdm.nextInt(130));
//		}
//		BasicDBObject meta = new BasicDBObject().append("userId", userId).append("lastModified", System.currentTimeMillis());
//		String pictureId = ModuleAppDict.self.pictures.saveFile(ImageUtil.readBytes(source), userId + "~" + fileName, contentType, meta);
//		meta.append("pictureId", pictureId);
//		String shotId = ModuleAppDict.self.shots.saveFile(ImageUtil.readBytes(shotImage), oUser.getLong("userId") + "~" + fileName, contentType, meta);
//		String tableName = "i" + userId;
//		BasicDBObject oRecord = new BasicDBObject();
//		oRecord.append("fileName", fileName);
//		int pos = fileName.lastIndexOf(".");
//		String title = fileName;
//		if (pos > 0) {
//			title = title.substring(0, pos);
//		}
//		oRecord.append("title", title).append("tag", "").append("state", RecordState.New.ordinal());
//		oRecord.append("lastModified", System.currentTimeMillis());
//		oRecord.append("contentType", contentType);
//		oRecord.append("shot", shotId).append("picture", pictureId);
//		oRecord.append("source", "upload");
//		DataCollection.insert(oUser.getString("server"), "pictures", tableName, oRecord);
//		return new String[] { shotId, pictureId };
//	}
//
	public void handleStream(int messageType, DBObject oReq, BasicDBObject oResult) {
		MessageType msgType = MessageType.values()[messageType];
		switch (msgType) {
		case UPLOADPICTURE:
//			handleUploadPicture((BasicDBObject) oReq, oResult);
			break;
		case UPLOADFILE:
			break;
		}
	}

	public BasicDBObject execute(String sender, int deviceIndex, int serviceIndex, MessageScope scope, int messageType, int messageVersion, String messageId,
			String messageTag, String messageTo, long messageTime, DBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		ServiceName serviceName = ServiceName.values()[serviceIndex];
		MessageType msgType = MessageType.values()[messageType];
		switch (serviceName) {
		case Stream:
			handleStream(messageType, oReq, oResult);
			break;
		case Dict:
			if (messageTag.equalsIgnoreCase("checkCookie")) {
				String c = RedisPool.use().get(oReq.get("cookieId").toString());
				if (c != null) {
					oResult.putAll((BSONObject) JSON.parse(c));
				}
			} else if (messageTag.equalsIgnoreCase("checkSession")) {
				oResult = ServiceFactory.socket().checkSession(oReq);
			}
			break;
		case HtmlPage:
			handleHtmlPage((BasicDBObject) oReq, oResult);
			break;
		case Servlet:
			handleServlet((BasicDBObject) oReq, oResult);
			break;
		case RemoteCall:
			handleRemoteCall((BasicDBObject) oReq, oResult);
			break;
		case Schedule:
			switch (msgType) {
			case DATACHANGE:
				if (messageTag.equalsIgnoreCase("price")) {
					IndexServiceIntf index = (IndexServiceIntf) ModuleFactory.moduleInstanceBy(String.valueOf(oReq.get("indexName")));
				}
				break;
			}
			break;
//		case Test:
//			oResult = (BasicDBObject) oReq;
//			oResult.append("mqId", mq.getId());
//			break;
		case TripIndex:
		case RequireIndex:
		case TopicIndex:
		case ProductIndex:
			String indexName = serviceName.name().toLowerCase();
			IndexServiceIntf index = (IndexServiceIntf) ModuleFactory.moduleInstanceBy(indexName);
			if (index == null) {
				return oResult.append("xeach", false);
			}
			index.attachMQ(mq);
			switch (msgType) {
			case UPDATE:
				int count = index.add(oReq);
				LogMessage msg = new LogMessage(LogLevel.info, "updateIndex");
				msg.add("from", sender);
				msg.add("msgTag", messageTag);
				msg.add("count", count);
				mq.event.notifyWebSocket(msg);
				break;
			case SEARCH:
				BasicDBObject oUser = (BasicDBObject) JSON.parse(messageTag);
				oResult.putAll((BSONObject) index.search(oReq));
				String q = oReq.containsField("question") ? String.valueOf(oReq.get("question")) : "";
				LogMessage oLog = new LogMessage(LogLevel.info, "Seach");
				oLog.add("from", sender.toString());
				oLog.add("key", q);
				mq.event.notifyWebSocket(oLog);
				oLog.add("question", q);
				oLog.add("req", oReq.toString());
				break;
			case REQUEST:
				ModuleProduct solrindex = (ModuleProduct) ModuleFactory.moduleInstanceBy("productindex");
				BasicDBList skus = (BasicDBList) oReq;
				BasicDBObject sku = null;
				BasicDBList reskus = new BasicDBList();
				String skuId = null;
				for (Object db : skus) {
					sku = (BasicDBObject) db;
					skuId = sku.getString("skuId");
					String content = solrindex.dynamicDB.get(skuId);
					sku = (BasicDBObject) JSON.parse(content);
					reskus.add(sku);
				}
				oResult.append("results", reskus);
			case REMOVE:
				index.remove(oReq);
				break;
			case COMMIT:
				index.commit();
			case SYNC:
				break;
			case CLEARCACHE:
				break;
			default:
				break;
			}
			oResult.append("xeach", true);
			break;

		default:
			break;
		}
		return oResult;
	}

	@Override
	public void start(boolean isReload) {

	}

	@Override
	public void stop() {
	}

	@Override
	public boolean isAlive() {
		return true;
	}

	@Override
	public String getId() {
		return "processor";
	}

}
