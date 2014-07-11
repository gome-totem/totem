package org.z.core.stocket;

import org.z.global.dict.CompressMode;
import org.z.global.dict.Global;
import org.z.global.dict.Global.SocketDataType;
import org.z.global.util.StringUtil;


public class SocketResponse {
	public SocketDataType dataType = null;
	public byte[] bytes = null;
	public CompressMode compressMode = CompressMode.NONE;
	public boolean enableCompress = false;

	public SocketResponse(SocketDataType dataType, Object o, boolean enableCompress) {
		this.dataType = dataType;
		switch (dataType) {
		case json:
			this.bytes = StringUtil.toBytes(o.toString());
			break;
		case bytes:
			this.bytes = (byte[]) o;
			break;
		case string:
			this.bytes = StringUtil.toBytes((String) o);
			break;
		}
		this.enableCompress = enableCompress;
		this.init();
	}

	protected void init() {
		if (this.enableCompress && bytes.length >= Global.DataPacketLimit) {
			bytes = StringUtil.compress(bytes);
			compressMode = CompressMode.SNAPPY;
		}
	}

}
