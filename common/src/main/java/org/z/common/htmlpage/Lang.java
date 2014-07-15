package org.z.common.htmlpage;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.environment.Const;
import org.z.store.mongdb.DataSet;

public class Lang {
	protected static final Logger logger = LoggerFactory.getLogger(Lang.class);
	public static HashSet<String> Codes = new HashSet<String>();
	public static ConcurrentHashMap<String, String> langCaches = new ConcurrentHashMap<String, String>();
	public String langName = null;
	public String pageName = null;
	public int mode = 0;

	static {
		Codes.add("en");
		Codes.add("nl");
		Codes.add("fr");
		Codes.add("de");
		Codes.add("ja");
		Codes.add("it");
		Codes.add("pt");
		Codes.add("es");
		Codes.add("sv");
		Codes.add("zh");
	}

	public Lang(String pageName, String langName) {
		this.pageName = pageName;
		this.langName = langName;
	}

	public String get(String scopeName, int termId, String defaultValue) {
		return get(scopeName, String.valueOf(termId), defaultValue);
	}

	public String get(String scopeName, String termId, String defaultValue) {
		if (langName.equalsIgnoreCase("zh")) {
			return defaultValue;
		}
		String key = scopeName + "~" + langName + "~" + termId;
		String langValue = langCaches.get(key);
		if (langValue != null) {
			return langValue;
		}
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select " + langName
				+ " from lang_dict where scope_name=? and token_id=?", new String[] { scopeName, termId }, 0, 0);
		if (rows.length == 0) {
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "insert into lang_dict(scope_name,token_id,zh)values(?,?,?)", new String[] {
					scopeName, termId, defaultValue });
			langCaches.put(key, defaultValue);
			return defaultValue;
		} else {
			langValue = rows[0][0];
			if (StringUtils.isEmpty(langValue)) {
				langValue = defaultValue;
			}
			langCaches.put(key, langValue);
		}
		return langValue;
	}

}
