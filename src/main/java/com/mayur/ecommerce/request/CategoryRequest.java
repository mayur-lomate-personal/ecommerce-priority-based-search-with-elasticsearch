package com.mayur.ecommerce.request;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CategoryRequest {
    private String name;
    private UUID parentCategoryId;
}
