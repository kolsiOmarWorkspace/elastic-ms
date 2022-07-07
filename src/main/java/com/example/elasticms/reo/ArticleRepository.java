package com.example.elasticms.reo;

import com.example.elasticms.mapping.Article;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ArticleRepository extends ElasticsearchRepository<Article, String> {

    @Query("{\n" +
            "\"aggregations\": {\n" +
            "    \"by_category\": {\n" +
            "      \"terms\": {\n" +
            "        \"field\": \"category\",\n" +
            "        \"size\": 1000,\n" +
            "        \"min_doc_count\": 1,\n" +
            "        \"shard_min_doc_count\": 0,\n" +
            "        \"show_term_doc_count_error\": false,\n" +
            "        \"order\": [\n" +
            "          {\n" +
            "            \"_count\": \"desc\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"_key\": \"asc\"\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "}\n")
    public SearchHits<Article> findAgg();
}
