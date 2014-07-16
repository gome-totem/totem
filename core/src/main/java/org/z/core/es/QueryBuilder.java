package org.z.core.es;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.script.ScriptScoreFunctionBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacetBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.es.plugins.functionquery.CustomFunctionScoreQueryBuilder;
import org.z.core.module.ModuleProduct;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
public class QueryBuilder {
	protected static Logger logger = LoggerFactory.getLogger(QueryBuilder.class);
	public enum FilterType{
		TERM,RANGE,NOT,OR,FILTER
	}
	private final String [] commFields= {"productId", "skuId", "productTag", "skuNo","salesVolume","promoScore"
			,"shopId","shopName","shopType","evaluateCount"};
	private List<FilterBuilder> filters= Lists.newArrayList();
//	private AndFilterBuilder andFilterBuilder;
	BoolFilterBuilder andFilterBuilder;
	private StringBuilder querySql;
	private SearchRequestBuilder searchBuilder;
	private Integer facetSize=200;//默认所有facet 最大200
	private ScriptScoreFunctionBuilder functionBuilder;
	private static final String defaultScript="ModuleProduct";
	private boolean debug=false;
	private List<String> returnFields=null;
	private Boolean fake=null;
	private String regionId=null;
	
	public QueryBuilder(){
		//searchBuilder=ESClientUtils.getTransportClient().prepareSearch().setPreference("_local");
		searchBuilder=ESClientUtils.getTransportClient().prepareSearch().setPreference("_local").setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
	}

	public QueryBuilder setScript(String script){
		if(functionBuilder==null)
			functionBuilder=ScoreFunctionBuilders.scriptFunction(StringUtils.isEmpty(script)?defaultScript:script);
		
		return this;
	}
	public QueryBuilder setScriptParm(String key,Object value){
		
		setScript(null);
		functionBuilder.param(key, value);
		return this;
	}
	public QueryBuilder setFacetSize(Integer size){
		this.facetSize=size;
		return this;
	}
	
	public QueryBuilder from(int from){
		this.searchBuilder.setFrom(from);
		return this;
	}
	
	public QueryBuilder size(int size){
		this.searchBuilder.setSize(size);
		return this;
	}
	
	public QueryBuilder sort(String field, SortOrder order){
		this.searchBuilder.addSort(SortBuilders.fieldSort(field).order(order).missing("_last"));
		return this;
	}
	public QueryBuilder regionId(String regionId){
		this.regionId=regionId;
		return this;
	}
	public QueryBuilder fake(boolean fake){
		this.fake=fake;
		return this;
	}
	
	public QueryBuilder addTermFilter(String field,Object value){
		
		return addFilter(FilterType.TERM,field,value,null,null,null,null);
			
	}
	public QueryBuilder addNotFilter(String field,Object value){
		
		return addFilter(FilterType.NOT,field,value,null,null,null,null);
			
	}
	/**
	 * 格式 [1 to 100]
	 * @param range
	 * @return
	 */
	public QueryBuilder addRangeFilter(String field,String range){
		
		range=range.replace("[", "").replace("]", "").replace("TO ", "").replace("*", String.valueOf(Integer.MAX_VALUE));
		String[] rangeArr=range.split(" ");
		if(rangeArr!=null&&rangeArr.length==2)
			return addFilter(FilterType.RANGE,field,rangeArr[0],rangeArr[1],null,null,null);
		else 
			return this;	
	}
	public QueryBuilder addRangeFilter(String field,Object from,Object to){
		
		return addFilter(FilterType.RANGE, field, from, to, null, null, null);
			
	}
	public QueryBuilder addOrFilter(String field,List orValues){
		
		return addFilter(FilterType.OR,field,null,null,null,orValues,null);
			
	}
	public QueryBuilder addOrFilter(BasicDBObject orfilters){
		
		return addFilter(FilterType.OR,null,null,null,null,null,orfilters);
			
	}
	public QueryBuilder addFilter(FilterBuilder filter){
		
		return addFilter(FilterType.FILTER,null,null,null,filter,null,null);
	}
	 
	public QueryBuilder addFilter(FilterType type,String  field,Object value,Object toValue,FilterBuilder filter,List orValues ,BasicDBObject orFilters) {
		
		if(andFilterBuilder==null){
//			andFilterBuilder= FilterBuilders.andFilter().cache(true);
			andFilterBuilder=FilterBuilders.boolFilter().cache(true);
//			searchBuilder.setPostFilter(andFilterBuilder);
		}
		switch(type){
			case TERM:
				filters.add(FilterBuilders.termFilter(field, value));
				 break;
			case RANGE:
				filters.add(FilterBuilders.rangeFilter(field).from(value).to(toValue));
				break;
			case NOT:
//				filters.add(FilterBuilders.notFilter(FilterBuilders.termFilter(field, value)).cache(true));
				filters.add(FilterBuilders.boolFilter().cache(true).mustNot(FilterBuilders.termFilter(field, value)).cache(true));
				break;
			case OR:
				
				if(orValues==null||orValues.size()<1){
					if(orFilters!=null&&orFilters.size()>0)
					 filters.add(buildOrFilter(orFilters));
					else
					 return this;
				}else{
					filters.add(buildOrFilter(field,orValues));
				}
				break;
			case FILTER:
				filters.add(filter);
				break;
		}
		return this;
	}
	
	public BoolFilterBuilder buildOrFilter(String field ,List orValues){
		BoolFilterBuilder  orFilter=FilterBuilders.boolFilter().cache(true);
		for(Object obj:orValues ){
			orFilter.should(FilterBuilders.termFilter(field, obj));
		}
//		OrFilterBuilder  orFilter=FilterBuilders.orFilter().cache(true);
//		for(Object obj:orValues ){
//			orFilter.add(FilterBuilders.termFilter(field, obj));
//		}
		return orFilter;
	}
	public BoolFilterBuilder buildOrFilter(BasicDBObject orFilters){
		
		BoolFilterBuilder  orFilter=FilterBuilders.boolFilter().cache(true);
		for(Map.Entry entry : orFilters.entrySet()) {  
			orFilter.should(FilterBuilders.termFilter((String)entry.getKey(), entry.getValue()));
        } 
//		OrFilterBuilder  orFilter=FilterBuilders.orFilter().cache(true);
//		
//		for(Map.Entry entry : orFilters.entrySet()) {  
//			orFilter.add(FilterBuilders.termFilter((String)entry.getKey(), entry.getValue()));
//        } 
		
		return orFilter;
	}
	
	public QueryBuilder addFacet(String ... fields){
		
		if(fields==null||fields.length<1)
			return this;
		
		for(String field:fields)
			 this.addFacet(field,true,null);
		
		 return this;
	}
	
	public QueryBuilder addFacet(String field,boolean filtered,Integer size){
		
		TermsFacetBuilder termfacet=FacetBuilders.termsFacet(field).field(field);
		termfacet.size(size!=null&&size!=0?size:this.facetSize);
		
//		if(filtered){
//			termfacet.facetFilter(andFilterBuilder);
//		}
		searchBuilder.addFacet(termfacet);
		return this;
	}
	
	
	public QueryBuilder setQuery(org.elasticsearch.index.query.QueryBuilder query){
		searchBuilder.setQuery(query);
		return this;
	}
	public QueryBuilder addField(String field){
		searchBuilder.addField(field);
		return this;
	}
	public QueryBuilder setIndices(String ... indices){
		searchBuilder.setIndices(indices);
		return this;
	}
	public QueryBuilder setTypes(String ... types){
		searchBuilder.setTypes(types);
		return this;
	}
	public QueryBuilder removeFilter(FilterBuilder filter){
		filters.remove(filter);
		return this;
	}
	public QueryBuilder setExplain(boolean enabled){
		searchBuilder.setExplain(enabled);
		debug=true;
		return this;
	}
	
	public SearchResponse get(){
		
		return this.get(null);
	}
	public QueryBuilder setQuery(String sql){
		
		if(!StringUtils.isEmpty(sql)){
			if(functionBuilder==null&&!sql.contains(ModuleProduct.SQL_ALL)){
				searchBuilder.setQuery(QueryBuilders.queryString(sql));
			}else{
				CustomFunctionScoreQueryBuilder functionQuery=null;
				if(sql.contains(ModuleProduct.SQL_ALL)){
					 functionQuery=  new CustomFunctionScoreQueryBuilder(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), andFilterBuilder) ).add(functionBuilder.lang("native")).boostMode("sum");
				}else{
					 functionQuery=  new CustomFunctionScoreQueryBuilder(QueryBuilders.filteredQuery(QueryBuilders.queryString(sql), andFilterBuilder)   ).add(functionBuilder.lang("native")).boostMode("sum");
				}
				if(fake!=null){
					functionQuery.fake(fake);
				}
				if(!StringUtils.isEmpty(regionId)){
					functionQuery.regionId(regionId);
				}
				
				searchBuilder.setQuery(functionQuery);
			}
		}
		return this;
	}
	public SearchResponse get(String query){
		
		if(filters!=null&&filters.size()>0){
			if(filters.size()>0){
				for(FilterBuilder filter:filters){
					andFilterBuilder.must(filter);
				}
			}
		}
		
		setQuery(query);
		
		if(returnFields==null){
			searchBuilder.addFields(commFields);
		}
		
		if(debug){
			logger.info("search DSL: [{}]",new Object[]{searchBuilder.toString()});
			debug=false;
		}
		return searchBuilder.get();
	}

}
