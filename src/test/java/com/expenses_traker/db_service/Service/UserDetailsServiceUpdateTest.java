package com.expenses_traker.db_service.Service;

import com.expenses_traker.db_service.Entity.UserDetailsEntity;
import com.expenses_traker.db_service.Repository.UserDetailsRepo;
import com.expenses_traker.db_service.pojo.UserInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveInsertOperation;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserDetailsServiceUpdateTest {

    private UserDetailsRepo userDetailsRepo;
    private R2dbcEntityTemplate template;
    private UserDetailsService service;

    @BeforeEach
    void setUp() {
        userDetailsRepo = mock(UserDetailsRepo.class);
        template = mock(R2dbcEntityTemplate.class);
        service = new UserDetailsService(userDetailsRepo, template);
    }

    @Test
    void updateCreatesProfileWhenNoRowExists() {
        when(userDetailsRepo.findById("auth-user")).thenReturn(Mono.empty());
        @SuppressWarnings("unchecked")
        ReactiveInsertOperation.ReactiveInsert<UserDetailsEntity> insert =
                mock(ReactiveInsertOperation.ReactiveInsert.class);
        when(template.insert(UserDetailsEntity.class)).thenReturn(insert);
        when(insert.using(any(UserDetailsEntity.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        UserInput input = new UserInput();
        input.setFirstName("Ada");
        input.setLastName("Lovelace");
        input.setDob(LocalDate.parse("1815-12-10"));

        StepVerifier.create(service.updateUser("auth-user", input))
                .assertNext(response -> {
                    assertEquals(false, response.isError());
                    assertEquals("auth-user", response.getData().getId());
                    assertEquals("Ada", response.getData().getFirstName());
                    assertEquals(input.getDob(), response.getData().getDob());
                })
                .verifyComplete();

        verify(template).insert(UserDetailsEntity.class);
    }
}
