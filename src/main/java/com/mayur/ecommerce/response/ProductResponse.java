package com.mayur.ecommerce.response;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ProductResponse {

    private UUID id;
    private String name;
    private String description;
    private double price;
    private boolean onSale;
    private double popularity; // Popularity might be included if it's relevant to the response
    private List<String> tags;
}
