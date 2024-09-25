package com.mayur.ecommerce.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ProductRequest {

    @NotNull(message = "Name cannot be null")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot be longer than 500 characters")
    private String description;

    @NotNull(message = "Price cannot be null")
    private double price;

    private List<String> tags;
    private boolean onSale;

    @NotNull(message = "Category IDs cannot be null")
    private List<UUID> categoryIds;
}
