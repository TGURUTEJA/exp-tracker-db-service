package com.expenses_traker.db_service.Service;

import com.expenses_traker.db_service.Entity.CategoryEntity;
import com.expenses_traker.db_service.Exception.AppException;
import com.expenses_traker.db_service.Exception.ErrorCode;
import com.expenses_traker.db_service.Repository.CategoryRepo;
import com.expenses_traker.db_service.pojo.CategoryPojo;
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

    private static final Set<String> ALLOWED_TYPES = Set.of("INCOME", "WANT", "NEED");

    public Mono<CategoryEntity> createCategory(CategoryPojo categoryPojo) {

        if (categoryPojo == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Category data cannot be null");
        }

        String userId = safeTrim(categoryPojo.getUserId());
        String name = safeTrim(categoryPojo.getName());
        String type = normalizeType(categoryPojo.getType());

        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "User ID cannot be null or empty");
        }
        if (name == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Category name cannot be null or empty");
        }
        if (type == null || !isValidType(type)) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Invalid category type");
        }

        CategoryEntity entity = new CategoryEntity();
        entity.setUserId(userId);
        entity.setName(name);
        entity.setType(type);
        entity.setIcon(categoryPojo.getIcon());
        entity.setColor(categoryPojo.getColor());

        return categoryRepo.save(entity);
    }

    public Mono<List<CategoryEntity>> createMultipleCategories(List<CategoryPojo> input) {

        if (input == null || input.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Category list cannot be empty");
        }

        Set<String> requestDupGuard = new HashSet<>();

        return Flux.fromIterable(input)
                .flatMap(pojo -> {

                    String userId = safeTrim(pojo.getUserId());
                    String name = safeTrim(pojo.getName());
                    String type = normalizeType(pojo.getType());

                    if (userId == null || name == null || type == null || !isValidType(type)) {
                        return Mono.error(new AppException(ErrorCode.INVALID_INPUT, "Invalid category data"));
                    }

                    String key = userId + "|" + name;
                    if (!requestDupGuard.add(key)) {
                        return Mono.error(new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS, "Duplicate category in request"));
                    }

                    CategoryEntity entity = new CategoryEntity();
                    entity.setUserId(userId);
                    entity.setName(name);
                    entity.setType(type);
                    entity.setIcon(pojo.getIcon());
                    entity.setColor(pojo.getColor());

                    return Mono.just(entity);
                })
                .collectList()
                .flatMap(list -> categoryRepo.saveAll(list).collectList());
    }

    public Mono<CategoryEntity> getCategoryById(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Category id cannot be null");
        }

        return categoryRepo.findById(id)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.INVALID_REFERENCE, "Category not found")));
    }

    public Mono<List<CategoryEntity>> getAllCategoriesByUserId(String userId) {
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "User ID cannot be null");
        }

        return categoryRepo.findByUserId(userId).collectList();
    }

    public Mono<List<CategoryEntity>> getAllCategories() {
        return categoryRepo.findAll().collectList();
    }

    public Mono<CategoryEntity> updateCategory(Long id, CategoryPojo input) {

        if (id == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Category id cannot be null");
        }
        String userId = input == null ? null : safeTrim(input.getUserId());
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "User ID cannot be null or empty");
        }

        return categoryRepo.findByIdAndUserId(id, userId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.INVALID_REFERENCE, "Category not found")))
                .flatMap(existing -> {

                    String name = input.getName() != null ? safeTrim(input.getName()) : null;
                    String type = input.getType() != null ? normalizeType(input.getType()) : null;

                    if (input.getName() != null && name == null) {
                        return Mono.error(new AppException(ErrorCode.INVALID_INPUT, "Category name cannot be empty"));
                    }
                    if (type != null && !isValidType(type)) {
                        return Mono.error(new AppException(ErrorCode.INVALID_INPUT, "Invalid category type"));
                    }
                    if (name != null) existing.setName(name);
                    if (type != null) existing.setType(type);
                    if (input.getIcon() != null) existing.setIcon(input.getIcon());
                    if (input.getColor() != null) existing.setColor(input.getColor());

                    return categoryRepo.save(existing);
                });
    }

    public Mono<Boolean> deleteCategory(Long id, String userId) {

        if (id == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Category id cannot be null");
        }
        String validatedUserId = safeTrim(userId);
        if (validatedUserId == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "User ID cannot be null or empty");
        }

        return categoryRepo.findByIdAndUserId(id, validatedUserId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.INVALID_REFERENCE, "Category not found")))
                .flatMap(entity -> categoryRepo.delete(entity).thenReturn(true));
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
        return ALLOWED_TYPES.contains(type);
    }
}