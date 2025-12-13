package com.gradr;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Statistics - Serializable class for caching statistics data
 * Used with ConcurrentHashMap for thread-safe statistics caching
 */
public class Statistics implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private double classAverage;
    private int totalStudents;
    private int totalGrades;
    private double highestGrade;
    private double lowestGrade;
    private LocalDateTime lastCalculated;
    private String cacheKey;
    
    public Statistics(String cacheKey, double classAverage, int totalStudents, 
                    int totalGrades, double highestGrade, double lowestGrade) {
        this.cacheKey = cacheKey;
        this.classAverage = classAverage;
        this.totalStudents = totalStudents;
        this.totalGrades = totalGrades;
        this.highestGrade = highestGrade;
        this.lowestGrade = lowestGrade;
        this.lastCalculated = LocalDateTime.now();
    }
    
    // Getters
    public double getClassAverage() { return classAverage; }
    public int getTotalStudents() { return totalStudents; }
    public int getTotalGrades() { return totalGrades; }
    public double getHighestGrade() { return highestGrade; }
    public double getLowestGrade() { return lowestGrade; }
    public LocalDateTime getLastCalculated() { return lastCalculated; }
    public String getCacheKey() { return cacheKey; }
    
    // Setters
    public void setClassAverage(double classAverage) { this.classAverage = classAverage; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    public void setTotalGrades(int totalGrades) { this.totalGrades = totalGrades; }
    public void setHighestGrade(double highestGrade) { this.highestGrade = highestGrade; }
    public void setLowestGrade(double lowestGrade) { this.lowestGrade = lowestGrade; }
    public void setLastCalculated(LocalDateTime lastCalculated) { this.lastCalculated = lastCalculated; }
    
    public boolean isExpired(long cacheTimeoutMinutes) {
        if (lastCalculated == null) return true;
        return java.time.Duration.between(lastCalculated, LocalDateTime.now())
                .toMinutes() > cacheTimeoutMinutes;
    }
}

