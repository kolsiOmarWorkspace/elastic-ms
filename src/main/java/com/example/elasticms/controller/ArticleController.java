package com.example.elasticms.controller;

import com.example.elasticms.mapping.Article;
import com.example.elasticms.reo.ArticleRepository;
import com.example.elasticms.service.ArticleService;
import com.example.elasticms.service.Impl.ArticleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "article")
public class ArticleController {

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    ArticleService articleService;

    @RequestMapping(path = "all", method = RequestMethod.GET)
    public SearchHits<Article> findAll(Pageable pageable) {
        return articleService.findAllHists(pageable.getPageNumber(), pageable.getPageSize());
    }

    @RequestMapping(path = "date_range", method = RequestMethod.GET)
    public SearchHits<Article> findByDateRange(@RequestParam String from, @RequestParam String to, Pageable pageable) {
        System.out.println(pageable.getPageNumber() + "  " +  pageable.getPageSize());
        return articleService.findAllHistsByDateRange(LocalDate.parse(from), LocalDate.parse(to), pageable.getPageNumber(), pageable.getPageSize());
    }

    @RequestMapping(path = "agg_by_category", method = RequestMethod.GET)
    public Map<String, Long> agg_by_category(Pageable pageable) {
        return articleService.findAggregationByCategories(pageable);
    }

    @RequestMapping(path = "match_by_filed", method = RequestMethod.GET)
    public SearchPage<Article>  match_by_filed(Pageable pageable, @RequestParam String field, @RequestParam String filter, @RequestParam boolean precision) {
        return articleService.findQueryMatchByCategory(pageable.getPageNumber(), pageable.getPageSize(), field, filter, precision);
    }

    @RequestMapping(path = "match_phrase_by_filed", method = RequestMethod.GET)
    public SearchPage<Article>  match_phrase_by_filed(Pageable pageable, @RequestParam String field, @RequestParam String filter) {
        return articleService.findQueryMatchPhraseByHeadline(pageable.getPageNumber(), pageable.getPageSize(), field, filter);
    }

    @RequestMapping(path = "multi_match_by_filed", method = RequestMethod.GET)
    public SearchPage<Article>  multi_match_by_filed(Pageable pageable, @RequestParam List<String> field, @RequestParam String filter) {
        System.out.println(field);
        return articleService.findQueryMultiMatchByHeadline(pageable.getPageNumber(), pageable.getPageSize(), field, filter);
    }
}
