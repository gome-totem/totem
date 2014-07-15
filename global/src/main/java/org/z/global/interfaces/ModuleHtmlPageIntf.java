package org.z.global.interfaces;


import org.apache.velocity.Template;

import com.mongodb.BasicDBObject;


public interface ModuleHtmlPageIntf extends ModuleIntf {

	public boolean isAuthPage(String pageName);

	public Template getTemplate(String pageName);

	public Class<?> classBy(String name);

	public BasicDBObject pageBy(String id);

	public BasicDBObject scriptBy(String id);

	public BasicDBObject styleBy(String id);

}
