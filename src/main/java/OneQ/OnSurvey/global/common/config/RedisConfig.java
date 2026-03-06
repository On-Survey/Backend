package OneQ.OnSurvey.global.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Bean
    public RedissonClient redisson(RedisProperties redisProperties) {
        Config config = new Config();

        SingleServerConfig singleServerConfig = config.useSingleServer()
            .setAddress(String.format("redis://%s:%d", redisProperties.getHost(), redisProperties.getPort()));
        if (redisProperties.getPassword() != null && !redisProperties.getPassword().isBlank()) {
            singleServerConfig.setPassword(redisProperties.getPassword());
        }

        return Redisson.create(config);
    }
}
