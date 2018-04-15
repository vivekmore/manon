package manon.app.cache;

import manon.app.info.service.InfoServiceImpl;
import manon.game.world.service.WorldServiceImpl;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static manon.app.config.SpringProfiles.REDIS_CACHE;

@Configuration
@Profile(REDIS_CACHE)
public class RedisCacheConfig extends CachingConfigurerSupport {
    
    @Bean
    public RedisCacheManager cacheManager(RedisTemplate redisTemplate) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1));
        Set<String> cacheNames = new HashSet<>();
        cacheNames.addAll(InfoServiceImpl.CACHES);
        cacheNames.addAll(WorldServiceImpl.CACHES);
        return RedisCacheManager.builder(redisTemplate.getConnectionFactory())
                .cacheDefaults(config)
                .initialCacheNames(cacheNames)
                .build();
    }
}
