package com.linkShortner.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;


    @Column(nullable =  false)
    private String password ;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePesist(){
        this.createdAt = LocalDateTime.now();
    }
}
