package com.example.elasticms.controller;

import com.example.elasticms.mapping.PhaseActionDto;
import com.example.elasticms.service.IlmActionsService;
import com.example.elasticms.service.MapperService;
import org.elasticsearch.client.core.AcknowledgedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "ilm")
public class IlmActionController {

    @Autowired
    IlmActionsService ilmActionsService;

    @Autowired
    MapperService mapperService;

    @RequestMapping(path = "/create_or_update_policy", method = RequestMethod.POST)
    public List<PhaseActionDto> createOrUpdatePolicy(@RequestBody Map<String, Map<String, Object>> phaseActionDtos, @RequestParam String policy_name) throws Exception {
        return mapperService.mapIlmCreation(phaseActionDtos, policy_name);
    }

    @RequestMapping(path = "/delete_policy", method = RequestMethod.DELETE)
    public AcknowledgedResponse deletePolicy(@RequestParam String policyName) throws Exception {
        return ilmActionsService.deletePolicy(policyName);
    }

    @RequestMapping(path = "all_policies", method = RequestMethod.GET)
    public Map<String, Object> allPolicies(@RequestParam(required = false) String... policyName) {
        try {
            return ilmActionsService.getLifecyclePolicyResponse(policyName);
        } catch (IOException e) {
            return new HashMap<>();
        }
    }
 }
