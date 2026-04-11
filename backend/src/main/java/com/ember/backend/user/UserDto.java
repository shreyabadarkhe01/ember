package com.ember.backend.user;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
}