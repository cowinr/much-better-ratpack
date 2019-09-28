package com.example.muchbetter;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Simple auth token generator.
 */
@Component
public class TokenGenerator {

    /**
     * Generate simple random String.
     */
    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}