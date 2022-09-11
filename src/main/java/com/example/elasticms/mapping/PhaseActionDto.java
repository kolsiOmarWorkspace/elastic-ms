package com.example.elasticms.mapping;


import lombok.*;
import org.elasticsearch.core.TimeValue;

import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class PhaseActionDto {
    private String phase_name;
    private TimeValue time_value;
    private List<Map<String, Object>> phase_action;
}
