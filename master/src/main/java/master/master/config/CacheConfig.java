package master.master.config;

import java.time.Duration;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
    RedisCacheConfiguration reservationsCache =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues();
    RedisCacheConfiguration planningCache =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .disableCachingNullValues();

    return builder ->
        builder
            .withCacheConfiguration("clientReservations", reservationsCache)
            .withCacheConfiguration("clientReservationDtos", reservationsCache)
            .withCacheConfiguration("employeePlanning", planningCache)
            .withCacheConfiguration("employeePlannings", planningCache)
            .withCacheConfiguration("employeeWeeklySchedule", planningCache)
            .withCacheConfiguration("employeeHourlyPlanning", planningCache);
  }
}
