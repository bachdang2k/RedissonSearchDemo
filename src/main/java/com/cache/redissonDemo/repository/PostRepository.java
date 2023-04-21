package com.cache.redissonDemo.repository;

import com.cache.redissonDemo.model.CategoryStats;
import com.cache.redissonDemo.model.Page;
import com.cache.redissonDemo.model.Post;
import com.cache.redissonDemo.service.CacheKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.aggr.Reducers;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Repository
@RequiredArgsConstructor
public class PostRepository {

    private static final Integer PAGE_SIZE = 5;
    private static final long EXPIRE_TIME = 300;
    private final RedisTemplate<String, String> redisTemplate;
    private final UnifiedJedis jedis;

    public Post save(Post post) throws JsonProcessingException {
        if (post.getPostId() == null) {
            post.setPostId(UUID.randomUUID().toString());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String key = "post:" + post.getPostId();
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(post), EXPIRE_TIME, TimeUnit.SECONDS);
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

    public void deleteAll() {
        Set<String> keys = redisTemplate.opsForSet().members("post");
        if (!ObjectUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
        redisTemplate.delete("post");
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

    public Page search(String content, Set<String> tags, Integer page) {

        Long totalResults = 0L;

        StringBuilder queryBuilder = new StringBuilder();

        if (content != null && !ObjectUtils.isEmpty(content)) {
            queryBuilder.append("@content:").append(content);
        }

        if (tags != null && !ObjectUtils.isEmpty(tags)) {
            queryBuilder.append("@tag:{").append(tags.stream().collect(Collectors.joining("|"))).append("}");
        }

        String queryCriteria = queryBuilder.toString();
        Query query = null;

        if (ObjectUtils.isEmpty(queryCriteria)) {
            query = new Query();
        } else {
            query = new Query(queryCriteria);
        }

        query.limit(PAGE_SIZE * (page-1), PAGE_SIZE);
        SearchResult searchResult = jedis.ftSearch("post-idx", query);
        totalResults = searchResult.getTotalResults();
        int numberOfPage = (int) Math.ceil((double)totalResults/PAGE_SIZE);

        List<Post> postList = searchResult.getDocuments()
                .stream()
                .map(this::convertDocumentToPostByGson)
                .collect(Collectors.toList());

        return Page.builder()
                .posts(postList)
                .total(totalResults)
                .totalPage(numberOfPage)
                .currentPage(page)
                .build();
    }

    private Post convertDocumentToPostByGson(Document document) {
        Gson gson = new Gson();
        String jsonDoc = document
                .getProperties()
                .iterator()
                .next()
                .getValue()
                .toString();
        return gson.fromJson(jsonDoc, Post.class);
    }

    private Post convertDocumentToPostByJackson(String post) throws JsonProcessingException {
        return new ObjectMapper().readValue(post, Post.class);
    }

    public List<CategoryStats> getCategoryWiseTotalPost() {
        AggregationBuilder aggregationBuilder = new AggregationBuilder();

        aggregationBuilder.groupBy("@tags",
                Reducers.count().as("NO_OF_POST"),
                Reducers.avg("views").as("AVERAGE_VIEWS"));

        AggregationResult aggregationResult = jedis.ftAggregate("post-idx", aggregationBuilder);

        List<CategoryStats> categoryStatsList = new ArrayList<>();

        LongStream.range(0, aggregationResult.getTotalResults())
                .mapToObj(idx -> aggregationResult.getRow((int) idx))
                .forEach(row -> {
                    categoryStatsList.add(
                            CategoryStats.builder()
                                    .totalPosts(row.getLong("NO_OF_POST"))
                                    .averageViews(new DecimalFormat("#.##").format(row.getDouble("AVERAGE_VIEWS")))
                                    .tags(row.getString("tags"))
                                    .build()
                    );
                });

        return categoryStatsList;
    }
}
