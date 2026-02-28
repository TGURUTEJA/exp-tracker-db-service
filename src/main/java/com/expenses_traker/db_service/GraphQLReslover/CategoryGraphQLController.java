package com.expenses_traker.db_service.GraphQLReslover;


import com.expenses_traker.db_service.Entity.CategoryEntity;
import com.expenses_traker.db_service.Service.CategoryService;
import com.expenses_traker.db_service.pojo.APIResponse;
import com.expenses_traker.db_service.pojo.CategoryPojo;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.Arguments;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CategoryGraphQLController {

    private final CategoryService categoryService;

    //Create Category
    @MutationMapping
    public Mono<APIResponse<CategoryEntity>> createCategory(@Argument("input") CategoryPojo input) {
        return categoryService.createCategory(input);
    }
    @MutationMapping
    public Mono<APIResponse<List<CategoryEntity>>> createMultipleCategories(@Argument List<CategoryPojo> input) {
        return categoryService.createMultipleCategories(input);
    }
    //Read Category by Id
    @QueryMapping
    public Mono<APIResponse<CategoryEntity>> getCategoryById(@Argument Long id) {
        return categoryService.getCategoryById(id);
    }
    @QueryMapping
    public Mono<APIResponse<List<CategoryEntity>>> getCategoriesByUserId(@Argument String userId) {
        return categoryService.getAllCategoriesByUserId(userId);
    }
    @QueryMapping
    public Mono<APIResponse<List<CategoryEntity>>> getAllCategories() {
        return categoryService.getAllCategories();
    }
    //Update Category
    @MutationMapping
    public Mono<APIResponse<CategoryEntity>> updateCategory(@Argument Long id,@Argument CategoryPojo input) {
        return categoryService.updateCategory(id, input);
    }
    //Delete Category
    @MutationMapping
    public Mono<APIResponse<Boolean>> deleteCategory(@Argument Long id) {
        return categoryService.deleteCategory(id);
    }
}
