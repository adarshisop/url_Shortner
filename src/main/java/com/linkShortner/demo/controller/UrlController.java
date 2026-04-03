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
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class UrlController {
    private final UrlService urlService;

    @RequestMapping("/api/urls/shorten")
    public ResponseEntity<String> shorten(@RequestBody ShortenRequest request){

            String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String shortUrl = urlService.shortenUrl(request, email);
            return ResponseEntity.ok(shortUrl);

    }
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode ){

           String originalUrl =  urlService.getOriginalUrl(shortCode);
           return ResponseEntity.status(302)
                   .location(URI.create(originalUrl))
                   .build();

    }

    @GetMapping("/api/urls/my-links")
    ResponseEntity<?> getAllUrls(){

            String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Url> urls = urlService.getUrlsByUser(email);
            return ResponseEntity.ok(urls);

    }

    @DeleteMapping("api/urls/{shortCode}")
    public ResponseEntity<String> deleteUrl(@PathVariable String shortCode){

            String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String result = urlService.deleteUrl(shortCode, email);
            return ResponseEntity.ok(result);

    }
    @GetMapping("api/urls/{shortCode}/qr")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String shortCode) throws Exception{
        byte[] qrCode = urlService.generateQrCode(shortCode);
        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(qrCode);

    }

    @GetMapping("/api/urls/{shortCode}/analytics")
    public ResponseEntity<Map<String, Long>> getAnalytics(@PathVariable String shortCode) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String, Long> analytics = urlService.getAnalytics(shortCode, email);
        return ResponseEntity.ok(analytics);
    }
}
