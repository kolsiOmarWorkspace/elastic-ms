package com.example.elasticms.mapping;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.time.LocalDate;

@Document(indexName = "article")
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Article implements Serializable {

    @Id
    private String id;
    private String category;
    private String headline;
    private String authors;
    private String link;
    private String short_description;
    private LocalDate date;
}
