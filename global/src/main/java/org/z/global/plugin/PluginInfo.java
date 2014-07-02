package org.z.global.plugin;

import java.util.List;

import org.z.global.io.Strings;


public class PluginInfo {
    public static final String DESCRIPTION_NOT_AVAILABLE = "No description found.";
    public static final String VERSION_NOT_AVAILABLE = "NA";
    
    private String name;
    private String description;
    private boolean site;
    private boolean jvm;
    private String version;
    private boolean isolation;

    private List<PluginInfo> infos;
    public PluginInfo() {
    }
    
    public PluginInfo(String name, String description, boolean site, boolean jvm, String version, boolean isolation) {
        this.name = name;
        this.description = description;
        this.site = site;
        this.jvm = jvm;
        if (Strings.hasText(version)) {
            this.version = version;
        } else {
            this.version = VERSION_NOT_AVAILABLE;
        }
        this.isolation = isolation;
    }
    
    public void add(PluginInfo info){
    	this.infos.add(info);
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isSite() {
		return site;
	}

	public void setSite(boolean site) {
		this.site = site;
	}

	public boolean isJvm() {
		return jvm;
	}

	public void setJvm(boolean jvm) {
		this.jvm = jvm;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isIsolation() {
		return isolation;
	}

	public void setIsolation(boolean isolation) {
		this.isolation = isolation;
	}
    
    
    

}
