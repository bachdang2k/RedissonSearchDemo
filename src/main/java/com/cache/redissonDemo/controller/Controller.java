package com.cache.redissonDemo.controller;

import com.cache.redissonDemo.model.Post;
import com.cache.redissonDemo.repository.PostRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class Controller {

    private final PostRepository postRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Post> getObject(@PathVariable String id) throws JsonProcessingException {
        return new ResponseEntity<>(postRepository.findById(id), HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<Post>> getAll() throws JsonProcessingException {
        return new ResponseEntity<>(postRepository.getAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Post> putObject(@RequestBody Post post) throws JsonProcessingException {
        return new ResponseEntity<>(postRepository.save(post), HttpStatus.CREATED);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteObject(@PathVariable String id) {
        return new ResponseEntity<>(postRepository.delete(id), HttpStatus.OK);
    }
}
