package org.z.core.test;


public class SkipListEntry {
	public String key;
	public Integer value;

	public int pos; //主要为了打印 链表用

	public SkipListEntry up, down, left, right; // 上下左右 四个指针

	public static String negInf = new String("-oo"); // 负无穷
	public static String posInf = new String("+oo"); // 正无穷

	public SkipListEntry(String k, Integer v) {
		key = k;
		value = v;

		up = down = left = right = null;
	}

	public Integer getValue() {
		return value;
	}

	public String getKey() {
		return key;
	}

	public Integer setValue(Integer val) {
		Integer oldValue = value;
		value = val;
		return oldValue;
	}

	public boolean equals(Object o) {
		SkipListEntry ent;
		try {
			ent = (SkipListEntry) o; // 检测类型
		} catch (ClassCastException ex) {
			return false;
		}
		return (ent.getKey() == key) && (ent.getValue() == value);
	}

	public String toString() {
		return "(" + key + "," + value + ")";
	}
}
