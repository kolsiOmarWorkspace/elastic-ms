package com.example.elasticms.service;

import com.example.elasticms.mapping.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ArticleService {
    SearchHits<Article> findAllHists(int page, int size);

    SearchHits<Article> findAllHistsByDateRange(LocalDate from, LocalDate to, int page, int size);

    Map<String, Long> findAggregationByCategories(Pageable pageable);

    SearchPage<Article> findQueryMatchByCategory(int page, int size, String field, String filter, boolean precision);

    SearchPage<Article> findQueryMatchPhraseByHeadline(int page, int size, String field, String filter);

    SearchPage<Article> findQueryMultiMatchByHeadline(int page, int size, List<String> field, String filter,String boost_field);
}
