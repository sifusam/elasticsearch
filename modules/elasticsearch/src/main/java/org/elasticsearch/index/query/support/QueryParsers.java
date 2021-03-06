/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.elasticsearch.index.query.support;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.lucene.search.AndFilter;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.query.QueryParseContext;

/**
 * @author kimchy (shay.banon)
 */
public final class QueryParsers {

    private QueryParsers() {

    }

    public static MultiTermQuery.RewriteMethod parseRewriteMethod(@Nullable String rewriteMethod) {
        if (rewriteMethod == null) {
            return MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT;
        }
        if ("constant_score_auto".equals(rewriteMethod) || "constant_score_auto".equals(rewriteMethod)) {
            return MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT;
        }
        if ("scoring_boolean".equals(rewriteMethod) || "scoringBoolean".equals(rewriteMethod)) {
            return MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE;
        }
        if ("constant_score_boolean".equals(rewriteMethod) || "constantScoreBoolean".equals(rewriteMethod)) {
            return MultiTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE;
        }
        if ("constant_score_filter".equals(rewriteMethod) || "constantScoreFilter".equals(rewriteMethod)) {
            return MultiTermQuery.CONSTANT_SCORE_FILTER_REWRITE;
        }
        if (rewriteMethod.startsWith("top_terms_")) {
            int size = Integer.parseInt(rewriteMethod.substring("top_terms_".length()));
            return new MultiTermQuery.TopTermsScoringBooleanQueryRewrite(size);
        }
        if (rewriteMethod.startsWith("topTerms")) {
            int size = Integer.parseInt(rewriteMethod.substring("topTerms".length()));
            return new MultiTermQuery.TopTermsScoringBooleanQueryRewrite(size);
        }
        if (rewriteMethod.startsWith("top_terms_boost_")) {
            int size = Integer.parseInt(rewriteMethod.substring("top_terms_boost_".length()));
            return new MultiTermQuery.TopTermsBoostOnlyBooleanQueryRewrite(size);
        }
        if (rewriteMethod.startsWith("topTermsBoost")) {
            int size = Integer.parseInt(rewriteMethod.substring("topTermsBoost".length()));
            return new MultiTermQuery.TopTermsBoostOnlyBooleanQueryRewrite(size);
        }
        throw new ElasticSearchIllegalArgumentException("Failed to parse rewrite_method [" + rewriteMethod + "]");
    }

    public static Query wrapSmartNameQuery(Query query, @Nullable MapperService.SmartNameFieldMappers smartFieldMappers,
                                           QueryParseContext parseContext) {
        if (query == null) {
            return null;
        }
        if (smartFieldMappers == null) {
            return query;
        }
        if (!smartFieldMappers.hasDocMapper() || !smartFieldMappers.explicitTypeInName()) {
            return query;
        }
        DocumentMapper docMapper = smartFieldMappers.docMapper();
        return new FilteredQuery(query, parseContext.cacheFilter(docMapper.typeFilter(), null));
    }

    public static Filter wrapSmartNameFilter(Filter filter, @Nullable MapperService.SmartNameFieldMappers smartFieldMappers,
                                             QueryParseContext parseContext) {
        if (smartFieldMappers == null) {
            return filter;
        }
        if (!smartFieldMappers.hasDocMapper() || !smartFieldMappers.explicitTypeInName()) {
            return filter;
        }
        DocumentMapper docMapper = smartFieldMappers.docMapper();
        return new AndFilter(ImmutableList.of(parseContext.cacheFilter(docMapper.typeFilter(), null), filter));
    }
}
