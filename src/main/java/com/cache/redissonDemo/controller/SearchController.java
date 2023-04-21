package com.cache.redissonDemo.controller;

import com.cache.redissonDemo.model.Page;
import com.cache.redissonDemo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class SearchController {
    private final PostService postService;

    @GetMapping("/search")
    public Page search(@RequestParam(name = "content", required = false) String content,
                       @RequestParam(name = "tags", required = false) Set<String> tags,
                       @RequestParam(name = "page", defaultValue = "1") Integer page
                       ) {
        return postService.search(content, tags, page);
    }


}
