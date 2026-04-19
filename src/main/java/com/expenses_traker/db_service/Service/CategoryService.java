package com.expenses_traker.db_service.Service;

import com.expenses_traker.db_service.Entity.CategoryEntity;
import com.expenses_traker.db_service.Repository.CategoryRepo;
import com.expenses_traker.db_service.pojo.APIResponse;
import com.expenses_traker.db_service.pojo.CategoryPojo;
import com.expenses_traker.db_service.util.ResponseHealper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepo categoryRepo;
    private final ResponseHealper responseHealper;

    private static final Set<String> ALLOWED_TYPES = Set.of("INCOME", "WANT", "NEED");

    //Create
     public Mono<APIResponse<CategoryEntity>> createCategory(CategoryPojo categoryPojo) {
         if(categoryPojo==null){
             return Mono.just(responseHealper.buildErrorResponse("Category data cannot be null"));
         }
         String userId = safeTrim(categoryPojo.getUserId());
         String name = safeTrim(categoryPojo.getName());
         String type = normalizeType(categoryPojo.getType());

         if (userId == null) {
             return Mono.just(responseHealper.buildErrorResponse("userId cannot be null or empty"));
         }
         if (name == null) {
             return Mono.just(responseHealper.buildErrorResponse("Category name cannot be null or empty"));
         }
         if (type == null) {
             return Mono.just(responseHealper.buildErrorResponse("Category type cannot be null or empty"));
         }
         if (!isValidType(type)) {
             return Mono.just(responseHealper.buildErrorResponse("Invalid category type. Allowed: INCOME, WANT, NEED"));
         }

         return categoryRepo.existsByUserIdAndName(userId, name)
                 .flatMap(exists -> {
                     if (exists) {
                         return Mono.just(responseHealper.buildErrorResponse("Category already exists for this user"));
                     }
                     CategoryEntity entity = new CategoryEntity();
                     entity.setUserId(userId);
                     entity.setName(name);
                     entity.setType(type);
                     entity.setIcon(categoryPojo.getIcon());
                     entity.setColor(categoryPojo.getColor());
                     return categoryRepo.save(entity)
                             .map(res -> responseHealper.buildSuccessResponse(res, "Category created successfully"))
                             .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Error creating category: " + e.getMessage())));
                 });
     }
     //Create multiple categories
    public Mono<APIResponse<List<CategoryEntity>>> createMultipleCategories(List<CategoryPojo> categoryPojo){
        if (categoryPojo == null || categoryPojo.isEmpty()) {
            return Mono.just(responseHealper.buildErrorResponse("Category list cannot be null or empty"));
        }

        Set<String> requestDupGuard = new HashSet<>();

        return Flux.fromIterable(categoryPojo)
                .flatMap(pojo -> {
                    if (pojo == null) {
                        return Mono.error(new IllegalArgumentException("Category item cannot be null"));
                    }
                    String userId = safeTrim(pojo.getUserId());
                    String name = safeTrim(pojo.getName());
                    String type = normalizeType(pojo.getType());

                    if (userId == null) {
                        return Mono.error(new IllegalArgumentException("userId cannot be null or empty"));
                    }
                    if (name == null) {
                        return Mono.error(new IllegalArgumentException("Category name cannot be null or empty"));
                    }
                    if (type == null || !isValidType(type)) {
                        return Mono.error(new IllegalArgumentException("Invalid category type. Allowed: INCOME, WANT, NEED"));
                    }

                    String key = userId + "|" + name;
                    if (!requestDupGuard.add(key)) {
                        return Mono.error(new IllegalArgumentException("Duplicate category in request for user"));
                    }

                    return categoryRepo.existsByUserIdAndName(userId, name)
                            .flatMap(exists -> {
                                if (exists) {
                                    return Mono.error(new IllegalArgumentException("Category already exists for this user"));
                                }
                                CategoryEntity entity = new CategoryEntity();
                                entity.setUserId(userId);
                                entity.setName(name);
                                entity.setType(type);
                                entity.setIcon(pojo.getIcon());
                                entity.setColor(pojo.getColor());
                                return Mono.just(entity);
                            });
                })
                .collectList()
                .flatMap(entities -> categoryRepo.saveAll(entities).collectList())
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
                    .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Error fetching categories: " + e.getMessage())));
        }

        public Mono<APIResponse<List<CategoryEntity>>> getAllCategories() {
            return categoryRepo.findAll()
                    .collectList()
                    .map(res -> responseHealper.buildSuccessResponse(res, "All categories fetched successfully"))
                    .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Error fetching all categories: " + e.getMessage())));
        }
    //Update Category
    public Mono<APIResponse<CategoryEntity>> updateCategory(Long id, CategoryPojo categoryPojo) {
        if (id == null) {
            return Mono.just(responseHealper.buildErrorResponse("Category id cannot be null"));
        }
        if (categoryPojo == null) {
            return Mono.just(responseHealper.buildErrorResponse("Category data cannot be null"));
        }
        return categoryRepo.findById(id)
                .flatMap(existingCategory -> {
                    String newName = categoryPojo.getName() == null ? null : safeTrim(categoryPojo.getName());
                    if (categoryPojo.getName() != null && newName == null) {
                        return Mono.error(new IllegalArgumentException("Category name cannot be null or empty"));
                    }

                    String newType = categoryPojo.getType() == null ? null : normalizeType(categoryPojo.getType());
                    if (categoryPojo.getType() != null) {
                        if (newType == null) {
                            return Mono.error(new IllegalArgumentException("Category type cannot be null or empty"));
                        }
                        if (!isValidType(newType)) {
                            return Mono.error(new IllegalArgumentException("Invalid category type. Allowed: INCOME, WANT, NEED"));
                        }
                    }

                    Mono<Void> duplicateCheck = Mono.empty();
                    if (newName != null && !newName.equals(existingCategory.getName())) {
                        duplicateCheck = categoryRepo.existsByUserIdAndNameAndIdNot(existingCategory.getUserId(), newName, id)
                                .flatMap(exists -> exists
                                        ? Mono.error(new IllegalArgumentException("Category already exists for this user"))
                                        : Mono.empty());
                    }

                    return duplicateCheck.then(Mono.defer(() -> {
                        if (newName != null) existingCategory.setName(newName);
                        if (newType != null) existingCategory.setType(newType);
                        if (categoryPojo.getIcon() != null) existingCategory.setIcon(categoryPojo.getIcon());
                        if (categoryPojo.getColor() != null) existingCategory.setColor(categoryPojo.getColor());
                        return categoryRepo.save(existingCategory);
                    }));
                })
                .map(res -> responseHealper.buildSuccessResponse(res, "Category updated successfully"))
                .defaultIfEmpty(responseHealper.buildErrorResponse("Category not found"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse(
                        (e instanceof IllegalArgumentException) ? e.getMessage() : "Error updating category: " + e.getMessage()
                )));
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

    private static String safeTrim(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static String normalizeType(String type) {
        String t = safeTrim(type);
        return t == null ? null : t.toUpperCase();
    }

    private static boolean isValidType(String type) {
        return type != null && ALLOWED_TYPES.contains(type);
    }

}
