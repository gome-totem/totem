/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.z.core.es.plugins.functionquery;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.ToStringUtils;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.ScoreFunction;
import org.z.core.es.plugins.ESProductScript;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;


public class CustomFunctionScoreQuery extends  Query{
    Query subQuery;
    final ScoreFunction function;
    float maxBoost = Float.MAX_VALUE;
    CombineFunction combineFunction;
    String regionId=null;
    boolean fake=true;
    
    public CustomFunctionScoreQuery(Query subQuery, ScoreFunction function) {
        this.subQuery = subQuery;
        this.function = function;
        this.combineFunction = function.getDefaultScoreCombiner();
    }

    public void setCombineFunction(CombineFunction combineFunction) {
        this.combineFunction = combineFunction;
    }
    
    public void setMaxBoost(float maxBoost) {
        this.maxBoost = maxBoost;
    }

    public float getMaxBoost() {
        return this.maxBoost;
    }

    public Query getSubQuery() {
        return subQuery;
    }

    public ScoreFunction getFunction() {
        return function;
    }
    public String setRegionId(String regionId){
        return this.regionId=regionId;
        
    }
    public String getRegionId(){
        return this.regionId;
    }
    public Boolean setFake(Boolean fake){
        return this.fake=fake;
        
    }
    public Boolean getFake(){
        return this.fake;
    }
    

    @Override
    public Query rewrite(IndexReader reader) throws IOException {
        Query newQ = subQuery.rewrite(reader);
        if (newQ == subQuery) {
            return this;
        }
        CustomFunctionScoreQuery bq = (CustomFunctionScoreQuery) this.clone();
        bq.subQuery = newQ;
        return bq;
    }

    @Override
    public void extractTerms(Set<Term> terms) {
        subQuery.extractTerms(terms);
    }

    @Override
    public Weight createWeight(IndexSearcher searcher) throws IOException {
        Weight subQueryWeight = subQuery.createWeight(searcher);
        return new CustomBoostFactorWeight(subQueryWeight);
    }

    class CustomBoostFactorWeight extends Weight {

        final Weight subQueryWeight;

        public CustomBoostFactorWeight(Weight subQueryWeight) throws IOException {
            this.subQueryWeight = subQueryWeight;
        }

        public Query getQuery() {
            return CustomFunctionScoreQuery.this;
        }

        @Override
        public float getValueForNormalization() throws IOException {
            float sum = subQueryWeight.getValueForNormalization();
            sum *= getBoost() * getBoost();
            return sum;
        }

        @Override
        public void normalize(float norm, float topLevelBoost) {
            subQueryWeight.normalize(norm, topLevelBoost * getBoost());
        }

        @Override
        public Scorer scorer(AtomicReaderContext context, boolean scoreDocsInOrder, boolean topScorer, Bits acceptDocs) throws IOException {
            // we ignore scoreDocsInOrder parameter, because we need to score in
            // order if documents are scored with a script. The
            // ShardLookup depends on in order scoring.
            Scorer subQueryScorer = subQueryWeight.scorer(context, true, false, acceptDocs);
            if (subQueryScorer == null) {
                return null;
            }
            function.setNextReader(context);
            return new CustomBoostFactorScorer(this, subQueryScorer, function, maxBoost, combineFunction);
        }

        @Override
        public Explanation explain(AtomicReaderContext context, int doc) throws IOException {
            Explanation subQueryExpl = subQueryWeight.explain(context, doc);
            if (!subQueryExpl.isMatch()) {
                return subQueryExpl;
            }
            String exp="";
            java.util.List<IndexableField> fieldss=context.reader().document(doc).getFields();
            for(int i=0;i<fieldss.size();i++){
                if (fieldss.get(i) instanceof StoredField){
                    
                    StoredField sf=(StoredField)fieldss.get(i);
                  
                    exp= sf.binaryValue().utf8ToString();
                   
                }
            }
            function.setNextReader(context);
            Explanation functionExplanation = function.explainScore(doc, subQueryExpl);
            
            Explanation functionExplanation1 =new Explanation();
            functionExplanation1.setValue(functionExplanation.getValue());
            functionExplanation1.setDescription(functionExplanation.getDescription());
            
            
            BasicDBObject dboDoc=(BasicDBObject)JSON.parse(exp);
            double promoScore=dboDoc.getDouble("promoScore");
            double weight=dboDoc.getDouble("weight");
            String skuNo=dboDoc.getString("skuNo");
//            int stock=ESProductScript.getStock(skuNo, regionId);
//            
//                if(fake){
//                     addExplainDetail(weight,10,stock,1000,promoScore,5,functionExplanation1);
//                }else{
//                     addExplainDetail(weight,100,stock,1000,promoScore,80,functionExplanation1);
//                      }
           
            return combineFunction.explain(getBoost(), subQueryExpl, functionExplanation1, maxBoost);
        }
    }

    private void addExplainDetail(double weight,int weightFactor,int stock,int stockFactor,double promoScore ,int promoScoreFactor,Explanation explSrc){
        
            double factor1=Math.sqrt(ESProductScript.div(weight, weightFactor));
            String factor2=ESProductScript.mul(stock, stockFactor);
            String factor3=ESProductScript.mul(promoScore, promoScoreFactor);
            float catScore=explSrc.getValue()-Float.valueOf(ESProductScript.sum(factor1, factor2,factor3));
            
            Explanation expWeight=new Explanation(Float.valueOf(String.valueOf(factor1)),new StringBuffer().append(" Math.sqrt(div(weight=").append(weight).append(",").append(weightFactor).append(")").toString());
            Explanation expstock=new Explanation(Float.valueOf(factor2),new StringBuffer().append(" mul(stock=").append(stock).append(",").append(stockFactor).append(")").toString());
            Explanation exppromoScore=new Explanation(Float.valueOf(factor3),new StringBuffer().append(" mul(promoScore=").append(promoScore).append(",").append(promoScoreFactor).append(")").toString());
         
            explSrc.addDetail(expWeight);
            explSrc.addDetail(expstock);
            explSrc.addDetail(exppromoScore);
            if(catScore!=0){
                explSrc.addDetail(new Explanation(catScore,new StringBuffer().append(" categorieScore").toString()));
                }
            
    }
    
    static class CustomBoostFactorScorer extends Scorer {

        private final float subQueryBoost;
        private final Scorer scorer;
        private final ScoreFunction function;
        private final float maxBoost;
        private final CombineFunction scoreCombiner;

        private CustomBoostFactorScorer(CustomBoostFactorWeight w, Scorer scorer, ScoreFunction function, float maxBoost, CombineFunction scoreCombiner)
                throws IOException {
            super(w);
            this.subQueryBoost = w.getQuery().getBoost();
            this.scorer = scorer;
            this.function = function;
            this.maxBoost = maxBoost;
            this.scoreCombiner = scoreCombiner;
        }

        @Override
        public int docID() {
            return scorer.docID();
        }

        @Override
        public int advance(int target) throws IOException {
            return scorer.advance(target);
        }

        @Override
        public int nextDoc() throws IOException {
            return scorer.nextDoc();
        }

        @Override
        public float score() throws IOException {
            float score = scorer.score();
            return scoreCombiner.combine(subQueryBoost, score,
                    function.score(scorer.docID(), score), maxBoost);
        }

        @Override
        public int freq() throws IOException {
            return scorer.freq();
        }

        @Override
        public long cost() {
            return scorer.cost();
        }
    }

    public String toString(String field) {
        StringBuilder sb = new StringBuilder();
        sb.append("function score (").append(subQuery.toString(field)).append(",function=").append(function).append(')');
        sb.append(ToStringUtils.boost(getBoost()));
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        CustomFunctionScoreQuery other = (CustomFunctionScoreQuery) o;
        return this.getBoost() == other.getBoost() && this.subQuery.equals(other.subQuery) && this.function.equals(other.function)
                && this.maxBoost == other.maxBoost;
    }

    public int hashCode() {
        return subQuery.hashCode() + 31 * function.hashCode() ^ Float.floatToIntBits(getBoost());
    }
}
