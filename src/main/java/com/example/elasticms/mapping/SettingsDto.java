package com.example.elasticms.mapping;

import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SettingsDto {
    private String creation_date;
    private String number_of_replicas;
    private String number_of_shards;
    private String provided_name;
    private String _tier_preference;
    private String uuid;
    private String created;
}
