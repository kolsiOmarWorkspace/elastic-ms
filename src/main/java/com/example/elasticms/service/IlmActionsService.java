package com.example.elasticms.service;

import com.example.elasticms.mapping.PhaseActionDto;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.AcknowledgedResponse;
import org.elasticsearch.client.indexlifecycle.*;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.core.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class IlmActionsService {

    @Autowired
    RestHighLevelClient client;

    public AcknowledgedResponse setPhaseAction(List<PhaseActionDto> phaseActionDtos, String policy_name) throws IOException {
        Map<String, Phase> phases = new HashMap<>();

        phaseActionDtos.stream().forEach(x -> {
            Map<String, LifecycleAction> actions = new HashMap<>();
            x.getPhase_action().forEach(y -> {
                try {
                    if (y.get(SetPriorityAction.NAME) != null) {
                        phases.put(x.getPhase_name(), new Phase(x.getPhase_name(), x.getTime_value(), setPriorityAction(y, actions, x.getPhase_name())));
                    } else if (y.get(UnfollowAction.NAME) != null) {
                        phases.put(x.getPhase_name(), new Phase(x.getPhase_name(), x.getTime_value(), unfollowAction(actions, x.getPhase_name())));
                    } else if (y.get(RolloverAction.NAME) != null) {
                        phases.put(x.getPhase_name(), new Phase(x.getPhase_name(), x.getTime_value(), rolloverAction(y, actions, x.getPhase_name())));
                    } else if (y.get(ReadOnlyAction.NAME) != null) {
                        phases.put(x.getPhase_name(), new Phase(x.getPhase_name(), x.getTime_value(), readOnlyAction(actions, x.getPhase_name())));
                    } else if (y.get(AllocateAction.NAME) != null) {
                        phases.put(x.getPhase_name(), new Phase(x.getPhase_name(), x.getTime_value(), allocateAction(y, actions, x.getPhase_name())));
                    } else if (y.get(DeleteAction.NAME) != null) {
                        phases.put(x.getPhase_name(), new Phase(x.getPhase_name(), x.getTime_value(), deleteAction(actions, x.getPhase_name())));
                    } else if (y.get(ForceMergeAction.NAME) != null) {
                        phases.put(x.getPhase_name(), new Phase(x.getPhase_name(), x.getTime_value(), forceMergeAction(y, actions, x.getPhase_name())));
                    }  else if (y.get(FreezeAction.NAME) != null) {
                        phases.put(x.getPhase_name(), new Phase(x.getPhase_name(), x.getTime_value(), freezeAction( actions, x.getPhase_name())));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());

                }
            });
        });

        LifecyclePolicy policy = new LifecyclePolicy(policy_name, phases);

        PutLifecyclePolicyRequest request = new PutLifecyclePolicyRequest(policy);


        return client.indexLifecycle().putLifecyclePolicy(request, RequestOptions.DEFAULT);
    }

    private Map<String, LifecycleAction> freezeAction(Map<String, LifecycleAction> actions, String phase_name) {
    if (phase_name.equals("cold")) {
        actions.put(FreezeAction.NAME, new FreezeAction());
    } else {
        throw new RuntimeException("Phases allowed for action " + FreezeAction.NAME + ": Coold ! ");
    }
    return actions;
    }

    private Map<String, LifecycleAction> forceMergeAction(Map<String, Object> priority, Map<String, LifecycleAction> actions, String phase_name) {
        if (phase_name.equals("warm") || phase_name.equals("hot")) {

            Integer maxNumSegments = ((Map<String, Integer>) priority.get("forcemerge")).get("max_num_segments");

            actions.put(ForceMergeAction.NAME, new ForceMergeAction(maxNumSegments != null ? maxNumSegments : null));
        } else {
            throw new RuntimeException("Phases allowed for action " + ForceMergeAction.NAME + " : Warm, Hot");
        }
        return actions;
    }

    private Map<String, LifecycleAction> deleteAction(Map<String, LifecycleAction> actions, String phase_name) {
        if (phase_name.equals("delete")) {
            actions.put(DeleteAction.NAME, new DeleteAction());
        } else {
            throw new RuntimeException("Phases allowed for action" + DeleteAction.NAME + " : delete!");
        }
        return actions;
    }

    private Map<String, LifecycleAction> allocateAction(Map<String, Object> priority, Map<String, LifecycleAction> actions, String phase_name) {
        if (phase_name.equals("warm") || phase_name.equals("cold")) {
            Integer numberOfReplicas = (Integer) ((Map<String, Object>) priority.get("allocate")).get("number_of_replicas");
            Map<String, String> include = (Map<String, String>) ((Map<String, Object>) priority.get("allocate")).get("include");
            Map<String, String> exclude = (Map<String, String>) ((Map<String, Object>) priority.get("allocate")).get("exclude");
            Map<String, String> require = (Map<String, String>) ((Map<String, Object>) priority.get("allocate")).get("require");


            actions.put(AllocateAction.NAME, new AllocateAction(numberOfReplicas != null ? numberOfReplicas : null, include != null ? include : null, exclude != null ? exclude : null, require != null ? require : null));
        } else {
            throw new RuntimeException("Phases allowed for action : " + AllocateAction.NAME + " : warm, cold");
        }

        return actions;
    }

    private Map<String, LifecycleAction> readOnlyAction(Map<String, LifecycleAction> actions, String phase_name) {
        if (phase_name.equals("hot") || phase_name.equals("warm") || phase_name.equals("cold")) {
            actions.put(ReadOnlyAction.NAME, new ReadOnlyAction());
        } else {
            throw new RuntimeException("Phases allowed for action " + ReadOnlyAction.NAME + " : hot, warm , cold!");
        }
        return actions;
    }

    private Map<String, LifecycleAction> rolloverAction(Map<String, Object> priority, Map<String, LifecycleAction> acrions, String phaseName) {
        if (phaseName.equals("hot")) {

            Object maxSize = ((Map<String, Object>) priority.get("rollover")).get("max_size");
            Object maxPrimaryShardSize = ((Map<String, Object>) priority.get("rollover")).get("max_primary_shard_size");
            Object maxAge = ((Map<String, Object>) priority.get("rollover")).get("max_age");
            Object maxDocs = ((Map<String, Object>) priority.get("rollover")).get("max_docs");

            acrions.put(RolloverAction.NAME,
                    new RolloverAction(maxSize != null ? ByteSizeValue.parseBytesSizeValue(maxSize.toString(), maxSize.toString()) : null,
                            maxPrimaryShardSize != null ? ByteSizeValue.parseBytesSizeValue(maxPrimaryShardSize.toString(), maxSize.toString()) : null,
                            maxAge != null ? TimeValue.parseTimeValue(maxAge.toString(), maxAge.toString()) : null,
                            maxDocs != null ? Long.valueOf(maxDocs.toString()) : null));
        } else {
            throw new RuntimeException("Phase allowed for action " + RolloverAction.NAME + " : hot!");
        }

        return acrions;
    }

    private Map<String, LifecycleAction> setPriorityAction(Map<String, Object> priority, Map<String, LifecycleAction> actions, String phaseName) {
        if (phaseName.equals("hot") || phaseName.equals("warm") || phaseName.equals("cold")) {
            actions.put(SetPriorityAction.NAME, new SetPriorityAction(((Map<String, Integer>) priority.get(SetPriorityAction.NAME)).get("priority")));

        } else {
            throw new RuntimeException("Phases allowed for action " + SetPriorityAction.NAME + " : hot, warm, cold");
        }

        return actions;
    }

    private Map<String, LifecycleAction> unfollowAction(Map<String, LifecycleAction> actions, String phaseName) {
        if (phaseName.equals("hot") || phaseName.equals("warm") || phaseName.equals("cold") || phaseName.equals("frozen")) {
            actions.put(UnfollowAction.NAME, new UnfollowAction());
        } else {
            throw new RuntimeException("Phases allowed for action " + UnfollowAction.NAME + " : hot, warm, cold, frozen");
        }

        return actions;
    }

    public Map<String, Object> getLifecyclePolicyResponse(String... policyName) throws IOException {
        Map<String, Object> returnedMap = new HashMap<>();

        GetLifecyclePolicyRequest allRequest = new GetLifecyclePolicyRequest(policyName);

        GetLifecyclePolicyResponse response = client.indexLifecycle().getLifecyclePolicy(allRequest, RequestOptions.DEFAULT);

        response.getPolicies().forEach(x -> returnedMap.put(x.key, x.value));
        return returnedMap;
    }

    public AcknowledgedResponse deletePolicy(String policyName) throws Exception {
        DeleteLifecyclePolicyRequest request = new DeleteLifecyclePolicyRequest(policyName);
        return client.indexLifecycle().deleteLifecyclePolicy(request, RequestOptions.DEFAULT);
    }
}

