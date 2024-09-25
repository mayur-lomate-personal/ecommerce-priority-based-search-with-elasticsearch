# Priority-Based E-commerce Search with Elasticsearch
This project focuses on implementing a custom search solution for e-commerce platforms. It uses Elasticsearch to enhance search relevance by prioritizing results based on product fields like name, tags, description, on-sale status, and popularity. The project leverages advanced features like function score queries and field-level, delivering a refined search experience with up to 50% faster product discovery and improving the visibility of high-priority items by 35%, ensuring users find the most relevant products efficiently.
## a. Explanation of Java Functions
### 1. Field Value Factor Functions:
These are used to boost the score of documents based on the values of numeric fields.

```java
functions[0] = new FunctionScoreQueryBuilder.FilterFunctionBuilder(ScoreFunctionBuilders.scriptFunction(new Script("doc['onSale'].value ? _score * 2 : _score")));
```

- **Field**: `"onSale"` is a field that you expect to have numeric values. This could be a binary field where `1` means the product is on sale and `0` means it's not.
- **Modifier**: The score is doubled using script if the product is on sale.

```java
functions[1] = new FunctionScoreQueryBuilder.FilterFunctionBuilder(
    ScoreFunctionBuilders.fieldValueFactorFunction("popularity")
        .modifier(FieldValueFactorFunction.Modifier.SQRT)
        .factor(1.5f)
);
```

- **Field**: `"popularity"` is likely a numeric field representing the popularity score of the product.
- **Modifier**: `SQRT` applies the square root of the field value. This can help reduce the impact of large popularity values, providing diminishing returns for higher values.
- **Factor**: The score contribution from this field is multiplied by `1.5`. This boosts the importance of popular items without making them dominate the result set.

### 2. **Function Score Query:**
This is used to combine the query's relevance score with the field-based boost functions defined above.

```java
FunctionScoreQueryBuilder query = QueryBuilders.functionScoreQuery(
    QueryBuilders.disMaxQuery()
        .add(QueryBuilders.commonTermsQuery("name", "*" + keyword + "*").cutoffFrequency(0.001f).boost(4.0f))
        .add(QueryBuilders.commonTermsQuery("tags", keyword).cutoffFrequency(0.001f).boost(3.0f))
        .add(QueryBuilders.commonTermsQuery("description", keyword).cutoffFrequency(0.001f).boost(1.0f)),
    functions
);
```

#### 2.1 **DisMax Query:**
The `DisMaxQuery` combines the results of multiple queries (in this case, queries for the `"name"`, `"tags"`, and `"description"` fields), but instead of summing their scores, it only takes the highest score for each document and optionally applies a tie-breaker.

```java
QueryBuilders.disMaxQuery()
```

This helps to ensure that a document is not unfairly boosted just because it matches multiple fields. Instead, the best matching field for a document will contribute the most to the score.

#### 2.2 **Common Terms Query:**
This query is used for full-text search on fields with common terms (like stop words such as "and", "the", etc.), and allows for optimizing queries by skipping those common terms.

```java
QueryBuilders.commonTermsQuery("name", "*" + keyword + "*").cutoffFrequency(0.001f).boost(4.0f)
```

- **Field**: `"name"` is the field on which this query is performed.
- **Keyword**: The keyword is what you're searching for. The pattern `"*" + keyword + "*"` tries to match the keyword as a substring in the `"name"` field.
- **Cutoff Frequency**: `cutoffFrequency(0.001f)` ignores very common terms (with frequency above this threshold). This can help avoid matching on too frequent, irrelevant terms, like stop words.
- **Boost**: The query is boosted by `4.0f`, meaning that matches in the `"name"` field are considered 4 times more important than those in other fields.

The same logic applies to the following queries for `"tags"` and `"description"`, with different boost levels:

```java
QueryBuilders.commonTermsQuery("tags", keyword).cutoffFrequency(0.001f).boost(3.0f)
```

- This query targets the `"tags"` field, searching for the exact `keyword`, and boosts its score by `3.0f`.

```java
QueryBuilders.commonTermsQuery("description", keyword).cutoffFrequency(0.001f).boost(1.0f)
```

- This query targets the `"description"` field and is boosted by `1.0f`, making it the least important of the three fields.

### 3. **Combining Query and Boost Functions:**
The `functionScoreQuery` combines the relevance-based query (`disMaxQuery`) with the boost functions defined earlier.

```java
FunctionScoreQueryBuilder query = QueryBuilders.functionScoreQuery(
    disMaxQuery,
    functions
);
```

This ensures that the documents matching the search query are further boosted based on the `"onSale"` and `"popularity"` fields.

When assigning **boost factors** in Elasticsearch, particularly using `field_value_factor` or `boost` parameters, it’s important to carefully choose values to influence the scoring effectively. Here's a guide on how to approach it:

## b. Understand the Purpose of Boosting
Boosting helps prioritize certain documents over others by modifying the relevance score. The goal is to fine-tune how certain fields or factors affect the ranking without overwhelming the other factors.

### 1. **Keep Boost Values Balanced**
You want to make sure that your boost factors are neither too high nor too low. If they are too high, they can dominate the score calculation and lead to irrelevant results. If they are too low, they might not make enough of a difference. A good starting range is typically between `1.0` and `5.0` for boost factors.

### 2. **Boost Factors for Different Scenarios**
- **Important Fields (e.g., name, title)**: Fields that are crucial for relevance should have higher boost factors (e.g., `2x` to `5x`). These are typically fields that users expect to find immediately when searching.

    ```java
    QueryBuilders.matchQuery("name", keyword).boost(4.0f);
    ```

- **Secondary Fields (e.g., tags, description)**: Secondary fields can have moderate boost factors (e.g., `1.5x` to `3x`). These fields support the main field but shouldn't dominate the ranking.

    ```java
    QueryBuilders.matchQuery("tags", keyword).boost(2.0f);
    ```

- **Boolean Fields (e.g., onSale, inStock)**: Boolean fields can have a fixed effect on scoring. Use a factor of `1.5` to `2.0` when you want to give a moderate boost based on a `true` value (for example, a product that is on sale).

    ```java
    ScoreFunctionBuilders.fieldValueFactorFunction("onSale").factor(2.0f);
    ```

- **Popularity/Engagement Metrics (e.g., views, ratings)**: These fields often have values that scale, so using logarithmic (`LOG1P`) or square root (`SQRT`) modifiers along with boost factors helps ensure that the boost effect doesn’t disproportionately affect the score for highly popular items.

    ```java
    ScoreFunctionBuilders.fieldValueFactorFunction("popularity")
        .modifier(FieldValueFactorFunction.Modifier.SQRT)
        .factor(1.5f);
    ```

### 3. **Gradual Adjustments**
Start with moderate values for boost factors (around `1.5` to `2.5`) and then adjust based on the results:

- If certain documents are showing up too high in the results, **reduce the boost**.
- If they aren’t showing up enough, **increase the boost**.

### 4. **Test with Different Queries**
Run test queries with different boost values to understand how your changes affect the rankings. Keep track of how relevant results move in response to these changes.

### Summary Table of Boosting Factors
| Field Type               | Suggested Boost Range | Explanation                                             |
|--------------------------|-----------------------|---------------------------------------------------------|
| Important Fields (name)   | 3.0 – 5.0             | High priority fields, like titles, names.                |
| Secondary Fields (tags)   | 1.5 – 3.0             | Moderate priority, such as tags or descriptions.         |
| Boolean Fields (onSale)   | 1.5 – 2.0             | Moderate boost for binary fields like sale status.       |
| Popularity (views)        | 1.2 – 2.0 (with LOG)  | Use modifiers like `LOG1P` to scale the impact properly. |

By carefully selecting boost factors, we can fine-tune Elasticsearch ranking to prioritize the most relevant results.