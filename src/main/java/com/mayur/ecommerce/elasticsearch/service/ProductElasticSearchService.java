package com.mayur.ecommerce.elasticsearch.service;

import com.mayur.ecommerce.elasticsearch.model.Product;
import com.mayur.ecommerce.model.Category;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.script.Script;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductElasticSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public ProductElasticSearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        // Create a multi-match query that boosts the name, tags, and description fields

        FunctionScoreQueryBuilder.FilterFunctionBuilder[] functions = new FunctionScoreQueryBuilder.FilterFunctionBuilder[2];

        functions[0] = new FunctionScoreQueryBuilder.FilterFunctionBuilder(ScoreFunctionBuilders.scriptFunction(new Script("doc['onSale'].value ? _score * 2 : _score")));
        functions[1] = new FunctionScoreQueryBuilder.FilterFunctionBuilder(ScoreFunctionBuilders.fieldValueFactorFunction("popularity")
                        .modifier(FieldValueFactorFunction.Modifier.SQRT)
                        .factor(1.5f));
        FunctionScoreQueryBuilder query = QueryBuilders.functionScoreQuery(
                QueryBuilders.disMaxQuery()
                        .add(QueryBuilders.commonTermsQuery("name", "*" + keyword + "*").cutoffFrequency(0.001f).boost(4.0f))
                        .add(QueryBuilders.commonTermsQuery("tags", keyword).cutoffFrequency(0.001f).boost(3.0f))
                        .add(QueryBuilders.commonTermsQuery("description", keyword).cutoffFrequency(0.001f).boost(1.0f)),
                functions// Boost name, tags, and description
        );
        NativeSearchQuery searchQuery = new NativeSearchQuery(query);
        // Execute the query and return results
        SearchHits<Product> searchHits = elasticsearchOperations.search(searchQuery, Product.class);
        List<Product> products = searchHits.getSearchHits()
                .stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        return new PageImpl<>(products, pageable, searchHits.getTotalHits());
    }

    public Product saveProduct(com.mayur.ecommerce.model.Product product) {
        Product elasticSearchProduct = new Product();
        elasticSearchProduct.setId(product.getId());
        elasticSearchProduct.setDescription(product.getDescription());
        elasticSearchProduct.setCategories(product.getCategories().stream().map(Category::getName).collect(Collectors.toList()));
        elasticSearchProduct.setName(product.getName());
        elasticSearchProduct.setTags(product.getTags());
        elasticSearchProduct.setOnSale(product.isOnSale());
        elasticSearchProduct.setPopularity(product.getPopularity());
        return elasticsearchOperations.save(elasticSearchProduct);
    }
}
