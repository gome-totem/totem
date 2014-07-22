package org.z.core.es;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.module.ModuleAppDict;
import org.z.global.connect.ZeroConnect;
import org.z.global.util.EmptyUtil;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class CategoryDict {

	protected static Logger logger = LoggerFactory.getLogger(CategoryDict.class);
	protected static ConcurrentHashMap<String, BasicDBObject> categories = new ConcurrentHashMap<String, BasicDBObject>();
	protected static ConcurrentHashMap<String, BasicDBList> facetCaches = new ConcurrentHashMap<String, BasicDBList>();
	protected static ConcurrentHashMap<String, BasicDBList> categoryCaches = new ConcurrentHashMap<String, BasicDBList>();
	protected static ConcurrentHashMap<String, Integer> catIds = new ConcurrentHashMap<String, Integer>();
	protected static ConcurrentHashMap<String, BasicDBList> recommendMap = new ConcurrentHashMap<String, BasicDBList>();

	public static void addClearCache(String catId) {
		catIds.put(catId, 0);
	}

	public static void clearCache() {
		for (String catId : catIds.keySet()) {
			clear(catId);
		}
		logger.info("CategoryDict clearCache");
		catIds.clear();
	}

	public static void clear(String catId) {
		if (EmptyUtil.notEmpty(catId)) {
			categories.remove(catId);
			facetCaches.remove(catId);
			categoryCaches.remove(catId);
			removeRecommend(catId);
		} else {
			categories.clear();
			facetCaches.clear();
			categoryCaches.clear();
			recommendMap.clear();
			logger.info("CategoryDict,remove all cache");
		}
	}

	public static void removeRecommend(String catId) {
		recommendMap.remove(catId);
	}

	public static void setRecommend(String catId, BasicDBList recommend) {
		recommendMap.put(catId, recommend);
	}

	public static BasicDBList getRecommend(String catId) {
		return recommendMap.get(catId);
	}

	public static boolean containRecommend(String catId) {
		return recommendMap.containsKey(catId);
	}

	public static void setFacetCache(String catId, BasicDBList facets) {
		facetCaches.put(catId, facets);
	}

	public static void setCategoryCache(String catId, BasicDBList facets) {
		categoryCaches.put(catId, facets);
	}

	public static BasicDBList getFacetCache(String catId) {
		if (catId == null) {
			return null;
		}
		return facetCaches.get(catId);
	}

	public static BasicDBList getCategoryCache(String catId) {
		if (catId == null) {
			return null;
		}
		return categoryCaches.get(catId);
	}

	public static boolean containFacetCache(String catId) {
		return facetCaches.containsKey(catId);
	}

	public static boolean containCategoryCache(String catId) {
		return categoryCaches.containsKey(catId);
	}

	public static int getTotalCount() {
		return categories.size();
	}

	public static BasicDBObject getCategory(String catId) {
		if (catId == null) {
			return null;
		}
		BasicDBObject dbObject = categories.get(catId);
		if (EmptyUtil.isEmpty(dbObject)) {
			dbObject = ModuleAppDict.self.readDBObject("category", catId);
			if (EmptyUtil.notEmpty(dbObject)) {
				categories.put(catId, dbObject);
			} else {
				categories.put(catId, new BasicDBObject().append("1", 0));
			}
		}
		if (dbObject != null && dbObject.getInt("1", -1) == 0) {
			return null;
		}
		return dbObject;
	}

	public static synchronized void update(BasicDBList categories) {
		for (int i = 0; i < categories.size(); i++) {
			updateCategory((BasicDBObject) categories.get(i));
		}
	}

	protected static void updateCategory(BasicDBObject newCategory) {
		String newCatId = newCategory.getString("catId");
		BasicDBList newParentCategories = ((BasicDBList) newCategory.remove("parentCategories"));
		BasicDBObject oldCategory = getCategory(newCatId);
		if (oldCategory == null) {
			oldCategory = new BasicDBObject();
		}
		oldCategory.putAll((Map<String, Object>) newCategory);
		if (EmptyUtil.notEmpty(newParentCategories)) {
			BasicDBObject newParentCategory = (BasicDBObject) newParentCategories.get(0);
			updateCategory(newParentCategory);
			String oldParentId = oldCategory.getString("parentId");
			String newParentId = newParentCategory.getString("catId");
			if (!newParentId.equals(oldParentId)) {
				oldCategory.append("parentId", newParentId);
				int newLevel = newCategory.getInt("level");
				int oldLevel = oldCategory.getInt("level");
				if (newLevel == oldLevel) {
					BasicDBObject oldParentCategory = getCategory(oldParentId);
					BasicDBList childs = (BasicDBList) oldParentCategory.remove("childs");
					for (Object object : childs) {
						if (((BasicDBObject) object).getString("catId", "").equals(newCatId)) {
							childs.remove(object);
							break;
						}
					}
					oldParentCategory.put("childs", childs);
					write(oldParentId, oldParentCategory.toString(), true);
					BasicDBObject newRedisParentCategory = getCategory(newParentId);
					childs = (BasicDBList) newRedisParentCategory.remove("childs");
					childs.add(getChild(newCategory));
					newRedisParentCategory.append("childs", childs);
					write(newParentId, newRedisParentCategory.toString(), true);
				} else {
					// TODO
				}
			}
		}
		write(newCatId, oldCategory.toString(), true);
	}

	protected static BasicDBObject getChild(BasicDBObject src) {
		BasicDBObject dest = new BasicDBObject();
		dest.append("catId", src.getString("catId"));
		dest.append("catName", src.getString("catName"));
		if (src.getInt("level", -1) == 1) {
			dest.append("url", src.getString("url", ""));
			dest.append("icon", src.getString("icon", ""));
		}
		return dest;
	}

	protected static String getCatalog(String catId) {
		String catalog = null;
		if (catId.equalsIgnoreCase("coo8StoreShopRootCategory")) {
			catalog = "shop";
		} else if (catId.equalsIgnoreCase("coo8StoreRootCategory")) {
			catalog = "coo8";
		} else if (catId.equalsIgnoreCase("homeStoreRootCategory")) {
			catalog = "gome";
		} else if (catId.equalsIgnoreCase("rootGomeartCategory")) {
			catalog = "gomeart";
		} else if (catId.equalsIgnoreCase("defectiveStoreRootCategory")) {
			catalog = "defective";
		}
		return catalog;
	}

	public static synchronized void sync(BasicDBList items) {
		logger.info("start sync category");
		for (Object object : items) {
			BasicDBList roots = (BasicDBList) ((BasicDBObject) object).get("rootCategories");
			for (int i = 0; roots != null && i < roots.size(); i++) {
				BasicDBObject oRoot = (BasicDBObject) roots.get(i);
				String catId = oRoot.getString("catId");
				String catalog = getCatalog(catId);
				if (catalog == null) {
					logger.info("catalog is null cateId is [{}]", new Object[] { catId });
					continue;
				}
				writeChildCategory(catalog, writeCategory(catalog, oRoot, null, 0), oRoot.getString("catId"), 1);
			}
		}
		logger.info("writed [{}] Category To Dict ", new Object[] { getTotalCount() });
	}

	protected static BasicDBList writeCategory(String catalog, BasicDBObject one, String parentId, int level) {
		String categoryId = one.getString("catId");
		one.append("level", level).append("catalog", catalog);
		BasicDBList childs = (BasicDBList) one.remove("childCategories");
		boolean isLeaf = childs == null || childs.size() == 0;
		processFacets(one, catalog, categoryId, isLeaf);
		one.append("isLeaf", isLeaf);
		if (EmptyUtil.notEmpty(parentId)) {
			one.append("parentId", parentId);
		}
		BasicDBList items = getChildCategories(childs, level);
		one.append("childs", items);
		categories.put(categoryId, one);
		write(categoryId, one.toString(), false);
		return childs;
	}

	public static void write(String catId, String category, boolean update) {
		ModuleAppDict.self.write("category", catId, category);
		if (update) {
			ModuleAppDict.self.dispatchClearCache("facetCache", new BasicDBObject().append("id", catId));
		}
	}

	private static void processFacets(BasicDBObject one, String catalog, String catId, boolean isLeaf) {
		if (("gome".equals(catalog) || "defective".equals(catalog)) && isLeaf) {
			one.remove("facets");
			DBObject remoteCall = ZeroConnect.remoteCall("facet", "getCategoryFacetByCategoryId", new Object[] { catId });
			BasicDBObject response = (BasicDBObject) remoteCall.get("response");
			if (!response.getBoolean("success", false)) {
				logger.error("remote call moduleName [{}] & methodName [{}] & args [{}] not successful,error is [{}]",
						new Object[] { "facet", "getCategoryFacetByCategoryId", catId, response.getString("msg") });
				return;
			}
			BasicDBObject categoryFacet = (BasicDBObject) response.get("returnValue");
			if (EmptyUtil.isEmpty(categoryFacet)) {
				logger.warn("remote call moduleName [{}] & methodName [{}] & args [{}],returnValue is null", new Object[] { "facet", "getCategoryFacetByCategoryId", catId });
				return;
			}
			one.append("facets", categoryFacet.get("facets"));
		}
	}

	protected static BasicDBList getChildCategories(BasicDBList childs, int level) {
		BasicDBList items = new BasicDBList();
		for (int i = 0; childs != null && i < childs.size(); i++) {
			BasicDBObject oChild = (BasicDBObject) childs.get(i);
			BasicDBObject oItem = new BasicDBObject();
			oItem.append("catId", oChild.getString("catId"));
			oItem.append("catName", oChild.getString("catName"));
			if (level == 1) {
				oItem.append("url", oChild.getString("url", ""));
				oItem.append("icon", oChild.getString("icon", ""));
			}
			items.add(oItem);
		}
		return items;
	}

	protected static void writeChildCategory(String catalog, BasicDBList childs, String parentId, int level) {
		if (childs == null || childs.size() == 0) {
			return;
		}
		for (int i = 0; i < childs.size(); i++) {
			BasicDBObject child = (BasicDBObject) childs.get(i);
			writeChildCategory(catalog, writeCategory(catalog, child, parentId, level), child.getString("catId"), level + 1);
		}
	}

	public static void main(String[] args) throws IOException {

	}

}
