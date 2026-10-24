package com.careerforge.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * High-performance caching configuration.
 * Accelerates read-heavy, low-mutation database queries (skills taxonomy,
 * locations, company catalogs, and platform KPI metrics) while enforcing
 * deterministic cache invalidation upon writes.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    public static final String CACHE_SKILLS = "skills";
    public static final String CACHE_LOCATIONS = "locations";
    public static final String CACHE_COMPANIES = "companies";
    public static final String CACHE_COMPANY_BY_ID = "companyById";
    public static final String CACHE_PLATFORM_KPIS = "platformKpis";
    public static final String CACHE_SKILL_DEMAND_SUPPLY = "skillDemandSupply";

    @Bean
    public CacheManager cacheManager() {
        log.info("Initializing high-performance Spring CacheManager with predefined cache domains");
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of(
                CACHE_SKILLS,
                CACHE_LOCATIONS,
                CACHE_COMPANIES,
                CACHE_COMPANY_BY_ID,
                CACHE_PLATFORM_KPIS,
                CACHE_SKILL_DEMAND_SUPPLY
        ));
        return cacheManager;
    }
}
