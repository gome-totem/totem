package org.z.global.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompressTool {
	protected static Logger logger = LoggerFactory.getLogger(CompressTool.class);
	public static final char[] PRINTABLE_ALPHABET = { '!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2',
			'3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
			'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
			'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', };

	public static byte[] loadCompressContent(String fileName) {
		File file = new File(fileName);
		if (!file.exists())
			return null;
		StringBuffer buffer = new StringBuffer();
		try {
			FileInputStream _fileStream = new FileInputStream(fileName);
			InputStreamReader _reader = new InputStreamReader(_fileStream, "UTF-8");
			BufferedReader br = new BufferedReader(_reader);
			String _text = null;
			while (((_text = br.readLine()) != null)) {
				buffer.append(_text + System.getProperty("line.separator"));
			}
			_reader.close();
			_fileStream.close();
			return compress(buffer.toString().getBytes("UTF-8"));
		} catch (Exception e) {
			logger.error("loadCompressContent", e);
			return null;
		}
	}

	public static byte[] compress(String content) {
		try {
			return compress(content.getBytes("UTF-8"));
		} catch (Exception e) {
			return content.getBytes();
		}
	}

	public static String deCompress(byte[] compressedBytes) {
		try {
			return new String(decompress(compressedBytes), "UTF-8");
		} catch (Exception e) {
			return new String(compressedBytes);
		}
	}

	public static String compressText(String content) {
		try {
			return new String(compress(content), "UTF-8");
		} catch (Exception e) {
			return content;
		}
	}

	public static String deCompressText(String content) {
		try {
			return deCompress(content.getBytes("UTF-8"));
		} catch (Exception e) {
			return content;
		}
	}

	public static String encrypt(byte[] key, byte[] bytes) {
		try {
			byte[] cBytes = AES4all.encrypt2(key, "PKCS5Padding", bytes);
			return Base64.encodeBase64String(cBytes);
		} catch (Exception e) {
			e.printStackTrace();
			return Base64.encodeBase64String(bytes);
		}
	}

	public static String encrypt(byte[] key, String content) {
		try {
			return encrypt(key, content.getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
			return content;
		}
	}

	public static byte[] decrypt(byte[] key, String content) {
		try {
			byte[] bytes = Base64.decodeBase64(content);
			bytes = AES4all.decrypt2(key, "PKCS5Padding", bytes);
			return bytes;
		} catch (Exception e) {
			e.printStackTrace();
			return content.getBytes();
		}
	}

	/**
	 * Compress data.
	 * 
	 * @param bytesToCompress
	 *            is the byte array to compress.
	 * @return a compressed byte array.
	 * @throws java.io.IOException
	 */
	private static byte[] compress(byte[] bytesToCompress) throws IOException {
		// Compressor with highest level of compression.
		Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION);
		compressor.setInput(bytesToCompress); // Give the compressor the data to
		// compress.
		compressor.finish();

		// Create an expandable byte array to hold the compressed data.
		// It is not necessary that the compressed data will be smaller than
		// the uncompressed data.
		ByteArrayOutputStream bos = new ByteArrayOutputStream(bytesToCompress.length);

		// Compress the data
		byte[] buf = new byte[bytesToCompress.length + 100];
		while (!compressor.finished()) {
			bos.write(buf, 0, compressor.deflate(buf));
		}

		bos.close();

		// Get the compressed data
		return bos.toByteArray();
	}

	/**
	 * Decompress data.
	 * 
	 * @param compressedBytes
	 *            is the compressed byte array.
	 * @return decompressed byte array.
	 * @throws java.io.IOException
	 * @throws java.util.zip.DataFormatException
	 */
	public static byte[] decompress(byte[] compressedBytes) throws IOException, DataFormatException {
		// Initialize decompressor.
		Inflater decompressor = new Inflater();
		decompressor.setInput(compressedBytes); // Give the decompressor the
		// data to decompress.
		decompressor.finished();

		// Create an expandable byte array to hold the decompressed data.
		// It is not necessary that the decompressed data will be larger than
		// the compressed data.
		ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedBytes.length);

		// Decompress the data
		byte[] buf = new byte[compressedBytes.length + 100];
		while (!decompressor.finished()) {
			bos.write(buf, 0, decompressor.inflate(buf));
		}

		bos.close();

		// Get the decompressed data.
		return bos.toByteArray();
	}

	public static String getMD5(byte[] source) {
		String s = null;
		char hexDigits[] = { // 用来将字节转换成 16 进制表示的字符
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			md.update(source);
			byte tmp[] = md.digest(); // MD5 的计算结果是一个 128 位的长整数，
			// 用字节表示就是 16 个字节
			char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，
			// 所以表示成 16 进制需要 32 个字符
			int k = 0; // 表示转换结果中对应的字符位置
			for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节
				// 转换成 16 进制字符的转换
				byte byte0 = tmp[i]; // 取第 i 个字节
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
				// >>> 为逻辑右移，将符号位一起右移
				str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
			}
			s = new String(str); // 换后的结果转换为字符串

		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	public static String createPassword(int length) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 1; i <= length; i++) {
			int index = RandomUtils.nextInt(PRINTABLE_ALPHABET.length);
			buffer.append(PRINTABLE_ALPHABET[index]);
		}
		return buffer.toString();
	}

	public static String decode(String content) {
		String key = content.substring(0, 32);
		key = TextUtil.normalize(key);
		content = content.substring(32, content.length());
		try {
			byte[] keys = Hex.decodeHex(key.toCharArray());
			content = new String(decrypt(keys, content), "UTF-8");
			return content;
		} catch (Exception e) {
			return null;
		}
	}

	public static void main(String[] args) throws IOException, DataFormatException, DecoderException {

		System.out.println(CompressTool.getMD5("13911338377".getBytes()));

		byte[] key = Hex.decodeHex("ed967cdc9bf2358130aea872e42a21b0".toCharArray());
		String value = "<?xml version=\"1.0\" encoding=\"utf-8\"?><map x=\"0\" y=\"0\">";
		String result = CompressTool.encrypt(key, value);
		System.out.println(result);
		System.out.println(new String(CompressTool.decrypt(key, result), "UTF-8"));

		// Compress a byte array..
		byte[] compressedBytes = compress("This is a 肖明肖明肖明肖明肖明very very very very very very very ... large string".getBytes());
		System.out.println("Compressed string length: " + String.valueOf(compressedBytes.length));

		// Decompress a byte array.
		byte[] decompressedBytes = decompress(compressedBytes);
		System.out.println("Decompressed string:" + new String(decompressedBytes));
	}
}
