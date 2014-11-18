package org.x.server.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.gome.totem.sniper.util.StringUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class Tool {

	public static BasicDBObject parseSku(BasicDBObject oResult) {
		if(oResult == null) return null;
		BasicDBObject product = (BasicDBObject) oResult.get("response");
		if(product == null) return null;
		product = (BasicDBObject) product.get("results");
		if(product == null) return null;
		BasicDBList products = (BasicDBList) product.get("products");
		if(products == null) return null;
		
		if(products.size() > 0){
			product = (BasicDBObject) products.get(0);
			if(product == null) return null;
			
			BasicDBList categories = (BasicDBList) product.get("categories");
			if(categories == null) return null;
			String catId = "", catName = "";
			
			BasicDBList cates = new BasicDBList();
			for (int i = 0, len = categories.size(); i < len; i++) {
				BasicDBObject categroy = (BasicDBObject) categories.get(i);
				String catalog = categroy.getString("catalog","");
				if(catalog.equalsIgnoreCase("shop")){
					continue;
				}else if(catalog.equalsIgnoreCase("coo8")){
					catId = categroy.getString("mappingCatId", "");
					catName = categroy.getString("catName", "");
				}else{
					catId = categroy.getString("catId", "");
					catName = categroy.getString("catName", "");
				}
				
				cates.add(new BasicDBObject().append("catId", catId).append("catName", catName));
			}
			
			int skuIndex = product.getInt("skuIndex", 0);
			products = (BasicDBList) product.get("skus");
			
			product = (BasicDBObject) products.get(skuIndex);
			
			String img = getDefaultImg((BasicDBList)product.get("imgs"), false);
			product.append("img", img).append("cates", cates);
			return new BasicDBObject().append("product", product);
		}else{
			return null;
		}
	}

	private static String getDefaultImg(BasicDBList skuImgs, boolean isGomeart) {
		String skuImgUrl = "";
		List<String> skuImgUrls = new ArrayList<String>();
		for (int k = 0; skuImgs != null && k < skuImgs.size(); k++) {
			BasicDBObject skuImg = (BasicDBObject) skuImgs.get(k);
			String skuImgNo = skuImg.getString("no", "");
			if (!StringUtils.isEmpty(skuImgNo)) {
				int no = Integer.parseInt(skuImgNo);
				if (no == 1)
					skuImgUrls.add(skuImg.getString("url", skuImg.getString("url")));
			}
		}

		if (isGomeart && skuImgUrls != null && skuImgUrls.size() > 0) {
			skuImgUrl = skuImgUrls.get(0);
		} else if (skuImgUrls != null && skuImgUrls.size() > 0)
			skuImgUrl = StringUtil.randomImgServerName() + skuImgUrls.get(0);
		return skuImgUrl;
	}
	
	public static boolean isSuccessed(BasicDBObject result){
		if(result != null){
			result = (BasicDBObject) result.get("response");
			if(result != null){
				String str = result.getString("_id", "");
				if(!StringUtils.isEmpty(str)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static void main(String[] args) {
		System.out.println(Integer.MAX_VALUE);
	}
}
