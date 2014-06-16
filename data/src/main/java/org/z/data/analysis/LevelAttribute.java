package org.z.data.analysis;

import org.apache.lucene.util.Attribute;

public interface LevelAttribute extends Attribute{
	
	public void setLevel(int level);
	
	public int  getLevel();

}
