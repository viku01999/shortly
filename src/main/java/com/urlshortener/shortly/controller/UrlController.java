package com.urlshortener.shortly.controller;

import com.urlshortener.shortly.dto.UrlRequestDTO;
import com.urlshortener.shortly.dto.UrlResponseDTO;
import com.urlshortener.shortly.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/url")
public class UrlController {

    private final UrlShortenerService urlShortenerService;

    public UrlController(UrlShortenerService service) {
        this.urlShortenerService = service;
    }

    @PostMapping("/shorten")
    public ResponseEntity<UrlResponseDTO> shortenUrl(@RequestBody UrlRequestDTO requestDTO) {
        return ResponseEntity.ok(urlShortenerService.shortenUrl(requestDTO));
    }

    @GetMapping("/{shortCode}")
    public void redirect(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        try {
            String longUrl = urlShortenerService.getOriginalUrl(shortCode);
            response.sendRedirect(longUrl);
        } catch (RuntimeException ex) {
            if ("URL not found".equals(ex.getMessage())) {
                response.setContentType("text/plain");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("This is an invalid short URL!");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Error: " + ex.getMessage());
            }
        }
    }
}
