package com.ember.backend.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    //@PrePersist is a JPA lifecycle callback used to execute logic before an entity is persisted to the database.
    //@CreationTimeStamp can also be used to automatically set the createdAt field when the entity is saved, but it requires additional configuration and dependencies.

    @PrePersist
    //JPA needs a method to execute at a specific lifecycle event. Annotations like PrePersist don’t work on variables, they work on methods.
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}