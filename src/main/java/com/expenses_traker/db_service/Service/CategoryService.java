package com.expenses_traker.db_service.Service;

import com.expenses_traker.db_service.Entity.CategoryEntity;
import com.expenses_traker.db_service.Repository.CategoryRepo;
import com.expenses_traker.db_service.pojo.APIResponse;
import com.expenses_traker.db_service.pojo.CategoryPojo;
import com.expenses_traker.db_service.util.ResponseHealper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepo categoryRepo;
    private final ResponseHealper responseHealper;

    //Create
     public Mono<APIResponse<CategoryEntity>> createCategory(CategoryPojo categoryPojo) {
         if(categoryPojo==null){
             return Mono.just(responseHealper.buildErrorResponse("Category data cannot be null"));
         }
         CategoryEntity entity = new CategoryEntity();
         entity.setUserId(categoryPojo.getUserId());
         entity.setName(categoryPojo.getName());
         entity.setType(categoryPojo.getType());
         entity.setIcon(categoryPojo.getIcon());
         entity.setColor(categoryPojo.getColor());
         return categoryRepo.save(entity)
                 .map(res -> responseHealper.buildSuccessResponse(res, "Category created successfully"))
                 .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Error creating category: " + e.getMessage())));
     }
     //Create multiple categories
    public Mono<APIResponse<List<CategoryEntity>>> createMultipleCategories(List<CategoryPojo> categoryPojo){
        List<CategoryEntity> entities = categoryPojo.stream().map(pojo -> {
            CategoryEntity entity = new CategoryEntity();
            entity.setUserId(pojo.getUserId());
            entity.setName(pojo.getName());
            entity.setType(pojo.getType());
            entity.setIcon(pojo.getIcon());
            entity.setColor(pojo.getColor());
            return entity;
        }).toList();

        return categoryRepo.saveAll(entities)
                .collectList()
                .map(res -> responseHealper.buildSuccessResponse(res, "Categories created successfully"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Error creating categories: " + e.getMessage())));
    }

     //read
        //-Get Category by Id
        public Mono<APIResponse<CategoryEntity>> getCategoryById(Long id) {
         if(id==null){
                return Mono.just(responseHealper.buildErrorResponse("Category id cannot be null"));
         }
            return categoryRepo.findById(id)
                    .map(res -> responseHealper.buildSuccessResponse(res, "Category fetched successfully"))
                    .defaultIfEmpty(responseHealper.buildErrorResponse("Category not found"))
                    .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Error fetching category: " + e.getMessage())));
        }
        //-Get All Categories by user id
        public Mono<APIResponse<List<CategoryEntity>>> getAllCategoriesByUserId(String userId) {
            if(userId==null){
                return Mono.just(responseHealper.buildErrorResponse("userId cannot be null"));
            }
         return categoryRepo.findByUserId(userId).collectList()
                    .map(res -> responseHealper.buildSuccessResponse(res, "Categories fetched successfully"))
                    .onErrorResume(e -> Mono.just(responseHealper.buildSuccessResponse(new ArrayList<>(),"Error fetching categories: " + e.getMessage())));
        }
        //get All Categories
        public Mono<APIResponse<List<CategoryEntity>>> getAllCategories() {
            return categoryRepo.findAll()
                    .collectList()
                    .map(res -> responseHealper.buildSuccessResponse(res, "All Categories fetched successfully"))
                    .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Error fetching all categories: " + e.getMessage())));
        }
    //Update Category
    public Mono<APIResponse<CategoryEntity>> updateCategory(Long id, CategoryPojo categoryPojo) {
        if (id == null) {
            return Mono.just(responseHealper.buildErrorResponse("Category id cannot be null"));
        }
        return categoryRepo.findById(id)
                .flatMap(existingCategory -> {
                    existingCategory.setName(categoryPojo.getName() != null ? categoryPojo.getName() : existingCategory.getName());
                    existingCategory.setType(categoryPojo.getType() != null ? categoryPojo.getType() : existingCategory.getType());
                    existingCategory.setIcon(categoryPojo.getIcon() != null ? categoryPojo.getIcon() : existingCategory.getIcon());
                    existingCategory.setColor(categoryPojo.getColor() != null ? categoryPojo.getColor() : existingCategory.getColor());
                    return categoryRepo.save(existingCategory);
                })
                .map(res -> responseHealper.buildSuccessResponse(res, "Category updated successfully"))
                .defaultIfEmpty(responseHealper.buildErrorResponse("Category not found"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Error updating category: " + e.getMessage())));
    }
    //Delete Category
    public Mono<APIResponse<Boolean>> deleteCategory(Long id) {
        if (id == null) {
            return Mono.just(responseHealper.buildErrorResponse("Category id cannot be null"));
        }
        return categoryRepo.findById(id)
                .flatMap(existingCategory ->
                        categoryRepo.delete(existingCategory)
                                .then(Mono.just(responseHealper.buildSuccessResponse(true, "Category deleted successfully")))
                )
                .defaultIfEmpty(responseHealper.buildErrorResponse("Category not found"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Error deleting category: " + e.getMessage())));
    }

}
