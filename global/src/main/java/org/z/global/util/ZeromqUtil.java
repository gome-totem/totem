package org.z.global.util;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.util.StringUtil;
import org.zeromq.ZFrame;
import org.zeromq.ZMsg;

import com.mongodb.BasicDBObject;

public class ZeromqUtil {
	public static byte[] EMPTY = "".getBytes();
	public static BasicDBObject EMPEYOBJECT = new BasicDBObject();
	public static ZFrame ZEMPTY = new ZFrame(EMPTY);
	public static byte[] MsgId = "ID".getBytes();
	public static byte[] MsgTag = "TAG".getBytes();
	public static byte[] MsgTo = "TO".getBytes();
	protected static Logger logger = LoggerFactory.getLogger(ZeromqUtil.class);

	public static byte[] getBytes(String content) {
		return StringUtil.toBytes(content);
	}

	protected static byte[] getBytes(long l) {
		byte b[] = new byte[8];
		ByteBuffer buf = ByteBuffer.wrap(b);
		buf.putLong(l);
		return b;
	}

	protected static byte[] getBytes(int i) {
		byte b[] = new byte[4];

		ByteBuffer buf = ByteBuffer.wrap(b);
		buf.putInt(i);
		return b;
	}

	protected static long getLong(byte[] b) {
		ByteBuffer buf = ByteBuffer.wrap(b);
		return buf.getLong();
	}

	protected static int getInt(byte[] b) {
		ByteBuffer buf = ByteBuffer.wrap(b);
		return buf.getInt();
	}

	public static void free(ZFrame frame) {
		if (frame == null) {
			return;
		}
		frame.destroy();
	}

	public static void free(ZMsg msg) {
		if (msg == null) {
			return;
		}
		msg.destroy();
	}

	protected static void main(String[] args) {
		long v = 2000;
		byte[] values = ZeromqUtil.getBytes(v);
		System.out.println(ZeromqUtil.getLong(values));
	}
}
