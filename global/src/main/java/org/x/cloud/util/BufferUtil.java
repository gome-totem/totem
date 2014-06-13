package org.x.cloud.util;

import java.nio.ByteBuffer;

public class BufferUtil {

	static ByteBuffer duplicateAndMerge(ByteBuffer[] buffers) {

		if (buffers.length == 0) {
			return ByteBuffer.allocate(0);

		} else if (buffers.length == 1) {
			return buffers[0].duplicate();

		} else {
			int size = 0;
			for (ByteBuffer byteBuffer : buffers) {
				if (byteBuffer != null) {
					size += byteBuffer.remaining();
				}
			}

			ByteBuffer buffer = ByteBuffer.allocate(size);
			for (ByteBuffer byteBuffer : buffers) {
				if (byteBuffer != null) {
					int pos = byteBuffer.position();
					int limit = byteBuffer.limit();
					buffer.put(byteBuffer);

					byteBuffer.position(pos);
					byteBuffer.limit(limit);
				}
			}

			buffer.flip();
			return buffer;
		}
	}

	static ByteBuffer merge(ByteBuffer buffer, ByteBuffer[] tailBuffers) {

		if ((buffer == null) || (buffer.remaining() == 0)) {
			return merge(tailBuffers);
		}

		int size = buffer.remaining();
		for (ByteBuffer byteBuffer : tailBuffers) {
			if (byteBuffer != null) {
				size += byteBuffer.remaining();
			}
		}

		ByteBuffer result = ByteBuffer.allocate(size);
		result.put(buffer);
		for (ByteBuffer byteBuffer : tailBuffers) {
			if (byteBuffer != null) {
				int pos = byteBuffer.position();
				int limit = byteBuffer.limit();
				result.put(byteBuffer);

				byteBuffer.position(pos);
				byteBuffer.limit(limit);
			}
		}

		result.flip();
		return result;
	}

	static ByteBuffer merge(ByteBuffer[] buffers) {

		if ((buffers == null) || (buffers.length == 0)) {
			return ByteBuffer.allocate(0);
		} else {
			int size = 0;
			for (ByteBuffer byteBuffer : buffers) {
				if (byteBuffer != null) {
					size += byteBuffer.remaining();
				}
			}

			ByteBuffer buffer = ByteBuffer.allocate(size);
			for (ByteBuffer byteBuffer : buffers) {
				if (byteBuffer != null) {
					int pos = byteBuffer.position();
					int limit = byteBuffer.limit();
					buffer.put(byteBuffer);

					byteBuffer.position(pos);
					byteBuffer.limit(limit);
				}
			}

			buffer.flip();
			return buffer;
		}
	}

	static ByteBuffer merge(ByteBuffer buffer, ByteBuffer tailBuffer) {
		if ((buffer == null) || (buffer.remaining() == 0)) {
			return tailBuffer;
		}

		if ((tailBuffer == null) || (tailBuffer.remaining() == 0)) {
			return buffer;
		}

		ByteBuffer result = ByteBuffer.allocate(buffer.remaining() + tailBuffer.remaining());
		result.put(buffer);
		result.put(tailBuffer);
		result.flip();

		return result;
	}

	public static byte[] merge(byte[] bytes, byte[] tailBytes) {
		byte[] b = new byte[bytes.length + tailBytes.length];

		System.arraycopy(bytes, 0, b, 0, bytes.length);
		System.arraycopy(tailBytes, 0, b, bytes.length, tailBytes.length);

		return b;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
