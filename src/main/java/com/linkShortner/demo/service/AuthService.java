package com.linkShortner.demo.service;

import com.linkShortner.demo.DTO.LoginRequest;
import com.linkShortner.demo.DTO.RegisterRequest;
import com.linkShortner.demo.entity.User;
import com.linkShortner.demo.repository.UserRepository;
import com.linkShortner.demo.security.JwtFilter;
import com.linkShortner.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

 public String register(RegisterRequest request){
     if (userRepository.findByEmail(request.getEmail()).isPresent()) {
         throw new RuntimeException("User already exists");
     }
     User user = new User();
     user.setEmail(request.getEmail());
     user.setPassword(passwordEncoder.encode(request.getPassword()));
     userRepository.save(user);
     return "user registered successfully";
 }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(user.getEmail());
    }
}
