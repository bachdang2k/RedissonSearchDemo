package com.cache.redissonDemo.repository;

import com.cache.redissonDemo.model.Post;
import com.cache.redissonDemo.service.CacheKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class PostRepository {

    private final RedisTemplate<String, String> redisTemplate;



    public Post save(Post post, long expireTime) throws JsonProcessingException {
        if (post.getPostId() == null) {
            post.setPostId(UUID.randomUUID().toString());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String key = "post:" + post.getPostId();
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(post),expireTime, TimeUnit.SECONDS);
        redisTemplate.opsForSet().add("post", key);
        return post;
    }

    public List<Post> getAll() throws JsonProcessingException {
        Set<String> keys = redisTemplate.opsForSet().members("post");
        if (!ObjectUtils.isEmpty(keys)) {
            List<String> posts = redisTemplate.opsForValue().multiGet(keys);
            List<Post> result = new ArrayList<>();
            assert posts != null;
            for (String post:posts) {
                result.add(new ObjectMapper().readValue(post, Post.class));
            }
            return result;
        }
        return null;
    }

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
