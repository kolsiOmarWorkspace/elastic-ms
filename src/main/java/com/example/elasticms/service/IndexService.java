package com.example.elasticms.service;


import com.example.elasticms.mapping.SettingsDto;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class IndexService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ElasticsearchOperations elasticsearchTemplate;

    public CreateIndexResponse createIndex(String indexName, int numberOfShards, int numberOfReplicas, String jsonObj, String alias) throws IOException {

        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);

        // create settings
        createIndexRequest.settings(Settings.builder().put("index.number_of_shards", numberOfShards).put("index.number_of_replicas", numberOfReplicas));

        // create mappings
        createIndexRequest.mapping(jsonObj, XContentType.JSON);

        // create alias
        createIndexRequest.alias(new Alias(alias));

        CreateIndexResponse response = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);

        return response;
    }

    public AcknowledgedResponse deleteIndex(String indexName) throws IOException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);

        AcknowledgedResponse deleteIndexResponse = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);

        return deleteIndexResponse;
    }

    public Object findIndex(String indexName, boolean responseIsBoolean) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);

        if (responseIsBoolean) {
            return client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        } else {
            GetIndexResponse response = client.indices().get(getIndexRequest, RequestOptions.DEFAULT);
            Map<String, Object> objectMap = new HashMap<>();

            objectMap.put("indices", Arrays.asList(response.getIndices()));
            objectMap.put("mappings", response.getMappings().get(indexName));
            objectMap.put("aliases", response.getAliases().get(indexName));
            objectMap.put("settings", mapTheSettings(response.getSettings().get(indexName)));

            return objectMap;

        }
    }

    private SettingsDto mapTheSettings(Settings settings) {
        SettingsDto settingsDto = new SettingsDto(settings.get("index.creation_date"), settings.get("index.number_of_replicas"), settings.get("index.number_of_shards"),
                settings.get("index.provided_name"), settings.get("index.routing.allocation.include._tier_preference"), settings.get("index.uuid"),
                settings.get("index.version.created"));
        return settingsDto;
    }

    public Object findIndexByAlias(String patternName, boolean responseIsBoolean) throws IOException {
        GetAliasesRequest request = new GetAliasesRequest();
        request.aliases(patternName);

        if (!responseIsBoolean) {
            return client.indices().getAlias(request, RequestOptions.DEFAULT);
        } else {
            return client.indices().existsAlias(request, RequestOptions.DEFAULT);
        }
    }

}
