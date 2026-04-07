package com.urlshortener.shortly.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.urlshortener.shortly.entity.UrlMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, UrlMapping> redisTemplate(RedisConnectionFactory factory) {
        /**
         * Without java module
         * */
        RedisTemplate<String, UrlMapping> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 1. Configure ObjectMapper to handle Java 8 dates (Instant) (Register all java module)
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Fixes the "Instant not supported" error
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Saves dates in readable ISO-8601 format

        // 2. Use the Typed Serializer with your configured mapper
        // Note: Use the constructor since .setObjectMapper() is deprecated in Spring Data Redis 3.x+
        Jackson2JsonRedisSerializer<UrlMapping> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, UrlMapping.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // For hash serializer
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();

        /**
         * Without java module
         * */
//        RedisTemplate<String, UrlMapping> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//
//        // Using the Typed Serializer fixes the "@class" issue
//        // Since there are no Dates, we don't need a custom ObjectMapper module
//        Jackson2JsonRedisSerializer<UrlMapping> serializer =
//                new Jackson2JsonRedisSerializer<>(UrlMapping.class);
//
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(serializer);


        return template;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplateTestConnection(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

}
