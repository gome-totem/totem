package org.z.core.stocket;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.z.global.util.StringUtil;

public class WebSocketEncoder extends OneToOneEncoder {
	protected ModuleSocket module = null;

	public WebSocketEncoder(ModuleSocket module) {
		this.module = module;
	}

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		byte[] bytes = null;
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		if (msg instanceof String) {
			bytes = StringUtil.toBytes((String) msg);
		} else if (msg instanceof ByteBuffer) {
			bytes = ((ByteBuffer) msg).array();
		}
		buffer.writeBytes(bytes);
		return buffer;
	}
}
