package org.z.core.stocket;

import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.dict.Global;
import org.z.global.dict.Global.SocketHeadTag;
import org.z.global.factory.SocketSession;
import org.z.global.util.SocketUtil;
import org.z.global.zk.ServiceName;

public class StreamDecoder extends LengthFieldBasedFrameDecoder {
	protected static Logger logger = LoggerFactory.getLogger(StreamDecoder.class);
	private AtomicLong jobNo = new AtomicLong(0);
	protected static Integer jobCount = 0;
	protected ModuleSocket module = null;
	protected static Integer FrameSize = 30 * 1024 * 1024;

	public StreamDecoder(ModuleSocket module) {
		super(FrameSize, 0, 4, 0, 4);
		this.module = module;
	}

	public String allocateJobNo() {
		if (jobNo.get() >= Long.MAX_VALUE) {
			jobNo.set(0);
		}
		return String.valueOf(jobNo.addAndGet(1));
	}

	protected boolean hasHeader(ChannelBuffer buffer) {
		if (buffer.readableBytes() < 8) {
			return false;
		}
		int[] headers = new int[2];
		headers[0] = buffer.getInt(buffer.readerIndex());
		headers[1] = buffer.getInt(buffer.readerIndex() + 4);
		if (headers[0] == 19750 && headers[1] == 424) {
			return true;
		}
		return false;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		ChannelBuffer frame = (ChannelBuffer) super.decode(ctx, channel, buffer);
		if (frame == null || frame.capacity() <= 12) {
			return null;
		}
		frame.skipBytes(4 * 2);
		SocketHeadTag headTag = SocketHeadTag.values()[frame.readInt()];
		switch (headTag) {
		case json:
			try {
				SocketHeader header = new SocketHeader();
				header.device = frame.readInt();
				header.serverName = SocketUtil.readString(frame);
				header.serverIP = SocketUtil.readString(frame);
				header.userId = SocketUtil.readString(frame);
				header.serviceName = SocketUtil.readString(frame);
				header.serviceIndex = frame.readInt();
				if (header.serviceIndex == ServiceName.Server.ordinal()) {
					String ip = channel.getRemoteAddress().toString();
					ip = ip.substring(1, ip.length() - 1);
					if (!ip.startsWith(Global.ServerIP) && SocketUtil.checkAllowIP(ip) == false) {
						logger.info("request Service=[{}] ip=[{}] is blocked", new String[] { "server", channel.getRemoteAddress().toString() });
						return null;
					}
				}
				synchronized (jobCount) {
					jobCount++;
				}
				header.messageScope = frame.readInt();
				header.messageType = frame.readInt();
				header.messageVersion = frame.readInt();
				header.messageId = SocketUtil.readString(frame);
				header.messageTag = SocketUtil.readString(frame);
				header.messageTo = SocketUtil.readString(frame);
				header.timestamp = frame.readLong();
				header.compressMode = frame.readInt();
				int length = frame.readInt();
				byte[] bytes = new byte[length];
				frame.readBytes(bytes, 0, length);
				SocketSession session = this.module.channelBy(channel.getId());
				return new SocketEventImpl(session, header, bytes, allocateJobNo());
			} catch (Exception e) {
				logger.error("StreamDecoder", e.getMessage());
				return null;
			}
		default:
			break;
		}
		return null;
	}

	@Override
	protected ChannelBuffer extractFrame(ChannelBuffer buffer, int index, int length) {
		return buffer.slice(index, length);
	}
}
