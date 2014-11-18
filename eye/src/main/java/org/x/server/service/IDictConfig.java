package org.x.server.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.BasicDBObject;

public interface IDictConfig {

	void list(HttpServletRequest req, HttpServletResponse resp);

	void addServer(HttpServletRequest req, HttpServletResponse resp);

	void delServer(HttpServletRequest req, HttpServletResponse resp);

	void updateServer(HttpServletRequest req, HttpServletResponse resp);

	void checkData(HttpServletRequest req, HttpServletResponse resp);

	Map<String, Object> showServer(HttpServletRequest req, HttpServletResponse resp);

	BasicDBObject addFilter(HttpServletRequest req, HttpServletResponse resp);
	
	BasicDBObject initFilter(HttpServletRequest req, HttpServletResponse resp);

	BasicDBObject delFilter(HttpServletRequest req, HttpServletResponse resp);

	BasicDBObject ascFilter(HttpServletRequest req, HttpServletResponse resp);



}
