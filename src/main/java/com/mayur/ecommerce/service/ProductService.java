package com.mayur.ecommerce.service;

import com.mayur.ecommerce.elasticsearch.service.ProductElasticSearchService;
import com.mayur.ecommerce.model.Category;
import com.mayur.ecommerce.model.Product;
import com.mayur.ecommerce.repository.CategoryRepository;
import com.mayur.ecommerce.repository.ProductRepository;
import com.mayur.ecommerce.request.ProductRequest;
import com.mayur.ecommerce.response.ProductResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductElasticSearchService productElasticSearchService;

    @Autowired
    public ProductService(@Qualifier("jpaProductRepository")ProductRepository productRepository, CategoryRepository categoryRepository, ProductElasticSearchService productElasticSearchService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productElasticSearchService = productElasticSearchService;
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setOnSale(productRequest.isOnSale());
        product.setTags(productRequest.getTags());
        List<Category> categories = categoryRepository.findAllById(productRequest.getCategoryIds());
        product.setCategories(categories);
        product = productRepository.save(product);
        productElasticSearchService.saveProduct(product);
        return convertToResponse(product);
    }

    public Page<ProductResponse> searchProducts(String query, Pageable pageable) {
        Page<com.mayur.ecommerce.elasticsearch.model.Product> elasticSearchProducts = productElasticSearchService.searchProducts(query, pageable);
        List<Product> products = productRepository.findAllById(elasticSearchProducts.stream().map(com.mayur.ecommerce.elasticsearch.model.Product::getId).collect(Collectors.toList()));
        List<ProductResponse> productResponses = products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(productResponses, pageable, elasticSearchProducts.getTotalElements());
    }

    private ProductResponse convertToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setOnSale(product.isOnSale());
        response.setPopularity(product.getPopularity());
        response.setTags(product.getTags());
        return response;
    }
}
