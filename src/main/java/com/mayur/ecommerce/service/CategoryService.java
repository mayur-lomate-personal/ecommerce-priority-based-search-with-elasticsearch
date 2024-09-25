package com.mayur.ecommerce.service;

import com.mayur.ecommerce.model.Category;
import com.mayur.ecommerce.repository.CategoryRepository;
import com.mayur.ecommerce.request.CategoryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(@Valid CategoryRequest categoryRequest) {
        Category category = new Category();
        category.setName(categoryRequest.getName());

        // Set parent category if present
        if (categoryRequest.getParentCategoryId() != null) {
            // Retrieve parent category from the repository
            Optional<Category> parentCategory = categoryRepository.findById(categoryRequest.getParentCategoryId());
            parentCategory.ifPresent(category::setParentCategory);
        }

        // Save the category to the database
        return categoryRepository.save(category);
    }
}
