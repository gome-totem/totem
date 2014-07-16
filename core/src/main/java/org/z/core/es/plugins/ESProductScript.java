
package org.z.core.es.plugins;


import java.math.BigDecimal;
import java.util.Map;

import org.elasticsearch.index.fielddata.ScriptDocValues.Strings;
import org.elasticsearch.script.AbstractFloatSearchScript;
import org.z.core.module.ModuleProduct;
import org.z.global.util.StringUtil;

import com.mongodb.BasicDBList;

/**
 * script for product sort
 * @author dinghb
 *
 */
public class ESProductScript extends AbstractFloatSearchScript {
    
    private boolean comMode;
    private boolean fake;
    private String fakeCatId;
    
    
    
    public ESProductScript(Map<String, Object> params){
    	
        this.fake=(Boolean)params.get(ModuleProduct.PARAM_FAKE);
        this.comMode=(Boolean)params.get(ModuleProduct.PARAM_COMMODE);
        this.fakeCatId=(String)params.get("fakeCatId");
    }

    @Override
    public float runAsFloat() {
        float result=10;
        double promScore= this.docFieldDoubles("promoScore").isEmpty()?0:this.docFieldDoubles("promoScore").getValue();
        double weight= this.docFieldDoubles("weight").isEmpty()?0:this.docFieldDoubles("weight").getValue();
        int stock	=1;//this.getStock(skuNo);
        
        String addtionalScore="0";
        Strings  categories=this.docFieldStrings("categories");
       
        if(!categories.isEmpty()&&categories.getValues().contains(fakeCatId)){
        		addtionalScore="100";
        	}
        if(comMode){
        		if(fake){
        			result=Float.valueOf(sum(Math.sqrt(div(weight,10)),mul(stock, 1000),mul(promScore,5),addtionalScore));
        			
        		}else{
        			result=Float.valueOf(sum(Math.sqrt(div(weight,100)),mul(stock, 1000),mul(promScore,80)));
        		}
        	}
        return 0;
    }
    
    
    public static String sum(double v1, String ...vns ) {
    	
		BigDecimal sum = new BigDecimal(v1);
		for(String vn:vns){
			sum=sum.add(new BigDecimal(vn));
		}
		return sum.setScale(10, BigDecimal.ROUND_HALF_UP).toString();
	} 
	
	public static double div(double v1, int v2) {
		
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		
		return b1.divide(b2).doubleValue();
	} 
	
	public static String mul(int v1, int v2) {
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.multiply(b2).toString();
	} 
	public static String mul(double v1, int v2) {
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.multiply(b2).toString();
	} 
	public static String mul(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.multiply(b2).toString();
	} 
	
	public static void main(String[] args) {
	System.out.println(Float.valueOf(sum(Math.sqrt(div(1,10)),mul(1, 50),mul(1,5))));
	
	}

	
}
