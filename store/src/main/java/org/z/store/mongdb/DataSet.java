package org.z.store.mongdb;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.util.StringUtil;
import org.z.store.mysql.MySqlConnect;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class DataSet {
	protected static Logger logger = LoggerFactory.getLogger(DataSet.class);

	public static boolean update(String serverName, String dbName, String sql, String[] values, String[] appendValues) {
		String[] params = new String[values.length + (appendValues == null ? 0 : appendValues.length)];
		System.arraycopy(values, 0, params, 0, values.length);
		if (appendValues != null) {
			System.arraycopy(appendValues, 0, params, values.length, appendValues.length);
		}
		return update(serverName, dbName, sql, params);
	}

	public static boolean update(String serverName, String dbName, String sql, Object[] values) {
		Connection connection = MySqlConnect.getDbConnection(serverName, dbName);
		if (connection == null) {
			String text_ = "serverName=" + serverName + "&dbName=" + dbName + "& get connecton null.";
			logger.error(text_);
			return false;
		}
		try {
			if (!connection.getAutoCommit()) {
				connection.setAutoCommit(true);
			}
			updateByConnection(connection, sql, values);
			return true;
		} catch (Exception e) {
			logger.error(sql, e);
			return false;
		} finally {
			MySqlConnect.close(connection);
		}
	}

	public static void updateByConnection(Connection connection, String sql, Object[] values) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(sql);
		for (int i = 0; values != null && i < values.length; i++) {
			statement.setObject(i + 1, values[i]);
		}
		statement.execute();
	}

	public static int[] update(String serverName, String dbName, String sql, String[][] values) {
		Connection connection = MySqlConnect.getDbConnection(serverName, dbName);
		if (connection == null) {
			String text_ = "serverName=" + serverName + "&dbName=" + dbName + "& get connecton null.";
			logger.error(text_);
			return null;
		}
		try {
			if (connection.getAutoCommit()) {
				connection.setAutoCommit(false);
			}
			connection.commit();
			return updateByConnection(connection, sql, values);
		} catch (Exception e) {
			logger.error(sql, e);
			return null;
		} finally {
			MySqlConnect.close(connection);
		}
	}

	public static int[] updateByConnection(Connection connection, String sql, String[][] values) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(sql);
		for (int recIndex = 0; recIndex < values.length; recIndex++) {
			for (int i = 0; values != null && i < values[recIndex].length; i++) {
				statement.setString(i + 1, values[recIndex][i]);
			}
			statement.addBatch();
		}
		int[] result = statement.executeBatch();
		return result;
	}

	public static BasicDBList queryDBList(String serverName, String dbName, String sql, int offset, int count) {
		return queryDBList(serverName, dbName, sql, null, offset, count);
	}

	public static BasicDBObject queryDBObject(String serverName, String dbName, String sql, int offset, int count) {
		BasicDBList list = queryDBList(serverName, dbName, sql, null, offset, count);
		if (list == null || list.size() == 0) {
			return null;
		}
		return (BasicDBObject) list.get(0);
	}

	public static BasicDBObject queryDBObject(String serverName, String dbName, String sql, Object[] values, int offset, int count) {
		BasicDBList list = queryDBList(serverName, dbName, sql, values, offset, count);
		if (list == null || list.size() == 0) {
			return null;
		}
		return (BasicDBObject) list.get(0);
	}

	public static String[][] query(String serverName, String dbName, String sql, Object[] values, int offset, int count) {
		return query(serverName, dbName, sql, values, offset, count, null);
	}

	public static String[][] query(String serverName, String dbName, String sql, Object[] values, int offset, int count, List<String> fieldNames) {
		Connection connection = MySqlConnect.getDbConnection(serverName, dbName);
		if (connection == null) {
			String text_ = "serverName=" + serverName + "&dbName=" + dbName + "& get connecton null.";
			logger.error(text_);
			return null;
		}
		return query(connection, sql, values, offset, count, fieldNames);
	}

	public static String[][] query(Connection connection, String sql, Object[] values, int offset, int count, List<String> fieldNames) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String[][] data = null;
		try {
			if (offset > 0) {
				stmt = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			} else {
				stmt = connection.prepareStatement(sql);
			}

			for (int i = 0; values != null && i < values.length; i++) {
				stmt.setObject(i + 1, values[i]);
			}
			int fetchSize = count;
			if (fetchSize > 100 || fetchSize < 1) {
				fetchSize = 100;
			}
			if (count > 0) {
				stmt.setMaxRows(offset + count);
				stmt.setFetchSize(fetchSize);
			} else {
				count = 100;
			}

			rs = stmt.executeQuery();
			if (offset > 0) {
				rs.absolute(offset);
			}

			data = getStringData(rs, count, rs.getMetaData().getColumnCount());
			if (fieldNames != null) {
				for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
					fieldNames.add(rs.getMetaData().getColumnLabel(i + 1));
				}
			}
		} catch (Exception e) {
			logger.error("sql=" + sql, e);
			return null;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException ex) {
			}
			MySqlConnect.close(connection);
		}
		return data;
	}

	public static String[][] query(String serverName, String dbName, String sql, int offset, int count) {
		return query(serverName, dbName, sql, offset, count, null);
	}

	public static String[][] query(String serverName, String dbName, String sql, int offset, int count, List<String> fieldNames) {
		Connection connection = MySqlConnect.getDbConnection(serverName, dbName);
		if (connection == null) {
			String text_ = "serverName=" + serverName + "&dbName=" + dbName + "& get connecton null.";
			logger.error(text_);
			return null;
		}
		return query(connection, sql, null, offset, count, fieldNames);
	}

	private static String[][] getStringData(ResultSet rs, int count, int fieldCount) {
		try {
			String[][] data = new String[count][fieldCount];
			int size = 0;
			while (rs.next()) {
				if (size == count) {
					count = (count * 3) / 2 + 1;
					String[][] oldData = data;
					data = new String[count][fieldCount];
					System.arraycopy(oldData, 0, data, 0, size);
				}
				for (int i = 0; i < fieldCount; i++) {
					if (Types.TIMESTAMP == rs.getMetaData().getColumnType(i + 1)) {
						Timestamp timestamp = rs.getTimestamp(i + 1);
						if (timestamp != null) {
							data[size][i] = String.valueOf(timestamp.getTime());
						} else {
							data[size][i] = null;
						}
						continue;
					} else if (Types.DATE == rs.getMetaData().getColumnType(i + 1)) {
						Date date = rs.getDate(i + 1);
						if (date != null) {
							data[size][i] = String.valueOf(date.getTime());
						} else {
							data[size][i] = null;
						}
						continue;
					}
					data[size][i] = rs.getString(i + 1);
					if (StringUtil.isEmpty(data[size][i])) {
						data[size][i] = "";
					}
				}
				size++;
			}
			if (size == data.length) {
				return data;
			} else {
				String[][] re = new String[size][fieldCount];
				System.arraycopy(data, 0, re, 0, size);
				return re;
			}
		} catch (Exception e) {
			logger.error("getStringData", e);
			return null;
		}
	}

	public static BasicDBList queryDBList(String serverName, String dbName, String sql, Object[] values, int offset, int count) {
		ArrayList<BasicDBObject> list = queryDBObjects(serverName, dbName, sql, values, offset, count);
		BasicDBList items = new BasicDBList();
		for (int i = 0; list != null && i < list.size(); i++) {
			items.add(list.get(i));
		}
		return items;
	}

	public static ArrayList<BasicDBObject> queryDBObjects(String serverName, String dbName, String sql, int offset, int count) {
		return queryDBObjects(serverName, dbName, sql, null, offset, count);
	}

	public static ArrayList<BasicDBObject> queryDBObjects(String serverName, String dbName, String sql, Object[] values, int offset, int count) {
		Connection connection = MySqlConnect.getDbConnection(serverName, dbName);
		if (connection == null) {
			String text_ = "serverName=" + serverName + "&dbName=" + dbName + "& get connecton null.";
			logger.error(text_);
			return null;
		}
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<BasicDBObject> data = null;
		try {
			if (offset > 0) {
				stmt = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			} else {
				stmt = connection.prepareStatement(sql);
			}

			for (int i = 0; values != null && i < values.length; i++) {
				stmt.setObject(i + 1, values[i]);
			}
			int fetchSize = count;
			if (fetchSize > 100 || fetchSize < 1) {
				fetchSize = 100;
			}
			if (count > 0) {
				stmt.setMaxRows(offset + count);
				stmt.setFetchSize(fetchSize);
			}
			rs = stmt.executeQuery();
			if (offset > 0) {
				rs.absolute(offset);
			}
			data = getDBObjectData(rs, count, rs.getMetaData().getColumnCount());
		} catch (Exception e) {
			logger.error("sql=[{}]  error=[{}]", new Object[] { sql, e.getMessage() });
			return null;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException ex) {
			}
			MySqlConnect.close(connection);
		}
		return data;
	}

	private static ArrayList<BasicDBObject> getDBObjectData(ResultSet rs, int count, int fieldCount) {
		ArrayList<BasicDBObject> data = new ArrayList<BasicDBObject>();
		try {
			int size = 0;
			ResultSetMetaData meta = rs.getMetaData();
			while (rs.next()) {
				if (count != 0 && size > count) {
					break;
				}
				BasicDBObject oItem = new BasicDBObject();
				for (int i = 0; i < fieldCount; i++) {
					int index = i + 1;
					int type = meta.getColumnType(index);
					String fieldName = meta.getColumnLabel(index);
					switch (type) {
					case Types.INTEGER:
					case Types.TINYINT:
					case Types.SMALLINT:
						oItem.put(fieldName, rs.getInt(index));
						break;
					case Types.TIMESTAMP:
						Timestamp timestamp = rs.getTimestamp(index);
						if (timestamp != null) {
							oItem.put(fieldName, timestamp.getTime());
						} else {
							oItem.put(fieldName, 0L);
						}
						break;
					case Types.DATE:
						Date date = rs.getDate(index);
						if (date != null) {
							oItem.put(fieldName, date.getTime());
						} else {
							oItem.put(fieldName, 0);
						}
						break;
					case Types.BIGINT:
					case Types.ROWID:
						oItem.put(fieldName, rs.getLong(index));
						break;
					case Types.NUMERIC:
					case Types.FLOAT:
					case Types.REAL:
					case Types.DECIMAL:
						oItem.put(fieldName, rs.getFloat(index));
						break;
					case Types.DOUBLE:
						oItem.put(fieldName, rs.getDouble(index));
						break;
					case Types.CHAR:
					case Types.VARCHAR:
					case Types.LONGVARCHAR:
					case Types.NCHAR:
					case Types.NVARCHAR:
					case Types.LONGNVARCHAR:
					case Types.SQLXML:
						String value = rs.getString(index);
						oItem.put(fieldName, StringUtils.isEmpty(value) ? "" : value);
						break;
					case Types.BOOLEAN:
						oItem.put(fieldName, rs.getBoolean(index));
						break;
					}
				}
				size++;
				data.add(oItem);
			}
			return data;
		} catch (Exception e) {
			logger.error("getDBObjectData", e);
			return null;
		}
	}

	public static String toSql(int type, String[] names) {
		if (names == null || names.length == 0) {
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < names.length; i++) {
			buffer.append(",");
			switch (type) {
			case -1:
				buffer.append("?");
				break;
			case 0:
				buffer.append(names[i]);
				break;
			case 1:
				buffer.append(names[i] + "=?");
				break;
			}
		}
		return buffer.toString();
	}

	public static String[] toSqlFields(String[] values, String[] names) {
		if (values == null || values.length == 0) {
			return null;
		}
		String[] fieldNames = new String[values.length];
		for (int i = 0; i < values.length && i < names.length; i++) {
			fieldNames[i] = names[i];
		}
		return fieldNames;
	}

	public static String[] toParameters(Object... args) {
		return toParameters(0, args);
	}

	public static String[] toParameters(ArrayList<String> args) {
		return args.toArray(new String[0]);
	}

	public static String[] toParameters(int mode, Object... args) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null) {
				if (mode == 0) {
					list.add("");
				}
				continue;
			}
			if (args[i] instanceof String) {
				list.add(args[i].toString());
			} else if (args[i] instanceof String[]) {
				String[] values = (String[]) args[i];
				for (int t = 0; t < values.length; t++) {
					list.add(values[t]);
				}
			} else {
				list.add(args[i].toString());
			}
		}
		String[] results = new String[list.size()];
		list.toArray(results);
		return results;

	}

	public static long insert(String serverName, String dbName, String sql, Object[] values) {
		long AutoId = -1;
		Connection connection = MySqlConnect.getDbConnection(serverName, dbName);
		if (connection == null) {
			return AutoId;
		}
		try {
			if (!connection.getAutoCommit()) {
				connection.setAutoCommit(true);
			}
			PreparedStatement statement = connection.prepareStatement(sql);
			for (int i = 0; values != null && i < values.length; i++) {
				statement.setObject(i + 1, values[i]);
			}
			statement.execute();

			ResultSet rs = statement.executeQuery("SELECT LAST_INSERT_ID()");
			if (rs.next()) {
				AutoId = rs.getLong(1);
			}
		} catch (Exception e) {
			logger.error(sql, e);
			return AutoId;
		} finally {
			MySqlConnect.close(connection);
		}
		return AutoId;
	}

	public static Connection GetConnection(String serverName, String dbName) {
		Connection connection = MySqlConnect.getDbConnection(serverName, dbName);
		return connection;
	}

	public static boolean update(PreparedStatement statement, String sql, String[][] values) {
		try {
			statement.clearBatch();
			statement.clearParameters();
			statement.addBatch(sql);

			for (int recIndex = 0; recIndex < values.length; recIndex++) {
				for (int i = 0; values != null && i < values[recIndex].length; i++) {
					statement.setString(i + 1, values[recIndex][i]);
				}

				statement.addBatch();
			}

			return statement.execute();
		} catch (Exception e) {
			return false;
		}
	}

}
