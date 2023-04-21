package com.cache.redissonDemo.service;

import com.cache.redissonDemo.model.Page;
import com.cache.redissonDemo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    public Page search(String content, Set<String> tags, Integer page) {
        return postRepository.search(content, tags, page);
    }
}
