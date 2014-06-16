package org.z.global.util;

import java.util.Collection;
import java.util.Map;

/**
 * 判断对象是否为空
 * 
 * @author 蒋礼俊
 * @version 2013-4-13 下午1:38:29
 */
public final class EmptyUtil {

	private EmptyUtil() {
	}

	/**
	 * 判断字符串为空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0 || str.equalsIgnoreCase("null") || str.equalsIgnoreCase("undefined");
	}

	/**
	 * 判断字符串不为空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean notEmpty(String str) {
		return !isEmpty(str);
	}

	/**
	 * 
	 * 判断数组为空
	 * 
	 * @param array
	 * 
	 * @return
	 */
	public static boolean isEmpty(Object[] array) {
		return array == null || array.length == 0;
	}

	/**
	 * 判断数组不为空
	 * 
	 * @param array
	 * @return
	 */
	public static boolean notEmpty(Object[] array) {
		return !isEmpty(array);
	}

	/**
	 * 判断object为空
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isEmpty(Object obj) {
		if (obj instanceof String) {
			return isEmpty((String) obj);
		} else if (obj instanceof Object[]) {
			return isEmpty((Object[]) obj);
		} else if (obj instanceof Collection) {
			return isEmpty((Collection<?>) obj);
		} else if (obj instanceof Map) {
			return isEmpty((Map<?, ?>) obj);
		} else if (obj == null) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * 判断集合为空
	 * 
	 * @param collection
	 *            集合
	 * @return
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * 
	 * 判断集合不为空
	 * 
	 * @param collection
	 * 
	 * @return
	 */
	public static boolean notEmpty(Collection<?> collection) {
		return !isEmpty(collection);
	}

	/**
	 * 判断map不为空
	 * 
	 * @param map
	 * @return
	 */
	public static boolean notEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

	/**
	 * 判断map为空
	 * 
	 * @param map
	 * @return
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * 判断object不为空
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean notEmpty(Object obj) {
		return !isEmpty(obj);
	}
}
