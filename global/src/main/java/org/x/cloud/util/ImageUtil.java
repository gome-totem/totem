package org.x.cloud.util;



import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImagingOpException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.x.cloud.dict.Global.ImageType;
import org.x.cloud.util.Scalr.Method;

public class ImageUtil {
	private static int getLength(String text) {
		int length = 0;
		for (int i = 0; i < text.length(); i++) {
			if (new String(text.charAt(i) + "").getBytes().length > 1) {
				length += 2;
			} else {
				length += 1;
			}
		}
		return length / 2;
	}

	public static BufferedImage pressImage(BufferedImage source, Image markImage, float alpha) {
		if (markImage == null)
			return source;
		try {
			int width = source.getWidth(null);
			int height = source.getHeight(null);
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			g.drawImage(source, 0, 0, width, height, null);
			if (markImage != null) {
				int wMark = markImage.getWidth(null);
				int hMark = markImage.getHeight(null);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
				g.drawImage(markImage, (width - wMark) / 2, (height - hMark) / 2, wMark, hMark, null);
			}
			g.dispose();
			return image;
		} catch (Exception e) {
			return source;
		}
	}

	public static BufferedImage pressText(BufferedImage source, String content, float alpha) {
		if (StringUtils.isEmpty(content)) {
			return source;
		}
		try {
			int width = source.getWidth(null);
			int height = source.getHeight(null);
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			g.drawImage(source, 0, 0, width, height, null);
			g.setColor(Color.white);
			int fontStyle = Font.CENTER_BASELINE;
			int fontSize = 30;
			g.setFont(new Font("Courier", fontStyle, fontSize));
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
			g.drawString(content, (width - getLength(content) * fontSize) - 10, height - fontSize - 10);
			g.dispose();
			return image;
		} catch (Exception e) {
			return source;
		}
	}

	public static byte[] readBytes(String file) {
		Image source;
		try {
			source = ImageIO.read(new File(file));
			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
			ImageIO.write((BufferedImage) source, "jpg", bStream);
			return bStream.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	public static byte[] readBytes(BufferedImage image) {
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", bStream);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return bStream.toByteArray();
	}

	public static BufferedImage readImage(File file) {
		try {
			return (BufferedImage) ImageIO.read(file);
		} catch (IOException e) {
			return null;
		}
	}

	public static BufferedImage readImageFromBytes(byte[] bytes) {
		Image source;
		try {
			source = ImageIO.read(new ByteArrayInputStream(bytes));
			return (BufferedImage) source;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static BufferedImage readImageFromStream(InputStream inputStream) {
		Image source;
		try {
			source = ImageIO.read(inputStream);
			return (BufferedImage) source;
		} catch (IOException e) {
			return null;
		}
	}

	public static boolean writeFile(byte[] bSource, String fileName) {
		Image source;
		try {
			source = ImageIO.read(new ByteArrayInputStream(bSource));
			ImageIO.write((BufferedImage) source, "png", new File(fileName));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static boolean writeFile(BufferedImage source, String fileName) {
		try {
			ImageIO.write(source, "png", new File(fileName));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static BufferedImage resize(ImageType type, BufferedImage iSource) {
		BufferedImage iDest = null;
		int w = iSource.getWidth();
		if (type == ImageType.Face) {
			iDest = Scalr.resize(iSource, Method.SPEED, w > 230 ? 230 : w);
		} else if (type == ImageType.Auth) {
			iDest = Scalr.resize(iSource, Method.SPEED, w > 480 ? 480 : w);
		} else if (type == ImageType.Snapshot) {
			iDest = Scalr.resize(iSource, Method.SPEED, w > 300 ? 300 : w);
		} else {
			iDest = Scalr.resize(iSource, Method.SPEED, w > 800 ? 800 : w);
		}
		return iDest;
	}

	public static BufferedImage resize(ImageType type, byte[] source) {
		BufferedImage iSource = readImageFromBytes(source);
		return resize(type, iSource);
	}

	public static BufferedImage crop(BufferedImage src, int x, int y, int width, int height) throws IllegalArgumentException, ImagingOpException {
		return Scalr.crop(src, x, y, width, height, new BufferedImageOp[0]);
	}

	public static void main(String[] args) throws IOException {

	}
}