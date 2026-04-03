package com.linkShortner.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table(name = "clicks")
@Entity
public class Click {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id ;

    @ManyToOne
    @JoinColumn(name = "url_id")
    private Url url;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @PrePersist
    public void prePersist(){
        this.clickedAt = LocalDateTime.now();
    }
}
