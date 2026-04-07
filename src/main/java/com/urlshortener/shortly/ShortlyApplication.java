package com.urlshortener.shortly;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;


@SpringBootApplication
public class ShortlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShortlyApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady(){
		System.out.println("Server started at http://localhost:6690");
	}

	/**
	 * To check redis connection -> working or not
	 * */
	@Bean
	public CommandLineRunner testRedisConnection(RedisTemplate<String, Object> redisTemplateTestConnection) {
		return args -> {
			try {
				redisTemplateTestConnection.opsForValue().set("test-key", "redis-working");
				String value = (String) redisTemplateTestConnection.opsForValue().get("test-key");
				System.out.println("Redis Connected! Value: " + value);
			} catch (Exception e) {
				System.out.println("Redis NOT Connected: " + e.getMessage());
			}
		};
	}

}
