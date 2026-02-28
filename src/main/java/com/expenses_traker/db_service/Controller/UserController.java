//package com.expenses_traker.db_service.Controller;
//
//import com.expenses_traker.db_service.Entity.UserDetailsEntity;
//import com.expenses_traker.db_service.Service.UserDetailsService;
//
//import com.expenses_traker.db_service.pojo.APIResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//public class UserController {
//
//    @Autowired
//    private final UserDetailsService userService;
//
//    // UserDetails endpoints
//    @GetMapping("/")
//    public String getAllUsers() {
//        return "this is DB service for expenses tracker";
//    }
//
//    @GetMapping("/user/{id}")
//    public Mono<APIResponse<UserDetailsEntity>> getUserById(@PathVariable String id) {
//        return userService.getUserById(id);
//    }
//    @GetMapping("/user")
//    public Mono<APIResponse<List<UserDetailsEntity>>> getAllUsersDetails() {
//        return userService.getAllUsersDetails();
//    }
//    @PostMapping("/user")
//    public Mono<APIResponse<UserDetailsEntity>> createUser(@RequestBody UserDetailsEntity user) {
//        return userService.createUser(user);
//    }
//
//
//}
