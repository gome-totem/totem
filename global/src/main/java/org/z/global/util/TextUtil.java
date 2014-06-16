package org.z.global.util;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

import com.mongodb.BasicDBObject;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public class TextUtil {

	public static String extractText(String html) {
		if (org.z.global.util.StringUtil.isEmpty(html)) {
			return "";
		}
		html = html.trim();
		HtmlDom dom = new HtmlDom();
		dom.load(html, "utf-8");
		dom.clean(false);
		dom.getNodeHtml(dom.rootNode.domNode);
		return dom.textBuffer.toString();
	}

	public static Date parseDate(String date) {
		date = date.replaceAll("/", "-");
		ArrayList<String> dates = new ArrayList<String>();
		String token = "";
		for (int i = 0; i < date.length(); i++) {
			char c = date.charAt(i);
			if (c == '-') {
				if (token.length() > 0) {
					dates.add(token);
				}
				token = "";
				continue;
			}
			if (Character.isDigit(c)) {
				token += c;
			} else {
				break;
			}
		}
		if (token.length() > 0) {
			dates.add(token);
		}
		if (dates.size() < 2 || dates.size() >= 4) {
			return null;
		}
		if (dates.size() == 2) {
			date = DateUtil.getYear(new Date()) + "-" + dates.get(0) + "-" + dates.get(1);
		} else {
			date = dates.get(0) + "-" + dates.get(1) + "-" + dates.get(2);
		}
		return DateUtil.convertDate(date);
	}

	// 加密
	public static String encrypt(String key, String content) {
		// 判断Key是否为16位
		if (key.length() != 16) {
			return null;
		}
		byte[] raw = key.getBytes();
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			// "算法/模式/补码方式"
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
			IvParameterSpec iv = new IvParameterSpec("YIQIHI.XEACH.COM".getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			byte[] encrypted = cipher.doFinal(content.getBytes());
			return new BASE64Encoder().encode(encrypted);
		} catch (Exception e) {
			return content;
		}
	}

	// 解密
	public static String decrypt(String key, String content) {
		try {
			// 判断Key是否为16位
			if (key.length() != 16) {
				return null;
			}
			byte[] raw = key.getBytes("ASCII");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			IvParameterSpec iv = new IvParameterSpec("YIQIHI.XEACH.COM".getBytes());
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] encrypted1 = new BASE64Decoder().decodeBuffer(content);// 先用base64解密
			byte[] original = cipher.doFinal(encrypted1);
			String originalString = new String(original);
			return originalString;
		} catch (Exception ex) {
			return null;
		}
	}

	// 加密
	public static String easyEncrypt(String password, String content) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(password.getBytes("UTF-8"), 0, password.length());
			byte[] rawKey = md.digest();
			SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(rawKey);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
			byte[] encrypted = cipher.doFinal(content.getBytes("UTF-8"));
			return new BASE64Encoder().encodeBuffer(encrypted).toString();
		} catch (Exception e) {
			return content;
		}
	}

	// 解密
	public static String easyDecrypt(String password, String content) {
		try {
			if (password.length() != 16) {
				return null;
			}
			byte[] raw = password.getBytes("ASCII");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			byte[] encrypted1 = new BASE64Decoder().decodeBuffer(content);// 先用base64解密
			byte[] original = cipher.doFinal(encrypted1);
			String originalString = new String(original, "utf-8");
			return originalString;
		} catch (Exception e) {
			e.printStackTrace();
			return content;
		}
	}

	public static String easyDecompress(String encdata) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			InflaterOutputStream zos = new InflaterOutputStream(bos);
			zos.write(new BASE64Decoder().decodeBuffer(encdata));
			zos.close();
			return new String(bos.toByteArray());
		} catch (Exception ex) {
			return "UNZIP_ERR";
		}
	}

	public static String easyCompress(String data) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DeflaterOutputStream zos = new DeflaterOutputStream(bos);
			zos.write(data.getBytes());
			zos.close();
			return new String(new BASE64Encoder().encodeBuffer(bos.toByteArray()));
		} catch (Exception ex) {
			return "ZIP_ERR";
		}
	}

	public static String normalize(String content) {
		int count = 1;
		int index = (int) Math.pow(2, count++);
		char[] values = content.toCharArray();
		while (index < values.length) {
			if (index == 0)
				continue;
			char c = values[index];
			values[index] = values[index - 1];
			values[index - 1] = c;
			index = (int) Math.pow(2, count++);
		}
		return String.valueOf(values);
	}

	public String decode(String content) {
		String key = content.substring(0, 32);
		key = TextUtil.normalize(key);
		content = content.substring(32, content.length());
		try {
			byte[] keys = Hex.decodeHex(key.toCharArray());
			content = new String(CompressTool.decrypt(keys, content), "UTF-8");
			return content;
		} catch (Exception e) {
			return null;
		}
	}

	public static void main(String[] args) {
		BasicDBObject oResult=new BasicDBObject();
		for(int i=0;i<100;i++){
			oResult.append("id"+i, i);
		}
		String content=easyCompress(oResult.toString());
		System.out.println(content);
		System.out.println(easyDecompress(content));

	}

}
