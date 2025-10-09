package org.perun.registrarprototype.security.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is to ensure that userinfo and perun ID (and potentially more data if extended) is cached and not called for each request.
 * TODO determine the cache duration and size
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager manager = new SimpleCacheManager();

    CaffeineCache userInfoCache = new CaffeineCache("userInfo", Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)  // TODO set this to more or less match JWT token expiration
        .maximumSize(5000)                       // TODO no idea how many active users there can be at once
        .recordStats().build());

    CaffeineCache perunIdCache = new CaffeineCache("perunUserId", Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)  // TODO this can prolly be more that the jwt exp
        .maximumSize(5000)                       // TODO no idea how many active users there can be at once
        .recordStats().build());

    CaffeineCache rolesCache = new CaffeineCache("roles", Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)  // TODO set this via config property (as well as the others)
        .maximumSize(5000)                       // TODO no idea how many active users there can be at once
        .recordStats().build());

    manager.setCaches(List.of(userInfoCache, perunIdCache, rolesCache));
    return manager;
  }
}