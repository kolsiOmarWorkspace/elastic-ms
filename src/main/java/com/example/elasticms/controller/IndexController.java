package com.example.elasticms.controller;


import com.example.elasticms.service.IndexService;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(path = "index")
public class IndexController {

    @Autowired
    IndexService indexService;

    @RequestMapping(path = "/create_index", method = RequestMethod.PUT)
    public CreateIndexResponse createIndexResponse(@RequestParam String indexName,
                                                   @RequestParam(defaultValue = "1", required = false) int number_of_shards,
                                                   @RequestParam(defaultValue = "1", required = false) int number_of_replicas,
                                                   @RequestBody(required = false) String jsonObj,
                                                   @RequestParam(required = false) String alias) throws IOException {
        return indexService.createIndex(indexName, number_of_shards, number_of_replicas, jsonObj, alias);
    }

    @RequestMapping(path = "delete_index", method = RequestMethod.DELETE)
    public AcknowledgedResponse deleteIndex(String indexName) throws IOException {
        return indexService.deleteIndex(indexName);
    }

    @RequestMapping(path = "find_index", method = RequestMethod.GET)
    public Object findIndex(@RequestParam String indexName, @RequestParam(defaultValue = "true" , required = false) boolean responseIsBoolean) throws IOException {
        return indexService.findIndex(indexName, responseIsBoolean);
    }

    @RequestMapping(path = "/find_by_pattern", method = RequestMethod.GET)
    public Object find_by_pattern(@RequestParam String patternName, @RequestParam(defaultValue = "true", required = false) boolean responseIsBoolean) throws IOException {
        return indexService.findIndexByAlias(patternName, responseIsBoolean);
    }


}
