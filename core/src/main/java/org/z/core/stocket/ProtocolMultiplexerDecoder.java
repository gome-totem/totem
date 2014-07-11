package org.z.core.stocket;

import java.nio.ByteBuffer;

import org.java_websocket.WebSocket.Role;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft.HandshakeState;
import org.java_websocket.exceptions.IncompleteHandshakeException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.Handshakedata;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.dict.Global.SocketConnectType;
import org.z.global.factory.SocketSession;
import org.z.global.util.StringUtil;

public class ProtocolMultiplexerDecoder extends FrameDecoder {
	protected static Logger logger = LoggerFactory.getLogger(ProtocolMultiplexerDecoder.class);
	private ModuleSocket module = null;

	public ProtocolMultiplexerDecoder(ModuleSocket module) {
		this.module = module;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		if (buffer.readableBytes() < 8) {
			return null;
		}
		SocketSession session = module.channelBy(channel.getId());
		logger.info("channelId=[{}] & SessionSize=[{}]", new Object[] { channel.getId(), module.channels.size() });
		if (session != null) {
			switch (session.type) {
			case stream:
				switchToStream(session, ctx);
				break;
			case http:
				switchToHttp(session, ctx);
				break;
			case websocket:
				switchToWebSocket(session, ctx);
				break;
			}
			return buffer.readBytes(buffer.readableBytes());
		}
		session = parseHeaderTag(ctx, buffer);
		if (session == null) {
			buffer.skipBytes(buffer.readableBytes());
			ctx.getChannel().close();
			return null;
		}
		switch (session.type) {
		case stream:
			switchToStream(session, ctx);
			break;
		case http:
			switchToHttp(session, ctx);
			break;
		case websocket:
			switchToWebSocket(session, ctx);
			break;
		}
		return buffer.readBytes(buffer.readableBytes());
	}

	private String getFlashPolicy() {
		return "<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"" + module.port + "\" /></cross-domain-policy>\0";
	}

	public HandshakeState isFlashEdgeCase(ByteBuffer request) throws IncompleteHandshakeException {
		request.mark();
		if (request.limit() > Draft.FLASH_POLICY_REQUEST.length) {
			return HandshakeState.NOT_MATCHED;
		} else if (request.limit() < Draft.FLASH_POLICY_REQUEST.length) {
			throw new IncompleteHandshakeException(Draft.FLASH_POLICY_REQUEST.length);
		} else {
			for (int flash_policy_index = 0; request.hasRemaining(); flash_policy_index++) {
				if (Draft.FLASH_POLICY_REQUEST[flash_policy_index] != request.get()) {
					request.reset();
					return HandshakeState.NOT_MATCHED;
				}
			}
			return HandshakeState.MATCHED;
		}
	}

	private SocketSession checkHandShake(ChannelHandlerContext ctx, ByteBuffer buffer) {
		String content = StringUtil.toString(buffer.array());
		SocketSession session = null;
		if (!content.startsWith("<policy-file-request") && !content.startsWith("GET")) {
			return session;
		}
		buffer.rewind();
		buffer.mark();
		HandshakeState isflashedgecase = isFlashEdgeCase(buffer);
		try {
			if (isflashedgecase == HandshakeState.MATCHED) {
				session = module.newSession(ctx.getChannel(), SocketConnectType.websocket);
				session.setAttachment(new WebSocketAttach(getFlashPolicy()));
				return session;
			}
			HandshakeState handshakestate = null;
			for (Draft d : module.known_drafts) {
				try {
					d.setParseMode(Role.SERVER);
					buffer.reset();
					Handshakedata tmphandshake = d.translateHandshake(buffer);
					if (tmphandshake instanceof ClientHandshake == false) {
						return session;
					}
					ClientHandshake handshake = (ClientHandshake) tmphandshake;
					handshakestate = d.acceptHandshakeAsServer(handshake);
					if (handshakestate == HandshakeState.MATCHED) {
						session = module.newSession(ctx.getChannel(), SocketConnectType.websocket);
						session.setDraft(d);
						session.setAttachment(new WebSocketAttach(handshake, d));
						return session;
					}
				} catch (Exception e) {
				}
			}
			return session;
		} catch (Exception e) {

		}
		return session;
	}

	private SocketSession parseHeaderTag(ChannelHandlerContext ctx, ChannelBuffer buffer) {
		int[] headers = new int[2];
		headers[0] = buffer.getInt(buffer.readerIndex() + 4);
		headers[1] = buffer.getInt(buffer.readerIndex() + 8);
		SocketSession session = null;
		if (headers[0] == 19750 && headers[1] == 424) {
			session = module.newSession(ctx.getChannel(), SocketConnectType.values()[0]);
			return session;
		}
		headers[0] = buffer.getUnsignedByte(buffer.readerIndex());
		headers[1] = buffer.getUnsignedByte(buffer.readerIndex() + 1);
		boolean b = headers[0] == 'G' && headers[1] == 'E' || // GET
				headers[0] == 'P' && headers[1] == 'O' || // POST
				headers[0] == 'P' && headers[1] == 'U' || // PUT
				headers[0] == 'H' && headers[1] == 'E' || // HEAD
				headers[0] == 'O' && headers[1] == 'P' || // OPTIONS
				headers[0] == 'P' && headers[1] == 'A' || // PATCH
				headers[0] == 'D' && headers[1] == 'E' || // DELETE
				headers[0] == 'T' && headers[1] == 'R' || // TRACE
				headers[0] == 'C' && headers[1] == 'O' || // CONNECT
				headers[0] == '<' && headers[1] == 'p'; // policy-file-request
		if (b == false) {
			return session;
		}
		byte[] dst = new byte[buffer.readableBytes()];
		buffer.getBytes(buffer.readerIndex(), dst, 0, dst.length);
		ByteBuffer buf = ByteBuffer.wrap(dst);
		session = checkHandShake(ctx, buf);
		return session == null ? module.newSession(ctx.getChannel(), SocketConnectType.values()[1]) : session;
	}

	private void switchToHttp(SocketSession session, ChannelHandlerContext ctx) {
		ChannelPipeline p = ctx.getPipeline();
		p.remove("handler");
		p.addLast("idleStateHandler", new IdleStateHandler(module.timer, 0, 0, ModuleSocket.HTTP_IDLE_SECONDS));
		p.addLast("idleHandler", new IdleHandler(module, session));
		p.addLast("decoder", new HttpRequestDecoder());
		p.addLast("aggregator", new HttpChunkAggregator(65536));
		p.addLast("encoder", new HttpResponseEncoder());
		p.addLast("deflater", new HttpContentCompressor());
		p.addLast("handler", new HttpHandler(module, session));
		p.remove(this);
	}

	private void switchToWebSocket(SocketSession session, ChannelHandlerContext ctx) {
		ChannelPipeline p = ctx.getPipeline();
		p.addLast("idleStateHandler", new IdleStateHandler(module.timer, 0, 0, ModuleSocket.WEBSOCKET_IDLE_SECONDS));
		p.addLast("idleHandler", new IdleHandler(module, session));
		p.addLast("decoder", new WebSocketDecoder(module));
		p.addLast("encoder", new WebSocketEncoder(module));
		p.addLast("handler", new WebSocketHandler(module, session));
		p.remove(this);
	}

	private void switchToStream(SocketSession session, ChannelHandlerContext ctx) {
		ChannelPipeline p = ctx.getPipeline();
		p.addLast("idleStateHandler", new IdleStateHandler(module.timer, 0, 0, ModuleSocket.STREAM_IDLE_SECONDS));
		p.addLast("idleHandler", new IdleHandler(module, session));
		p.addLast("decoder", new StreamDecoder(module));
		p.addLast("encoder", new StreamEncoder(module));
		p.addLast("handler", new StreamHandler(module, session));
		p.remove(this);
	}

}
