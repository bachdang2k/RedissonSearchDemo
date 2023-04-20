package com.cache.redissonDemo;

import com.cache.redissonDemo.model.Post;
import com.cache.redissonDemo.repository.PostRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Array;
import java.util.Arrays;

@SpringBootApplication
@RequiredArgsConstructor
public class RedissonDemoApplication {

	private static final Logger logger = LoggerFactory.getLogger(RedissonDemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(RedissonDemoApplication.class, args);
	}

	private final PostRepository postRepository;

	private final RedisTemplate<String, String> redisTemplate;

	@Value("classpath:data.json")
	Resource resourceFile;

	@Bean
	CommandLineRunner loadData() {

		logger.info("loadData ============> CommandLineRunner");

		return args -> {

			String data = new String(resourceFile.getInputStream().readAllBytes());

			ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			Post[] posts = objectMapper.readValue(data, Post[].class);

			Arrays.stream(posts).forEach(post -> {
				try {
					postRepository.save(post, 300);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
			});

		};
	}

}
