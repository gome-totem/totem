package org.x.cloud.util;

import java.util.HashSet;
import java.util.Iterator;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.x.cloud.dict.ConfigFile;

public class SocketUtil {

	private static HashSet<String> iptables = new HashSet<String>();
	private static String allowIP = ConfigFile.rock().getItem("AppSocketAllowIP", "");
	static {
		String[] values = allowIP.split("\\,");
		for (int i = 0; i < values.length; i++) {
			if (StringUtil.isEmpty(values[i]) == false) {
				iptables.add(values[i]);
			}
		}
	}

	public static boolean checkAllowIP(String ip) {
		for (Iterator<String> i = iptables.iterator(); i.hasNext();) {
			String v = i.next();
			if (ip.startsWith(v)) {
				return true;
			}
		}
		return false;
	}

	public static String readString(ChannelBuffer buffer) {
		return StringUtil.toString(readBytes(buffer));
	}

	public static void writeString(ChannelBuffer buffer, String content) {
		byte[] bytes = StringUtil.toBytes(content);
		writeBytes(buffer, bytes);
	}

	public static void writeBytes(ChannelBuffer buffer, byte[] bytes) {
		buffer.writeInt(bytes.length);
		buffer.writeBytes(bytes);
	}

	public static byte[] readBytes(ChannelBuffer buffer) {
		int size = buffer.readInt();
		return buffer.readBytes(size).array();
	}

	public static byte[] readBytes(ChannelBuffer source, int length) {
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
		int size = 800;
		byte[] bytes = new byte[size];
		while (length > size) {
			source.readBytes(bytes, 0, size);
			buf.writeBytes(bytes);
			length -= size;
		}
		if (length > 0) {
			bytes = new byte[length];
			source.readBytes(bytes, 0, length);
			buf.writeBytes(bytes);
		}
		return buf.array();
	}

}
