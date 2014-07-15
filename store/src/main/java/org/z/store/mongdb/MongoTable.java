package org.z.store.mongdb;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class MongoTable {



	protected String quoted(String value) {
		return "'" + value + "' ";
	}

	/**
	 * 更新JSON数组<深度为1>节点的数据，如果没有的话，插入。
	 * 
	 * @param oCurrent
	 *            当前对象
	 * @param fieldName
	 *            主字段
	 * @param listFieldName
	 *            数组，字段名称
	 * @param listFieldValue
	 *            数组，对应的值
	 * @param values
	 *            数组，附加字段值
	 * @return
	 */
	protected DBObject updateDBList(DBObject oCurrent, String fieldName,
			String listFieldName, Object listFieldValue,
			Map<String, Object> values) {
		BasicDBList list = (BasicDBList) oCurrent.get(fieldName);
		DBObject oItem = null;
		if (list == null) {
			list = new BasicDBList();
			oItem = BasicDBObjectBuilder.start(listFieldName, listFieldValue)
					.get();
			oItem.putAll(values);
			list.add(oItem);
			oCurrent.put(fieldName, list);
		} else {
			int index = -1;
			for (int i = 0; i < list.size(); i++) {
				oItem = (DBObject) list.get(i);
				if ((oItem.get(listFieldName).equals(listFieldValue))) {
					index = i;
					break;
				}
			}
			if (index == -1) {
				oItem = BasicDBObjectBuilder.start(values).append(
						listFieldName, listFieldValue).get();
				oItem.putAll(values);
				list.add(oItem);
			} else {
				oItem.putAll(values);
			}
		}
		return oItem;
	}

	/**
	 * 更新JSON数组<深度为2>节点的数据，如果没有的话，插入。
	 */
	protected DBObject updateDBList(DBObject oCurrent, String fieldName1,
			String listFieldName1, Object listFieldValue1, String fieldName2,
			String listFieldName2, Object listFieldValue2,
			Map<String, Object> values) {
		DBObject oItem1 = this.updateDBList(oCurrent, fieldName1,
				listFieldName1, listFieldValue1, new HashMap<String, Object>());
		return this.updateDBList(oItem1, fieldName2, listFieldName2,
				listFieldValue2, values);
	}

	/*
	 * 删除JSON数组<深度为1>节点的数据
	 */
	protected int removeDBList(DBObject oCurrent, String fieldName,
			String listFieldName, Object listFieldValue) {
		BasicDBList list = (BasicDBList) oCurrent.get(fieldName);
		int result = 0;
		if (list == null) {
			return result;
		}
		int i = 0;
		while (i < list.size()) {
			DBObject oItem = (DBObject) list.get(i);
			if ((oItem.get(listFieldName).equals(listFieldValue))) {
				list.remove(i);
				result++;
				continue;
			}
			i++;
		}
		return result;
	}

	/*
	 * 删除JSON数组<深度为2>节点的数据
	 */
	protected int removeDBList(DBObject oCurrent, String fieldName1,
			String listFieldName1, Object listFieldValue1, String fieldName2,
			String listFieldName2, Object listFieldValue2) {
		BasicDBList list = (BasicDBList) oCurrent.get(fieldName1);
		int result = 0;
		if (list == null) {
			return result;
		}
		int i = 0;
		while (i < list.size()) {
			DBObject oItem = (DBObject) list.get(i);
			if ((oItem.get(listFieldName1).equals(listFieldValue1))) {
				return removeDBList(oItem, fieldName2, listFieldName2,
						listFieldValue2);
			}
			i++;
		}
		return result;
	}

}
