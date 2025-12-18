package com.gradr;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * CacheManager - Thread-safe caching system with LRU eviction policy
 * 
 * Features:
 * - ConcurrentHashMap for thread-safe cache storage
 * - LRU (Least Recently Used) eviction policy
 * - Maximum cache size (150 entries)
 * - Cache statistics (hit rate, miss rate, times, evictions)
 * - Background thread for stale entry refresh
 * - Cache warming on startup
 * - Manual cache clear option
 * - Cache contents display with access timestamps
 */
public class CacheManager {
    
    /**
     * CacheEntry - Represents a cached item with metadata
     */
    public static class CacheEntry {
        private final String key;
        private final Object value;
        private final CacheType type;
        private final LocalDateTime createdAt;
        private LocalDateTime lastAccessed;
        private final long ttl; // Time to live in milliseconds
        private int accessCount;
        
        public CacheEntry(String key, Object value, CacheType type, long ttl) {
            this.key = key;
            this.value = value;
            this.type = type;
            this.createdAt = LocalDateTime.now();
            this.lastAccessed = LocalDateTime.now();
            this.ttl = ttl;
            this.accessCount = 0;
        }
        
        public void touch() {
            this.lastAccessed = LocalDateTime.now();
            this.accessCount++;
        }
        
        public boolean isExpired() {
            if (ttl <= 0) return false; // No expiration
            long age = java.time.Duration.between(createdAt, LocalDateTime.now()).toMillis();
            return age > ttl;
        }
        
        // Getters
        public String getKey() { return key; }
        public Object getValue() { return value; }
        public CacheType getType() { return type; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getLastAccessed() { return lastAccessed; }
        public long getTtl() { return ttl; }
        public int getAccessCount() { return accessCount; }
    }
    
    /**
     * CacheType - Types of cacheable objects
     */
    public enum CacheType {
        STUDENT,
        GRADE_REPORT,
        STATISTICS
    }
    
    /**
     * CacheStatistics - Statistics about cache performance
     */
    public static class CacheStatistics {
        private final long totalHits;
        private final long totalMisses;
        private final double hitRate;
        private final double missRate;
        private final long averageHitTime;
        private final long averageMissTime;
        private final int totalEntries;
        private final long memoryUsage;
        private final int evictionCount;
        
        public CacheStatistics(long totalHits, long totalMisses, 
                              long totalHitTime, long totalMissTime,
                              int totalEntries, long memoryUsage, int evictionCount) {
            this.totalHits = totalHits;
            this.totalMisses = totalMisses;
            long totalRequests = totalHits + totalMisses;
            this.hitRate = totalRequests > 0 ? (totalHits * 100.0 / totalRequests) : 0.0;
            this.missRate = totalRequests > 0 ? (totalMisses * 100.0 / totalRequests) : 0.0;
            this.averageHitTime = totalHits > 0 ? (totalHitTime / totalHits) : 0;
            this.averageMissTime = totalMisses > 0 ? (totalMissTime / totalMisses) : 0;
            this.totalEntries = totalEntries;
            this.memoryUsage = memoryUsage;
            this.evictionCount = evictionCount;
        }
        
        // Getters
        public long getTotalHits() { return totalHits; }
        public long getTotalMisses() { return totalMisses; }
        public double getHitRate() { return hitRate; }
        public double getMissRate() { return missRate; }
        public long getAverageHitTime() { return averageHitTime; }
        public long getAverageMissTime() { return averageMissTime; }
        public int getTotalEntries() { return totalEntries; }
        public long getMemoryUsage() { return memoryUsage; }
        public int getEvictionCount() { return evictionCount; }
    }
    
    // Thread-safe cache storage
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    
    // LRU tracking - LinkedHashMap for access order
    private final LinkedHashMap<String, String> lruOrder = new LinkedHashMap<>(16, 0.75f, true);
    private final ReentrantReadWriteLock lruLock = new ReentrantReadWriteLock();
    
    // Cache configuration
    private static final int MAX_CACHE_SIZE = 150;
    private static final long DEFAULT_TTL = 30 * 60 * 1000; // 30 minutes
    
    // Statistics tracking
    private final AtomicLong totalHits = new AtomicLong(0);
    private final AtomicLong totalMisses = new AtomicLong(0);
    private final AtomicLong totalHitTime = new AtomicLong(0);
    private final AtomicLong totalMissTime = new AtomicLong(0);
    private final AtomicInteger evictionCount = new AtomicInteger(0);
    
    // Background refresh thread
    private ScheduledExecutorService refreshExecutor;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    
    public CacheManager() {
        startBackgroundRefresh();
    }
    
    /**
     * Get value from cache
     */
    public Object get(String key) {
        long startTime = System.nanoTime();
        
        CacheEntry entry = cache.get(key);
        
        if (entry != null && !entry.isExpired()) {
            // Cache hit
            entry.touch();
            updateLRU(key);
            
            long hitTime = System.nanoTime() - startTime;
            totalHits.incrementAndGet();
            totalHitTime.addAndGet(hitTime);
            
            return entry.getValue();
        } else {
            // Cache miss or expired
            if (entry != null) {
                // Remove expired entry
                cache.remove(key);
                removeFromLRU(key);
            }
            
            long missTime = System.nanoTime() - startTime;
            totalMisses.incrementAndGet();
            totalMissTime.addAndGet(missTime);
            
            return null;
        }
    }
    
    /**
     * Put value into cache
     */
    public void put(String key, Object value, CacheType type) {
        put(key, value, type, DEFAULT_TTL);
    }
    
    /**
     * Put value into cache with custom TTL
     */
    public void put(String key, Object value, CacheType type, long ttl) {
        // Check if we need to evict
        if (cache.size() >= MAX_CACHE_SIZE && !cache.containsKey(key)) {
            evictLRU();
        }
        
        CacheEntry entry = new CacheEntry(key, value, type, ttl);
        cache.put(key, entry);
        updateLRU(key);
    }
    
    /**
     * Update LRU order
     */
    private void updateLRU(String key) {
        lruLock.writeLock().lock();
        try {
            lruOrder.put(key, key); // Access order maintained by LinkedHashMap
        } finally {
            lruLock.writeLock().unlock();
        }
    }
    
    /**
     * Remove from LRU order
     */
    private void removeFromLRU(String key) {
        lruLock.writeLock().lock();
        try {
            lruOrder.remove(key);
        } finally {
            lruLock.writeLock().unlock();
        }
    }
    
    /**
     * Evict least recently used entry
     */
    private void evictLRU() {
        lruLock.writeLock().lock();
        try {
            if (lruOrder.isEmpty()) {
                // Fallback: remove first entry from cache
                String firstKey = cache.keySet().iterator().next();
                cache.remove(firstKey);
                evictionCount.incrementAndGet();
                return;
            }
            
            // Get least recently used (first in LinkedHashMap with access order)
            String lruKey = lruOrder.keySet().iterator().next();
            cache.remove(lruKey);
            lruOrder.remove(lruKey);
            evictionCount.incrementAndGet();
        } finally {
            lruLock.writeLock().unlock();
        }
    }
    
    /**
     * Invalidate cache entry
     */
    public void invalidate(String key) {
        cache.remove(key);
        removeFromLRU(key);
    }
    
    /**
     * Invalidate all entries of a specific type
     */
    public void invalidateByType(CacheType type) {
        List<String> keysToRemove = new ArrayList<>();
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().getType() == type) {
                keysToRemove.add(entry.getKey());
            }
        }
        for (String key : keysToRemove) {
            invalidate(key);
        }
    }
    
    /**
     * Clear all cache
     */
    public void clear() {
        cache.clear();
        lruLock.writeLock().lock();
        try {
            lruOrder.clear();
        } finally {
            lruLock.writeLock().unlock();
        }
    }
    
    /**
     * Get cache statistics
     */
    public CacheStatistics getStatistics() {
        // Estimate memory usage (rough calculation)
        long memoryUsage = cache.size() * 1024; // Rough estimate: 1KB per entry
        
        return new CacheStatistics(
            totalHits.get(),
            totalMisses.get(),
            totalHitTime.get() / 1_000_000, // Convert to milliseconds
            totalMissTime.get() / 1_000_000,
            cache.size(),
            memoryUsage,
            evictionCount.get()
        );
    }
    
    /**
     * Get all cache entries with metadata
     */
    public List<Map<String, Object>> getCacheContents() {
        List<Map<String, Object>> contents = new ArrayList<>();
        
        for (CacheEntry entry : cache.values()) {
            Map<String, Object> entryInfo = new HashMap<>();
            entryInfo.put("key", entry.getKey());
            entryInfo.put("type", entry.getType().name());
            entryInfo.put("createdAt", entry.getCreatedAt());
            entryInfo.put("lastAccessed", entry.getLastAccessed());
            entryInfo.put("accessCount", entry.getAccessCount());
            entryInfo.put("ttl", entry.getTtl());
            entryInfo.put("isExpired", entry.isExpired());
            contents.add(entryInfo);
        }
        
        // Sort by last accessed time (most recent first)
        contents.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("lastAccessed");
            LocalDateTime timeB = (LocalDateTime) b.get("lastAccessed");
            return timeB.compareTo(timeA);
        });
        
        return contents;
    }
    
    /**
     * Start background refresh thread
     */
    private void startBackgroundRefresh() {
        if (isRunning.get()) {
            return;
        }
        
        isRunning.set(true);
        refreshExecutor = Executors.newScheduledThreadPool(1);
        
        // Refresh stale entries every 5 minutes
        refreshExecutor.scheduleAtFixedRate(() -> {
            refreshStaleEntries();
        }, 5, 5, TimeUnit.MINUTES);
    }
    
    /**
     * Refresh stale cache entries
     */
    private void refreshStaleEntries() {
        List<String> staleKeys = new ArrayList<>();
        
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                staleKeys.add(entry.getKey());
            }
        }
        
        // Remove stale entries (they'll be reloaded on next access)
        for (String key : staleKeys) {
            invalidate(key);
        }
    }
    
    /**
     * Cache warming - preload frequently accessed data
     */
    public void warmCache(StudentManager studentManager, GradeManager gradeManager) {
        // Warm student cache
        List<Student> students = studentManager.getStudentsList();
        for (Student student : students) {
            put("student:" + student.getStudentId(), student, CacheType.STUDENT);
        }
        
        // Warm statistics cache (cache the statistics report string)
        try {
            StatisticsCalculator statsCalc = new StatisticsCalculator(gradeManager, studentManager);
            String statsReport = statsCalc.generateClassStatistics();
            if (statsReport != null && !statsReport.isEmpty()) {
                put("statistics:class", statsReport, CacheType.STATISTICS);
            }
        } catch (Exception e) {
            // Ignore - will be computed on demand
        }
    }
    
    /**
     * Shutdown cache manager
     */
    public void shutdown() {
        isRunning.set(false);
        if (refreshExecutor != null) {
            refreshExecutor.shutdown();
            try {
                if (!refreshExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    refreshExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                refreshExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}

