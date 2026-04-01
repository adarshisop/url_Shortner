package com.linkShortner.demo.repository;

import com.linkShortner.demo.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortCode(String shortCode);
    List<Url> findByUserId(Long userId);
}
