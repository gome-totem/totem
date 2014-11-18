package org.x.server.service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.BasicDBObject;

public interface IPromotion {
	public boolean checkCategory(String catId);
	public boolean checkScore(String catId, float score );
	public void dosearch(HttpServletRequest req, HttpServletResponse resp, boolean isHomeCat) throws IOException;
	public void setScore(String skuId, String catId, float score, boolean isHomeCat, HttpServletResponse resp) throws IOException;
	public void getCategory(HttpServletRequest req, HttpServletResponse resp) throws IOException;
	public void getPromoScore(HttpServletRequest req, HttpServletResponse resp, boolean isHomeCat) throws IOException;
	public BasicDBObject dataCheck(String skuId, String catId, float score, boolean isHomeCat);
	public void delScore(String skuId, String catId, HttpServletResponse resp);
}
