package com.linkShortner.demo.controller;

import com.linkShortner.demo.DTO.ShortenRequest;
import com.linkShortner.demo.entity.Url;
import com.linkShortner.demo.entity.User;
import com.linkShortner.demo.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URL;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class UrlController {
    private final UrlService urlService;

    @RequestMapping("/api/urls/shorten")
    public ResponseEntity<String> shorten(@RequestBody ShortenRequest request){
        try{
            String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String shortUrl = urlService.shortenUrl(request, email);
            return ResponseEntity.ok(shortUrl);
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode ){
       try{
           String originalUrl =  urlService.getOriginalUrl(shortCode);
           return ResponseEntity.status(302)
                   .location(URI.create(originalUrl))
                   .build();
       }catch (RuntimeException e){
           return ResponseEntity.notFound().build();
       }
    }

    @GetMapping("/api/urls/my-links")
    ResponseEntity<?> getAllUrls(){
        try {
            String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Url> urls = urlService.getUrlsByUser(email);
            return ResponseEntity.ok(urls);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("api/urls/{shortCode}")
    public ResponseEntity<String> deleteUrl(@PathVariable String shortCode){
        try {
            String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String result = urlService.deleteUrl(shortCode, email);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
