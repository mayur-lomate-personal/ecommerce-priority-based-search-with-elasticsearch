package com.mayur.ecommerce.elasticsearch.repository;

import com.mayur.ecommerce.elasticsearch.model.Product;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository("elasticsearchProductRepository")
public interface ProductRepository extends ElasticsearchRepository<Product, UUID> {
    List<Product> findByName(String name);
    List<Product> findByDescriptionContains(String description);
}

