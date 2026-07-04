package com.fitness.userservice.service;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.model.User;
import com.fitness.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public UserResponse register(RegisterRequest request) {
        // 1. If user already exists by email, map and return existing details
        if (repository.existsByEmail(request.getEmail())) {
            User existingUser = repository.findByEmail(request.getEmail());
            UserResponse userResponse = new UserResponse();
            
            userResponse.setId(existingUser.getId());
            userResponse.setFirebaseId(existingUser.getFirebaseId()); // Changed from Keycloak and fixed duplicate assignment
            userResponse.setEmail(existingUser.getEmail());
            userResponse.setPassword(existingUser.getPassword());
            userResponse.setLastName(existingUser.getLastName());
            userResponse.setFirstName(existingUser.getFirstName());
            userResponse.setCreatedAt(existingUser.getCreatedAt());
            userResponse.setUpdatedId(existingUser.getUpdatedId());
            
            return userResponse;
        }

        // 2. Map incoming request fields to the new User Entity structure
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setFirebaseId(request.getFirebaseId()); // Changed from keycloakId

        User savedUser = repository.save(user);

        // 3. Construct and return response DTO
        UserResponse userResponse = new UserResponse();
        userResponse.setId(savedUser.getId());
        userResponse.setFirebaseId(savedUser.getFirebaseId()); // Changed from Keycloak and fixed duplicate assignment
        userResponse.setEmail(savedUser.getEmail());
        userResponse.setPassword(savedUser.getPassword());
        userResponse.setLastName(savedUser.getLastName());
        userResponse.setFirstName(savedUser.getFirstName());
        userResponse.setCreatedAt(savedUser.getCreatedAt());
        userResponse.setUpdatedId(savedUser.getUpdatedId());

        return userResponse;
    }

    public UserResponse getUserProfile(String userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setFirebaseId(user.getFirebaseId()); // Ensure this is returned in profile queries
        userResponse.setEmail(user.getEmail());
        userResponse.setPassword(user.getPassword());
        userResponse.setLastName(user.getLastName());
        userResponse.setFirstName(user.getFirstName());
        
        return userResponse;
    }

    public Boolean existByUserId(String userId) {
        // Changed repository method invocation to match the updated field name
        return repository.existsByFirebaseId(userId);
    }
}
