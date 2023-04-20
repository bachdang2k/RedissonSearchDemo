package com.cache.redissonDemo.repository;

import com.cache.redissonDemo.model.Post;
import com.cache.redissonDemo.service.CacheKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class PostRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public String put(Post post, long expireTime) throws JsonProcessingException {
        String key = CacheKey.genPostKey(post.getPostId());
        ObjectMapper ow = new ObjectMapper();
        redisTemplate.opsForValue().set(key, ow.writeValueAsString(post), expireTime, TimeUnit.SECONDS);
        return "Create Successful";
    }

    public Post findById(String postId) throws JsonProcessingException {
        String key = "post:" + postId;
        return new ObjectMapper().readValue(redisTemplate.opsForValue().get(key), Post.class);
    }

    public String delete(String postId) {
        String key = "post:" + postId;
        redisTemplate.opsForValue().getAndDelete(key);
        return "Delete Successful";
    }


}
