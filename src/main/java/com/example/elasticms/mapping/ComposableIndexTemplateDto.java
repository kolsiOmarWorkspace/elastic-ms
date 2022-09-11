package com.example.elasticms.mapping;

import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.cluster.metadata.Template;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ComposableIndexTemplateDto {
    private String indexTemplateName;

    private Map<String, Object> details = new HashMap<>();

    public ComposableIndexTemplateDto(List<String> indexPatterns, Template template, List<String> componentTemplates, Long priority, Long version, Map<String, Object> metadata){
        this.details.put("indexPatterns", indexPatterns);
        this.details.put("template", template);
        this.details.put("componentTemplates", componentTemplates);
        this.details.put("priority", priority);
        this.details.put("version", version);
        this.details.put("metadata", metadata);
    }
}
