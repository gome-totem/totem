package org.z.data.analysis;

import org.apache.lucene.util.AttributeImpl;

public final class LevelAttributeImpl extends AttributeImpl implements LevelAttribute {
	private static int level=0; 
	public LevelAttributeImpl() {}
	
	@SuppressWarnings("static-access")
	public void setLevel(int level) {
		this.level=level;
	}
	@Override
	public int  getLevel() {
		return level;
	}
	@Override
	public void clear() {
		level =0;
	}
	@Override
	public void copyTo(AttributeImpl target) {
		LevelAttribute attr = (LevelAttribute) target;
		    attr.setLevel(level);
	}

}
