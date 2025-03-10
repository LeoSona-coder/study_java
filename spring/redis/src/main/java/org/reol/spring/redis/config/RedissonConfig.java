package org.reol.spring.redis.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(
        prefix = "spring.redis"
)
@Data
@Configuration
public class RedissonConfig {
    private String host;
    private String port;
    private Integer database;
    private String password;
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer()
                .setAddress(String.format("redis://%s:%s",host,port))
                .setPassword(password)
                .setDatabase(database);
        return Redisson.create(config);
    }

}
