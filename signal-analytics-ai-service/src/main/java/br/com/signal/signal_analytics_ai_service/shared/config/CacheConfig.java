package br.com.signal.signal_analytics_ai_service.shared.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "analyticsMySummary",
                "analyticsSellerSummary",
                "analyticsCustomerSummary",
                "analyticsSellerTopProducts",
                "analyticsCustomerSpending",
                "analyticsPeriodSummary",
                "analyticsMyChart",
                "analyticsSellerChart",
                "analyticsCustomerChart",
                "storeById",
                "aiAnswer",
                "knowledgeRetrieval"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(60, TimeUnit.SECONDS));
        return cacheManager;
    }
}
