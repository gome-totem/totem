package org.z.global.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bson.BSONObject;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class DBObjectUtil {

	public static BasicDBObject getErrorObject() {
		return new BasicDBObject().append("xeach", false);
	}

	public static BasicDBList toList(String content) {
		if (StringUtil.isEmpty(content)) {
			return new BasicDBList();
		}
		return (BasicDBList) JSON.parse(content);
	}

	public static List<DBObject> toList(BasicDBList list) {
		List<DBObject> result = new ArrayList<DBObject>();
		for (int i = 0; i < list.size(); i++) {
			result.add((DBObject) list.get(i));
		}
		return result;
	}

	public static Object selectXPath(BasicDBObject object, String path) {
		String[] paths = path.split("\\.");
		BasicDBObject child = object;
		for (int i = 0; i < paths.length - 1; i++) {
			child = (BasicDBObject) child.get(paths[i]);
			if (child == null) {
				return null;
			}
		}
		return child.get(paths[paths.length - 1]);
	}

	public static void deleteFields(DBObject object, String... names) {
		for (int i = 0; names != null && i < names.length; i++) {
			object.removeField(names[i]);
		}
	}

	public static void deleteFieldsStartWith(DBObject object, String prefix) {
		ArrayList<String> names = new ArrayList<String>();
		for (Iterator<String> i = object.keySet().iterator(); i.hasNext();) {
			String name = i.next();
			if (name.startsWith(prefix)) {
				names.add(name);
			}
		}
		if (names.size() == 0) {
			return;
		}
		String[] names_ = new String[names.size()];
		names.toArray(names_);
		deleteFields(object, names_);
	}

	public static List<?> wrapList(DBObject... objects) {
		List<DBObject> results = new ArrayList<DBObject>();
		for (int i = 0; i < objects.length; i++) {
			results.add(objects[i]);
		}
		return results;
	}

	public static String[] wrapArray(List<?> list, String fieldName) {
		String[] results = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			BasicDBObject o = (BasicDBObject) list.get(i);
			String fieldValue = o.getString(fieldName);
			results[i] = fieldValue;
		}
		return results;
	}

	public static String[] wrapArray(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof String[]) {
			return (String[]) o;
		} else {
			List<?> list = (List<?>) o;
			String[] results = new String[list.size()];
			list.toArray(results);
			return results;
		}
	}

	public static String toString(List<?> list, String fieldName) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) instanceof String) {
				if (buffer.length() > 0) {
					buffer.append(",");
				}
				buffer.append(list.get(i).toString());
			} else if (StringUtil.isEmpty(fieldName) && ((BasicDBObject) list.get(i)).containsField(fieldName)) {
				if (buffer.length() > 0) {
					buffer.append(",");
				}
				buffer.append(((BasicDBObject) list.get(i)).getString(fieldName));
			}
		}
		return buffer.toString();
	}

	public static ObjectId[] wrapObjectIds(List<?> list, String fieldName) {
		ObjectId[] results = new ObjectId[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object o = list.get(i);
			if (o instanceof String) {
				results[i] = new ObjectId(o.toString());
			} else {
				String fieldValue = ((BasicDBObject) o).getString(fieldName);
				results[i] = new ObjectId(fieldValue);
			}
		}
		return results;
	}
	public static ObjectId[] wrapObjectIds(BasicDBList list) {
		ObjectId[] results = new ObjectId[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object o = list.get(i);
			if (o instanceof String) {
				results[i] = new ObjectId(o.toString());
			} 
		}
		return results;
	}
	public static String wrapSqlIn(String fieldName, List<?> list) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			BasicDBObject o = (BasicDBObject) list.get(i);
			String fieldValue = o.getString(fieldName);
			buffer.append("'" + fieldValue + "'");
			if (i < list.size() - 1) {
				buffer.append(",");
			}
		}
		return buffer.toString();
	}

	public static void sortRows(BasicDBList rows, List<?> docs, String keyFieldName) {
		if (rows.size() == 0) {
			return;
		}
		HashMap<String, BasicDBObject> objects = new HashMap<String, BasicDBObject>();
		while (rows.size() > 0) {
			BasicDBObject oItem = (BasicDBObject) rows.get(0);
			objects.put(oItem.getString(keyFieldName), oItem);
			rows.remove(0);
		}
		int index = 0;
		while (index < docs.size()) {
			BasicDBObject o = (BasicDBObject) docs.get(index);
			String keyId = o.getString("keyId");
			BasicDBObject oItem = objects.get(keyId);
			if (oItem == null) {
				docs.remove(index);
				continue;
			}
			index++;
			rows.add(oItem);
		}
	}

	public static BasicDBObject queryAddIn(BasicDBObject dbQuery, String fieldName, String[] values) {
		dbQuery.append(fieldName, new BasicDBObject().append("$in", values));
		return dbQuery;
	}

	public static BasicDBObject queryAddInObjectIds(BasicDBObject dbQuery, String fieldName, ObjectId[] values) {
		dbQuery.append(fieldName, new BasicDBObject().append("$in", values));
		return dbQuery;
	}

	public static void copyToDBList(String[][] rows, BasicDBList list, List<String> fieldNames) {
		for (int i = 0; i < rows.length; i++) {
			BasicDBObject o = new BasicDBObject();
			for (int t = 0; t < fieldNames.size(); t++) {
				String fieldName = fieldNames.get(t);
				o.append(fieldName, rows[i][t]);
			}
			list.add(o);
		}
	}

	public static void quickSortByNumDesc(BasicDBList rows, final String fieldName) {
		Collections.sort(rows, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				BasicDBObject object1 = (BasicDBObject) o1;
				BasicDBObject object2 = (BasicDBObject) o2;
				return object2.getInt(fieldName, 0) - object1.getInt(fieldName, 0);
			}
		});
	}

	public static void quickSortByNumAsc(BasicDBList rows, final String fieldName) {
		Collections.sort(rows, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				BasicDBObject object1 = (BasicDBObject) o1;
				BasicDBObject object2 = (BasicDBObject) o2;
				return object1.getInt(fieldName, 0) - object2.getInt(fieldName, 0);
			}
		});
	}

	public static BasicDBObject merge(BasicDBObject source, BasicDBObject dest) {
		boolean b1 = source == null;
		boolean b2 = dest == null;
		if (b1 && b2)
			return null;
		if (b1 == false && b2)
			return source;
		if (b2 == false && b1)
			return dest;
		source.putAll((BSONObject) dest);
		return source;
	}

	public static BasicDBObject selectBy(BasicDBObject oReq, String... fields) {
		BasicDBObject oResult = new BasicDBObject();
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			if (oReq.containsField(field) == false) {
				continue;
			}
			oResult.put(field, oReq.get(field));
		}
		return oResult;
	}

	public static void removeBy(BasicDBObject oReq, String... fields) {
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			oReq.removeField(field);
		}
	}

	public static void copyBy(BasicDBObject oSource, BasicDBObject oDest, String... fields) {
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			oDest.append(field, oSource.get(field));
		}
	}

	public static void copyBy(BasicDBObject oSource, String sourceField, BasicDBObject oDest, String destField) {
		Object value = oSource.get(sourceField);
		if (value == null)
			return;
		if ((value instanceof String) && (StringUtils.isEmpty((String) value))) {
			return;
		}
		oDest.append(destField, value);
	}

	public static BasicDBObject fillZero(BasicDBObject oReq, String... fields) {
		BasicDBObject oResult = new BasicDBObject();
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			if (oReq.containsField(field) == false) {
				continue;
			}
			oResult.put(field, "");
		}
		return oResult;
	}

}
