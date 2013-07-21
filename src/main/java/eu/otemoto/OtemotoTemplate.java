package eu.otemoto;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class OtemotoTemplate {
	/***/
	private SQLiteDatabase db;

	public void setDb(SQLiteDatabase db) {
		this.db = db;
	}

	/**
	 * クラス名からテーブル名を取得
	 * 
	 * @return テーブル名
	 */
	protected static String getTableName(Class<?> entityClass) {
		String tableName = entityClass.getSimpleName();
		tableName = tableName.substring(0, tableName.length() - 6);
		tableName = camelToSnake(tableName);
		return tableName;
	}

	protected static String camelToSnake(String value) {
		String result = value.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2").replaceAll("([a-z])([A-Z])", "$1_$2");
		result = result.toUpperCase(Locale.ENGLISH);
		return result;
	}

	protected static String snakeToCamel(String targetStr) {
		Pattern pattern = Pattern.compile("_([a-z])");
		Matcher matcher = pattern.matcher(targetStr.toLowerCase());
		StringBuffer stringBuffer = new StringBuffer(targetStr.length());
		while (matcher.find()) {
			matcher.appendReplacement(stringBuffer, matcher.group(1).toUpperCase());
		}
		matcher.appendTail(stringBuffer);
		return stringBuffer.toString();
	}

	protected static String[] convertColumns(Class<?> clazz) {
		// フィールドから列名を取得
		Field[] fields = clazz.getDeclaredFields();
		String[] columns = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String column = field.getName();
			column = camelToSnake(column);
			columns[i] = column;
		}
		return columns;
	}

	protected static Object setValue(Field field, Object param, Object value) {
		try {
			field.setAccessible(true);
			field.set(param, value);
			Object parameter = field.get(param);
			return parameter;
		} catch (Exception e) {
			throw new RuntimeException("error", e);
		}
	}

	protected static <RESULT> RESULT createInstance(Class<RESULT> resultClass) {
		try {
			return resultClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("error", e);
		}
	}

	protected static <RESULT> Field getDeclaredField(Class<RESULT> resultClass, String name) {
		try {
			return resultClass.getDeclaredField(name);
		} catch (Exception e) {
			throw new RuntimeException("error", e);
		}
	}

	protected static Object getValue(Field field, Object param) {
		try {
			field.setAccessible(true);
			return field.get(param);
		} catch (Exception e) {
			throw new RuntimeException("error", e);
		}
	}

	protected static Object getValue(Field field, Cursor cursor) {
		String propertyName = field.getName();
		propertyName = camelToSnake(propertyName);
		int columnIndex = cursor.getColumnIndex(propertyName);
		Class<?> type = field.getType();
		if (type.equals(Integer.class)) {
			return cursor.getInt(columnIndex);
		} else if (type.equals(Long.class)) {
			return cursor.getLong(columnIndex);
		} else if (type.equals(Short.class)) {
			return cursor.getShort(columnIndex);
		} else if (type.equals(Float.class)) {
			return cursor.getFloat(columnIndex);
		} else if (type.equals(Double.class)) {
			return cursor.getDouble(columnIndex);
		} else if (type.equals(String.class)) {
			return cursor.getString(columnIndex);
		} else {
			return cursor.getBlob(columnIndex);
		}
	}

	public <RESULT> List<RESULT> findList(Class<RESULT> resultClass, RESULT param) {
		String[] columns = convertColumns(resultClass);
		String where = "";
		List<String> whereArgList = new ArrayList<String>();
		if (param != null) {
			Field[] fields = resultClass.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				Object object = getValue(field, param);
				if (object == null) {
					continue;
				}

				//
				String column = columns[i];
				where += column + " = ? AND";

				//
				String value = object.toString();
				whereArgList.add(value);
			}
		}

		if (where.length() != 0) {
			where = where.substring(0, where.length() - 4);
		}

		String[] whereArgs = whereArgList.toArray(new String[0]);
		String tableName = getTableName(resultClass);

		String sql = "SELECT * FROM " + tableName;
		if (where.length() != 0) {
			sql += " WHERE " + where;
		}

		return queryForList(resultClass, sql, whereArgs);
	}

	protected static <RESULT> List<RESULT> cursorToList(Class<RESULT> resultClass, Cursor cursor) {
		List<RESULT> list = new ArrayList<RESULT>();
		while (cursor.moveToNext()) {
			RESULT entity = createInstance(resultClass);
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				String column = cursor.getColumnName(i);
				column = snakeToCamel(column);
				Field field = getDeclaredField(resultClass, column);
				Object value = getValue(field, cursor);
				setValue(field, entity, value);
			}
			list.add(entity);
		}
		return list;
	}

	public <RESULT> List<RESULT> queryForList(Class<RESULT> resultClass, String sql, String[] selectionArgs) {
		Cursor cursor = db.rawQuery(sql, selectionArgs);
		List<RESULT> list = cursorToList(resultClass, cursor);
		cursor.close();
		return list;
	}

	public int queryForInt(String sql, String[] selectionArgs) {
		Cursor cursor = db.rawQuery(sql, selectionArgs);
		if (!cursor.moveToNext()) {
			// TODO なかったときの扱いはruntime exception?
			cursor.close();
			return 0;
		}
		int result = cursor.getInt(0);
		cursor.close();
		return result;
	}

	public void execSQL(String sql) {
		this.db.execSQL(sql);
	}

	public void execSQL(String sql, Object[] bindArgs) {
		this.db.execSQL(sql, bindArgs);
	}

	public <RESULT> long add(Class<RESULT> resultClass, RESULT entity) {
		String sql = getSqlForInsert(resultClass, entity);
		SQLiteStatement stmt = db.compileStatement(sql);
		Field[] fields = resultClass.getDeclaredFields();
		int no = 1;
		for (Field field : fields) {
			Object parameter = getValue(field, entity);
			if (parameter == null) {
				continue;
			}

			Class<?> type = field.getType();
			if (type.equals(Integer.class)) {
				stmt.bindLong(no, (Integer) parameter);
			} else if (type.equals(Long.class)) {
				stmt.bindLong(no, (Long) parameter);
			} else if (type.equals(Short.class)) {
				stmt.bindLong(no, (Short) parameter);
			} else if (type.equals(Float.class)) {
				stmt.bindDouble(no, (Float) parameter);
			} else if (type.equals(Double.class)) {
				stmt.bindDouble(no, (Double) parameter);
			} else if (type.equals(String.class)) {
				stmt.bindString(no, (String) parameter);
			} else {
				stmt.bindBlob(no, (byte[]) parameter);
			}
			no++;
		}

		return stmt.executeInsert();

	}

	protected static <RESULT> String getSqlForInsert(Class<RESULT> resultClass, RESULT entity) {
		String tableName = getTableName(resultClass);
		String sql = "INSERT INTO " + tableName + " (";
		String[] columns = convertColumns(resultClass);
		Field[] fields = resultClass.getDeclaredFields();
		int count = 0;
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Object object = getValue(field, entity);
			if (object == null) {
				continue;
			}

			String column = columns[i];
			sql += column + ", ";

			count++;
		}

		sql = sql.substring(0, sql.length() - 2);
		sql += ") VALUES (";
		for (int i = 0; i < count; i++) {
			sql += "?, ";
		}
		sql = sql.substring(0, sql.length() - 2);
		sql += ")";

		return sql;
	}

	public <RESULT> int update(Class<RESULT> resultClass, RESULT param) {
		ContentValues values = new ContentValues();
		Field[] fields = resultClass.getDeclaredFields();
		for (Field field : fields) {
			Object parameter = getValue(field, param);
			if (parameter == null) {
				continue;
			}

			Class<?> type = field.getType();
			String propertyName = camelToSnake(field.getName());
			if (type.equals(Integer.class)) {
				values.put(propertyName, (Integer) parameter);
			} else if (type.equals(Long.class)) {
				values.put(propertyName, (Long) parameter);
			} else if (type.equals(Short.class)) {
				values.put(propertyName, (Short) parameter);
			} else if (type.equals(Float.class)) {
				values.put(propertyName, (Float) parameter);
			} else if (type.equals(Double.class)) {
				values.put(propertyName, (Double) parameter);
			} else if (type.equals(String.class)) {
				values.put(propertyName, (String) parameter);
			} else {
				values.put(propertyName, (byte[]) parameter);
			}
		}

		String tableName = getTableName(resultClass);

		Field field = getDeclaredField(resultClass, "id");
		Object id = getValue(field, param);
		String[] whereArgs = { id.toString() };
		// TODO pkをどうするか？
		String where = "id=?";
		int count = db.update(tableName, values, where, whereArgs);
		return count;
	}

	public <RESULT> int delete(Class<RESULT> resultClass, RESULT param) {
		String where = "";
		ArrayList<String> argList = new ArrayList<String>();
		Field[] fields = resultClass.getDeclaredFields();
		for (Field field : fields) {
			Object parameter = getValue(field, param);
			if (parameter == null) {
				continue;
			}

			String fieldName = field.getName();
			fieldName = camelToSnake(fieldName);
			where += fieldName + " = ? AND";
			argList.add(parameter.toString());
		}

		if (where.length() != 0) {
			where = where.substring(0, where.length() - 4);
		}

		String tableName = getTableName(resultClass);
		String[] whereArgs = (String[]) argList.toArray(new String[0]);
		int count = db.delete(tableName, where, whereArgs);
		return count;
	}

	public <RESULT> RESULT findByPrimaryKey(Class<RESULT> resultClass, Object key) {
		// TODO Keyパラメータどうする？難しい。
		RESULT param = createInstance(resultClass);
		Field field = getDeclaredField(resultClass, "id");
		setValue(field, param, key);

		List<RESULT> entityList = findList(resultClass, param);

		if (entityList.size() == 0) {
			return null;
		}

		return entityList.get(0);
	}
}
