package com.fitness.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true , nullable = false)
    private String email;
    
    // 🟢 CHANGED: Swapped keycloakId for firebaseId and mapped it cleanly
    @Column(name = "firebase_id", unique = true)
    private String firebaseId; 
    
    @Column(nullable = false)
    private String password;
    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedId; // Note: This is usually named 'updatedAt', but leaving it as 'updatedId' so it doesn't break your existing DTOs!
}
