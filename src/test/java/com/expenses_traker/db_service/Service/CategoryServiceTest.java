//package com.expenses_traker.db_service.Service;
//
//import com.expenses_traker.db_service.Entity.CategoryEntity;
//import com.expenses_traker.db_service.Repository.CategoryRepo;
//import com.expenses_traker.db_service.pojo.CategoryPojo;
//import com.expenses_traker.db_service.util.ResponseHealper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class CategoryServiceTest {
//
//    @Mock
//    private CategoryRepo categoryRepo;
//
//    private final ResponseHealper responseHealper = new ResponseHealper();
//
//    private CategoryService categoryService;
//
//    @BeforeEach
//    void setUp() {
//        categoryService = new CategoryService(categoryRepo, responseHealper);
//    }
//
//    @Test
//    void createCategory_rejectsInvalidType() {
//        CategoryPojo pojo = new CategoryPojo();
//        pojo.setUserId("1");
//        pojo.setName("Food");
//        pojo.setType("BAD");
//
//        StepVerifier.create(categoryService.createCategory(pojo))
//                .assertNext(res -> {
//                    assert res.isError();
//                    assert res.getMessage().contains("Invalid category type");
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    void createCategory_rejectsDuplicateForUser() {
//        CategoryPojo pojo = new CategoryPojo();
//        pojo.setUserId("1");
//        pojo.setName("Food");
//        pojo.setType("NEED");
//
//        when(categoryRepo.existsByUserIdAndName("1", "Food")).thenReturn(Mono.just(true));
//
//        StepVerifier.create(categoryService.createCategory(pojo))
//                .assertNext(res -> {
//                    assert res.isError();
//                    assert res.getMessage().contains("already exists");
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    void createCategory_savesWhenValidAndNotDuplicate() {
//        CategoryPojo pojo = new CategoryPojo();
//        pojo.setUserId("1");
//        pojo.setName("Food");
//        pojo.setType("need"); // should normalize to NEED
//
//        when(categoryRepo.existsByUserIdAndName("1", "Food")).thenReturn(Mono.just(false));
//        when(categoryRepo.save(any(CategoryEntity.class))).thenAnswer(inv -> {
//            CategoryEntity e = inv.getArgument(0);
//            e.setId(10L);
//            return Mono.just(e);
//        });
//
//        StepVerifier.create(categoryService.createCategory(pojo))
//                .assertNext(res -> {
//                    assert !res.isError();
//                    assert res.getData() != null;
//                    assert "NEED".equals(res.getData().getType());
//                })
//                .verifyComplete();
//    }
//}
//
