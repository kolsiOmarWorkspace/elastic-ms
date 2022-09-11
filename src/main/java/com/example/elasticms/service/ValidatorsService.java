package com.example.elasticms.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidatorsService {

    @Autowired
    IlmActionsService ilmActionsService;

    @Autowired
    TemplateIndicesService templateIndicesService;

    public boolean validateIlm(String policyName) {
        try {
            ilmActionsService.getLifecyclePolicyResponse(policyName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateIndexTemplate(String indexTemplateName) {
        try {
            return (boolean) templateIndicesService.findIndexTemplate(indexTemplateName, true);
        } catch (Exception e) {
            return false;
        }
    }
}
