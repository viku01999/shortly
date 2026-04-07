package com.urlshortener.shortly.service;

import com.urlshortener.shortly.dto.UrlRequestDTO;
import com.urlshortener.shortly.dto.UrlResponseDTO;
import com.urlshortener.shortly.entity.UrlMapping;
import com.urlshortener.shortly.repository.UrlMappingRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UrlShortenerService {

    private final UrlMappingRepository urlMappingRepository;

    public UrlShortenerService(UrlMappingRepository repository) {
        this.urlMappingRepository = repository;
    }

    public UrlResponseDTO shortenUrl(UrlRequestDTO request) {
        Optional<UrlMapping> existing = urlMappingRepository.findByLongUrl(request.getLongUrl());
        if (existing.isPresent()) {
            return new UrlResponseDTO("http://localhost:6690/api/url/" + existing.get().getShortCode());
        }

        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setLongUrl(request.getLongUrl());
        urlMapping.setShortCode(UUID.randomUUID().toString().substring(0, 8));

        urlMappingRepository.save(urlMapping);
        return new UrlResponseDTO("http://localhost:6690/api/url/" + urlMapping.getShortCode());
    }

    public String getOriginalUrl(String shortCode) {
        UrlMapping urlMapping = urlMappingRepository.findByShortCode(shortCode).orElseThrow(
                () -> new RuntimeException("URL not found")
        );
        urlMapping.setClickCount(urlMapping.getClickCount() + 1);
        urlMappingRepository.save(urlMapping);

        return urlMapping.getLongUrl();
    }

}
