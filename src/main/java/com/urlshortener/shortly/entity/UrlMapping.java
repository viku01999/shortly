package com.urlshortener.shortly.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "url_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "long_user", nullable = false, unique = true)
    private String longUrl;

    @Column(name = "short_code", nullable = false, unique = true)
    private String shortCode;

    @Column(name = "click_count")
    private int clickCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    @CreationTimestamp
    private Instant createdAt;
}
