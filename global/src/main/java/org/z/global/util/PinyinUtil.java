package org.z.global.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinUtil {
	public static String[] toSpell(String chines) {
		StringBuilder pyShort = new StringBuilder();
		StringBuilder pyLong = new StringBuilder();
		char[] nameChar = chines.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (int i = 0; i < nameChar.length; i++) {
			if (nameChar[i] > 128) {
				try {
					String[] strs = PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat);
					if (strs == null)
						continue;
					pyShort.append(strs[0].charAt(0));
					pyLong.append(strs[0].replaceAll(":", ""));
				} catch (BadHanyuPinyinOutputFormatCombination e) {
				}
			} else {
				pyShort.append(nameChar[i]);
				pyLong.append(nameChar[i]);
			}
		}
		return new String[] { pyShort.toString(), pyLong.toString() };
	}

}
