package com.linkShortner.demo.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.linkShortner.demo.DTO.ShortenRequest;
import com.linkShortner.demo.entity.Click;
import com.linkShortner.demo.entity.Url;
import com.linkShortner.demo.entity.User;
import com.linkShortner.demo.repository.ClickRepository;
import com.linkShortner.demo.repository.UrlRepository;
import com.linkShortner.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UrlService {
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final ClickRepository clickRepository;
    private final RedisTemplate<String , String> redisTemplate;

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
        if (request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
            if (urlRepository.findByShortCode(request.getCustomCode()).isPresent()) {
                throw new RuntimeException("Custom code already taken");
            }
            shortCode = request.getCustomCode();
        } else {
            do {
                shortCode = generateShortCode();
            } while (urlRepository.findByShortCode(shortCode).isPresent());
        }

        Url url = new Url();
        url.setOriginalUrl(request.getOriginalUrl());
        url.setShortCode(shortCode);
        url.setUser(user);
        url.setExpiresAt(request.getExpiresAt());
        urlRepository.save(url);

        return baseUrl + "/" + shortCode;
    }

    public String getOriginalUrl(String shortCode) {
        String cachedUrl = redisTemplate.opsForValue().get(shortCode);
        if (cachedUrl != null) {
            return cachedUrl;
        }

       Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("URL has expired");
        }

        // record the click
        Click click = new Click();
        click.setUrl(url);
        clickRepository.save(click);

        // update total click count
        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        // store in cache for next time
        redisTemplate.opsForValue().set(shortCode, url.getOriginalUrl(), 24, java.util.concurrent.TimeUnit.HOURS);

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
        urlRepository.delete(url);
        redisTemplate.delete(shortCode); // remove from cache
       return "Url deleted successfuly";
    }

    public byte[] generateQrCode(String shortCode) throws Exception{
       Url url = urlRepository.findByShortCode(shortCode)
               .orElseThrow(() -> new RuntimeException("Url not found"));
       String fullShortUrl = baseUrl + "/" + shortCode;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                fullShortUrl ,
                BarcodeFormat.QR_CODE,
                200,
                200
        );
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix , "PNG" , outputStream);
        return outputStream.toByteArray();
    }

    public Map<String, Long> getAnalytics(String shortCode, String email) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        if (!url.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not authorized to view these analytics");
        }

        LocalDateTime now = LocalDateTime.now();

        Long today = clickRepository.countClicksSince(url, now.toLocalDate().atStartOfDay());
        Long thisWeek = clickRepository.countClicksSince(url, now.minusDays(7));
        Long thisMonth = clickRepository.countClicksSince(url, now.minusDays(30));
        Long total = (long) clickRepository.findByUrl(url).size();

        Map<String, Long> analytics = new HashMap<>();
        analytics.put("today", today);
        analytics.put("thisWeek", thisWeek);
        analytics.put("thisMonth", thisMonth);
        analytics.put("total", total);

        return analytics;
    }

}
