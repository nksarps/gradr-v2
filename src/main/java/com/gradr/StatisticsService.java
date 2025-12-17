package com.gradr;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * StatisticsService - Responsible for statistics computation and caching
 * Adheres to Single Responsibility Principle by focusing only on statistics
 * 
 * Responsibilities:
 * - Cache computed statistics
 * - Provide statistics with automatic caching
 * - Invalidate stale statistics
 * 
 * Thread Safety:
 * - ConcurrentHashMap for thread-safe statistics cache
 */
public class StatisticsService {
    // ConcurrentHashMap for thread-safe statistics cache
    // Time Complexity: O(1) average case for concurrent access
    private final Map<String, Statistics> statsCache = new ConcurrentHashMap<>();
    
    /**
     * Get or compute statistics with caching
     * Time Complexity: O(1) average case if cached, O(n) if needs computation
     */
    public Statistics getStatistics(String cacheKey, Supplier<Statistics> calculator) {
        Statistics stats = statsCache.get(cacheKey);
        
        if (stats == null || stats.isExpired(5)) { // 5 minute cache timeout
            // Compute statistics
            stats = calculator.get();
            statsCache.put(cacheKey, stats);
        }
        
        return stats;
    }
    
    /**
     * Clear statistics cache
     * Time Complexity: O(1) - ConcurrentHashMap clear operation
     */
    public void clearStatisticsCache() {
        statsCache.clear();
    }
    
    /**
     * Invalidate a specific cache entry
     * Time Complexity: O(1) - ConcurrentHashMap remove operation
     */
    public void invalidateStatistics(String cacheKey) {
        statsCache.remove(cacheKey);
    }
    
    /**
     * Get cache size
     * Time Complexity: O(1) - Map size operation
     */
    public int getCacheSize() {
        return statsCache.size();
    }
}
