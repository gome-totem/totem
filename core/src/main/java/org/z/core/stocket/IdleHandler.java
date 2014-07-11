package org.z.core.stocket;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.factory.SocketSession;

public class IdleHandler extends IdleStateAwareChannelHandler {
	protected static Logger logger = LoggerFactory.getLogger(IdleHandler.class);
	protected ModuleSocket module = null;
	protected SocketSession session = null;

	public IdleHandler(ModuleSocket module, SocketSession session) {
		this.module = module;
		this.session = session;
	}

	@Override
	public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
		if (e.getState() == IdleState.ALL_IDLE) {
			logger.info("channelId[{}] idle.", new Object[] { ctx.getChannel().getId() });
			e.getChannel().close();
		}
	}


}
