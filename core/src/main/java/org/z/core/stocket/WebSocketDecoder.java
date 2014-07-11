package org.z.core.stocket;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketDecoder extends FrameDecoder {
	protected static Logger logger = LoggerFactory
			.getLogger(WebSocketDecoder.class);

	protected ModuleSocket module = null;

	public WebSocketDecoder(ModuleSocket module) {
		this.module = module;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {
		byte[] dst = new byte[buffer.readableBytes()];
		buffer.readBytes(dst, 0, dst.length);
		ByteBuffer buf = ByteBuffer.wrap(dst);
		return buf;
	}
}
