package com.cache.redissonDemo;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class RedissonDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedissonDemoApplication.class, args);
	}

}
