package org.z.core.stocket;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.factory.SocketSession;

public class StreamHandler extends SimpleChannelHandler {
	protected static Logger logger = LoggerFactory.getLogger(StreamHandler.class);
	protected ModuleSocket module = null;
	protected SocketSession session = null;

	public StreamHandler(ModuleSocket module, SocketSession session) {
		this.module = module;
		this.session = session;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		SocketEventImpl event = (SocketEventImpl) e.getMessage();
		if (event == null) {
			return;
		}
		module.handle(event);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.warn("exceptionCaught", e.getCause());
		e.getChannel().close();
	}
	
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelClosed(ctx, e);
		logger.info("channel Id[{}] close.", new Object[] { ctx.getChannel().getId() });
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelDisconnected(ctx, e);
		logger.info("channel Id[{}] disconnected.", new Object[] { ctx.getChannel().getId() });
		module.removeSession(session, ctx.getChannel());
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
