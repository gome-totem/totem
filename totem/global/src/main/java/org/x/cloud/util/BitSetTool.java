package org.x.cloud.util;

import java.util.BitSet;

/**
 * 
 * @author ming.xiao
 * 
 */
public class BitSetTool {

	/**
	 * Returns a bitset of the given byte array. The byte-ordering of bytes must
	 * be big-endian which means the most significant bit is in element 0. Big
	 * endian machine: It thinks the first byte it reads is the biggest.
	 */
	public static BitSet toBitSet(byte[] bytes) {
		BitSet bits = new BitSet();
		for (int i = 0; i < bytes.length * 8; i++) {
			if ((bytes[i / 8] & (128 >> (i % 8))) > 0) {
				bits.set(i);
			}
		}
		return bits;
	}

	/**
	 * Returns a byte array of at least length 1. The byte-ordering of the
	 * result is big-endian which means the most significant bit is in element
	 * 0. The bit at index 0 of the bit set is assumed to be the most
	 * significant bit.
	 */
	public static byte[] toByteArray(BitSet bits) {
		int bitLen = bits.length();
		int byteLen = (((bitLen % 8) == 0) ? (bitLen / 8) : (bitLen / 8 + 1));
		byte[] bytes = new byte[byteLen];
		for (int i = 0; i < bits.length(); i++) {
			if (bits.get(i)) {
				bytes[i / 8] |= (128 >> (i % 8));
			}
		}
		return bytes;
	}

	/**
	 * Returns a conventional string representation of the given bitset which
	 * means that we use a string of 1 and 0 to represent the bitset.
	 */
	public static String toStr(BitSet bits) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < bits.length(); i++) {
			if (bits.get(i)) {
				sb.append("1");
			} else {
				sb.append("0");
			}
		}

		return sb.toString();
	}

	public static BitSet fromStr(String value) {
		BitSet result = new BitSet();
		for (int i = 0; i < value.length(); i++) {
			if (value.charAt(i) == '1') {
				result.set(i);
			}
		}
		return result;
	}

	/**
	 * Gets the comparison bit of the two bitsets. If the lengths of the two
	 * bitsets are different and the prefix of the longer bitset is the shorter
	 * bitset, then returns the first 1 bit after that prefix (i.e. the shorter
	 * bitset is padded with 0 bits). If the two bitsets are equal, then returns
	 * 0.
	 */
	public static int getComparisonBit(BitSet bit1, BitSet bit2) {
		int bit1Len = bit1.length();
		int bit2Len = bit2.length();
		int maxLen = Math.max(bit1Len, bit2Len);
		int i = 0;
		boolean found = false;
		for (i = 0; (i < maxLen) && !found; i++) {
			if ((i < bit1Len) && (i < bit2Len)) {
				if (bit1.get(i) != bit2.get(i)) {
					found = true;
				}
			} else {
				if (bit1Len > bit2Len) {
					if (bit1.get(i)) {
						found = true;
					}
				} else {
					if (bit2.get(i)) {
						found = true;
					}
				}
			}
		}
		if (!found) {
			i = 0;
		}

		return i;
	}

	public static void main(String[] args) throws Exception {
		String str = "0";
		System.out.println("Test:" + str);
		BitSet bits = BitSetTool.fromStr("000111000");
		System.out.println(bits.get(3));
		bits.set(3,false);
		System.out.println(BitSetTool.toStr(bits));
		bits = BitSetTool.toBitSet(str.getBytes());
		System.out.println(BitSetTool.toStr(bits));
		int bitIndex = 359;
		System.out.println((bitIndex + 1) + "th bit is " + bits.get(bitIndex));
		String newStr = new String(BitSetTool.toByteArray(bits));
		System.out.println("BitSetTool of " + str + " is "
				+ BitSetTool.toStr(bits));
		System.out.println("String of " + BitSetTool.toStr(bits) + " is "
				+ newStr);
		System.out.println("length of the new string is " + newStr.length());
		String str1 = "Hq";
		BitSet bits1 = BitSetTool.toBitSet(str1.getBytes());
		System.out.println("BitSetTool of " + str1 + " is "
				+ BitSetTool.toStr(bits1));
		System.out.println("CB of " + str + " and " + str1 + " is "
				+ BitSetTool.getComparisonBit(bits, bits1));
	}
}
