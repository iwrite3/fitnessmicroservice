package com.fitness.userservice.controller;
import jakarta.validation.Valid;
// Note: If you are using an older version of Spring Boot (2.x), it will be: import javax.validation.Valid;
import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor

public class UserController {
    private final  UserService userService;
@GetMapping("/{userId}/validate")

 public ResponseEntity<Boolean> validateUser(@PathVariable String userId){
    return ResponseEntity.ok(userService.existByUserId(userId)) ;
}
@PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request){

       return ResponseEntity.ok(userService.register(request));
    }
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, welcome to the Fitness Microservice!";
    }
}
