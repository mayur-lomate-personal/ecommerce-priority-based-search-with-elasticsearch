package com.mayur.ecommerce.repository;

import com.mayur.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository("jpaProductRepository")
public interface ProductRepository extends JpaRepository<Product, UUID> {
}
