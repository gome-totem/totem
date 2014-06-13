package org.z.common.io;

import java.io.IOException;
import java.io.OutputStream;


public class StringOutputStream extends OutputStream {
	private StringBuffer buf = new StringBuffer();

	public void write(byte[] b) throws IOException {
		buf.append(new String(b));
	}

	public void write(byte[] b, int off, int len) throws IOException {
		buf.append(new String(b, off, len));
	}

	public void write(int b) throws IOException {
		byte[] bytes = new byte[1];
		bytes[0] = (byte) b;
		buf.append(new String(bytes));
	}

	public String toString() {
		return buf.toString();
	}
}
