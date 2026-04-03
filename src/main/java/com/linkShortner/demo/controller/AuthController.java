package com.linkShortner.demo.controller;

import com.linkShortner.demo.DTO.LoginRequest;
import com.linkShortner.demo.DTO.RegisterRequest;
import com.linkShortner.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
   private final AuthService authService;


   @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request){

           String result = authService.register(request);
           return ResponseEntity.ok(result);


       }

     @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request){

           String result = authService.login(request);
           return  ResponseEntity.ok(result);

     }
}
