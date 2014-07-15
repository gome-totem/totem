package org.z.global.object;

public class KeyValue {

	public String name = null;

	public Object value = null;

	public static KeyValue create(String name, Object value) {
		KeyValue result = new KeyValue();
		result.name = name;
		result.value = value;
		return result;
	}

	public static KeyValue[] create(String name, String[] values) {
		KeyValue[] result = new KeyValue[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = create(name, values[i]);
		}
		return result;
	}

	public static KeyValue create(String name) {
		KeyValue result = new KeyValue();
		result.name = name;
		return result;
	}

}
