package org.x.cloud.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class AES4all {
	private static KeyGenerator kgen = null;
	static {
		try {
			kgen = KeyGenerator.getInstance("AES");
			kgen.init(128);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Cipher getAESCBCEncryptor(byte[] keyBytes, byte[] IVBytes,
			String padding) throws Exception {
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(IVBytes);
		Cipher cipher = Cipher.getInstance("AES/CBC/" + padding);
		cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
		return cipher;
	}

	public static Cipher getAESCBCDecryptor(byte[] keyBytes, byte[] IVBytes,
			String padding) throws Exception {
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(IVBytes);
		Cipher cipher = Cipher.getInstance("AES/CBC/" + padding);
		cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
		return cipher;
	}

	public static Cipher getAESECBEncryptor(byte[] keyBytes, String padding)
			throws Exception {
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/" + padding);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher;
	}

	public static Cipher getAESECBDecryptor(byte[] keyBytes, String padding)
			throws Exception {
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/" + padding);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher;
	}

	public static byte[] encrypt(Cipher cipher, byte[] dataBytes)
			throws Exception {
		ByteArrayInputStream bIn = new ByteArrayInputStream(dataBytes);
		CipherInputStream cIn = new CipherInputStream(bIn, cipher);
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		int ch;
		while ((ch = cIn.read()) >= 0) {
			bOut.write(ch);
		}
		cIn.close();
		return bOut.toByteArray();
	}

	public static byte[] decrypt(Cipher cipher, byte[] dataBytes)
			throws Exception {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		CipherOutputStream cOut = new CipherOutputStream(bOut, cipher);
		cOut.write(dataBytes);
		cOut.close();
		return bOut.toByteArray();
	}

	/**
	 * @param args
	 */

	public static byte[] encrypt1(byte[] keyBytes, byte[] ivBytes,
			String sPadding, byte[] messageBytes) throws Exception {
		Cipher cipher = getAESCBCEncryptor(keyBytes, ivBytes, sPadding);
		return encrypt(cipher, messageBytes);
	}

	public static byte[] decrypt1(byte[] keyBytes, byte[] ivBytes,
			String sPadding, byte[] encryptedMessageBytes) throws Exception {
		Cipher decipher = getAESCBCDecryptor(keyBytes, ivBytes, sPadding);
		return decrypt(decipher, encryptedMessageBytes);
	}

	public static byte[] encrypt2(byte[] keyBytes, String sPadding,
			byte[] messageBytes) throws Exception {
		Cipher cipher = getAESECBEncryptor(keyBytes, sPadding);
		return encrypt(cipher, messageBytes);
	}

	public static byte[] decrypt2(byte[] keyBytes, String sPadding,
			byte[] encryptedMessageBytes) throws Exception {
		Cipher decipher = getAESECBDecryptor(keyBytes, sPadding);
		return decrypt(decipher, encryptedMessageBytes);
	}

	public static String generateKey() {
		SecretKey secretKey = kgen.generateKey();
		return Hex.encodeHexString(secretKey.getEncoded());
	}

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 100; i++) {
			System.out.println(AES4all.generateKey());
		}
		String sDemoMesage = "This is a demo我的测试<!---> message from Java!";
		byte[] demoMesageBytes = sDemoMesage.getBytes();
		// shared secret
		// Initialization Vector - usually a random data, stored along with the
		// shared secret,
		// or transmitted along with a message.
		// Not all the ciphers require IV - we use IV in this particular sample
		byte[] demoKeyBytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
				0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
		byte[] demoIVBytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
				0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
		String sPadding = "ISO10126Padding"; // "ISO10126Padding",
		// "PKCS5Padding"

		System.out.println("Demo Key (base64): "
				+ new String(Base64.decodeBase64(demoKeyBytes)));
		System.out.println("Demo IV  (base64): "
				+ new String(Base64.decodeBase64(demoIVBytes)));

		byte[] demo1EncryptedBytes = encrypt1(demoKeyBytes, demoIVBytes,
				sPadding, demoMesageBytes);
		System.out.println("Demo1 encrypted (base64): "
				+ new String(Base64.decodeBase64(demo1EncryptedBytes)));
		byte[] demo1DecryptedBytes = decrypt1(demoKeyBytes, demoIVBytes,
				sPadding, demo1EncryptedBytes);
		System.out.println("Demo1 decrypted message : "
				+ new String(demo1DecryptedBytes));

		byte[] demo2EncryptedBytes = encrypt2(demoKeyBytes, sPadding,
				demoMesageBytes);
		System.out.println("Demo2 encrypted (base64): "
				+ new String(Base64.decodeBase64(demo2EncryptedBytes)));
		byte[] demo2DecryptedBytes = decrypt2(demoKeyBytes, sPadding,
				demo2EncryptedBytes);
		System.out.println("Demo2 decrypted message : "
				+ new String(demo2DecryptedBytes));

	}

}
