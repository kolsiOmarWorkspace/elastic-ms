package com.example.elasticms.service.Impl;


import com.example.elasticms.mapping.Article;
import com.example.elasticms.reo.ArticleRepository;
import com.example.elasticms.service.ArticleService;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


@Service
public class ArticleServiceImpl implements ArticleService {
    
    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    ElasticsearchOperations elasticsearchTemplate;


// Queries
    @Override
    public SearchHits<Article> findAllHists(int page, int size) {

//        GET enter_name_of_the_index_here/_search
//        {
//            "track_total_hits": true
//        }
        Pageable pageable = PageRequest.of(page, size);
        Query searchQuery = new NativeSearchQueryBuilder()
                .withTrackTotalHits(true).withPageable(pageable).build();

        SearchHits<Article> articleSearchHit = elasticsearchTemplate.search(searchQuery, Article.class, IndexCoordinates.of("article"));
        return articleSearchHit;
    }

//    Queries
    @Override
    public SearchHits<Article> findAllHistsByDateRange(LocalDate from, LocalDate to, int page, int size) {

//        GET enter_name_of_the_index_here/_search
//        {
//            "query": {
//            "Specify the type of query here": {
//                "Enter name of the field here": {
//                    "gte": "Enter lowest value of the range here",
//                            "lte": "Enter highest value of the range here"
//                }
//            }
//        }
//        }

        Pageable pageable = PageRequest.of(page, size);
        RangeQueryBuilder RangeQueryBuilder = QueryBuilders.rangeQuery("date").gte(from).lte(to);

        Query searchQuery = new NativeSearchQuery(RangeQueryBuilder).setPageable(pageable);
        SearchHits<Article> articleSearchHit = elasticsearchTemplate.search(searchQuery, Article.class, IndexCoordinates.of("article"));
        return articleSearchHit;
    }

    @Override
    public Map<String, Long> findAggregationByCategories(Pageable pageable) {
//        GET enter_name_of_the_index_here/_search
//        {
//            "aggs": {
//            "name your aggregation here": {
//                "specify aggregation type here": {
//                    "field": "name the field you want to aggregate here",
//                            "size": state how many buckets you want returned here
//                }
//            }
//        }
//        }
//        Query searchQuery = new NativeSearchQueryBuilder().addAggregation(AggregationBuilders.terms("by_category").field("category")).build();
        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .addAggregation(AggregationBuilders.terms("by_category").field("category").order(BucketOrder.key(true)).size(1000))
                .build();
        Aggregations aggregationsContainer =(Aggregations) elasticsearchTemplate.search(searchQuery, Article.class, IndexCoordinates.of("article")).getAggregations().aggregations();

        Map<String, Long> result = new HashMap<>();
        aggregationsContainer.asList().forEach(aggregation -> {
            ((Terms) aggregation).getBuckets()
                    .forEach(bucket -> result.put(bucket.getKeyAsString(), bucket.getDocCount()));
        });
        return result;
    }

    @Override
    public SearchPage<Article> findQueryMatchByCategory(int page, int size, String field, String filter, boolean precision) {

//        GET enter_name_of_the_index_here/_search
//        {
//            "query": {
//            "match": {
//                "Enter the name of the field": "Enter the value you are looking for"
//            }
//        },
//            "aggregations": {
//            "Name your aggregation here": {
//                "significant_text": {
//                    "field": "Enter the name of the field you are searching for"
//                }
//            }
//        }
//        }


//{
//  "query": {
//    "match": {
//      "Specify the field you want to search": {
//        "query": "Enter search terms",
//        "operator": "and" / "or"
//      }
//    }
//  }
//}
        Pageable pageable = PageRequest.of(page, size);
        Query query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery(field, filter).operator(precision ? Operator.AND : Operator.OR))
                .withAggregations(AggregationBuilders.terms("by_category").field("category").order(BucketOrder.key(true)).size(1000)).withPageable(pageable).build();
        SearchHits<Article> searchHits =  elasticsearchTemplate.search(query, Article.class, IndexCoordinates.of("article"));
//        Pa
        SearchPage<Article> searchPage = SearchHitSupport.searchPageFor(searchHits, pageable);
        return searchPage;
    }
}
