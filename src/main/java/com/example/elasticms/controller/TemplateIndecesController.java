package com.example.elasticms.controller;

import com.example.elasticms.service.TemplateIndicesService;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "template_index")
public class TemplateIndecesController {

    @Autowired
    TemplateIndicesService templateIndicesService;

    @RequestMapping(path = "/findIndexTemplate", method = RequestMethod.GET)
    public Object findIndexTemplate(@RequestParam String indexTemplate, @RequestParam(defaultValue = "true") boolean responseIsBoolean) throws IOException {
        return templateIndicesService.findIndexTemplate(indexTemplate, responseIsBoolean);
    }

    @RequestMapping(path = "/createOrUpdateComposableTemplate", method = RequestMethod.GET)
    public AcknowledgedResponse createOrUpdateComposableTemplate(@RequestParam String indexTemplate,
                                                                 @RequestParam(required = false, defaultValue = "false") boolean dataStream,
                                                                 @RequestParam(required = false)List<String> componentTemplate,
                                                                 @RequestParam(required = false) Long priority,
                                                                 @RequestParam(required = false) String... indexPatterns) throws IOException {
        return templateIndicesService.createComposableIndexTemplate(indexTemplate, List.of(indexPatterns), componentTemplate, dataStream,priority);
    }
}
