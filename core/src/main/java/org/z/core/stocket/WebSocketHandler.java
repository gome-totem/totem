package org.z.core.stocket;

import java.nio.ByteBuffer;
import java.util.List;

import org.bson.BSONObject;
import org.java_websocket.WebSocket.Role;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.Framedata.Opcode;
import org.java_websocket.handshake.HandshakeImpl1Server;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.util.Charsetfunctions;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.common.MessageBox;
import org.z.global.dict.Global;
import org.z.global.factory.SocketSession;
import org.z.global.util.DBObjectUtil;
import org.z.global.util.StringUtil;
import org.z.store.redis.RedisPool;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;


public class WebSocketHandler extends SimpleChannelUpstreamHandler {
	protected static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
	protected ModuleSocket module = null;
	protected SocketSession session = null;

	public WebSocketHandler(ModuleSocket module, SocketSession session) {
		this.module = module;
		this.session = session;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		ByteBuffer socketBuffer = (ByteBuffer) e.getMessage();
		WebSocketAttach attach = (WebSocketAttach) session.attachment;
		try {
			if (attach != null) {
				if (attach.content != null) {
					ctx.getChannel().write(attach.content);
				} else {
					ServerHandshakeBuilder response = new HandshakeImpl1Server();
					List<ByteBuffer> items = attach.draft.createHandshake(attach.draft.postProcessHandshakeResponseAsServer(attach.handshake, response),
							Role.SERVER);
					for (ByteBuffer b : items) {
						ctx.getChannel().write(b);
					}
				}
				session.setAttachment(null);
				return;
			}
			List<Framedata> frames;
			try {
				Draft d = (Draft) session.draft;
				if (d==null) return;
				frames = d.translateFrame(socketBuffer);
				for (Framedata f : frames) {
					Opcode curop = f.getOpcode();
					boolean fin = f.isFin();
					if (curop == Opcode.CLOSING) {
						int code = CloseFrame.NOCODE;
						String reason = "";
						if (f instanceof CloseFrame) {
							CloseFrame cf = (CloseFrame) f;
							code = cf.getCloseCode();
							reason = cf.getMessage();
						}
						continue;
					} else if (curop == Opcode.TEXT) {
						handleBy(Charsetfunctions.stringUtf8(f.getPayloadData()));
					} else if (curop == Opcode.BINARY) {
						handleBy(f.getPayloadData());
					}
				}
			} catch (InvalidDataException e1) {
				session.channel.close();
				logger.info("messageReceived", e1);
				return;
			}
		} finally {
			session.attachment = null;
		}
	}

	public void handleAuth(BasicDBObject oReq) {
		BasicDBObject oMsg = new BasicDBObject().append("xeach", false);
		String content = RedisPool.use().get(oReq.getString("cookieId"));
		oMsg.append("action", "hi");
		if (StringUtil.isEmpty(content)) {
			module.sendWebSocketMsg(session, oMsg.append("message", "您必须登录后,才能对话"));
			session.channel.close();
			return;
		}
		BasicDBObject oUser = (BasicDBObject) JSON.parse(content);
		session.userId = oUser.getLong("userId");
		session.userName = oUser.getString("userName");
		session.userServer = oUser.getString("server");
		session.callServer = oUser.getString("callServer");
		if (Global.DevelopMode == false && !session.userServer.equalsIgnoreCase(Global.ServerName)) {
			module.sendWebSocketMsg(session, oMsg.append("message", "您必须登录到[" + session.userServer + "]服务器,才能对话"));
			session.channel.close();
			return;
		}
		module.addUserSession(session);
		module.updateUserOnline(session, true);
		session.auth = true;
		BasicDBObject oWebMsg = new BasicDBObject();
		oWebMsg.append("action", "hi");
		module.sendWebSocketMsg(session, oWebMsg);
	}

	public void handleReadMsgBody(String boxServer, long boxId, String msgId) {
		BasicDBObject qField = new BasicDBObject();
		qField.append("pageMode", "next");
		qField.append("boxServer", boxServer);
		qField.append("boxId", boxId);
		qField.append("msgId", msgId);
		BasicDBObject oWebMsg = new BasicDBObject();
		oWebMsg.append("action", "readMsgBody");
		oWebMsg.append("xeach", true);
		oWebMsg.putAll((BSONObject) module.readMsgBody(session, qField));
		module.sendWebSocketMsg(session, oWebMsg);
	}

	public void replyHello(String msgId, int onlineCount) {
		BasicDBObject oWebMsg = new BasicDBObject();
		oWebMsg.append("action", "hi");
		oWebMsg.append("xeach", true);
		oWebMsg.append("msgId", msgId);
		oWebMsg.append("onlineCount", onlineCount);
		module.sendWebSocketMsg(session, oWebMsg);
	}

	public void handleHello(BasicDBObject oReq) {
		BasicDBObject oMsg = new BasicDBObject().append("xeach", false);
		String content = RedisPool.use().get(oReq.getString("cookieId"));
		oMsg.append("action", "hi");
		if (StringUtil.isEmpty(content)) {
			module.sendWebSocketMsg(session, oMsg.append("message", "您必须登录后,才能对话"));
			session.channel.close();
			return;
		}
		BasicDBObject oUser = (BasicDBObject) JSON.parse(content);
		session.userId = oUser.getLong("userId");
		session.userName = oUser.getString("userName");
		session.userServer = oUser.getString("server");
		session.callServer = oUser.getString("callServer");
		if (Global.DevelopMode == false && !session.userServer.equalsIgnoreCase(Global.ServerName)) {
			module.sendWebSocketMsg(session, oMsg.append("message", "您必须登录到[" + session.userServer + "]服务器,才能对话"));
			session.channel.close();
			return;
		}
		module.addUserSession(session);
		module.updateUserOnline(session, true);

		BasicDBList sockets = null;
		BasicDBObject oMsgHeader = null;
		String msgId = null;
		String boxServer = session.userServer;
		long boxId = session.userId;
		if (oReq.containsField("bizType") && oReq.containsField("catalog") && oReq.containsField("shortId")) {
			int catalog = oReq.getInt("catalog", 0);
			long shortId = oReq.getLong("shortId", 0);
			int bizType = oReq.getInt("bizType");
			long bizUserId = oReq.getLong("bizUserId");
			String bizTitle = oReq.getString("bizTitle");
			String bizName = oReq.getString("bizName");
			oMsgHeader = MessageBox.readHeader(session.userServer, session.userId, catalog, bizType, shortId);
			if (oMsgHeader == null) {
				BasicDBObject oActivity = module.readActivityBy(catalog, bizType, shortId);
				if (oActivity == null || oActivity.getLong("userId")==session.userId) {
					module.sendWebSocketMsg(session, oMsg.append("message", "抱歉,不能和自己对话."));
					session.channel.close();
					return;
				}
				oMsgHeader = MessageBox.writeHeader(session.userServer, session.userId, boxServer, boxId, null, session.userId,
						session.userName, bizUserId, catalog, bizType, bizTitle, bizName, shortId);
				msgId = oMsgHeader.getObjectId("_id").toString();
				oReq.append("msgId", msgId);
				logger.info("new MsgHeader[{}]", new String[] { msgId });
				sockets = MessageBox.createSockets(session.userServer, session.userId, session.userName, oActivity.getLong("userId"));
				MessageBox.writeSockets(boxServer, boxId, msgId, oMsgHeader.getLong("timestamp"), sockets);
				logger.info("write Sockets[{}]", new String[] { String.valueOf(sockets.size()) });
				BasicDBObject oContact = module.socketHandler.readContact(session.userId);
				for (int i = 0; i < sockets.size(); i++) {
					BasicDBObject oSocket = (BasicDBObject) sockets.get(i);
					if (oSocket.getLong("userId")==session.userId) {
						continue;
					}
					MessageBox.writeHeader(oSocket.getString("appserver"), oSocket.getLong("userId"), boxServer, boxId, msgId,
							session.userId, session.userName, bizUserId, catalog, bizType, bizTitle, bizName, shortId);
					module.socketHandler.writeContact(oSocket.getString("appserver"), oSocket.getLong("userId"), oContact);
				}
			} else {
				msgId = oMsgHeader.getString("_id");
			}
		} else if (oReq.containsField("msgId")) {
			msgId = oReq.getString("msgId");
			oMsgHeader = (BasicDBObject) RedisPool.use().getDBObject(msgId);
			if (oMsgHeader == null) {
				oMsgHeader = MessageBox.readHeader(session.userServer, session.userId, msgId);
			}
			if (oMsgHeader == null) {
				module.sendWebSocketMsg(session, oMsg.append("message", "抱歉,记录[" + msgId + "]不存在."));
				session.channel.close();
				return;
			}
			DBObjectUtil.copyBy(oMsgHeader, oReq, "catalog", "shortId", "bizType", "bizUserId", "bizTitle", "bizName");
			boxServer=oMsgHeader.getString("boxServer");
			boxId=oMsgHeader.getLong("boxId");
		}
		BasicDBObject record = MessageBox.readSocketRecord(session.userId, boxServer, boxId, msgId, "timestamp","sockets");
		if (record == null) {
			module.sendWebSocketMsg(session, oMsg.append("message", "抱歉,您没有权限."));
			session.channel.close();
			return;
		}
		oReq.append("boxServer", boxServer);
		oReq.append("boxId", boxId);
		if (sockets == null) {
			sockets = MessageBox.doFilterSockets(record, session.userId);
		}
		session.auth = true;
		RedisPool.use().set(msgId, oMsgHeader.toString());
		if (oReq.getBoolean("readMsgBody", false) == true) {
			handleReadMsgBody(boxServer, boxId, msgId);
		}
		int onlineCount = 0;
		BasicDBList oStates = module.dispatch("hi", session, sockets, oMsgHeader, oReq);
		for (int i = 0; oStates!=null && i < oStates.size(); i++) {
			BasicDBObject oState = (BasicDBObject) oStates.get(i);
			onlineCount += oState.getBoolean("online") ? 1 : 0;
		}
		replyHello(msgId, onlineCount);
	}

	public BasicDBObject errorMsg(String content) {
		BasicDBObject oMsg = new BasicDBObject().append("action", "msg").append("userName", "客服中心");
		oMsg.append("message", content);
		return oMsg;
	}

	public void handleBy(String content) {
		if (content.startsWith("{") == false)
			return;
		BasicDBObject oReq = (BasicDBObject) JSON.parse(content);
		String action = oReq.getString("action");
		if (action.equalsIgnoreCase("registerListener")) {
			int level = oReq.getInt("level", 0);
			module.registerWebSocketEvent(new WebSocketEventImpl(session, level));
		} else if (action.equalsIgnoreCase("removeListener")) {
			module.removeWebSocketEvent(session.channelId);
		} else if (action.equalsIgnoreCase("hi")) {
			handleHello(oReq);
		} else if (action.equalsIgnoreCase("auth")) {
			handleAuth(oReq);
		} else if (session.auth == false) {
			module.sendWebSocketMsg(session, errorMsg("非法访问"));
		} else if (oReq.containsField("msgId")) {
			String msgId = oReq.getString("msgId");
			BasicDBObject oMsgHeader = (BasicDBObject) RedisPool.use().getDBObject(msgId);
			if (oMsgHeader == null || oMsgHeader.containsField("boxServer") == false || oMsgHeader.containsField("boxId") == false) {
				module.sendWebSocketMsg(session, errorMsg("非法访问"));
				return;
			}
			String boxServer = oMsgHeader.getString("boxServer");
			long boxId = oMsgHeader.getLong("boxId");
			if (action.equalsIgnoreCase("msg")) {
				BasicDBObject record = MessageBox.readSocketRecord(session.userId, boxServer, boxId, oMsgHeader.get("_id"));
				if (record == null) {
					module.sendWebSocketMsg(session, errorMsg("非法访问"));
					return;
				}
				BasicDBList oSockets = MessageBox.doFilterSockets(record, session.userId);
				if (oSockets.size() == 0) {
					module.sendWebSocketMsg(session, errorMsg("非法访问"));
					return;
				}
				int level=record.getInt("w"+session.userId);
				int onlineCount = 0;
				long timestamp = MessageBox.writeBody(boxServer, boxId, msgId,level,session.userId, session.userName, oReq.getString("content"));
				MessageBox.updateSockets(boxServer, boxId, msgId, timestamp);
				oReq.append("writetime", timestamp);
				BasicDBList oStates = module.dispatch(action, session, oSockets, oMsgHeader, oReq);
				for (int i = 0; i < oStates.size(); i++) {
					BasicDBObject oState = (BasicDBObject) oStates.get(i);
					onlineCount += oState.getBoolean("online") ? 1 : 0;
				}
				BasicDBObject oEcho = new BasicDBObject().append("action", "echo").append("onlineCount", onlineCount);
				oEcho.append("msgId", msgId);
				oEcho.append("userId", session.userId);
				oEcho.append("userName", session.userName);
				oEcho.append("content", oReq.getString("content"));
				oEcho.append("ago", StringUtil.timeDiff(timestamp));
				oEcho.append("timestamp", StringUtil.formatDateTime(timestamp));
				module.sendWebSocketMsg(session, oEcho);
			} else if (action.equalsIgnoreCase("joinTalk")) {
				BasicDBObject oEcho = new BasicDBObject().append("action", action).append("xeach", module.joinTalk(session, oReq));
				module.sendWebSocketMsg(session, oEcho);
			} else if (action.equalsIgnoreCase("readMsgBody")) {
				BasicDBObject oEcho = module.readMsgBody(session, oReq);
				module.sendWebSocketMsg(session, oEcho);
			} else if (action.equalsIgnoreCase("readContactState")) {
				BasicDBObject oResult = new BasicDBObject().append("action", action).append("items", module.readContactState(session));
				module.sendWebSocketMsg(session, oResult);
			}
		}
	}

	public void handleBy(ByteBuffer buffer) {
		String content = StringUtil.toString(buffer.array());
		handleBy(content);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		logger.warn("exceptionCaught", e.getCause());
		e.getChannel().close();
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelDisconnected(ctx, e);
		logger.info("channel Id[{}] disconnected.", new Object[] { ctx.getChannel().getId() });
		module.removeSession(session, ctx.getChannel());
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelClosed(ctx, e);
		logger.info("channel Id[{}] close.", new Object[] { ctx.getChannel().getId() });
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelConnected(ctx, e);
		logger.info("channel Id[{}] open.", new Object[] { ctx.getChannel().getId() });
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelConnected(ctx, e);
		logger.info("channel Id[{}] connected.", new Object[] { ctx.getChannel().getId() });
	}

}
