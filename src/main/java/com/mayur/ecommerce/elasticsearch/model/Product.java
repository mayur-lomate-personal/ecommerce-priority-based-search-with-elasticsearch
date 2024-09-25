package com.mayur.ecommerce.elasticsearch.model;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(indexName = "products")
public class Product {
    @Id
    private UUID id;
    private String name;
    private String description;
    private List<String> categories;
    private List<String> tags;
    private boolean onSale;
    private double popularity;
}
