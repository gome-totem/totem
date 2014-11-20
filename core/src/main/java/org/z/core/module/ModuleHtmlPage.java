package org.z.core.module;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.environment.Config;
import org.z.global.environment.Const;
import org.z.global.interfaces.ModuleHtmlPageIntf;
import org.z.global.ip.IPSeeker;
import org.z.global.zk.ServerDict;

import com.mongodb.BasicDBObject;

public class ModuleHtmlPage implements ModuleHtmlPageIntf {
    protected static final Logger logger = LoggerFactory.getLogger(ModuleHtmlPage.class);
    public ConcurrentHashMap<String, Template> templates = new ConcurrentHashMap<String, Template>();
    public VelocityEngine ve = null;
    public String HomePage = Config.rock().getItem("HomePage", "index");
    public String HtmlPagePath = null;
    public ConcurrentHashMap<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();

    @Override
    public boolean init(boolean isReload) {
        if (isReload == true) {
            return true;
        }
        logger.info("init IP Dict");
        HtmlPagePath = Const.ConfigPath + "htmls/";
        IPSeeker.init();
        logger.info("init Page Engine[{}]", new Object[] { HtmlPagePath });
        try {
            ve = new VelocityEngine();
            ve.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, HtmlPagePath);
            ve.init();
        } catch (Exception e) {
            logger.error("init", e);
            return false;
        }
        return true;
    }

    @Override
    public void afterCreate(Object[] params) {
    }

    @Override
    public void start(boolean isReload) {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public String getId() {
        return "htmlpage";
    }

    public String cityByIP(String ip) {
        String v = IPSeeker.getCityNameByIP(ip);
        return v;
    }

    public void refreshPage(String pageName, BasicDBObject oPage) {
        BasicDBObject old = ServerDict.self.pageBy(pageName);
        if (old == null || !old.getString("version").equalsIgnoreCase(oPage.getString("version"))) {
            templates.remove(pageName);
        }
    }

    public void removePage(String pageName) {
        templates.remove(pageName);
    }

    @Override
    public synchronized Template getTemplate(String pageName) {
        String fileName = HtmlPagePath + pageName + ".htm";
        File f = new File(fileName);
        Template t = templates.get(pageName);
        if (t == null || f.lastModified() != t.getLastModified()) {
            t = loadTemplate(pageName, f.lastModified());
        }
        return t;
    }

    private Template loadTemplate(String pageName, long timestamp) {
        Template template = null;
        try {
            String fileName = pageName + ".htm";
            template = ve.getTemplate(fileName, "utf-8");
            template.setLastModified(timestamp);
            templates.put(pageName, template);
        } catch (Exception e) {
            logger.error("loadTemplate", e);
        }
        return template;
    }

    @Override
    public boolean isAuthPage(String pageName) {
        BasicDBObject oPage = ServerDict.self.pageBy(pageName);
        if (oPage == null) {
            return false;
        }
        return oPage.getBoolean("auth", false);
    }

    @Override
    public Class<?> classBy(String name) {
        try {
            Class<?> c = classes.get(name);
            if (c == null) {
                c = Class.forName(name);
                classes.put(name, c);
            }
            return c;
        } catch (Exception e) {
            logger.error("classBy", e);
            return null;
        }
    }

    @Override
    public BasicDBObject pageBy(String id) {
        return ServerDict.self.pageBy(id);
    }

    @Override
    public BasicDBObject scriptBy(String id) {
        return ServerDict.self.scriptBy(id);
    }

    @Override
    public BasicDBObject styleBy(String id) {
        return ServerDict.self.styleBy(id);
    }

}





