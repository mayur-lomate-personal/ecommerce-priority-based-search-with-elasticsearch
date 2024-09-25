package com.mayur.ecommerce.model;

import javax.persistence.*;


import java.util.List;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // UUID generation using AUTO strategy
    @Column(updatable = false, nullable = false)
    private UUID id;
    private String name;
    private String description;
    private double price;
    @ManyToMany
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;
    @ElementCollection
    private List<String> tags;
    private boolean onSale;
    private double popularity = 0;
    private int sales = 0;
    private int views = 0;
    private int reviews = 0;

    public void updatePopularity() {
        this.popularity = (sales * 0.5) + (views * 0.3) + (reviews * 0.2);  // Example formula
    }
    // Getters and Setters
}

