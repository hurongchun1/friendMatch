package com.hrc.friendMatch.config;

import io.lettuce.core.RedisClient;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * @FileName: RedissonConfig
 * @Description:
 * @Author: hrc
 * @CreateTime: 2025/5/11 21:27
 * @Version: 1.0.0
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "spring.redis")
public class RedissonConfig {

    private String url;

    private String password;

    private int database;

    @Bean
    public RedissonClient redissonClient(){

        Config config = new Config();
        config.useSingleServer().setAddress(url)
                .setPassword(password)
                .setDatabase(database);

        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
