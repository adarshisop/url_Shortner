package com.linkShortner.demo.repository;

import com.linkShortner.demo.entity.Click;
import com.linkShortner.demo.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ClickRepository extends JpaRepository<Click, Long> {
    List<Click> findByUrl(Url url);

    @Query("SELECT COUNT(c) FROM Click c WHERE c.url = :url AND c.clickedAt >= :since")
    Long countClicksSince(@Param("url") Url url, @Param("since") LocalDateTime since);
}
