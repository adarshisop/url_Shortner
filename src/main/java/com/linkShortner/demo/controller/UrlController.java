package com.linkShortner.demo.controller;

import com.linkShortner.demo.DTO.ShortenRequest;
import com.linkShortner.demo.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URL;

@RequiredArgsConstructor
@RestController
public class UrlController {
    private final UrlService urlService;

    @RequestMapping("/api/urls/shorten")
    public ResponseEntity<String> shorten(@RequestBody ShortenRequest request){
        try{
            String email = "test@gmail.com";
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

}
