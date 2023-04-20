package com.cache.redissonDemo.service;

public class CacheKey {

    public static String genPostKey(String postId){
        return "post:" + postId;
    }

}
