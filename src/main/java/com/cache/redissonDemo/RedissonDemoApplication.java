package com.cache.redissonDemo;

import com.cache.redissonDemo.model.Post;
import com.cache.redissonDemo.repository.PostRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;

import java.util.Arrays;

@SpringBootApplication
@RequiredArgsConstructor
public class RedissonDemoApplication {

	private static final Logger logger = LoggerFactory.getLogger(RedissonDemoApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(RedissonDemoApplication.class, args);
	}
	private final PostRepository postRepository;
	private final UnifiedJedis jedis;

//	@Autowired
//	private PostRepository postRepository;
//	@Autowired
//	private UnifiedJedis jedis;

	@Value("classpath:data.json")
	Resource resourceFile;

	@Bean
	CommandLineRunner loadData() {

		logger.info("loadData ============> CommandLineRunner");

		return args -> {

			postRepository.deleteAll();

			try {
				jedis.ftDropIndex("post-idx");
			} catch (Exception e) {
				logger.error(e.getMessage());
				logger.info("Index is not available");
			}

			String data = new String(resourceFile.getInputStream().readAllBytes());

			ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			Post[] posts = objectMapper.readValue(data, Post[].class);

			Arrays.stream(posts).forEach(post -> {
				try {
					postRepository.save(post);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
			});
			logger.info("loadData ============> Successful");

			Schema schema = new Schema()
					.addField(new Schema.Field(FieldName.of("$.content").as("content"), Schema.FieldType.TEXT, true, false))
					.addField(new Schema.Field(FieldName.of("$.title").as("title"), Schema.FieldType.TEXT, false, false))
					.addField(new Schema.Field(FieldName.of("$.tags[*]").as("tags"), Schema.FieldType.TAG, false, false))
					.addField(new Schema.Field(FieldName.of("$.views").as("views"), Schema.FieldType.NUMERIC, false, true));

			IndexDefinition indexDefinition
					= new IndexDefinition(IndexDefinition.Type.JSON).setPrefixes(new String[] {"post:"});

			jedis.ftCreate("post-idx",
					IndexOptions.defaultOptions().setDefinition(indexDefinition),
					schema);


			System.out.println(schema.toString());

		};
	}

}
