package org.z.core.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.dict.Global.BizType;
import org.z.global.dict.Global.CodeType;
import org.z.global.environment.Const;
import org.z.global.util.StringUtil;
import org.z.store.mongdb.DataSet;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class Facets {

	public ConcurrentHashMap<String, Facet> values = new ConcurrentHashMap<String, Facet>();
	private static Logger logger = LoggerFactory.getLogger(Facets.class);
	public static Facets self = null;

	public void init() {
		self = this;
		CodeType[] types = CodeType.values();
		int totalCount = 0;
		for (int i = 0; i < types.length; i++) {
			FacetInfo info = null;
			String typeName = types[i].name();
			switch (types[i]) {
			case contient:
				info = FacetInfo.register(i, "ct", typeName, "大洲");
				break;
			case country:
				info = FacetInfo.register(i, "c", typeName, "国家");
				break;
			case location:
				info = FacetInfo.register(i, "ln", typeName, "目的地");
				break;
			case category:
				info = FacetInfo.register(i, "cy", typeName, "服务类别");
				break;
			case serviceTag:
				info = FacetInfo.register(i, "st", typeName, "服务标签");
				break;
			case priceTag:
				info = FacetInfo.register(i, "pt", typeName, "价格标签");
				break;
			case priceRange:
				info = FacetInfo.register(i, "pr", typeName, "价格范围");
				break;
			case language:
				info = FacetInfo.register(i, "le", typeName, "会说的语言");
				break;
			case belongCity:
				info = FacetInfo.register(i, "bc", typeName, "居住城市");
				break;
			}
			if (info == null) {
				logger.error(typeName + " has not facetInfo");
				continue;
			}
			Facet facet = new Facet(info);
			facet.read();
			switch (types[i]) {
			case country:
				Facet f = this.facetBy(CodeType.contient);
				f.addChilds(facet.records);
				break;
			case location:
				f = this.facetBy(CodeType.country);
				f.addChilds(facet.records);
				break;
			}
			values.put(typeName, facet);
			totalCount += facet.size();
		}
		logger.info("Facet TotalCount=" + totalCount);
	}

	public String countryBy(String city) {
		Facet facet = this.facetBy(CodeType.location);
		BasicDBObject o = facet.valueBy(city);
		if (o == null)
			return "";
		return o.getString("p");
	}

	public BasicDBObject add(CodeType type, String value, int order) {
		Facet facet = values.get(type.name());
		if (facet == null) {
			facet = new Facet(FacetInfo.byName.get(type.name()));
			values.put(type.name(), facet);
		}
		BasicDBObject oRecord = facet.addValue(value, order);
		facet.write();
//		ZeroConnect.send(Device.SERVER, ServiceName.Dict, MessageScope.ALLAPP, MessageType.FACET, MessageVersion.MQ, type.name(), oRecord);
		return oRecord;
	}

	public Facet reload(String name) {
		Facet facet = new Facet(FacetInfo.byName.get(name));
		values.put(name, facet);
		facet.read();
		return facet;
	}

	public Facet facetBy(CodeType type) {
		Facet facet = values.get(type.name());
		if (facet == null) {
			facet = this.reload(type.name());
		}
		return facet;
	}

	public Facet facetBy(String name) {
		Facet facet = values.get(name);
		if (facet == null) {
			facet = this.reload(name);
		}
		return facet;
	}

	public BasicDBObject valueBy(CodeType type, String value) {
		Facet facet = values.get(type.name());
		if (facet == null) {
			facet = this.reload(type.name());
		}
		return facet.valueBy(value);
	}

	public BasicDBObject idBy(CodeType type, String id) {
		Facet facet = values.get(type.name());
		if (facet == null) {
			facet = this.reload(type.name());
		}
		return facet.idBy(id);
	}

	public BasicDBList parse(CodeType type, BasicDBList items) {
		BasicDBList results = new BasicDBList();
		Facet facet = this.facetBy(type);
		for (int i = 0; i < items.size(); i++) {
			String value = String.valueOf(items.get(i));
			if (facet.exists(value))
				continue;
			BasicDBObject oFacet = facet.addValue(value, Integer.MAX_VALUE);
			BasicDBObject oItem = new BasicDBObject();
			oItem.append("id", oFacet.getString("i")).append("value", value);
			results.add(oItem);
		}
		facet.write();
		return results;
	}

	public BasicDBList normalizeLocations(String shortId, BasicDBList locations) {
		Facet facet = this.facetBy(CodeType.location);
		BasicDBList results = new BasicDBList();
		for (int i = 0; i < locations.size(); i++) {
			Object loc = locations.get(i);
			if (loc instanceof String) {
				BasicDBObject oItem = facet.valueBy((String) loc);
				if (oItem == null) {
					logger.info("shortId=" + shortId + "&location=" + loc + " not match");
				} else {
					BasicDBObject oRecord = new BasicDBObject();
					oRecord.append("id", oItem.getString("i"));
					oRecord.append("value", loc);
					oRecord.append("country", oItem.getString("p"));
					results.add(oRecord);
				}
			} else if (loc instanceof BasicDBObject) {
				BasicDBObject oLoc = (BasicDBObject) loc;
				String value = oLoc.getString("name");
				if (StringUtil.isEmpty(value) == false) {
					oLoc.append("value", value);
					oLoc.removeField("name");
				} else {
					value = oLoc.getString("value");
				}
				BasicDBObject oItem = facet.valueBy(value);
				if (oItem == null) {
					logger.info("shortId=" + shortId + "&location=" + value + " not match");
					continue;
				}
				oLoc.append("id", oItem.getString("i"));
				oLoc.append("country", oItem.getString("p"));
				results.add(oLoc);
			}
		}
		return results;
	}

	public BasicDBList normalizeCountries(String country, BasicDBList locations) {
		Facet facet = this.facetBy(CodeType.country);
		BasicDBList results = new BasicDBList();
		String value = country;
		HashSet<String> values = new HashSet<String>();
		if (StringUtil.isEmpty(value) == false && facet.exists(value)) {
			BasicDBObject oItem = new BasicDBObject();
			BasicDBObject oFacet = facet.valueBy(value);
			oItem.append("value", value);
			oItem.append("id", oFacet.getString("i"));
			oItem.append("contient", oFacet.getString("p"));
			values.add(value);
			results.add(oItem);
		}
		for (int i = 0; i < locations.size(); i++) {
			BasicDBObject oLoc = (BasicDBObject) locations.get(i);
			value = oLoc.getString("country");
			if (values.contains(value))
				continue;
			BasicDBObject oFacet = facet.valueBy(value);
			if (oFacet == null)
				continue;
			values.add(value);
			BasicDBObject oItem = new BasicDBObject();
			oItem.append("value", value);
			oItem.append("id", oFacet.getString("i"));
			oItem.append("contient", oFacet.getString("p"));
			results.add(oItem);
		}
		return results;
	}

	public BasicDBList normalizeContients(BasicDBList countries) {
		Facet facet = this.facetBy(CodeType.contient);
		BasicDBList results = new BasicDBList();
		HashSet<String> values = new HashSet<String>();
		for (int i = 0; i < countries.size(); i++) {
			BasicDBObject oCountry = (BasicDBObject) countries.get(i);
			String value = oCountry.getString("contient");
			if (values.contains(value))
				continue;
			BasicDBObject oFacet = facet.valueBy(value);
			if (oFacet == null)
				continue;
			values.add(value);
			BasicDBObject oItem = new BasicDBObject();
			oItem.append("value", value);
			oItem.append("id", oFacet.getString("i"));
			results.add(oItem);
		}
		return results;
	}

	public BasicDBList[] normalize(CodeType[] types, BasicDBList items) {
		BasicDBList[] results = new BasicDBList[types.length];
		for (int i = 0; i < types.length; i++) {
			results[i] = new BasicDBList();
		}
		int index = 0;
		while (index < items.size()) {
			String value = String.valueOf(items.get(index));
			boolean done = false;
			BasicDBObject oItem = new BasicDBObject();
			for (int i = 0; i < types.length; i++) {
				Facet facet = this.facetBy(types[i]);
				if (facet.exists(value) == false) {
					continue;
				}
				BasicDBObject oFacet = facet.valueBy(value);
				oItem.append("id", oFacet.getString("i")).append("value", value);
				results[i].add(oItem);
				done = true;
				break;
			}
			if (done) {
				items.remove(index);
				continue;
			}
			index++;
		}
		return results;
	}

	public String categoryBy(BizType type, int index) {
		switch (type) {
		case trip:
			switch (index) {
			case 0:
				return "导游服务";
			case 1:
				return "个性化线路";
			}
			break;
		case car:
			return "租车服务";
		}
		return "其他服务";
	}

	public String correctValue(CodeType type, String value) {
		Facet facet = null;
		switch (type) {
		case serviceTag:
			if (value.equals("接机")) {
				return "能接机";
			} else if (value.equals("观光")) {
				return "熟悉当地景点";
			} else if (value.equals("美食")) {
				return "熟悉当地美食";
			} else if (value.equals("代购")) {
				return "能代购打折商品";
			} else if (value.equals("客栈")) {
				return "有私家旅馆";
			} else if (value.equals("有车")) {
				return "有私家车";
			} else if (value.equals("翻译")) {
				return "能胜任商务翻译";
			} else if (value.equals("夜总会")) {
				return "熟悉夜总会";
			} else if (value.equals("驾照")) {
				return "有驾照";
			} else if (value.equals("红灯区")) {
				return "熟悉红灯区";
			} else if (value.equals("住家")) {
				return "有私家旅馆";
			} else if (value.equals("美容")) {
				return "熟悉美容医院";
			} else if (value.equals("导游证")) {
				return "有导游证";
			} else if (value.equals("商务")) {
				return "能胜任商务翻译";
			} else if (value.equals("家庭")) {
				return "有私家旅馆";
			} else if (value.equals("商务翻译")) {
				return "能胜任商务翻译";
			} else if (value.equals("提供住宿")) {
				return "有私家旅馆";
			} else if (value.equals("提供接机")) {
				return "能接机";
			} else if (value.equals("兼职导游")) {
				return "我是兼职导游";
			} else if (value.equals("熟悉文化历史")) {
				return "熟悉人文历史";
			} else if (value.equals("熟悉美食")) {
				return "熟悉当地美食";
			} else if (value.equals("专业导游")) {
				return "我是职业导游";
			} else if (value.equals("代订酒店")) {
				return "能代订当地酒店";
			} else if (value.equals("伴游")) {
				return "我是兼职导游";
			} else if (value.equals("导游")) {
				return "我是职业导游";
			} else if (value.equals("景点")) {
				return "熟悉当地景点";
			} else if (value.equals("导游")) {
				return "我是职业导游";
			} else if (value.equals("导游")) {
				return "我是职业导游";
			} else if (value.equals("广东话")) {
				return "粤语";
			}
			facet = this.facetBy(CodeType.language);
			BasicDBObject oItem = facet.valueBy(value);
			if (oItem != null) {
				return oItem.getString("v");
			}
			break;
		case priceTag:
			if (value.equals("餐补")) {
				return "包含餐补";
			} else if (value.equals("住宿")) {
				return "包含住宿费";
			} else if (value.equals("汽油")) {
				return "包含车的油费";
			} else if (value.equals("过路")) {
				return "包含车的过路费";
			} else if (value.equals("停车")) {
				return "包含车的停车费";
			} else if (value.equals("交通")) {
				return "包含公共交通费";
			}
			break;
		}
		facet = this.facetBy(type);
		BasicDBObject oItem = facet.valueBy(value);
		if (oItem == null)
			return null;
		return oItem.getString("v");
	}

	@SuppressWarnings("unused")
	private void registerPriceRange() {
		Facet facet = new Facet(FacetInfo.byName.get(CodeType.priceRange.name()));
		int index = 0;
		facet.addValue("0-500", index++);
		facet.addValue("501-1000", index++);
		facet.addValue("1001-1500", index++);
		facet.addValue("1501-2000", index++);
		facet.write();
		logger.info("Register PriceRange done.");
	}

	@SuppressWarnings("unused")
	private void registerPriceTag() {
		Facet facet = new Facet(FacetInfo.byName.get(CodeType.priceTag.name()));
		int index = 0;
		facet.addValue("包含每天小费", index++);
		facet.addValue("包含餐补", index++);
		facet.addValue("包含住宿费", index++);
		facet.addValue("包含车的油费", index++);
		facet.addValue("包含车的过路费", index++);
		facet.addValue("包含车的停车费", index++);
		facet.addValue("包含公共交通费", index++);
		facet.write();
		logger.info("Register PriceTag done.");
	}

	@SuppressWarnings("unused")
	private void registerLanguage() {
		Facet facet = new Facet(FacetInfo.byName.get(CodeType.language.name()));
		int index = 0;
		facet.addValue("英语", index++);
		facet.addValue("法语", index++);
		facet.addValue("西班牙语", index++);
		facet.addValue("意大利语", index++);
		facet.addValue("德语", index++);
		facet.addValue("韩语", index++);
		facet.addValue("日语", index++);
		facet.addValue("普通话", index++);
		facet.addValue("上海话", index++);
		facet.addValue("粤语", index++);
		facet.addValue("闽南话", index++);
		facet.addValue("台语", index++);
		facet.addValue("四川话", index++);
		facet.write();
		logger.info("Register Language done.");
	}

	@SuppressWarnings("unused")
	private void registerServiceTag() {
		Facet facet = new Facet(FacetInfo.byName.get(CodeType.serviceTag.name()));
		int index = 0;
		facet.addValue("有私家车", index++);
		facet.addValue("有私家旅馆", index++);
		facet.addValue("有驾照", index++);
		facet.addValue("有导游证", index++);
		facet.addValue("能接机", index++);
		facet.addValue("能胜任商务翻译", index++);
		facet.addValue("能代购打折商品", index++);
		facet.addValue("能代订打折机票", index++);
		facet.addValue("能代订打折酒店", index++);
		facet.addValue("熟悉当地美食", index++);
		facet.addValue("熟悉当地景点", index++);
		facet.addValue("熟悉摄影", index++);
		facet.addValue("熟悉人文历史", index++);
		facet.addValue("熟悉美容医院", index++);
		facet.addValue("熟悉夜总会", index++);
		facet.addValue("熟悉红灯区", index++);
		facet.addValue("我是职业导游", index++);
		facet.addValue("我是兼职导游", index++);
		facet.addValue("我是留学生", index++);
		facet.addValue("我是当地旅行社", index++);
		facet.write();
		logger.info("Register ServiceTag done.");
	}

	@SuppressWarnings("unused")
	private void registerCategory() {
		Facet facet = new Facet(FacetInfo.byName.get(CodeType.category.name()));
		int index = 0;
		facet.addValue("其他服务", index++);
		facet.addValue("导游服务", index++);
		facet.addValue("租车服务", index++);
		facet.addValue("个性化线路", index++);
		facet.write();
		logger.info("Register Category done.");
	}

	@SuppressWarnings("unused")
	private void registerCountry() {
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select continent,country,weight from country_dict", new String[] {},
				0, 0);
		if (rows.length == 0)
			return;
		Facet country = facetBy(CodeType.country);
		HashMap<String, Long> contients = new HashMap<String, Long>();
		for (int i = 0; i < rows.length; i++) {
			String[] row = rows[i];
			int weight = Integer.parseInt(row[2]);
			Long c = contients.get(row[0]);
			if (c == null) {
				c = (long) 0;
			}
			c += weight;
			contients.put(row[0], c);
			BasicDBObject oCountry = country.addValue(row[1], weight);
			oCountry.append("p", row[0]);
			System.out.println("register country=" + row[1] + "&weight=" + weight);
		}
		country.write();
		Facet contient = facetBy(CodeType.contient);
		for (Iterator<Entry<String, Long>> i = contients.entrySet().iterator(); i.hasNext();) {
			Entry<String, Long> entry = i.next();
			String value = entry.getKey();
			long weight = entry.getValue();
			contient.addValue(value, (int) weight);
			System.out.println("register contient=" + value + "&weight=" + weight);
		}
		contient.write();
		logger.info("Register Country done.");
	}

	@SuppressWarnings("unused")
	private void registerLocation() {
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select country,city,weight from city_dict", new String[] {}, 0, 0);
		if (rows.length == 0)
			return;
		Facet country = facetBy(CodeType.country);
		Facet location = facetBy(CodeType.location);
		for (int i = 0; i < rows.length; i++) {
			String[] row = rows[i];
			if (country.exists(row[0]) == false) {
				System.out.println("**** register location fail,country=" + row[0]);
				continue;
			}
			int weight = Integer.parseInt(row[2]);
			BasicDBObject oItem = location.addValue(row[1], weight);
			oItem.append("p", row[0]);
			System.out.println("register location=" + row[1] + "&weight=" + weight);
		}
		location.write();
		logger.info("Register Location done.");
	}

}
