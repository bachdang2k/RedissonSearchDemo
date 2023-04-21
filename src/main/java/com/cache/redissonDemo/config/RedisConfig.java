package com.cache.redissonDemo.config;

import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.UnifiedJedis;

@Configuration
public class RedisConfig {
    @Bean
    public UnifiedJedis unifiedJedis() {
        return new UnifiedJedis();
    }
}
