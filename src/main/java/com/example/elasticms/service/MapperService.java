package com.example.elasticms.service;

import com.example.elasticms.mapping.PhaseActionDto;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.core.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@ConfigurationProperties
@Getter
@Setter
public class MapperService {

    private Map<String, String> variables;

    private Map<String, ?> importedList;

    @Autowired
    private Environment environment;

    @Autowired
    private IlmActionsService ilmActionsService;

    @Autowired
    private TemplateIndicesService templateIndicesService;

    @Autowired
    private ValidatorsService validatorsService;

    private static final Logger logger = LoggerFactory.getLogger(MapperService.class);

    public List<PhaseActionDto> mapIlmCreation(Map<String, Map<String, Object>> importedList, String policyName) throws Exception {
        logger.info("Mapping json" + policyName + ".json");
        List<PhaseActionDto> phaseActionDtoList = new ArrayList<>();

        ((Map<String, Map<String, Object>>) importedList.get("policy").get("phases")).forEach((x, y) -> {
            PhaseActionDto phaseActionDto = new PhaseActionDto();
            phaseActionDto.setPhase_name(x);

            phaseActionDto.setTime_value(y.get("min_age") != null ? TimeValue.parseTimeValue(y.get("min_age").toString(), y.get("min_age").toString()) : null);

            List<Map<String, Object>> phaseAction = new ArrayList<>();
            ((Map<String, Object>) y.get("actions")).entrySet().forEach(a -> {
                Map<String, Object> map = new HashMap<>();
                map.put(a.getKey(), a.getValue());
                phaseAction.add(map);
            });
            phaseActionDto.setPhase_action(phaseAction);
            phaseActionDtoList.add(phaseActionDto);
        });
        logger.info("Send Mapped " + policyName + ".json" + " to " + ilmActionsService.getClass());
        ilmActionsService.setPhaseAction(phaseActionDtoList, policyName);
        return phaseActionDtoList;
    }

    private AcknowledgedResponse mapTemplateCreation(Map<String, Map<String, Object>> importedList, String templateName) throws IOException {
        AcknowledgedResponse response = null;
        if (validatorsService.validateIndexTemplate(templateName)) {
            AcknowledgedResponse acknowledgedResponse = templateIndicesService.createComponentTemplate(templateName + "_component", null,
                    importedList.get("_meta") != null ? importedList.get("_meta") : null, templateName + "_component",
                    new Gson().toJson(importedList.get("template").get("mappings")), (Map<String, ?>) importedList.get("template").get("Settings"));

            if (acknowledgedResponse.isAcknowledged()) {
                response = templateIndicesService.createComposableIndexTemplate(templateName, List.of(new Gson().toJson(importedList.get("index_patterns"))
                        .replace("\"", "")), List.of(templateName + "_component"), true, importedList.get("priority") != null ? Long.valueOf(new Gson().toJson(importedList.get("priority"))) : null);
            }


        }
        return response;
    }

    public void readDirectories(String... resources) {
        List.of(resources).forEach(res -> {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("elastic_resources/" + res);
            InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(isr);

            List<String> stringList = bufferedReader.lines().collect(Collectors.toList());
            logger.info("Get policies from " + res + " directory");
            for (int i = 0; i < stringList.size(); i++) {
                readDirectory(res, stringList.get(i));
            }
        });
    }

    private void readDirectory(String res, String fileName) {
        InputStream fileInputStream = TypeReference.class.getResourceAsStream("elastic_resources/" + res + "/" + fileName);

        byte[] bytes = new byte[0];
        try {
            TypeReference<Map<String, ?>> typeReference = new TypeReference<>() {
            };

            ObjectMapper mapper = new ObjectMapper();
            bytes = fileInputStream.readAllBytes();

            String mappedString = new String(bytes, StandardCharsets.UTF_8);

            InputStream cleanInputStream = new ByteArrayInputStream(mappedString.getBytes());

            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

            Map<String, ?> mapped = mapper.readValue(cleanInputStream, typeReference);

            importedList = mapped;

            if (res.equals("ilm")) {
                mapIlmCreation((Map<String, Map<String, Object>>) importedList, fileName.replaceAll("\\.json", ""));
            }
            if (res.equals("template")) {
                mapTemplateCreation((Map<String, Map<String, Object>>) importedList, fileName.replaceAll("\\.json", ""));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
