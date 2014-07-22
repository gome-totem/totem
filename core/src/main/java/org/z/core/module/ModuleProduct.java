package org.z.core.module;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.sort.SortOrder;
import org.z.core.app.ModuleZeromq;
import org.z.core.es.CategoryDict;
import org.z.core.es.ESDict;
import org.z.core.es.ESIndex;
import org.z.core.es.QueryBuilder;
import org.z.data.analysis.SmartTokenizerFactory.TokenMode;
import org.z.global.dict.Global;
import org.z.global.interfaces.IndexServiceIntf;
import org.z.global.util.DBObjectUtil;
import org.z.global.util.EmptyUtil;
import org.z.global.util.PinyinUtil;
import org.z.global.util.StringUtil;

import redis.clients.jedis.ShardedJedis;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class ModuleProduct extends ESIndex implements IndexServiceIntf {

	protected ModuleZeromq mq = null;
	private static final String FIELD_PRODID = "productId";
	private static final String FIELD_PRODSTATE = "productState";
	private static final String FIELD_PRODTAG = "productTag";

	private static final String FIELD_SKUID = "skuId";
	private static final String FIELD_SKUNO = "skuNo";
	private static final String FIELD_SKUSTATE = "skuState";

	private static final String FIELD_SORTNO = "sortNo";

	private static final String FIELD_SHOPID = "shopId";
	private static final String FIELD_SHOPNAM = "shopName";
	private static final String FIELD_SHOPTYPE = "shopType";

	private static final String FIELD_PRICE = "price";
	private static final String FIELD_SALESV = "salesVolume";
	private static final String FIELD_STARTDATE = "startDate";
	private static final String FIELD_EVALUATECOUNT = "evaluateCount";
	private static final String FIELD_WEIGHT = "weight";

	private static final String FIELD_CATBRAND = "categoryBrand";
	private static final String FIELD_TITLE = "title";
	private static final String FIELD_NGRAM = "n-gram";
	private static final String FIELD_FACET = "facet";
	private static final String FIELD_PROMOSCORE = "promoScore";
	private static final String FIELD_PROMOTAG = "promoTag";

	private static final String FIELD_CATS = "categories";

	private static final String FIELD_JD_PRICE = "jdPrice";
	private static final String FIELD_JD_TIME = "jdTime";

	private static final String FIELD_DY_PREFIX_F = "f";
	private static final String FIELD_PREFIX_F = "f.";
	private static final String FIELD_DY_PREFIX_FV = "fv";
	private static final String FIELD_DY_PREFIX_FP = "fp";

	private static final String FIELD_PREFIX_S = "s.";

	private static final String FIELD_DRAGON_A = "a";
	private static final String FIELD_DRAGON_C = "c";

	public static final String PARAM_REGION = "regionId";
	public static final String PARAM_FAKE = "fake";
	public static final String PARAM_COMMODE = "comMode";
	public static final String DYNAMIC_PREFIX = "d";
	public static final String SQL_ALL = "*:*";

	public boolean tempDebug = false;
	SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	DB db = null;

	@Override
	public boolean init(boolean isReload) {
		if (super.init(isReload) == false) {
			return false;
		}
		return true;
	}

	@Override
	protected BasicDBObject getDocData(BasicDBObject product, ShardedJedis jedis) {

		BasicDBObject result = new BasicDBObject();
		List<BasicDBObject> resultDocs = Lists.newArrayList();
		result.put("docs", resultDocs);
		List<String> clearData = Lists.newArrayList();
		result.put("clearData", clearData);
		String productId = product.getString("id");

		long startDate = product.getLong("startDate", 0);
		long endDate = product.getLong("endDate", 0);
		int oProductState = product.getInt("state", -1);

		// product定时上下架
		boolean intoIndex = updateProductState(oProductState, startDate, endDate, null, productId, "productState");// ?
		if (oProductState != 4 && !intoIndex) {
			logger.warn("skip productId={}&productState={}", new Object[] { productId, oProductState });
			deleteDocByProductId(productId, jedis);
			return null;
		}
		// skus 如果为空 删除文档id，某个skus状态！=4 删除这个sku
		BasicDBList skus = (BasicDBList) product.get("skus");
		if (skus == null || skus.size() == 0) {
			logger.warn("skip productId={}&skus not exist.", new Object[] { productId, oProductState });
			deleteDocByProductId(productId, jedis);
			return null;
		} else {
			this.deleteDocByProductId(productId, skus, jedis);
		}

		// 没有categories删除文档
		BasicDBList categories = (BasicDBList) product.get("categories");
		if (EmptyUtil.isEmpty(categories)) {
			logger.warn("productId [{}],categories is null", new Object[] { productId });
			deleteDocByProductId(productId, jedis);
			return null;
		}

		// categories 中catalog是否含有 gome coo8 shop gomeart ，都不包含，删除文档
		boolean flag = false;
		String catalog = null;
		for (int i = 0; i < categories.size(); i++) {
			BasicDBObject oItem = (BasicDBObject) categories.get(i);
			String catId = oItem.getString("catId");
			if (EmptyUtil.isEmpty(catId)) {
				logger.warn("productId [{}],category [{}] has not field 'catId'.", new Object[] { productId, oItem.toString() });
				continue;
			}
		}
		if (!flag) {
			logger.warn("skip productId={}&catalog={},skus not exist.", new Object[] { product.getString("id"), catalog });
			deleteDocByProductId(productId, jedis);
			return null;
		}

		// siteIds 是什么 ？如果为空或者 gomeArtSite 删除文档
		BasicDBList siteIds = (BasicDBList) product.get("siteIds");
		if (siteIds == null || siteIds.contains("gomeArtSite")) {
			deleteDocByProductId(productId, jedis);
			return null;
		}
		// 合并SKU
		BasicDBObject multiSku = parseMultiSku(skus, categories, productId);
		if (multiSku == null) {
			logger.warn("skip productId={},multiSku not exist.", new Object[] { product.getString("id") });
			deleteDocByProductId(productId, jedis);
			return null;
		}

		BasicDBList oSkus = new BasicDBList();// 保存每个颜色
		StringBuilder indexSkus = new StringBuilder();

		// 是否多图展示
		boolean isMulti = multiSku.getBoolean("isMulti");
		// 如果不是多图展示，在索引库中存每个颜色中，价格最低的SKU
		BasicDBList multiSkus = (BasicDBList) multiSku.get("skus");

		BasicDBList productFacets = (BasicDBList) product.get("facets");
		for (int i = 0; multiSkus != null && i < multiSkus.size(); i++) {
			BasicDBObject skuBy = ((BasicDBObject) multiSkus.get(i));
			skus = (BasicDBList) skuBy.get("skus");
			BasicDBObject oSku = (BasicDBObject) skus.get(skuBy.getInt("index", 0));// 价格最低sku
			oSkus.add(oSku);
			if (isMulti == false) {
				BasicDBObject sku = addSku(resultDocs, clearData, oSku, product, productFacets, jedis);
				if (indexSkus.length() > 0) {
					indexSkus.append(",");
				}
				indexSkus.append(sku.getString("id"));
			}
		}

		// 如果是多图展示，只存一个价格最低的SKU
		if (isMulti == true && multiSku.get("sku") != null) {
			BasicDBObject sku = addSku(resultDocs, clearData, (BasicDBObject) multiSku.get("sku"), product, productFacets, jedis);
			if (indexSkus.length() > 0) {
				indexSkus.append(",");
			}
			indexSkus.append(sku.getString("id"));
		}
		product.append("colors", (BasicDBList) multiSku.get("colorNameList"));
		product.append("isMulti", isMulti);
		product.append("isBigImg", multiSku.getBoolean("isBigImg", false));
		product.append("indexSkus", indexSkus.toString());
		product.append("multiSkus", oSkus);

		ESDict.put(DYNAMIC_PREFIX, productId, product.toString(), false, true, jedis);
		return result;
	}

	@SuppressWarnings("unused")
	public BasicDBObject addSku(List<BasicDBObject> indexDocs, List<String> clearData, BasicDBObject oSku, BasicDBObject oProduct, BasicDBList productFacets, ShardedJedis jedis) {
		BasicDBObject indexDoc = new BasicDBObject();
		String productId = oProduct.getString("id");
		String skuId = oSku.getString("id");
		String skuName = oSku.getString("name");
		indexDoc.put(FIELD_SKUID, skuId);

		StringBuilder titleBuilder = new StringBuilder();
		StringBuilder categoryBrandBuilder = new StringBuilder();
		StringBuilder facetBuilder = new StringBuilder();

		titleBuilder.append(skuName);
		titleBuilder.append(" ");

		// brand 及其拼音加入 title catagorybrand
		String brand = oProduct.getString("brand");
		if (EmptyUtil.notEmpty(brand)) {
			categoryBrandBuilder.append(brand);
			categoryBrandBuilder.append(" ");
			brand = brand.replace("(", " ").replace(")", " ").replace("（", " ").replace("）", " ");
			titleBuilder.append(brand);
			titleBuilder.append(" ");
			String[] spells = getPinyins(brand);
			if (EmptyUtil.notEmpty(spells)) {
				for (String spell : spells) {
					titleBuilder.append(spell);
					titleBuilder.append(" ");
				}
			}
		}

		// // 店铺添加sortNo
		// String sortNo=oSku.getString("sortNo");
		// if(sortNo!=null){
		// indexDoc.put(FIELD_SORTNO, Integer.parseInt( sortNo));
		// logger.info("skuId={}&&sortNo ={}", new Object[] { skuId, sortNo});
		// }

		// 添加店铺子段 店铺名称加入title
		BasicDBObject oShop = (BasicDBObject) oProduct.get("shop");
		if (oShop != null) {
			indexDoc.put(FIELD_SHOPID, oShop.getString("id"));
			indexDoc.put(FIELD_SHOPNAM, oShop.getString("name"));
			indexDoc.put(FIELD_SHOPTYPE, Integer.parseInt(oShop.getString("type")));
			titleBuilder.append(oShop.getString("name"));
			titleBuilder.append(" ");
		}
		// 1,gome 2,coo8 3,gomeart
		int productTag = oProduct.getInt("productTag");
		// 如果 是国美商品 keyword 加入 title
		if (productTag == 1) {
			BasicDBList keywords = (BasicDBList) oProduct.get("keywords");
			for (int t = 0; keywords != null && t < keywords.size(); t++) {
				titleBuilder.append(keywords.get(t).toString());
				titleBuilder.append(" ");
			}
		}

		String skuNo = oSku.getString("skuNo");
		indexDoc.put(FIELD_SKUNO, skuNo);
		indexDoc.put(FIELD_SKUSTATE, oSku.getInt("state", 0));
		BasicDBObject oPrice = (BasicDBObject) oSku.get("price");
		double price = calcPrice(oPrice);

		indexDoc.put(FIELD_PRICE, price);
		indexDoc.put(FIELD_PRODID, productId);
		indexDoc.put(FIELD_PRODSTATE, oProduct.getInt("state", 0));
		indexDoc.put(FIELD_SALESV, oProduct.getInt("salesVolume", 0));

		BasicDBObject salesAreaVolume = (BasicDBObject) oSku.get("salesAreaVolume");
		if (EmptyUtil.notEmpty(salesAreaVolume)) {
			Set<Entry<String, Object>> entrySet = salesAreaVolume.entrySet();
			for (Entry<String, Object> entry : entrySet) {
				indexDoc.put(StringUtil.append(FIELD_PREFIX_S, entry.getKey()), Integer.parseInt(entry.getValue().toString()));
			}
		}
		indexDoc.put(FIELD_STARTDATE, oSku.getLong("startDate", 0));
		indexDoc.put(FIELD_PRODTAG, productTag);

		int s = 0;
		try {
			String pls = ESDict.runtimeProdDB.get("pls" + productId);
			s = (StringUtil.isEmpty(pls) ? oProduct.getInt("evaluateCount", 0) : Integer.valueOf(pls));
		} catch (Exception e) {
			s = 0;
		}
		indexDoc.put(FIELD_EVALUATECOUNT, s);
		float score = 0;
		indexDoc.put(FIELD_WEIGHT, score);

		// facet 创建
		BasicDBList skuFacets = (BasicDBList) oSku.get("facets");
		BasicDBObject facets = new BasicDBObject();
		mergeFacets(productFacets, facets);
		mergeFacets(skuFacets, facets);
		List<String> catIds = addFacetField(indexDoc, clearData, productId, skuId, productTag, (BasicDBList) oProduct.get("categories"), facets, titleBuilder, facetBuilder, price, jedis);
		int productType = 0;// 1 图书
		oProduct.append("productType", productType);
		ArrayList<String> tokens = parseTokens(categoryBrandBuilder.toString(), true);
		indexDoc.put(FIELD_CATBRAND, tokens.get(0));
		tokens = parseTokens(titleBuilder.toString(), true);
		indexDoc.put(FIELD_TITLE, tokens.get(0));
		indexDoc.put(FIELD_NGRAM, tokens.size() == 2 ? tokens.get(1) : "");
		tokens = parseTokens(facetBuilder.toString(), false);
		indexDoc.put(FIELD_FACET, tokens.get(0));

		addScheduleOnSale(productId, skuId, oPrice);

		// 促销得分（按促销排序）
		indexDoc.put(FIELD_PROMOSCORE, calcPromoScore(skuId));
		indexDocs.add(indexDoc);
		ESDict.put(DYNAMIC_PREFIX, skuNo, skuId, false, false, jedis);

		return oSku;
	}

	protected void deleteDocByProductId(String productId, BasicDBList skus, ShardedJedis jedis) {
		boolean f = true;
		if (EmptyUtil.notEmpty(skus)) {
			String json = dynamicDB.get(productId);
			if (EmptyUtil.isEmpty(json)) {
				return;
			}
			BasicDBObject product = (BasicDBObject) JSON.parse(json);
			BasicDBList oldSkus = (BasicDBList) product.get("skus");
			if (skus.size() == oldSkus.size()) {
				for (Object oldObj : oldSkus) {
					BasicDBObject oldSku = (BasicDBObject) oldObj;
					f = true;
					for (Object obj : skus) {
						BasicDBObject sku = (BasicDBObject) obj;
						if (oldSku.getString("id").equals(sku.getString("id")) && sku.getInt("state", 0) == 4) {
							f = false;
						}
					}
					if (f) {
						break;
					}
				}
			}
		}
		if (f) {
			this.deleteDocByProductId(productId, jedis);
		}
	}


	private float calcPromoScore(String skuId) {
		Float promoScore = ModuleAppDict.self.getPromoScore(skuId);
		if (promoScore == null) {
			return 0.0f;
		}
		logger.info("skuId={}&promoScore={}", new Object[] { skuId, promoScore });
		return promoScore;
	}

	public int calcPromoScore(String promoBusiType, long beginUsable, long endUsable, long current) {
		int promoScore = 0;
		if (beginUsable <= (current + 60000) && (current + 60000) < endUsable) {
			if ("sendRedCoupon".equalsIgnoreCase(promoBusiType)) {
				promoScore += 3;
			} else if ("sendBlueCoupon".equalsIgnoreCase(promoBusiType)) {
				promoScore += 2;
			} else if ("sendFreeGift".equalsIgnoreCase(promoBusiType)) {
				promoScore += 1;
			}
		}
		return promoScore;
	}

	private void addScheduleOnSale(String productId, String skuId, BasicDBObject oPrice) {
		if (oPrice.containsField("onSaleEndDate") && oPrice.containsField("onSaleStartDate")) {
			long onSaleStartDate = oPrice.getLong("onSaleStartDate");
			long onSaleEndDate = oPrice.getLong("onSaleEndDate");
			maybeAdd(productId, skuId, onSaleStartDate, "skuPrice", "startSale");
			maybeAdd(productId, skuId, onSaleEndDate, "skuPrice", "endSale");
		}
	}

	@SuppressWarnings("unused")
	private void maybeAdd(String productId, String skuId, long time, String type, String innerType) {
		if (time == 0) {
			logger.info("updateSchedule date isEmpty");
			return;
		}
		long current = System.currentTimeMillis();
		long maxScheduleDate = getMaxScheduleDate(current);
		if ((time > current && time < maxScheduleDate)) {
			BasicDBObject one = new BasicDBObject();
			one.append("type", type);
			one.append("skuId", skuId);
			one.append("Date", time);
			one.append("id", productId);
			String name = "updatePrice-" + productId + "-" + skuId + "-" + StringUtil.createUniqueID();
//			schedulerClient.addJob(JobInfoFactory.builder().content(one.toString()).name(name).group("sku-change").startDate(time).jobClass(JobSkuChange.class).triggerType(TriggerType.START_AT)
//					.executeMode(ExecuteMode.DISTRIBUTED).build());
			logger.info("add onsale schedule productId[{}] skuId[{}] type[{}] innerypTe [{}] time [{}]", new Object[] { productId, skuId, type, innerType, dateFm.format(new Date(time)) });
		}
	}

	protected List<String> addFacetField(BasicDBObject indexDoc, List<String> clearData, String productId, String skuId, int productTag, BasicDBList categories, BasicDBObject facets,
			StringBuilder categoryBrandBuilder, StringBuilder facetBuilder, double price, ShardedJedis jedis) {
		List<String> catIds = new ArrayList<String>();
		for (int i = 0; EmptyUtil.notEmpty(categories) && i < categories.size(); i++) {
			BasicDBObject oItem = (BasicDBObject) categories.get(i);
			String catalog = oItem.getString("catalog");
			// 1,gome 2,coo8 3,gomeart// 验证catalog是否有效
			boolean isCoo8Catalog = "coo8".equalsIgnoreCase(catalog);
			boolean isShopCatalog = "shop".equalsIgnoreCase(catalog);
			if (productTag == 1 && (isCoo8Catalog || isShopCatalog)) {
				continue;
			}
			String catId = oItem.getString("catId");
			if (isCoo8Catalog) {
				catId = oItem.getString("mappingCatId");
				if (EmptyUtil.isEmpty(catId)) {
					continue;
				}
			}
			BasicDBObject category = CategoryDict.getCategory(catId);
			if (EmptyUtil.isEmpty(category)) {
				if (isShopCatalog) {
					this.addArrayValue(indexDoc, FIELD_CATS, catId);
				}
				continue;
			}

			// categories 索引 该产品 第三级或者店铺类目第二级 cataid （末级？？）

			try {
				int level = category.getInt("level", -1);
				if (level == -1) {
					logger.error("catId={}&category={}", new Object[] { catId, category });
					continue;
				}

				if (level == 3 || (isShopCatalog && level == 2)) {
					if (!isShopCatalog) {
						categoryBrandBuilder.append(category.getString("catName")).append(" ");
					}
					catIds.add(catId);
					CategoryDict.addClearCache(catId);
					this.addArrayValue(indexDoc, FIELD_CATS, catId);
				}

			} catch (Exception e) {
				logger.error("catId=[{}],category=[{}]", new Object[] { catId, category });
				// modify
				continue;
			}
			BasicDBList categoryFacets = (BasicDBList) category.get("facets");
			if (EmptyUtil.isEmpty(categoryFacets)) {
				continue;
			}
			BasicDBObject categoryFacet = null, facet = null;
			String facetInfoId = null;
			for (int j = 0; j < categoryFacets.size(); j++) {// 使用比较全的类目facet便利，查找product
																// facet是为了防止产品中出现
																// 不包含在类目中的facet吗
																// 改段功能是往索引中保存product
																// 的facet
																// 子段，子段格式
																// f.catId.facetInfoId
																// facetId

				// 同时在dynamicDB中保存 key=f@facetId,value facetInfoId, fv@facetId
				// value=facetValue
				// 如果是品牌facet， dynamicDB中也保存fp@facetId 和拼音首字母大写 为啥？
				categoryFacet = (BasicDBObject) categoryFacets.get(j);
				facetInfoId = categoryFacet.getString("id");
				int type = categoryFacet.getInt("type");// 1是品牌,2是价格
				if (type != 2) {
					facet = (BasicDBObject) facets.get(StringUtil.append(catId, ".", facetInfoId));
					if (facet == null) {
						continue;
					}
					BasicDBObject values = (BasicDBObject) facet.get("values");
					if (EmptyUtil.isEmpty(values)) {
						continue;
					}

					this.addArrayValue(indexDoc, StringUtil.append(FIELD_PREFIX_F, catId, ".", facetInfoId), values.keySet().toArray(new String[0]));
					Iterator<Entry<String, Object>> iterator = values.entrySet().iterator();
					Entry<String, Object> next;
					while (iterator.hasNext()) {
						next = iterator.next();
						String facetId = next.getKey();
						Object facetValue = next.getValue();
						facetBuilder.append(String.valueOf(facetValue)).append(" ");

						putFacets2Dynamic(FIELD_DY_PREFIX_F, facetId, facetInfoId, clearData, jedis);
						putFacets2Dynamic(FIELD_DY_PREFIX_FV, facetId, facetValue.toString(), clearData, jedis);
						if (type == 1) {
							String[] pinyins = getPinyins(String.valueOf(facetValue));
							putFacets2Dynamic(FIELD_DY_PREFIX_FP, facetId, pinyins[1].substring(0, 1).toUpperCase(), clearData, jedis);
						}
					}
				} else if (type == 2) {// 索引中价格子段 保存 价格区间 ，根据product
										// price，和facet value 比较 找出区间 facetid
					BasicDBList values = (BasicDBList) categoryFacet.get("values");
					BasicDBObject ranges = (BasicDBObject) categoryFacet.get("ranges");
					if (EmptyUtil.isEmpty(values) || EmptyUtil.isEmpty(ranges) || values.size() != ranges.size()) {
						continue;
					}
					Iterator<Entry<String, Object>> iterator = ranges.entrySet().iterator();
					Entry<String, Object> next;
					while (iterator.hasNext()) {
						next = iterator.next();
						String facetValue = next.getKey();
						String facetId = String.valueOf(next.getValue());
						putFacets2Dynamic(FIELD_DY_PREFIX_F, facetId, facetInfoId, clearData, jedis);
						putFacets2Dynamic(FIELD_DY_PREFIX_FV, facetId, facetValue.toString(), clearData, jedis);
					}
					int index = -1;
					for (int k = 0; k < values.size(); k++) {
						if (StringUtil.parserInt(values.get(k).toString(), 0) > price) {
							index = k;
							break;
						}
					}
					String rangeValue = null;
					if (index == -1) {
						rangeValue = values.get(ranges.size() - 1) + "以上";
					} else if (index == 0) {
						continue;
					} else {
						rangeValue = values.get(index - 1) + "-" + values.get(index);
					}
					Object facetId = ranges.get(rangeValue);
					if (EmptyUtil.isEmpty(facetId)) {
						continue;
					}
					this.addArrayValue(indexDoc, StringUtil.append(FIELD_PREFIX_F, catId, ".", facetInfoId), facetId + "");
				}
			}
		}
		return catIds;
	}

	private void putFacets2Dynamic(String prefix, String key, String value, List<String> clearData, ShardedJedis jedis) {
		if (ESDict.put(prefix, key, value, true, false, jedis))
			clearData.add(StringUtil.append(prefix, "@", key, "@f"));
	}

	private String[] getPinyins(String chinese) {
		String[] spells = ModuleAppDict.self.getPinyin(chinese);
		if (EmptyUtil.isEmpty(spells)) {
			spells = PinyinUtil.toSpell(chinese);
		}
		return spells;
	}


	/**
	 *
	 * @param sources
	 * @param dest
	 */
	private void mergeFacets(BasicDBList sources, BasicDBObject dest) {
		if (EmptyUtil.isEmpty(sources)) {
			return;
		}
		BasicDBObject source = null;
		for (Object object : sources) {
			source = (BasicDBObject) object;
			BasicDBObject tempDest = new BasicDBObject();
			tempDest.putAll((Map<String, Object>) source);
			dest.append(StringUtil.append(source.getString("catId"), ".", source.getString("id")), tempDest);
		}
	}

	/**
	 * 1.首先分析是更新国美还是更新库巴的索引库 2.如果更新库巴的索引，返回state==4 and 价格最低的一个SKU。即：返回一个SKU
	 * 3.如果更新国美的索引，判断这个产品是否属于需要多图展示的分类 如果是多图展示的分类，返回state==4 and
	 * 价格最低的一个SKU。即：返回一个SKU 返回每个颜色下面价格最低的SKU。即：返回多个SKU
	 *
	 * @param skus
	 *            从ATG传过来的某个PRODUCT下面的所有的SKU
	 * @return 合并以后的SKUS
	 *
	 *
	 *         返回 state =4 且
	 */
	public BasicDBObject parseMultiSku(BasicDBList skus, BasicDBList categories, String productId) {

		if (skus == null || skus.size() < 1) {
			return null;
		}

		BasicDBList colorNameList = new BasicDBList();

		// 先按颜色分组
		HashMap<String, BasicDBList> colors = new HashMap<String, BasicDBList>();
		for (int i = 0; i < skus.size(); i++) {
			// 只保留state=4的产品
			BasicDBObject oSku = (BasicDBObject) skus.get(i);
			int oState = oSku.getInt("state", -1);
			long oStartDate = oSku.getLong("startDate", 0);
			long oEndDate = oSku.getLong("endDate", 0);
			String skuId = oSku.getString("id");
			boolean intoindex = updateProductState(oState, oStartDate, oEndDate, skuId, productId, "skuState");
			if (oState != 4 && !intoindex) {
				logger.warn("productId [{}], skuId [{}],skuState [{}]", new Object[] { productId, skuId, oState });
				continue;// 跳过状态不是4的
			}
			// 按颜色对SKUS分组
			String color = oSku.getString("color", "none").trim();
			BasicDBList items = colors.get(color);
			if (items == null) {
				items = new BasicDBList();
				colors.put(color, items);
				colorNameList.add(color);
			}
			items.add(oSku);
		}

		// 找出每个分组中价格最便宜的
		BasicDBList oSkusByColor = new BasicDBList();// 再次根据sku价格是否为空过滤,保存颜色分组信息，color
														// 颜色， index 最小价格sku
														// 索引，skus 该颜色的sku
		for (Iterator<Entry<String, BasicDBList>> i = colors.entrySet().iterator(); i.hasNext();) {
			Entry<String, BasicDBList> entry = i.next();
			BasicDBList skusByColor = entry.getValue();
			int itemIndex = 0;
			double price = Double.MAX_VALUE;
			BasicDBObject oSku = null;
			BasicDBList items = new BasicDBList();
			for (int t = 0; t < skusByColor.size(); t++) {
				oSku = (BasicDBObject) skusByColor.get(t);
				BasicDBObject oPrice = (BasicDBObject) oSku.get("price");
				if (oPrice == null) {
					continue;
				}
				double calcPrice = calcPrice(oPrice);
				items.add(oSku);
				oPrice.append("price", calcPrice);// ？？？
				if (calcPrice < price) {
					itemIndex = items.size() - 1;
					price = calcPrice;
				}
			}
			if (items.size() == 0) {
				continue;
			}
			BasicDBObject oColor = new BasicDBObject();
			oColor.append("color", entry.getKey());//
			oColor.append("index", itemIndex);// 最小价格索引
			oColor.append("skus", items);// 有价格并且状态4的sku
			oSkusByColor.add(oColor);
		}

		if (oSkusByColor.size() == 0) {
			return null;
		}

		BasicDBObject parseResult = new BasicDBObject();
		parseResult.append("isMulti", false);
		parseResult.append("isBigImg", false);
		parseResult.append("colorNameList", colorNameList);

		// 如果是国美产品，并且所在分类属于多图展示分类，返回一个SKU 确认isMulti和isBigImg 并 如果isMulti true
		// 多返回一个sku （所有颜色中，价格最低的）
		if (EmptyUtil.notEmpty(categories)) {
			for (Object object : categories) {
				BasicDBObject category = (BasicDBObject) object;
				String catId = category.getString("catId", "");
				String catalog = category.getString("catalog");
				if ("coo8".equals(catalog)) {
					catId = category.getString("mappingCatId");
				}
				if (EmptyUtil.isEmpty(catId)) {
					continue;
				}
				if (EmptyUtil.notEmpty(ModuleAppDict.self.getBigImgCatetory()) && ModuleAppDict.self.getBigImgCatetory().contains(catId)) {
					parseResult.append("isBigImg", true);
					break;
				}
			}

		}
		// 普通国美产品多个SKU展示 ？？？？什么是普通国美产品
		parseResult.append("skus", oSkusByColor);
		return parseResult;
	}

	/**
	 * 在所有颜色的SKU中，找出价格最低的一个SKU
	 *
	 * @param skusByColor
	 *            不同颜色的SKU List
	 *
	 * @return 价格最低的一个SKU
	 *
	 */
	public BasicDBObject parseSkuByPrice(BasicDBList skusByColor) {
		double price = Double.MAX_VALUE;
		int skuIndex = -1;
		for (int i = 0; i < skusByColor.size(); i++) {
			BasicDBObject oByColor = (BasicDBObject) skusByColor.get(i);
			BasicDBList oSkus = (BasicDBList) oByColor.get("skus");
			BasicDBObject oSku = (BasicDBObject) oSkus.get(oByColor.getInt("index"));
			BasicDBObject oPrice = (BasicDBObject) oSku.get("price");
			double calcPrice = calcPrice(oPrice);
			oPrice.append("price", calcPrice);
			if (calcPrice < price) {
				price = calcPrice;
				skuIndex = i;
			}
		}
		if (skuIndex < 0) {
			return null;
		}
		BasicDBObject byColor = (BasicDBObject) skusByColor.get(skuIndex);
		BasicDBList skus = (BasicDBList) byColor.get("skus");
		return (BasicDBObject) skus.get(byColor.getInt("index"));
	}

	public double calcPrice(BasicDBObject oPrice) {
		if (oPrice == null) {
			return 0;
		}
		double listPrice = oPrice.getDouble("listPrice", 0);
		double salePrice = oPrice.getDouble("salePrice", 0);
		long startDate = oPrice.getLong("onSaleStartDate", 0);
		long endDate = oPrice.getLong("onSaleEndDate", 0);
		long current = System.currentTimeMillis();
		if (current <= (startDate - 60000) || current >= (endDate - 60000)) {
			oPrice.append("price", listPrice);
			return listPrice;
		}
		salePrice = salePrice != 0 ? salePrice : listPrice;
		oPrice.append("price", salePrice);
		return salePrice;
	}

	@SuppressWarnings("unused")
	public boolean updateProductState(int state, long startDate, long endDate, String skuId, String productId, String Type) {
		boolean intoindex = false;
		long current = System.currentTimeMillis();
		long maxScheduleDate = getMaxScheduleDate(current);
		if (startDate != 0 && endDate != 0) {
			if (((state == 4 && endDate < maxScheduleDate) || (state != 4 && startDate > current && maxScheduleDate > startDate))) {
				long date = endDate;
				if (state != 4) {
					date = startDate;
					intoindex = true;
				}
				BasicDBObject schedule = new BasicDBObject();
				if (state == 4) {
					Type = Type + "x";
				} else {
					Type = Type + "s";
				}
				schedule.append("type", Type);
				if (skuId != null)
					schedule.append("skuId", skuId);
				schedule.append("Date", date);
				schedule.append("id", productId);
				logger.info("add  scheduler updatestate productid[{}]  skuId[{}]  state[{}] type[{} time [{}]", new Object[] { productId, skuId, state, Type, dateFm.format(new Date(date)) });
				String name = "updatestete-" + productId + "-" + skuId + "-" + StringUtil.createUniqueID();
//				schedulerClient.addJob(JobInfoFactory.builder().content(schedule.toString()).name(name).group("sku-change").startDate(date).jobClass(JobSkuChange.class)
//						.triggerType(TriggerType.START_AT).executeMode(ExecuteMode.DISTRIBUTED).build());

			}
		}
		return intoindex;
	}

	public long getMaxScheduleDate(long current) {
		return current + (7 * 24 * 3600 * 1000);
	}

	@Override
	public void updateProductInfo(String productId, String field, Object obj) {
		ShardedJedis jedis = null;
				int date = (Integer) obj;
				if (date == 0)
					return;
				String product = ESDict.get(DYNAMIC_PREFIX, productId, true, jedis);
				if (StringUtil.isEmpty(product)) {
					logger.warn("updateProductInfo is null");
					return;
				}
				BasicDBObject oProduct = (BasicDBObject) JSON.parse(product);
				String oSkus = oProduct.getString("indexSkus");
				if (StringUtil.isEmpty(oSkus)) {
					logger.warn("updateProductInfo  skus is null");
					return;
				}
				String[] skus = oSkus.split(",");
				for (String skuId : skus) {
					// logger.info("updateProductInfo productId[{}] sku[{}]  type [{}] popular [{}]",
					// new Object[] { productId, skuId, field, date });
					this.updateFiledById(skuId, field, date);
				}
	}

	@Override
	public void updateFiled(DBObject oReq) {
		updateField((BasicDBObject) oReq);
	}

	@SuppressWarnings("unchecked")
	public void updateField(BasicDBObject oReq) {
		if (oReq == null) {
			return;
		}
		ShardedJedis jedis = null;
		try {
			String skuId = oReq.getString(FIELD_SKUID);
			if (StringUtil.isEmpty(skuId)) {
				logger.error("updateField skuId is null,[{}]", oReq);
				return;
			}
			oReq.remove(FIELD_SKUID);
			BasicDBList dynamics = (BasicDBList) oReq.remove("dynamics");
			if (EmptyUtil.notEmpty(dynamics)) {
				Set<Entry<String, Object>> entries;
				for (Object object : dynamics) {
					entries = ((BasicDBObject) object).entrySet();
					for (Entry<String, Object> entry : entries) {
						String productId = entry.getKey();

						String json = ESDict.get(DYNAMIC_PREFIX, productId, true, jedis);// dynamicDB.get(productId);
						if (EmptyUtil.notEmpty(json)) {
							BasicDBObject product = (BasicDBObject) JSON.parse(json);
							product.putAll((Map<String, Object>) entry.getValue());
							// logger.info("update product  dynamics productId [{}] ",productId
							// );
							updateProductCache(productId, product.toString(), jedis);
						}
					}
				}
			}
			updateFiledsById(skuId, oReq);
		} finally {
		}
	}

	private void updateProductCache(String productId, String productValue, ShardedJedis jedis) {

		ESDict.put(DYNAMIC_PREFIX, productId, productValue, false, true, jedis);
		List<String> clearIds = Lists.newArrayList();
		clearIds.add(StringUtil.append(DYNAMIC_PREFIX, "@", productId, "@p"));

		ModuleAppDict.self.dispatchClearCache("indexCache", new BasicDBObject("clearIds", clearIds).append("fromIp", Global.localIP));
	}

	@Override
	public void updateFileds(Object oReq) {
		@SuppressWarnings("unchecked")
		ArrayList<BasicDBObject> list = (ArrayList<BasicDBObject>) oReq;
		BasicDBObject dPro;
		String skus;
		float score = 0;
		ShardedJedis jedis = null;
		try {
			for (BasicDBObject obj : list) {
				if (StringUtils.isEmpty(obj.getString("wpro")))
					continue;
				String json = ESDict.get(DYNAMIC_PREFIX, obj.getString("wpro"), true, jedis);
				if (EmptyUtil.notEmpty(json)) {
					dPro = (BasicDBObject) JSON.parse(json);
					skus = dPro.getString("indexSkus");
					if (StringUtil.isEmpty(skus))
						continue;
					for (String sku : skus.split(",")) {

						score = ((Number) obj.get(FIELD_WEIGHT)).floatValue();
						this.updateFiledById(sku, FIELD_WEIGHT, score);
					}
				}
			}
		} finally {
		}
	}

	@Override
	public BasicDBList getProductReqs(List<String> productIds) {
		BasicDBList dbList = new BasicDBList();
		BasicDBObject dynProduct, dbObject;
		ShardedJedis jedis =null;
		try {
			for (String productId : productIds) {
				String json = ESDict.get(DYNAMIC_PREFIX, productId, true, jedis);
				if (EmptyUtil.notEmpty(json)) {
					dynProduct = (BasicDBObject) JSON.parse(json);
					dbObject = new BasicDBObject();
					dbObject.append("id", productId);
					dbObject.append("categories", dynProduct.get("categories"));
					dbObject.append("brand", dynProduct.getString("brand"));
					dbList.add(dbObject);
				}
			}
		} finally {
		}
		return dbList;
	}

	@Override
	public BasicDBObject getProductIds(String catId, int page, int pageSize) {

		SearchResponse response = client.prepareSearch(indexName).setTypes(indexType).setPostFilter(FilterBuilders.termFilter(FIELD_CATS, catId)).setFrom(page * pageSize).setSize(pageSize)
				.addField(FIELD_PRODID).setSearchType(SearchType.DFS_QUERY_AND_FETCH).execute().actionGet();

		BasicDBObject dbObject = new BasicDBObject();
		List<String> productIds = new ArrayList<String>();
		dbObject.append("productIds", productIds);
		dbObject.append("count", response.getHits().getTotalHits());

		for (SearchHit hit : response.getHits()) {
			productIds.add(String.valueOf(hit.getFields().get(FIELD_PRODID).getValue()));
		}
		return dbObject;
	}

	@Override
	public void updateFacet(BasicDBObject facets) {
		if (EmptyUtil.isEmpty(facets)) {
			return;
		}
		Iterator<Entry<String, Object>> iterator = facets.entrySet().iterator();
		String productId;
		BasicDBObject product, productFacet, skuFacets, sku;
		BasicDBList skus;

		ShardedJedis jedis = null;//RedisPool.getESRedisPool().getJedis();
		try {
			while (iterator.hasNext()) {
				Entry<String, Object> next = iterator.next();
				productId = next.getKey();
				productFacet = (BasicDBObject) next.getValue();
				String json = ESDict.get(DYNAMIC_PREFIX, productId, true, jedis);
				if (EmptyUtil.notEmpty(json)) {
					product = (BasicDBObject) JSON.parse(json);
					product.append("facets", productFacet.get("facets"));
					skus = (BasicDBList) product.get("skus");
					skuFacets = (BasicDBObject) productFacet.get("skuFacets");
					for (int j = 0; EmptyUtil.notEmpty(skus) && j < skus.size(); j++) {
						sku = (BasicDBObject) skus.get(j);
						sku.append("facets", (BasicDBList) skuFacets.get(sku.getString("id")));
					}
					addDoc(product, jedis);
				}
			}
		} finally {
//			RedisPool.getESRedisPool().ret(jedis);
		}
	}



	@Override
	public void handleMsg(BasicDBObject msg) {
		if (msg == null) {
			logger.warn("handle msg error because no content in msg");
			return;
		}
		ShardedJedis jedis = null;
		try {

			String productId = msg.getString("id");
			String product = ESDict.get(DYNAMIC_PREFIX, productId, true, jedis);
			if (StringUtil.isEmpty(product)) {
				logger.warn("handleMsg get ESDict productId [{}] null!", new Object[] { productId });
				return;
			}
			BasicDBObject oProduct = (BasicDBObject) JSON.parse(product);

			String types = msg.getString("type");
			String skuId = msg.getString("skuId");

			logger.info("handleMsg ready to update schduler productId [{}] skuId [{}] type [{}]", new Object[] { productId, skuId, types });
			if ("skuPrice".equalsIgnoreCase(types)) {
				updateSkuPrice(skuId, oProduct);
			} else {
				updateProductStatus(oProduct, types, skuId);
			}
			updateProductCache(productId, oProduct.toString(), jedis);
		} finally {
//			RedisPool.getESRedisPool().ret(jedis);
		}
	}

	public boolean updateSkuPrice(String skuId, BasicDBObject oProduct) {
		BasicDBList oSkus = (BasicDBList) oProduct.get("skus");
		if (oSkus == null || oSkus.size() == 0) {
			return false;
		}

		double docPrice = Double.MAX_VALUE;
		for (int i = 0; i < oSkus.size(); i++) {
			BasicDBObject oSku = (BasicDBObject) oSkus.get(i);
			if (skuId.equals(oSku.getString("id"))) {

				BasicDBObject oPrice = (BasicDBObject) oSku.get("price");
				double price = calcPrice(oPrice);
				if (price <= docPrice) {
					docPrice = price;
				}
				oSku.put("onSale", false);
				break;
			}
		}
		// 在索引库里面没有这个商品，所以不更新商品价格
		if (docPrice == Double.MAX_VALUE) {
			return false;
		}

		// 重新计算facet中的价格区间
		BasicDBObject updateField = new BasicDBObject();
		updateFacetRanges(oProduct, docPrice, updateField);
		updateField.put(FIELD_PRICE, docPrice);
		this.updateFiledsById(skuId, updateField);
		logger.info("schdule update price [{}] skuId[{}]", new Object[] { docPrice, skuId });

		return true;
	}

	public void updateProductStatus(BasicDBObject oProduct, String type, String skuId) {
		String oSkus = oProduct.getString("indexSkus");
		if (type.equalsIgnoreCase("productStates")) {
			updateStatus(oSkus, 4, "product", skuId);
		} else if (type.equalsIgnoreCase("productStatex")) {
			updateStatus(oSkus, 5, "product", skuId);
		} else if (type.equalsIgnoreCase("skuStates")) {
			updateStatus(oSkus, 4, "sku", skuId);
		} else if (type.equalsIgnoreCase("skuStatex")) {
			updateStatus(oSkus, 5, "sku", skuId);
		}
	}

	private void updateStatus(String oSkus, int status, String type, String skuId) {
		if (!tempDebug) {
			if (StringUtil.isEmpty(oSkus)) {
				logger.info("updateProductInfo  skus is null");
				return;
			}
			String[] skus = oSkus.split(",");
			if (type.equalsIgnoreCase("sku")) {

				this.updateFiledById(skuId, FIELD_SKUSTATE, status);
				logger.info("updateskuState  sku[{}] statu [{}]", new Object[] { skuId, status });

			} else {
				for (String sku : skus) {
					this.updateFiledById(sku, FIELD_PRODSTATE, status);
					logger.info("updateproductState  sku[{}] statu [{}]", new Object[] { sku, status });
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void updateFacetRanges(BasicDBObject oProduct, double docPrice, BasicDBObject fields) {
		if (oProduct.containsField("categories")) {
			BasicDBList categories = (BasicDBList) oProduct.get("categories");
			for (Object object : categories) {
				BasicDBObject category = (BasicDBObject) object;
				String categoryId = category.getString("id");
				if (StringUtil.isEmpty(categoryId)) {
					continue;
				}
				BasicDBObject oCategory = CategoryDict.getCategory(categoryId);
				if (oCategory == null) {
					continue;
				}
				if (oCategory.containsField("catalog") == false) {
					continue;
				}
				BasicDBList facets = (BasicDBList) oCategory.get("facets");
				if (facets == null) {
					continue;
				}
				for (Object obj : facets) {
					BasicDBObject facet = (BasicDBObject) obj;
					if (facet.containsField("ranges") == false) {
						continue;
					}
					double price = docPrice;
					BasicDBList values = (BasicDBList) facet.get("values");// [0,123,456,789]
					BasicDBObject ranges = (BasicDBObject) facet.get("ranges");

					int index = 0;
					if (ranges == null)
						return;
					for (int k = 0; k < values.size(); k++) {
						if (StringUtil.parserInt(values.get(k).toString(), 0) > price) {
							index = k;
							break;
						}
					}

					String facetInfoId = facet.getString("id");
					String rangeValue = null;
					if (index == -1) {
						rangeValue = values.get(ranges.size() - 1) + "以上";
					} else if (index == 0) {
						continue;
					} else {
						rangeValue = values.get(index - 1) + "-" + values.get(index);
					}
					Object facetId = ranges.get(rangeValue);
					if (EmptyUtil.isEmpty(facetId)) {
						continue;
					}
					// fields.put(StringUtil.append("f.", categoryId, ".",
					// facetInfoId), facetId);
					// doc.addField(StringUtil.append("f.", categoryId, ".",
					// facetInfoId), facetId);
				}
			}
		}
	}

	@Override
	public BasicDBObject search(DBObject oRequire) {
		ShardedJedis jedis = null;//RedisPool.getESRedisPool().getJedis();
		try {
			return search(oRequire, jedis);
		} catch (Exception e) {
			e.printStackTrace();
			return new BasicDBObject();
		} finally {
//			RedisPool.getESRedisPool().ret(jedis);
		}
	}

	@SuppressWarnings("unused")
	public BasicDBObject search(DBObject oRequire, ShardedJedis jedis) {
		BasicDBObject oResult = new BasicDBObject();
		long start = System.currentTimeMillis();
		BasicDBObject oReq = (BasicDBObject) oRequire;
		String question = oReq.getString("question", "").trim();
		// logger.info("question:"+oReq);
		boolean hasQuestion = StringUtil.notEmpty(question);
		if (hasQuestion && question.length() > 50 && !question.startsWith("skuId:") && !question.startsWith("skuNo:") && !question.startsWith("productId:")) {
			question = question.substring(0, 50);
			oReq.append("question", question);
		}

		if (oReq.getBoolean("docCount", false)) {
			try {
				QueryBuilder queryBuilder = new QueryBuilder().setIndices(indexName).setTypes(indexType);
				question = StringUtil.readSpecial(question);
				if (!StringUtil.isEmpty(oReq.getString("catId"))) {
					queryBuilder.addTermFilter(oReq.getString("catalog"), oReq.getString("catId"));
				}

				if (hasQuestion) {
					ArrayList<BasicDBObject> tokens = parseTokens(question, TokenMode.search, true);
					queryBuilder.addTermFilter(FIELD_SKUSTATE, 4);
					queryBuilder.addTermFilter(FIELD_PRODSTATE, 4);
					queryBuilder.setQuery(parseQuestion(queryBuilder, tokens, oReq, oResult, true, true, false));
				} else {
					// queryBuilder.setQuery(SQL_ALL);

				}
				oResult.append("docCount", queryBuilder.get().getHits().getTotalHits());

			} catch (Exception e) {
				oResult.append("docCount", 0);
				oReq.append("e", e);
			}
			return oResult;
		}
		QueryBuilder queryBuilder = new QueryBuilder().setIndices(indexName).setTypes(indexType);
		String cityId = oReq.getString("regionId", "11010200");
		if (cityId.equalsIgnoreCase("0") || StringUtil.isEmpty(cityId)) {
			cityId = "11010200";
		}

		queryBuilder.setScriptParm(PARAM_REGION, cityId);
		queryBuilder.regionId(cityId);

		String catId = oReq.getString("catId"), fakeCatId = "";
		boolean hasCategory = EmptyUtil.notEmpty(catId);
		boolean xSearch = oReq.getBoolean("XSearch", false);
		String catalog = parseQuery(queryBuilder, oReq, oResult, hasCategory, catId, hasQuestion, question);
		if (!hasCategory && hasQuestion) {
//			ModuleFactory.SortRule().parseQuery(question, oReq.getBoolean("rewriteTag", true), oReq, queryBuilder, catalog, oResult);
			question = oReq.getString("question", question);// ModuleFactory.SortRule().parseQuery
															// 获取到了一个fakeCatId
			catId = fakeCatId = oReq.getString("fakeCatId");
			hasCategory = EmptyUtil.notEmpty(catId);
		}
		if (hasCategory && EmptyUtil.isEmpty(oReq.getString("shopId")) && EmptyUtil.isEmpty(CategoryDict.getCategory(catId))) {
			logger.error("category [{}] is empty!", catId);
			return oResult.append("is404", true);
		}
		if (xSearch && !hasCategory && !hasQuestion && !oReq.getBoolean("internalSearch", false)) {
			logger.info("catId and question is null,req={}", new Object[] { oReq });
			return oResult;
		}
		Map<String, BasicDBList> reqFacetMap = new HashMap<String, BasicDBList>();
		Map<String, BasicDBList> selectReqFacetMap = new HashMap<String, BasicDBList>();
		Map<String, BasicDBObject> facetInfos = null;
		String reqFacets = oReq.getString("facets");
		if (hasCategory) {
			facetInfos = parseReqFacets(queryBuilder, catId, reqFacets, reqFacetMap, selectReqFacetMap, jedis);
		}
		BasicDBList filterReqFacets = (BasicDBList) oReq.get("filterReqFacets");
		List<FilterBuilder> fqs = null;
		if (EmptyUtil.notEmpty(filterReqFacets) && hasCategory) {
			fqs = parseFilterFacets(queryBuilder, catId, filterReqFacets, jedis);
		}
		long startGetfacet = 0, endGetFacets = 0, startReadProduct = 0, endReadProduct = 0;
		try {

			SearchResponse response = getQueryResponse(queryBuilder, oReq, oResult, fqs, true);
			BasicDBList facets = null, categories = null;
			if (!hasQuestion && hasCategory && CategoryDict.containCategoryCache(catId) && CategoryDict.containFacetCache(catId)) {
				facets = CategoryDict.getFacetCache(catId);
				categories = CategoryDict.getCategoryCache(catId);
			} else if (!hasQuestion && hasCategory && EmptyUtil.notEmpty(reqFacets)) {

				Object tmpFacets = oReq.remove("facets");
				Object tmpProductTag = oReq.remove("productTag");
				logger.info("search again.");
				this.search(oReq, jedis);
				oReq.append("facets", tmpFacets);
				oReq.append("productTag", tmpProductTag);
				facets = CategoryDict.getFacetCache(catId);
				categories = CategoryDict.getCategoryCache(catId);
				if (reqFacets.contains("19I2")) {
					logger.info("==================catId" + catId + "," + facets);
				}
			} else if (hasCategory) {

				startGetfacet = System.currentTimeMillis();
				List<Facet> facetFields = response.getFacets().facets();
				Map<String, Integer> categoryMatches = new HashMap<String, Integer>();
				facets = new BasicDBList();
				for (int i = 0; facetFields != null && i < facetFields.size(); i++) {
					TermsFacet field = (TermsFacet) facetFields.get(i);
					if (field.getEntries().size() == 0) {
						// if(hasQuestion&&oReq.getString("question").equals("巴慕达")){
						// String facetInfoId = field.getName();
						// logger.info("enter bamuda:"+field.getEntries().size()+":facetInfoId="+facetInfoId);
						//
						// for (TermsFacet.Entry entry : field.getEntries()) {
						// String facetId = entry.getTerm().string();
						// String facetvalue=ESDict.get("fv", facetId, false,
						// jedis);
						// logger.info("facetId="+facetId+",value="+facetvalue);
						// }
						//
						// }
						// if(!hasQuestion&&"cat10000230".equals(catId)){
						// String facetInfoId = field.getName();
						// logger.info("enter bamuda:"+field.getEntries().size()+":facetInfoId="+facetInfoId);
						//
						// for (TermsFacet.Entry entry : field.getEntries()) {
						// String facetId = entry.getTerm().string();
						// String facetvalue=ESDict.get("fv", facetId, false,
						// jedis);
						// logger.info("facetId="+facetId+",value="+facetvalue);
						// }
						//
						// }
						continue;
					}

					String facetInfoId = field.getName();

					if (facetInfoId.startsWith("f.") && EmptyUtil.notEmpty(facetInfos)) {
						parseRespFacet(facetInfoId, facetInfos, field, facets, jedis);

					} else if (facetInfoId.equals("categories")) {

						for (TermsFacet.Entry entry : field.getEntries()) {
							String name = entry.getTerm().string();
							BasicDBObject category = CategoryDict.getCategory(name);
							if (EmptyUtil.isEmpty(name) || EmptyUtil.isEmpty(category) || !"gome".equalsIgnoreCase(category.getString("catalog"))) {
								continue;
							}
							categoryMatches.put(name, entry.getCount());
						}
					}
				}
				endGetFacets = System.currentTimeMillis();
				if (hasCategory && categoryMatches.size() == 0) {
					categoryMatches.put(catId, 0);
				}
				categories = readCategories(catId, categoryMatches, oReq.getString("pageName", "list"));
				boolean hasRequestFacets = reqFacets != null && !reqFacets.isEmpty();
				if (!hasQuestion && hasCategory && !hasRequestFacets) {
					CategoryDict.setFacetCache(catId, facets);
					CategoryDict.setCategoryCache(catId, categories);
				}
			}
			BasicDBList recommend = null;
			if (EmptyUtil.isEmpty(fakeCatId) && hasCategory && !oReq.getBoolean("isRecommend", false)) {
				if (!CategoryDict.containRecommend(catId)) {
					String skuIds = ModuleAppDict.self.getRecommend(catId);
					if (EmptyUtil.notEmpty(skuIds)) {
						oReq.append("isRecommend", true);
						oReq.append("question", StringUtil.append("skuId:", skuIds));
						BasicDBList products = (BasicDBList) this.search(oReq, jedis).get("products");
						Map<String, Integer> map = ModuleAppDict.self.getRecommendIndex(catId);
						for (Object object : products) {
							BasicDBObject product = (BasicDBObject) object;
							String skuId = product.getString("skuId");
							product.put("pos", map.get(skuId));
						}
//						DBObjectUtil.quickSortByNum(products, "pos");
						CategoryDict.setRecommend(catId, products);
						oReq.append("question", question);
					}
				}
				if (CategoryDict.containRecommend(catId)) {
					recommend = CategoryDict.getRecommend(catId);
					BasicDBList recommendList = new BasicDBList();
					int size = 0;
					for (Object object : recommend) {
						size = recommendList.size();
						if (size >= 3) {
							break;
						}
						if (true) {
							recommendList.add(object);
						}
					}
					size = recommendList.size();
					if (size > 0 && size < 3) {
						for (Object object : recommend) {
							if (recommendList.contains(object)) {
								continue;
							}
							size = recommendList.size();
							if (size >= 3) {
								break;
							}
							recommendList.add(object);
						}
					}
					recommend = recommendList;
				}
			}
			oResult.append("searchTime", response.getTookInMillis());
			// 1 图书
			oResult.append("totalCount", response.getHits().totalHits());
			oResult.append("reqFacets", selectReqFacetMap);
			oResult.append("facets", facets);
			if (oResult.getInt("searchLevel") == 1) {
				oResult.append("filterReqFacets", filterReqFacets);
			}
			oResult.append("recommend", recommend);

			oResult.append("fakeFacets", parseFakeFacets(response.getFacets(), (BasicDBList) oReq.get("fakeFacets")));
			oResult.append("categories", categories);
			startReadProduct = System.currentTimeMillis();
			oResult.append("products", readProducts(oReq, response, cityId, jedis));
			endReadProduct = System.currentTimeMillis();

		} catch (Exception e) {
			logger.error("search exception:[{}]", e.getMessage());
			e.printStackTrace();
		}

		try {
			long time = System.currentTimeMillis() - start;
			if (time > 0 && time <= 100) {
				logger.info(
						"L&s={}ms,c={},fc={},q={},m={}&qt={}&sl={}&fsl={}&ft={}&pt={}{}#hit={}*reqf={}",
						new Object[] { time, oReq.get("catId"), oReq.get("fakeCatId"), oReq.get("question"), oReq.get("mobile"), oResult.get("queryTime"), oResult.get("searchLevel"),
								oResult.get("fakeSearchLevel"), (endGetFacets - startGetfacet), (endReadProduct - startReadProduct), oReq.get("t"), oResult.get("totalCount"), oReq.getString("facets") });
			}
			if (time > 100) {
				logger.info("O&{}#{}s={}ms,c={},fc={},q={},m={}&qt={}&sl={}&fsl={}&ft={}&pt={}{}#hit={}*reqf={}", new Object[] { oResult.getInt("queryTime") > 100 ? "o" : "l",
						time > 3000 && time < 6000 ? "3" : time >= 6000 ? "6" : "n", time, oReq.get("catId"), oReq.get("fakeCatId"), oReq.get("question"), oReq.get("mobile"),
						oResult.get("queryTime"), oResult.get("searchLevel"), oResult.get("fakeSearchLevel"), (endGetFacets - startGetfacet), (endReadProduct - startReadProduct), oReq.get("t"),
						oResult.get("totalCount"), oReq.getString("facets") });
			}
		} catch (Exception e) {
			logger.error("print time log error!,c={}&fc={}&q={}", new Object[] { oReq.get("catId"), oReq.get("fakeCatId"), oReq.get("question") });
		}
		return oResult;
	}

	protected String parseQuery(QueryBuilder queryBuilder, BasicDBObject oReq, BasicDBObject oResult, boolean hasCategory, String categoryId, boolean hasQuestion, String question) {


		String price = oReq.getString("price", null);
		if (!EmptyUtil.isEmpty(price) && !StringUtil.isEmpty(price.trim())) {
			queryBuilder.addRangeFilter(FIELD_PRICE, price);
		}
		int sale = oReq.getInt("sale", 0);
		if (sale == 1) {
			queryBuilder.addRangeFilter(FIELD_PRODSTATE, 1l, Float.MAX_VALUE);
		}
		int productTag = oReq.getInt("productTag", 0);
		if (productTag != 0) {
			queryBuilder.addTermFilter(FIELD_PRODTAG, productTag);
		}
		int promoTag = oReq.getInt("promoTag", -1);
		if (promoTag == 618) {
			queryBuilder.addTermFilter(FIELD_PROMOTAG, 618);
		}
		boolean mobile = oReq.getBoolean("mobile", false);
		if (mobile) {
			queryBuilder.addNotFilter(FIELD_PRODTAG, 3);
			queryBuilder.addNotFilter(FIELD_PRODTAG, 4);
		} else {
			queryBuilder.addNotFilter(FIELD_PRODTAG, 4);
		}
		int shopType = oReq.getInt("shopType", -1);
		if (shopType == 1) {
			queryBuilder.addTermFilter(FIELD_SHOPTYPE, 1);
		}

		 // 点击店铺
		String shopId = oReq.getString("shopId", "");
		if (!StringUtil.isEmpty(shopId) && !StringUtil.isEmpty(shopId.trim())) {
			queryBuilder.addTermFilter(FIELD_SHOPID, shopId);
//			// sortNo 排序
//			BasicDBList sorts = new BasicDBList();
//			BasicDBObject oSort = new BasicDBObject();
//			oSort.put("name", FIELD_SORTNO);
//			oSort.put("order", "asc");
//			sorts.add(oSort);
//			oReq.put("sorts", sorts);
		}

		queryBuilder.addTermFilter(FIELD_SKUSTATE, 4);
		queryBuilder.addTermFilter(FIELD_PRODSTATE, 4);
		String skuId = oReq.getString("skuId");
		if (!EmptyUtil.isEmpty(skuId) && !StringUtil.isEmpty(skuId.trim())) {
			queryBuilder.addTermFilter(FIELD_SKUID, skuId);
		}
		String productId = oReq.getString("productId");
		if (!EmptyUtil.isEmpty(productId) && !StringUtil.isEmpty(productId.trim())) {
			queryBuilder.addTermFilter(FIELD_PRODID, productId);
		}
		String catalog = "categories";

		queryBuilder.addFacet(catalog);
		BasicDBList fakeFacets = (BasicDBList) oReq.get("fakeFacets");// ????????
		if (EmptyUtil.notEmpty(fakeFacets)) {
			queryBuilder.addFacet(fakeFacets.toArray(new String[0]));
		}
		if (hasCategory) {
			queryBuilder.addTermFilter(FIELD_CATS, categoryId);
		}
		int pageSize = oReq.getInt("pageSize", 10);
		int pageNumber = oReq.getInt("pageNumber", 1) - 1;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		pageSize = pageSize > 50 ? 50 : pageSize;

		queryBuilder.from(pageNumber * pageSize);
		queryBuilder.size(pageSize);

		BasicDBList sorts = (BasicDBList) oReq.get("sorts");

		if (sorts != null && sorts.size() == 1) {
			Object obj = sorts.get(0);
			BasicDBObject oSort = (BasicDBObject) obj;
			if (oSort != null && !oSort.isEmpty()) {

				queryBuilder.sort(oSort.getString("name", "_score"), oSort.getString("order", "desc").equalsIgnoreCase("desc") ? SortOrder.DESC : SortOrder.ASC);
			}
		}

		String testSorts = oReq.getString("testSorts");
		if (StringUtils.isNotEmpty(testSorts)) {
			testSorts = testSorts.replace(",", ":").replace(" ", ":").replace("-1", "d").replace("1", "a").replace("desc", "d").replace("asc", "a");
			String[] testSortsArr = testSorts.split(":");
			queryBuilder.sort(testSortsArr[0], testSortsArr[1].equalsIgnoreCase("d") ? SortOrder.DESC : SortOrder.ASC);
		}

		if (hasQuestion && (question.startsWith("skuNo:") || question.startsWith("productId:") || question.startsWith("skuId:"))) {
			String[] split = question.split(":");
			if (split.length == 2 && !StringUtils.isEmpty(split[1])) {
				List<String> ids = new ArrayList<String>();
				Collections.addAll(ids, split[1].replace(" ", " ").split(" "));
				queryBuilder.addOrFilter(split[0], ids);
			}
		}
		if (oReq.getBoolean("debug", false)) {
			queryBuilder.setExplain(true);
		}

		return catalog;
	}

	/**
	 * 当前类目所有 facet添加， 当前请求facets添加 orFILTER，， selectReqFacetMap 返回所有请求 facets
	 * 格式 "facetInfoId":[{id:facetId,value:facetValue，type:type} 返回 facetInfoMap
	 * 当前类目所有facets facetInfoMap {f.catId.facetInfoId:{一条facetInfoId的完整信息}}
	 *
	 * @param query
	 * @param categoryId
	 * @param reqFacets
	 * @param jedis
	 * @param question
	 *
	 * @return
	 */
	public Map<String, BasicDBObject> parseReqFacets(QueryBuilder queryBuilder, String categoryId, String reqFacets, Map<String, BasicDBList> reqFacetMap, Map<String, BasicDBList> selectReqFacetMap,
			ShardedJedis jedis) {
		BasicDBObject category = CategoryDict.getCategory(categoryId);
		if (EmptyUtil.isEmpty(category)) {
			logger.warn("catId [{}] does not exist in the category dict", new Object[] { categoryId });
			return null;
		}
		// facetInfoMap {f.catId.facetInfoId:{一条facetInfoId的完整信息}} (当前类目所有facet)
		Map<String, BasicDBObject> facetInfoMap = new HashMap<String, BasicDBObject>();
		if (EmptyUtil.notEmpty(reqFacets)) {
			int length = reqFacets.length();
			length = length / 4 * 4;// 将页面传入的facet字符串长度转化为4的倍数
			for (int i = 0; i < length; i = i + 4) {
				String facetId = reqFacets.substring(i, i + 4);
				String facetInfoId = ESDict.get("f", facetId, false, jedis);
				if (EmptyUtil.isEmpty(facetInfoId)) {
					continue;
				}
				// selectReqFacetMap
				// {"facetInfoId":[{id:facetId,value:facetValue，type:type}]}(当前请求facet)
				BasicDBList dbList = selectReqFacetMap.get(facetInfoId);
				if (dbList == null) {
					dbList = new BasicDBList();
					selectReqFacetMap.put(facetInfoId, dbList);
				}
				facetInfoId = StringUtil.append("f.", categoryId, ".", facetInfoId);
				// reqFacetMap {f.catId.facetInfoId:[facetId1,facetId2]}
				// (当前请求facet)
				BasicDBList facetIds = reqFacetMap.get(facetInfoId);
				if (facetIds == null) {
					facetIds = new BasicDBList();
					reqFacetMap.put(facetInfoId, facetIds);
				}
				facetIds.add(facetId);
				dbList.add(new BasicDBObject().append("id", facetId).append("value", ESDict.get("fv", facetId, false, jedis)));
			}
		}
		BasicDBList facetInfos = (BasicDBList) category.get("facets");
		if (EmptyUtil.isEmpty(facetInfos)) {
			return facetInfoMap;
		}
		facetInfos = (BasicDBList) facetInfos.copy();// TODO
		for (int i = 0; facetInfos != null && i < facetInfos.size(); i++) {
			BasicDBObject facetInfo = (BasicDBObject) facetInfos.get(i);
			String facetInfoId = facetInfo.getString("id");
			BasicDBList dbList = selectReqFacetMap.get(facetInfoId);
			if (EmptyUtil.notEmpty(dbList)) {
				for (Object obj : dbList) {
					((BasicDBObject) obj).append("type", facetInfo.getInt("type"));
				}
			}
			facetInfoId = StringUtil.append("f.", categoryId, ".", facetInfoId);
			facetInfoMap.put(facetInfoId, facetInfo);
			queryBuilder.addFacet(facetInfoId);
			BasicDBList facetIds = reqFacetMap.get(facetInfoId);
			if (EmptyUtil.isEmpty(facetIds)) {
				continue;
			}
			// 过滤facet

			queryBuilder.addOrFilter(facetInfoId, facetIds);
		}
		return facetInfoMap;
	}

	private List<FilterBuilder> parseFilterFacets(QueryBuilder queryBuilder, String catId, BasicDBList filterReqFacets, ShardedJedis jedis) {
		BasicDBObject category = CategoryDict.getCategory(catId);
		if (EmptyUtil.isEmpty(category)) {
			logger.warn("catId [{}] does not exist in the category dict", new Object[] { catId });
			return null;
		}
		// filterFacetMap {f.catId.facetInfoId:[facetIds]}
		Map<String, BasicDBList> filterFacetMap = new HashMap<String, BasicDBList>();

		for (int i = 0, len = filterReqFacets.size(); i < len; i++) {
			BasicDBObject filterFacet = (BasicDBObject) filterReqFacets.get(i);
			if (filterFacet.getBoolean("isPrice")) {
				BasicDBList prices = (BasicDBList) filterFacet.get("span");
				queryBuilder.addRangeFilter(FIELD_PRICE, Float.parseFloat(prices.get(0).toString()), Float.parseFloat(prices.get(1).toString()));
			} else {
				BasicDBList facetIds = (BasicDBList) filterFacet.get("facetID");
				if (EmptyUtil.isEmpty(facetIds)) {
					continue;
				}
				String facetId = facetIds.get(0).toString();
				String facetInfoId = ESDict.get("f", facetId, false, jedis);
				facetInfoId = StringUtil.append("f.", catId, ".", facetInfoId);
				filterFacetMap.put(facetInfoId, facetIds);
			}
		}

		BasicDBList facetInfos = (BasicDBList) category.get("facets");
		if (EmptyUtil.isEmpty(facetInfos)) {
			return null;
		}
		facetInfos = (BasicDBList) facetInfos.copy();// TODO
		List<FilterBuilder> fqs = Lists.newArrayList();
		for (int i = 0; facetInfos != null && i < facetInfos.size(); i++) {
			BasicDBObject facetInfo = (BasicDBObject) facetInfos.get(i);
			String facetInfoId = facetInfo.getString("id");
			facetInfoId = StringUtil.append("f.", catId, ".", facetInfoId);
			BasicDBList facetIds = filterFacetMap.get(facetInfoId);
			if (EmptyUtil.isEmpty(facetIds)) {
				continue;
			}

			BoolFilterBuilder orFilter = queryBuilder.buildOrFilter(facetInfoId, facetIds);
			queryBuilder.addFilter(orFilter);
			fqs.add(orFilter);
		}
		return fqs;
	}

	@SuppressWarnings("null")
	public SearchResponse getQueryResponse(QueryBuilder queryBuilder, BasicDBObject oReq, BasicDBObject oResult, List<FilterBuilder> fqs, boolean comMode) throws Exception {

		String question = oReq.getString("question", "").trim();
		if ((question.startsWith("skuNo:") || question.startsWith("productId:") || question.startsWith("skuId:"))) {
			question = "";
		}
		String et = oReq.getString("et", "").trim();
		if (EmptyUtil.notEmpty(et)) {
			question = StringUtil.append(question, " ", et);
		}
		int fsl = oResult.getInt("fakeSearchLevel", 0);
		ArrayList<BasicDBObject> tokens = parseTokens(question, TokenMode.search, true);
		queryBuilder.setQuery(parseQuestion(queryBuilder, tokens, oReq, oResult, true, true, comMode));
		long start = System.currentTimeMillis();
		SearchResponse reponse = queryBuilder.get();

		// logger.info("hit doc:"+reponse.getHits().getTotalHits()+"--------------use es search time:"+(System.currentTimeMillis()
		// - start));
		oResult.append("searchLevel", fsl == 2 ? fsl : 1);

		if (reponse.getHits().getTotalHits() == 0 && fsl == 0 && !oReq.getBoolean("internalSearch", false) && EmptyUtil.notEmpty(question)) {
			BasicDBList values = null;//(BasicDBList) secondSearch.convertQuery(question).get("values");
			logger.info("question={} token[{}]&secondsearch={}", new Object[] { question, tokens.toString(), values });
			if (EmptyUtil.isEmpty(values)) {
				oResult.append("queryTime", oResult.getLong("queryTime", 0) + (System.currentTimeMillis() - start));
				return reponse;
			}
			if (EmptyUtil.notEmpty(fqs)) {
				for (FilterBuilder fb : fqs) {
					queryBuilder.removeFilter(fb);
				}
			}
			question = values.get(0).toString();
			oResult.append("showWord", question);
			oReq.append("question", question);
			tokens = parseTokens(question, TokenMode.search, true);

			reponse = queryBuilder.get(parseQuestion(queryBuilder, tokens, oReq, oResult, true, true, comMode));
			oResult.append("searchLevel", 2);
		}

		oResult.append("queryTime", oResult.getLong("queryTime", 0) + (System.currentTimeMillis() - start));
		return reponse;
	}

	public String parseQuestion(QueryBuilder queryBuilder, ArrayList<BasicDBObject> tokens, BasicDBObject oReq, BasicDBObject oResult, boolean titleMode, boolean ngramMode, boolean comMode) {
		ArrayList<String> keys = new ArrayList<String>();
		ArrayList<String> ngrams = new ArrayList<String>();
		for (int i = 0; i < tokens.size(); i++) {
			BasicDBObject oToken = tokens.get(i);
			String token = oToken.getString("text");
			if (StringUtil.isEmpty(token.trim()))
				continue;
			switch (oToken.getInt("level")) {
			case 1:
			case 2:
				keys.add(token);
				break;
			case 3:
				String[] ns = token.split(" ");
				for (String n : ns) {
					ngrams.add(n);
				}
				break;
			}
		}

		StringBuilder sql = new StringBuilder();
		boolean fake = false;
		String fakeCatId = oReq.getString("fakeCatId");
		if (!StringUtil.isEmpty(fakeCatId)) {
			fake = true;
		}

		StringBuilder title = new StringBuilder();
		if (keys.size() > 0) {
			oResult.append("hlc", keys);
			String key = StringUtil.join(keys, "AND");
			oResult.append("keys", key);
			sql.append("categoryBrand:(");
			sql.append(key);
			sql.append(")^0.3 OR ");
			if (titleMode == true) {
				title.append(" title:(");
				title.append(StringUtil.join(keys, "AND"));
			}

			title.append(")^1.2");
			sql.append(" facet:(");
			sql.append(key + "");
			sql.append(")^0.3 ");

		}
		if (title.length() > 0) {
			sql.append(") OR (");
			sql.append(title);
		}
		if (ngrams.size() > 0) {
			if (sql.length() != 0) {
				sql.append(" ) OR (");
			}

			oResult.append("hle", ngrams);
			String ngram = StringUtil.join(ngrams, "AND");
			oResult.append("words", ngram);
			sql.append("n-gram:(");
			sql.append(ngram);
			sql.append(")^0.5 ");
		}
		// String cityId = oReq.getString("regionId", "11010200");
		// if (instock == 1) {
		//
		// if (cityId.equalsIgnoreCase("0")) {
		// cityId = "11010200";
		// }
		// cityId = newDragonDict.cityindex.get(cityId);
		// if (StringUtil.isEmpty(cityId)) {
		// cityId = "1";
		// }
		// String cityIndex = newDragonDict.citys.get(cityId);
		// if (StringUtil.isEmpty(cityIndex)) {
		// cityIndex = "1";
		// }
		// if (sql.length() > 0) {
		// sql.append(") AND (");
		// }
		// sql.append("  a :(");
		// sql.append("a");
		// sql.append(")^0.01 OR ");
		//
		// sql.append("c :(");
		// sql.append(cityIndex);
		// sql.append(")^0.01 ");
		// }
		if (sql.length() == 0) {
			sql.append(SQL_ALL);
		} else {
			if (fake && (keys.size() > 0 || ngrams.size() > 0)) {
				queryBuilder.setScriptParm("fakeCatId", fakeCatId);
			}
		}
		sql.insert(0, "(");
		sql.append(")");

		queryBuilder.setScriptParm(PARAM_FAKE, fake).setScriptParm(PARAM_COMMODE, comMode);
		queryBuilder.fake(fake);

		if (Global.DevelopMode == true) {
			logger.info("Search=" + sql.toString());
		}
		return sql.toString();
	}

	private void parseRespFacet(String facetInfoId, Map<String, BasicDBObject> facetInfos, TermsFacet field, BasicDBList facets, ShardedJedis jedis) {
		BasicDBObject facetInfo = facetInfos.get(facetInfoId);
		if (EmptyUtil.isEmpty(facetInfo) || field.getEntries() == null || field.getEntries().size() <= 0) {
			return;
		}

		BasicDBList facetValues = new BasicDBList();
		facetInfo.remove("values");
		facetInfo.append("items", facetValues);
		BasicDBObject indexs = (BasicDBObject) facetInfo.remove("indexs");
		for (TermsFacet.Entry entry : field.getEntries()) {
			String facetId = entry.getTerm().string();
			if (EmptyUtil.isEmpty(facetId)) {
				continue;
			}
			String prefix = ESDict.get("fp", facetId, false, jedis);
			BasicDBObject facet = new BasicDBObject().append("id", facetId).append("value", ESDict.get("fv", facetId, false, jedis)).append("count", entry.getCount());
			if (EmptyUtil.notEmpty(indexs)) {
				facet.append("index", indexs.getInt(facetId, Integer.MAX_VALUE));
			}
			if (prefix != null) {
				facet.append("prefix", prefix);
			}
			facetValues.add(facet);
		}
		StringUtil.quickSortByName("index", facetValues);
		facets.add(facetInfo);
	}

	protected BasicDBObject parseFakeFacets(Facets facets, BasicDBList reqFakeFacets) {
		BasicDBObject respFakeFacets = new BasicDBObject();
		if (EmptyUtil.notEmpty(facets) && EmptyUtil.notEmpty(facets.facets()) && EmptyUtil.notEmpty(reqFakeFacets)) {
			for (Facet facet : facets.facets()) {
				TermsFacet field = (TermsFacet) facet;
				if (field.getEntries().size() == 0)
					continue;
				String facetInfoId = field.getName();
				if (reqFakeFacets.contains(facetInfoId)) {// 以前包含的
															// facetInfoId用新的value代替

					BasicDBObject object = new BasicDBObject();
					for (TermsFacet.Entry entry : field.getEntries()) {
						String name = entry.getTerm().string();
						if (EmptyUtil.isEmpty(name)) {
							continue;
						}
						object.append(name, entry.getCount());
					}
					respFakeFacets.append(facetInfoId, object);
				}
			}
		}
		return respFakeFacets;
	}

	protected BasicDBList readProducts(BasicDBObject oReq, SearchResponse response, String cityId, ShardedJedis jedis) {
		BasicDBList items = new BasicDBList();
		if (response == null || response.getHits().getHits().length == 0) {
			return items;
		}
		int docOrder = 0;

		long cacheTime = 0;
		long parseTime = 0;
		for (SearchHit hit : response.getHits().getHits()) {
			Map<String, SearchHitField> fields = hit.getFields();
			BasicDBObject item = new BasicDBObject();
			String productId = ((String) fields.get(FIELD_PRODID).getValue());

			String skuId = ((String) fields.get(FIELD_SKUID).getValue());
			if (oReq.getBoolean("debug", false)) {
				item.append("explain", hit.explanation().toString());

				Set<String> fieldNames = fields.keySet();
				for (String fieldName : fieldNames) {
					item.append(fieldName, fields.get(fieldName) != null ? fields.get(fieldName).getValue() : null);
				}
			}
			int productTag = fields.get(FIELD_PRODTAG).getValue();
			String skuNo = fields.get(FIELD_SKUNO) != null ? (String) fields.get(FIELD_SKUNO).getValue() : null;
			item.append("id", productId);
			item.append("skuId", skuId);
			item.append("skuNo", skuNo);
			item.append("jdPrice", fields.get(FIELD_JD_PRICE) != null ? fields.get(FIELD_JD_PRICE).getValue() : null);
			item.append("jdTime", fields.get(FIELD_JD_TIME) != null ? fields.get(FIELD_JD_TIME).getValue() : null);
			item.append("productTag", productTag);
			item.append("salesVolume", fields.get(FIELD_SALESV) != null ? fields.get(FIELD_SALESV).getValue() : null);
			item.append("promoScore", fields.get(FIELD_PROMOSCORE) != null ? fields.get(FIELD_PROMOSCORE).getValue() : null);
			BasicDBObject shop = new BasicDBObject();
			if (productTag == 2) {
				item.append("shop", shop);
				shop.append("id", fields.get(FIELD_SHOPID) != null ? fields.get(FIELD_SHOPID).getValue() : null);
				shop.append("name", fields.get(FIELD_SHOPNAM) != null ? fields.get(FIELD_SHOPNAM).getValue() : null);
				shop.append("type", fields.get(FIELD_SHOPTYPE) != null ? fields.get(FIELD_SHOPTYPE).getValue() : null);
			}

			// int dragon = getnewStock(docScore);//getnewStock(skuNo, cityId);
			long startCache = System.currentTimeMillis();
			String content = ESDict.get(DYNAMIC_PREFIX, productId, true, jedis);
			cacheTime += System.currentTimeMillis() - startCache;

			if (EmptyUtil.notEmpty(content)) {
				long startParse = System.currentTimeMillis();
				BasicDBObject product = (BasicDBObject) JSON.parse(content);
				parseTime += System.currentTimeMillis() - startParse;
				BasicDBObject dynShop = (BasicDBObject) product.get("shop");
				if (EmptyUtil.notEmpty(dynShop)) {
					shop.putAll((Map<String, Object>) dynShop);
				}
				BasicDBList skus = (BasicDBList) product.get("skus");
				if (product.getBoolean("isMulti", false)) {
					skus = (BasicDBList) product.get("multiSkus");
				}
				if (skus != null) {
					for (int i = 0; i < skus.size(); i++) {
						BasicDBObject sku = (BasicDBObject) skus.get(i);
						if (skuId.equals(sku.getString("id", "-1"))) {
							product.put("skuIndex", i);
							break;
						}
					}
				}
				item.putAll((Map<?, ?>) product);
				item.append("skus", skus);
			}
			item.append("evaluateCount", fields.get(FIELD_EVALUATECOUNT) != null ? fields.get(FIELD_EVALUATECOUNT).getValue() : null);
			item.append("order", docOrder++);
			item.remove("facets");
			items.add(item);
		}
		oReq.append("t", new StringBuffer("&cache=").append(cacheTime).append("&parse=").append(parseTime).toString());
		return items;
	}



	@SuppressWarnings("unused")
	public BasicDBList readCategories(String defaultId, Map<String, Integer> counts, String pageName) {
		HashSet<String> parents = new HashSet<String>();
		HashSet<String> defaultNodes = new HashSet<String>();
		boolean hasCategory = !StringUtil.isEmpty(defaultId);
		if (hasCategory) {
			defaultNodes.add(defaultId);
		}
		for (Iterator<Entry<String, Integer>> i = counts.entrySet().iterator(); i.hasNext();) {
			Entry<String, Integer> entry = i.next();
			String catId = entry.getKey();
			BasicDBObject oCat = CategoryDict.getCategory(catId);
			if (oCat == null) {
				continue;
			}
			BasicDBObject oParent = CategoryDict.getCategory(oCat.getString("parentId"));
			boolean isDefault = hasCategory && defaultId.equalsIgnoreCase(catId);
			while (oParent != null && oParent.containsField("level") && oParent.getInt("level") >= 2) {
				if (isDefault) {
					defaultNodes.add(oParent.getString("catId"));
				}
				oParent = CategoryDict.getCategory(oParent.getString("parentId"));
			}
			if (oParent == null || !oParent.containsField("childs")) {
				continue;
			}
			parents.add(oParent.getString("catId"));
			if (isDefault) {
				defaultNodes.add(oParent.getString("catId"));
			}
		}
		BasicDBList results = new BasicDBList();
		for (Iterator<String> i = parents.iterator(); i.hasNext();) {
			String rootId = i.next();
			BasicDBObject oCat = CategoryDict.getCategory(rootId);
			int level = oCat.getInt("level", 1);
			if (level == 0)
				continue;
			BasicDBList list2 = (BasicDBList) oCat.get("childs");
			BasicDBList categoryOrder2 = ModuleAppDict.self.getCategoryOrder(rootId);
			if (categoryOrder2 == null) {
				categoryOrder2 = new BasicDBList();
			}

			String catId = oCat.getString("catId");
			String catalog = oCat.getString("catalog");
			int productTag = -1;
			if ("gome".equals(catalog)) {
				productTag = 1;
			} else if ("coo8".equals(catalog)) {
				productTag = 2;
			}

			// level-1
			BasicDBObject oResult = new BasicDBObject().append("count", 0);
			oResult.append("id", catId);
			oResult.append("name", oCat.getString("catName"));
			oResult.append("url", oCat.getString("url", ""));
			oResult.append("icon", oCat.getString("icon", ""));
			BasicDBList childList2 = new BasicDBList();
			if (defaultNodes.contains(rootId) == true) {
				oResult.append("isDefault", true);
			}
			oResult.append("childs", childList2);

			// level-2
			String id = null;
			int size2 = list2.size();
			for (int t = 0; t < size2; t++) {
				BasicDBObject oItem2 = (BasicDBObject) list2.get(t);
				id = oItem2.getString("catId");
				if (hasCategory == false && !counts.containsKey(id)) {
					continue;
				}
				long count = counts.containsKey(id) ? counts.get(id) : 0;
				BasicDBObject o = CategoryDict.getCategory(id);
				if (o != null) {
					oItem2.putAll((Map<String, Object>) o);
				}
				BasicDBObject oResult2 = new BasicDBObject();
				childList2.add(oResult2);
				oResult2.append("id", id);
				oResult2.append("name", oItem2.getString("catName"));
				int indexOf = categoryOrder2.indexOf(oItem2.getString("catId"));
				oResult2.append("order", indexOf == -1 ? 0 : size2 - indexOf);
				if (defaultNodes.contains(id)) {
					oResult2.append("isDefault", true);
				}

				// level-3
				BasicDBList childList3 = new BasicDBList();
				oResult2.append("childs", childList3);
				BasicDBList categoryOrder3 = ModuleAppDict.self.getCategoryOrder(id);
				if (categoryOrder3 == null) {
					categoryOrder3 = new BasicDBList();
				}
				BasicDBList list3 = (BasicDBList) oItem2.get("childs");
				if (list3 == null) {
					continue;
				}
				int size3 = list3.size();
				for (int k = 0; k < size3; k++) {
					BasicDBObject oItem3 = (BasicDBObject) list3.get(k);
					id = oItem3.getString("catId");
					if (hasCategory == false && !counts.containsKey(id)) {
						continue;
					}
					count = counts.containsKey(id) ? counts.get(id) : 0;
					BasicDBObject oResult3 = new BasicDBObject();
					childList3.add(oResult3);
					oResult3.append("id", id);
					indexOf = categoryOrder3.indexOf(oItem3.getString("catId"));
					oResult3.append("order", indexOf == -1 ? 0 : size3 - indexOf);
					oResult3.append("name", oItem3.getString("catName"));
					if (defaultNodes.contains(id)) {
						oResult3.append("isDefault", true);
					}
					oResult3.append("count", count);
					oResult2.append("count", oResult2.getInt("count", 0) + count);
				}
				oResult.append("count", oResult.getInt("count") + oResult2.getInt("count", 0));
				if (pageName.equalsIgnoreCase("search")) {
					DBObjectUtil.quickSortByNum(childList3, "isDefault", "count");
				} else if (pageName.equalsIgnoreCase("list")) {
					DBObjectUtil.quickSortByNum(childList3, "order");
				}
			}

			if (childList2 != null && childList2.size() > 0) {
				if (pageName.equalsIgnoreCase("search")) {
					DBObjectUtil.quickSortByNum(childList2, "count");
				} else if (pageName.equalsIgnoreCase("list")) {
					DBObjectUtil.quickSortByNum(childList2, "order");
				}
				oResult.append("count", ((BasicDBObject) childList2.get(0)).getInt("count", oResult.getInt("count")));
			} else {
				oResult.append("count", 0);
			}

			if (oResult.getInt("count") == 0 && pageName.equalsIgnoreCase("search")) {
				continue;

			}
			results.add(oResult);
		}
		if (pageName.equalsIgnoreCase("search")) {
			DBObjectUtil.quickSortByNum(results, "count");
		}
		return results;
	}

	@Override
	public String getCategoryId(BasicDBObject oReq, Object queryObj, String catalog, BasicDBObject oResult) {
		QueryBuilder queryBuilder = (QueryBuilder) queryObj;

		try {
			SearchResponse response = getQueryResponse(queryBuilder, oReq, oResult, null, false);

			List<Facet> facets = response.getFacets().facets();
			if (EmptyUtil.isEmpty(facets)) {
				return null;
			}
			for (Facet facetField : facets) {
				TermsFacet field = (TermsFacet) facetField;
				if (field.getEntries().size() == 0)
					continue;

				String fieldName = field.getName();
				if (fieldName.startsWith(catalog)) {
					Long categoryCount = null;
					String categoryId = null;
					String fakeCatId = null;
					for (TermsFacet.Entry entry : field.getEntries()) {
						long countLong = entry.getCount();
						categoryId = entry.getTerm().string();
						BasicDBObject oCat = CategoryDict.getCategory(categoryId);
						if (oCat == null) {
							logger.warn("CategoryDict.getCategory({}) is null", categoryId);
							continue;
						}
						int level = oCat.getInt("level", -1);
						if (level != 3) {
							continue;
						}
						if (categoryCount == null || categoryCount < countLong) {
							categoryCount = countLong;
							fakeCatId = categoryId;
						}
					}
					return fakeCatId;
				}
			}
		} catch (Exception e) {
			logger.error("getCategoryId,{}", e.getMessage());
		}
		return null;
	}

	protected boolean createMapping() {
		try {
			XContentBuilder mapping = XContentFactory
					.jsonBuilder()
					.startObject()
					.startObject(indexType)

					// .startObject("_source").field("compress",true).array("includes",
					// FIELD_TITLE,FIELD_PRICE,FIELD_DRAGON_C,FIELD_DRAGON_A,FIELD_WEIGHT,FIELD_EVALUATECOUNT).endObject()
					.startObject("_source").field("compress", true).endObject().startArray("dynamic_templates").startObject().startObject("facet_template").field("match", "f.*")
					.startObject("mapping").field("type", "string").field("index", "not_analyzed").endObject().field("match_mapping_type", "string").endObject().endObject().endArray()
					.startObject("properties").startObject(FIELD_SKUID).field("type", "string").field("store", true).field("index", "not_analyzed").endObject().startObject(FIELD_SKUNO)
					.field("type", "string").field("store", true).field("index", "not_analyzed").endObject().startObject(FIELD_SKUSTATE).field("type", "integer").field("store", true).endObject()
					.startObject(FIELD_PRODID).field("type", "string").field("store", true).field("index", "not_analyzed").endObject().startObject(FIELD_PRODTAG).field("type", "integer")
					.field("store", true).endObject().startObject(FIELD_PRODSTATE).field("type", "integer").field("store", true).endObject().startObject(FIELD_PROMOSCORE).field("type", "float")
					.field("store", true).endObject().startObject(FIELD_PRICE)
					.field("type", "float")
					.field("store", true)
					.endObject()

					.startObject(FIELD_SHOPID)
					.field("type", "string")
					.field("store", true)
					.field("index", "not_analyzed")
					.endObject()
					// 店铺商品首页排序
					.startObject(FIELD_SORTNO).field("type", "string").field("store", true).field("index", "not_analyzed").endObject()

					.startObject(FIELD_SHOPNAM).field("type", "string").field("store", true).field("index", "not_analyzed").endObject().startObject(FIELD_SHOPTYPE).field("type", "integer")
					.field("store", true).endObject().startObject(FIELD_EVALUATECOUNT).field("type", "integer").field("store", true).endObject().startObject(FIELD_SALESV).field("type", "integer")
					.field("store", true).endObject().startObject(FIELD_WEIGHT).field("type", "float").field("store", true).endObject().startObject(FIELD_STARTDATE).field("type", "long")
					.field("store", false).endObject()

					.startObject(FIELD_CATS).field("type", "string").field("store", false).field("index", "not_analyzed").endObject().startObject(FIELD_CATBRAND).field("type", "string")
					.field("store", false).field("analyzer", "whitespace").field("omit_norms", true).endObject().startObject(FIELD_TITLE).field("type", "string").field("store", false)
					.field("analyzer", "whitespace").field("omit_norms", true).field("similarity", "cbm25").endObject().startObject(FIELD_NGRAM).field("type", "string").field("store", false)
					.field("analyzer", "whitespace").field("omit_norms", true).endObject().startObject(FIELD_FACET).field("type", "string").field("store", false).field("analyzer", "whitespace")
					.field("omit_norms", true).endObject().startObject(FIELD_DRAGON_A).field("type", "string").field("store", false).field("analyzer", "whitespace").field("omit_norms", true)
					.endObject().startObject(FIELD_DRAGON_C).field("type", "string").field("store", false).field("analyzer", "whitespace").field("omit_norms", true).endObject().endObject()
					.endObject().endObject();

			PutMappingRequest mappingRequest = Requests.putMappingRequest(indexName).type(indexType).source(mapping);
			return client.admin().indices().putMapping(mappingRequest).actionGet().isAcknowledged();
		} catch (Exception e) {
			logger.error("create mapping  Fail.", e);
		}

		return true;

	}

	protected boolean createIndex() {
		try {
			IndicesExistsResponse indicesExistsResponse = client.admin().indices().prepareExists(indexName).execute().actionGet();
			if (indicesExistsResponse.isExists()) {
				return true;
			}
			Map<String,Object> setting = new HashMap<String,Object>();
			setting.put("index.number_of_shards", 4);
			setting.put("index.number_of_replicas", 0);
			setting.put("similarity.cbm25.type", "BM25");
			setting.put("similarity.cbm25.k1", 1.2f);
			setting.put("similarity.cbm25.b", 0.75f);
			setting.put("similarity.base.type", "simple");
			// setting.put("index.analysis.analyzer.default.type", "keyword");
			return client.admin().indices().prepareCreate(indexName).setSettings(setting).execute().actionGet().isAcknowledged();
		} catch (Exception e) {
			logger.error("create index Fail.", e);
		}
		return false;
	}

	protected void deleteDocByProductId(String productId, ShardedJedis jedis) {
		logger.info("delete productId={}", new String[] { productId });
		try {
			client.prepareDeleteByQuery(indexName).setQuery(QueryBuilders.termQuery(FIELD_PRODID, productId)).setTypes(indexType).execute();
			ESDict.delete(DYNAMIC_PREFIX, productId, jedis);
		} catch (Exception e) {
			logger.error("delete productId fail", e);
		}

	}

	protected void deleteDocBySkuId(String skuId) {

		logger.info("delete skuId={}", new String[] { skuId });
		try {
			client.prepareDelete(indexName, indexType, skuId).execute();
		} catch (Exception e) {
			logger.error("delete skuId", e);
		}

	}

	@SuppressWarnings({ "unchecked", "null" })
	@Override
	protected void addDoc(BasicDBObject product, ShardedJedis jedis) {

		BasicDBObject result = this.getDocData(product, jedis);
		if (result == null)
			return;
		List<BasicDBObject> docs = (List<BasicDBObject>) result.get("docs");
		if (docs == null && docs.size() == 0)
			return;
		for (BasicDBObject docData : docs) {
			XContentBuilder xcb = buildIndexParam(docData);
			if (xcb != null) {
				String skuId = docData.getString(FIELD_SKUID);
				client.prepareIndex(indexName, indexType, skuId).setSource(xcb).execute();
				logger.info(" add productId={}&skuId={}", new Object[] { docData.getString(FIELD_PRODID), FIELD_SKUID });
			}
		}

	}

	protected XContentBuilder buildIndexParam(BasicDBObject docData) {

		if (docData == null)
			return null;

		XContentBuilder xcb = null;
		try {
			xcb = XContentFactory.jsonBuilder().prettyPrint().startObject();
			for (Entry<String, Object> entry : docData.entrySet()) {
				xcb.field(entry.getKey(), entry.getValue());
			}
			xcb.endObject();
		} catch (Exception e) {
			logger.error("build BulkIndex skuid :" + docData.getString(FIELD_SKUID) + " Fail.", e);
		}

		return xcb;
	}

	@Override
	protected BasicDBObject buildBulkIndexParam(List<BasicDBObject> docs) {

		if (docs == null || docs.size() == 0)
			return null;

		BasicDBObject result = new BasicDBObject();
		List<String> productIds = new ArrayList<String>();
		List<IndexRequestBuilder> indexBuiders = new ArrayList<IndexRequestBuilder>();
		result.put("clearCache", productIds);
		result.put("docs", indexBuiders);

		for (BasicDBObject docData : docs) {
			XContentBuilder xcb = buildIndexParam(docData);
			if (xcb != null) {

				indexBuiders.add(client.prepareIndex(indexName, indexType).setId(docData.getString(FIELD_SKUID)).setSource(xcb));
				logger.info("add bulk productId={}&skuId={}", new Object[] { docData.getString(FIELD_PRODID), docData.getString(FIELD_SKUID) });

				String clearProId = StringUtil.append(DYNAMIC_PREFIX, "@", (String) docData.get(FIELD_PRODID), "@p");
				if (!productIds.contains(clearProId)) {
					productIds.add(clearProId);
				}

			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private void addArrayValue(BasicDBObject result, String field, Object value) {

		if (result == null)
			return;

		List<String> list = null;
		if (result.get(field) == null) {

			result.put(field, list = new ArrayList<String>());
		} else {
			list = (ArrayList<String>) result.get(field);
		}
		if (value instanceof String[]) {
			for (String v : (String[]) value) {
				list.add((String) v);
			}
		} else {
			list.add((String) value);
		}
	}

	public void writerText(String path, String content) {

		File dirFile = new File(path);

		if (!dirFile.exists()) {
			dirFile.mkdir();
		}

		try {
			BufferedWriter bw1 = new BufferedWriter(new FileWriter(path + "t.txt", true));
			bw1.write(content);
			bw1.flush();
			bw1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updatePromoTag() {

	}

	public static void main(String[] args) {
		ModuleProduct es = new ModuleProduct();
		es.writerText("/server/share/", "zhongwen乱麻");

		//
		// ModuleFactory.registerModule("mq", ModuleZeromq.class);
		// ModuleFactory.registerModule("subscribe", ModuleSubscribe.class);
		// // ModuleFactory.registerModule("jedis", Modulejedis.class, new
		// Object[] { Global.jedisAppPort });
		// ModuleFactory.registerModule("queue", ModuleQueue.class);
		// // ModuleFactory.registerModule("processor", ModuleProcessor.class);
		// ModuleFactory.registerModule("appdict", ModuleAppDict.class, new
		// Object[] {ESDict.class});
		// //
		// ModuleFactory.registerModule("productindex", ESProduct.class, new
		// Object[] { "product" ,"productType"});
		// // ModuleFactory.registerModule("ruledict", ModuleRuleDict.class);
		// ModuleFactory.registerLoadModule("appdict");
		// ModuleFactory.registerLoadModule("mq");
		// ModuleFactory.registerLoadModule("queue");
		// ModuleFactory.registerLoadModule("subscribe");
		// // ModuleFactory.registerLoadModule("socket");
		// // ModuleFactory.registerLoadModule("processor");
		// //
		// ModuleFactory.registerLoadModule("productindex");
		// // // ModuleFactory.registerLoadModule("ruledict");
		// ModuleFactory.loadModules();
		// ESProduct index = (ESProduct) ModuleFactory.productIndex();
		//
		// // RedisPool.init(new
		// String[]{"10.58.50.103","10.58.50.105","10.58.50.106","10.58.50.107","10.58.250.146"});
		// RedisPool.init();
		// ESDict.put("d", "12345", "111",false);
		// System.out.println(ESDict.get("d", "12345"));

		// BasicDBObject object = new BasicDBObject();
		// object.put("skuId", "1117010021");
		// object.put("promoScore", 0);
		// object.put("weight", 0);
		// // ModuleAppDict.self.putPromoScore(object.getString("skuId"), new
		// Float(object.getDouble("promoScore", 0.0d)));
		// index.updateFiled(object);

		// index.updateProductInfo("A0003375665","evaluateCount",1);

		// BasicDBList list=new BasicDBList();
		// list.add(new BasicDBObject("id","A0003375665").append("state", 5));
		// index.add(list);

		// List<String> keyWords=this.getData();

		// sshpass -p 'Dinghongbo-1' ssh dinghongbo@10.58.1.1
		// // String product=index.dynamicDB.get("A0004443725");
		// // BasicDBObject oProduct = (BasicDBObject) JSON.parse(product);
		// // index.add(oProduct);
		// // System.out.println("dd");
		//
		// DB db=MongodbUtils.getDB("10.58.50.55", 19753, "logs");
		// DBCollection coll=db.getCollection("dinghb_eff_log");
		//
		// int pageSize=500;
		// long total=coll.getCount();
		// long pageCount=total/pageSize+1;
		// DataResult result = new DataResult();
		// long totalTime=0;
		// for( int i=0;i<pageCount;i++){
		// System.out.println("cur page "+(i+1)+"/"+pageCount);
		// result.list.addAll(coll.find().limit(pageSize).skip(i*pageSize).toArray());
		// long start =System.currentTimeMillis();
		// index.add(result.list);
		// totalTime+=System.currentTimeMillis()-start;
		//
		// }
		// System.out.println(totalTime);

		// ESProduct pro=new ESProduct();
		// pro.client=ESClientUtils.getTransportClient();
		// pro.client.prepareDeleteByQuery(pro.indexName).setTypes(pro.indexType).setQuery(QueryBuilders.termQuery("c",
		// "aaa")).execute().actionGet();
		// if(pro.client==null||!pro.createIndex()||!pro.createMapping()){
		// }
		// pro.createIndex();
		// pro.createMapping();

		// BasicDBList list=new BasicDBList();
		// for(int i=0;i<10;i++){
		// List cats= new ArrayList<String>();
		// cats.add("100"+(i+1));
		// cats.add("100"+(i+1));
		// cats.add("1006"+(i+1));
		// BasicDBObject input=new BasicDBObject().append(FIELD_CATBRAND,
		// "测试 cat brand").append(FIELD_CATS,cats ).append(FIELD_DRAGON_A,
		// "a").append(FIELD_DRAGON_C, "aaa")
		// .append(FIELD_DY_PREFIX_F+"facetId.001.001",
		// "102ff").append(FIELD_DY_PREFIX_F+"facetId.001.002",
		// "101ff").append(FIELD_DY_PREFIX_FP,
		// "sanxing").append(FIELD_DY_PREFIX_FV,
		// "1mm22").append(FIELD_EVALUATECOUNT, 1)
		// .append(FIELD_FACET, "111 101 100").append(FIELD_NGRAM,
		// "ngram title sanxing shouji").append(FIELD_PRICE,
		// 1000f).append(FIELD_PRODID, "productId001")
		// .append(FIELD_PRODSTATE, 4).append(FIELD_PRODTAG,
		// 1).append(FIELD_PROMOSCORE, 22f).append(FIELD_SALESV,
		// 300).append(FIELD_SHOPID, "shopId001")
		// .append(FIELD_SHOPNAM, "店铺名称001").append(FIELD_SHOPTYPE,
		// 1).append(FIELD_SKUID, "skuId00"+i).append(FIELD_SKUNO,
		// "shuNo001").append(FIELD_SKUSTATE, 4)
		// .append(FIELD_STARTDATE, new Date().getTime()).append(FIELD_TITLE,
		// "产品标题 三星 手机 ").append(FIELD_WEIGHT, 12f);
		// pro.addDoc(input);
		// list.add(input);
		//
		//
		// }
		// pro.add(list);
		//
		// SearchResponse response=
		// pro.client.prepareSearch(pro.indexName).setTypes(pro.indexType)
		// // .setQuery(QueryBuilders.termQuery(FIELD_CATS, "B00100852"))
		// .setFilter(FilterBuilders.termFilter(FIELD_CATS, "B00100852"))
		// .addField("productId").execute().actionGet();
		// System.out.println(response.getHits().getHits()[0].getSource());
		// System.out.println(response.getHits().getTotalHits());
		// System.out.println(response.getHits().getHits()[0].getFields().get("productId").getValue());

		// BasicDBObject obj=pro.getProductIds("B00100852",0,10);
		// System.out.println(obj);

	}

	@Override
	public void updateMasloc(ArrayList<String> skuNos) {
		
	}

}
