package com.urlshortener.shortly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;


@SpringBootApplication
public class ShortlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShortlyApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady(){
		System.out.println("Server started at http://localhost:6690");
	}

}
