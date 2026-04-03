package com.linkShortner.demo.service;

import com.linkShortner.demo.DTO.ShortenRequest;
import com.linkShortner.demo.entity.Url;
import com.linkShortner.demo.entity.User;
import com.linkShortner.demo.repository.UrlRepository;
import com.linkShortner.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UrlService {
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    @Value("${app.base-url}")
    private String baseUrl;
   public String generateShortCode(){
       String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
       StringBuilder code = new StringBuilder();
       Random random = new Random();
       for (int i = 0; i < 6; i++) {
           code.append(characters.charAt(random.nextInt(characters.length())));
       }
       return code.toString();
   }

    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String shortenUrl(ShortenRequest request, String email) {
        if (!isValidUrl(request.getOriginalUrl())) {
            throw new RuntimeException("Invalid URL");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String shortCode;
        do {
            shortCode = generateShortCode();
        } while (urlRepository.findByShortCode(shortCode).isPresent());

        Url url = new Url();
        url.setOriginalUrl(request.getOriginalUrl());
        url.setShortCode(shortCode);
        url.setUser(user);
        urlRepository.save(url);

        return baseUrl + "/" + shortCode;
    }

    public String getOriginalUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return url.getOriginalUrl();
    }

    public List<Url> getUrlsByUser(String email){
       User user = userRepository.findByEmail(email).orElseThrow( () -> new RuntimeException("User not found"));
       return urlRepository.findByUserId(user.getId());
    }

    public String deleteUrl(String shortCode , String email){
       Url url = urlRepository.findByShortCode(shortCode)
               .orElseThrow(() -> new RuntimeException("Url not found"));
       if(!url.getUser().getEmail().equals(email)){
           throw new RuntimeException("You are not authorized to delete this url");
       }
       urlRepository.delete(url);
       return "Url deleted successfuly";
    }

}
