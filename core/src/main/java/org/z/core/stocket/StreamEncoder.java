package org.z.core.stocket;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

public class StreamEncoder extends LengthFieldPrepender {
	protected ModuleSocket module = null;

	public StreamEncoder(ModuleSocket module) {
		super(4);
		this.module = module;
	}

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (!(msg instanceof SocketResponse)) {
			return msg;
		}
		SocketResponse record = (SocketResponse) msg;
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		buffer.writeInt(record.dataType.ordinal());
		buffer.writeInt(record.compressMode.ordinal());
		buffer.writeInt(record.bytes.length);
		buffer.writeBytes(record.bytes);
		return super.encode(ctx, channel, buffer);
	}
}
