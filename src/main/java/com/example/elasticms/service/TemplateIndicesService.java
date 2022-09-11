package com.example.elasticms.service;

import com.example.elasticms.mapping.ComposableIndexTemplateDto;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.cluster.metadata.ComponentTemplate;
import org.elasticsearch.cluster.metadata.ComposableIndexTemplate;
import org.elasticsearch.cluster.metadata.Template;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TemplateIndicesService {

    @Autowired
    ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    RestHighLevelClient client;

    public Object findIndexTemplate(String indexTemplate, boolean responseIsBoolean) throws IOException {
        ComposableIndexTemplateExistRequest request = new ComposableIndexTemplateExistRequest(indexTemplate);
        GetComposableIndexTemplateRequest indexTemplateRequest = new GetComposableIndexTemplateRequest(indexTemplate);

        if (responseIsBoolean) {
            return client.indices().existsIndexTemplate(request, RequestOptions.DEFAULT);
        } else {
            try {
                List<ComposableIndexTemplateDto> indexTemplates = client.indices().getIndexTemplate(indexTemplateRequest, RequestOptions.DEFAULT)
                        .getIndexTemplates().entrySet().stream().map(x -> {
                            ComposableIndexTemplateDto composableIndexTemplateDto = new ComposableIndexTemplateDto(x.getValue().indexPatterns(), x.getValue().template(),
                                    x.getValue().composedOf(), x.getValue().priority(), x.getValue().version(), x.getValue().metadata());

                            composableIndexTemplateDto.setIndexTemplateName(x.getKey());
                            return composableIndexTemplateDto;
                        }).collect(Collectors.toList());
                return indexTemplate;
            } catch (Exception e) {
                return Arrays.asList();
            }
        }
    }

    public AcknowledgedResponse createComposableIndexTemplate(String indexTemplate, List<String> indexPatterns, List<String> componentTemplate, boolean dataStream, Long priority) throws IOException {
        PutComposableIndexTemplateRequest request = new PutComposableIndexTemplateRequest().name(indexTemplate);
        ComposableIndexTemplate composableIndexTemplate = new ComposableIndexTemplate(indexPatterns != null ? indexPatterns : null, null, componentTemplate != null ? componentTemplate : null
                , priority != null ? priority : null, null, null, dataStream ? new ComposableIndexTemplate.DataStreamTemplate() : null);

        request.indexTemplate(composableIndexTemplate);
        return client.indices().putIndexTemplate(request, RequestOptions.DEFAULT);
    }

    public AcknowledgedResponse createComponentTemplate(String componentTemplateName, Long version, Map<String, Object> metadata, String aliasName, String jsonObject, Map<String, ?> settings) throws IOException {
        PutComponentTemplateRequest putComponentTemplateRequest = new PutComponentTemplateRequest().name(componentTemplateName);

        Settings settingsResult = Settings.builder().loadFromMap(settings).build();
        Map<String, AliasMetadata> aliases = new HashMap<>();

        if (aliasName != null) {
            AliasMetadata aliasMetadata = AliasMetadata.builder(aliasName).build();
            aliases.put(aliasName, aliasMetadata);
        }

        Template template = new Template(settingsResult, jsonObject != null ? new CompressedXContent(jsonObject) : null, aliasName != null ? aliases : null);
        ComponentTemplate componentTemplate = new ComponentTemplate(template, version, metadata);
        putComponentTemplateRequest.componentTemplate(componentTemplate);
        return client.cluster().putComponentTemplate(putComponentTemplateRequest, RequestOptions.DEFAULT);
    }

    public void deleteIndexTemplate(String indexTemplate) throws IOException {
        DeleteComposableIndexTemplateRequest deleteComposableIndexTemplateRequest = new DeleteComposableIndexTemplateRequest(indexTemplate);
        client.indices().deleteIndexTemplate(deleteComposableIndexTemplateRequest, RequestOptions.DEFAULT);
    }
}
