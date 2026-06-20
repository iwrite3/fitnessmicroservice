package com.fitness.userservice.service;
import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.model.User;
import com.fitness.userservice.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;
    public UserResponse register( RegisterRequest request) {
        if(repository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already exist");
        }
   User user = new User();
    user.setEmail(request.getEmail());
    user.setPassword(request.getPassword());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    User savedUser =   repository.save(user);
    UserResponse userResponse = new UserResponse();
    userResponse.setId(savedUser.getId());
        userResponse.setEmail(savedUser.getEmail());
        userResponse.setPassword(savedUser.getPassword());
        userResponse.setLastName(savedUser.getLastName());
        userResponse.setFirstName(savedUser.getFirstName());
        userResponse.setCreatedAt(savedUser.getCreatedAt());
        userResponse.setUpdatedId(savedUser.getUpdatedId());

return userResponse;

   }
   public UserResponse getUserProfile(String userId){
        User user = repository.findById(userId)
       .orElseThrow(() ->new RuntimeException("User Not Found") );

       UserResponse userResponse = new UserResponse();
       userResponse.setId(user.getId());
       userResponse.setEmail(user.getEmail());
       userResponse.setPassword(user.getPassword());
       userResponse.setLastName(user.getLastName());
       userResponse.setFirstName(user.getFirstName());
return userResponse;
   }

    public Boolean existByUserId(String userId) {
        return repository.existsById(userId);
    }
}

