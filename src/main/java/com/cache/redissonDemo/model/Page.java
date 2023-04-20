package com.cache.redissonDemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class Page {
    private List<Page> posts;
    private Integer totalPage;
    private Integer currentPage;
    private Long total;
}
