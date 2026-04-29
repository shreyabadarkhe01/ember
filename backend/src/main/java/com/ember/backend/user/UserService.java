//package com.ember.backend.user;
//
//import com.ember.backend.common.AppException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class UserService {
//
//    private final UserRepository userRepository;
//    private final UserMapper userMapper;
//
//    public UserDto createUser(User user) {
//        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
//            throw new AppException("Email already exists", HttpStatus.BAD_REQUEST);
//        }
//        return userMapper.toDto(userRepository.save(user));
//    }
//
//    public List<UserDto> getAllUsers() {
//        return userRepository.findAll()
//                .stream()
//                .map(userMapper::toDto)
//                .collect(Collectors.toList());
//    }
//
//    public UserDto getUserById(Long id) {
//        return userRepository.findById(id)
//                .map(userMapper::toDto)
//                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
//    }
//}


package com.ember.backend.user;

import com.ember.backend.common.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    public UserDto createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new AppException("Email already exists", HttpStatus.BAD_REQUEST);
        }
        // Hash the password before saving — NEVER store plain text
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userMapper.toDto(userRepository.save(user));
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        return userMapper.toDto(user); // convert before returning
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found with email: " + email, HttpStatus.NOT_FOUND));
    }
}

