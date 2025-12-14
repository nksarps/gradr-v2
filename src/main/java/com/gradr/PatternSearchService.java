package com.gradr;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PatternSearchService - Advanced pattern-based search using regex
 * 
 * Features:
 * - Multiple search types (email domain, phone area code, student ID, name, custom)
 * - Match highlighting
 * - Search statistics
 * - Distribution statistics
 * - Case-insensitive matching
 * - Regex complexity hints
 */
public class PatternSearchService {
    
    private final StudentManager studentManager;
    
    public PatternSearchService(StudentManager studentManager) {
        this.studentManager = studentManager;
    }
    
    /**
     * Search result containing matched student and match information
     */
    public static class SearchResult {
        private final Student student;
        private final String matchedField;
        private final String matchedText;
        private final String highlightedText;
        
        public SearchResult(Student student, String matchedField, String matchedText, String highlightedText) {
            this.student = student;
            this.matchedField = matchedField;
            this.matchedText = matchedText;
            this.highlightedText = highlightedText;
        }
        
        public Student getStudent() { return student; }
        public String getMatchedField() { return matchedField; }
        public String getMatchedText() { return matchedText; }
        public String getHighlightedText() { return highlightedText; }
    }
    
    /**
     * Search statistics
     */
    public static class SearchStatistics {
        private final int totalScanned;
        private final int matchesFound;
        private final long searchTime;
        private final String regexComplexity;
        private final Map<String, Integer> distribution;
        
        public SearchStatistics(int totalScanned, int matchesFound, long searchTime, 
                               String regexComplexity, Map<String, Integer> distribution) {
            this.totalScanned = totalScanned;
            this.matchesFound = matchesFound;
            this.searchTime = searchTime;
            this.regexComplexity = regexComplexity;
            this.distribution = distribution;
        }
        
        public int getTotalScanned() { return totalScanned; }
        public int getMatchesFound() { return matchesFound; }
        public long getSearchTime() { return searchTime; }
        public String getRegexComplexity() { return regexComplexity; }
        public Map<String, Integer> getDistribution() { return distribution; }
        
        public double getMatchPercentage() {
            return totalScanned > 0 ? (matchesFound * 100.0 / totalScanned) : 0.0;
        }
    }
    
    /**
     * Search by email domain pattern
     */
    public SearchResults searchByEmailDomain(String domainPattern, boolean caseInsensitive) {
        // Convert pattern to regex (e.g., @university.edu -> .*@university\.edu$)
        String regex = domainPattern.startsWith("@") ? 
            ".*" + Pattern.quote(domainPattern) + "$" : 
            ".*@" + Pattern.quote(domainPattern) + "$";
        return searchByCustomPattern(regex, "email", caseInsensitive);
    }
    
    /**
     * Search by phone area code pattern
     */
    public SearchResults searchByPhoneAreaCode(String areaCodePattern, boolean caseInsensitive) {
        // Pattern matches area code in various phone formats
        String regex = ".*" + Pattern.quote(areaCodePattern) + ".*";
        return searchByCustomPattern(regex, "phone", caseInsensitive);
    }
    
    /**
     * Search by student ID pattern with wildcards
     */
    public SearchResults searchByStudentIdPattern(String idPattern, boolean caseInsensitive) {
        // Convert wildcards: * -> .*, ** -> .+
        String regex = idPattern.replace("**", ".+").replace("*", ".*");
        // Ensure it matches the full ID
        if (!regex.startsWith("^")) regex = "^" + regex;
        if (!regex.endsWith("$")) regex = regex + "$";
        return searchByCustomPattern(regex, "studentId", caseInsensitive);
    }
    
    /**
     * Search by name pattern
     */
    public SearchResults searchByNamePattern(String namePattern, boolean caseInsensitive) {
        // Simple pattern matching in name
        String regex = ".*" + Pattern.quote(namePattern) + ".*";
        return searchByCustomPattern(regex, "name", caseInsensitive);
    }
    
    /**
     * Search by custom regex pattern
     */
    public SearchResults searchByCustomPattern(String regexPattern, String fieldType, boolean caseInsensitive) {
        long startTime = System.currentTimeMillis();
        List<SearchResult> results = new ArrayList<>();
        List<Student> allStudents = studentManager.getStudentsList();
        
        // Compile pattern with case-insensitive flag if needed
        Pattern pattern;
        try {
            int flags = caseInsensitive ? Pattern.CASE_INSENSITIVE : 0;
            pattern = Pattern.compile(regexPattern, flags);
        } catch (java.util.regex.PatternSyntaxException e) {
            throw new IllegalArgumentException(
                "X VALIDATION ERROR: Invalid regex pattern\n" +
                "   Pattern: " + regexPattern + "\n" +
                "   Error: " + e.getMessage() + "\n" +
                "   Please check your regex syntax."
            );
        }
        
        // Analyze regex complexity
        String complexity = analyzeRegexComplexity(regexPattern);
        
        // Search through students
        for (Student student : allStudents) {
            String matchedField = null;
            String matchedText = null;
            String highlightedText = null;
            
            // Check different fields based on fieldType
            if (fieldType == null || fieldType.equals("email")) {
                if (student.getEmail() != null) {
                    Matcher matcher = pattern.matcher(student.getEmail());
                    if (matcher.find()) {
                        matchedField = "email";
                        matchedText = student.getEmail();
                        highlightedText = highlightMatch(student.getEmail(), matcher);
                    }
                }
            }
            
            if (matchedText == null && (fieldType == null || fieldType.equals("phone"))) {
                if (student.getPhone() != null) {
                    Matcher matcher = pattern.matcher(student.getPhone());
                    if (matcher.find()) {
                        matchedField = "phone";
                        matchedText = student.getPhone();
                        highlightedText = highlightMatch(student.getPhone(), matcher);
                    }
                }
            }
            
            if (matchedText == null && (fieldType == null || fieldType.equals("studentId"))) {
                Matcher matcher = pattern.matcher(student.getStudentId());
                if (matcher.find()) {
                    matchedField = "studentId";
                    matchedText = student.getStudentId();
                    highlightedText = highlightMatch(student.getStudentId(), matcher);
                }
            }
            
            if (matchedText == null && (fieldType == null || fieldType.equals("name"))) {
                Matcher matcher = pattern.matcher(student.getName());
                if (matcher.find()) {
                    matchedField = "name";
                    matchedText = student.getName();
                    highlightedText = highlightMatch(student.getName(), matcher);
                }
            }
            
            if (matchedText != null) {
                results.add(new SearchResult(student, matchedField, matchedText, highlightedText));
            }
        }
        
        long searchTime = System.currentTimeMillis() - startTime;
        
        // Calculate distribution statistics
        Map<String, Integer> distribution = calculateDistribution(results, fieldType);
        
        SearchStatistics stats = new SearchStatistics(
            allStudents.size(),
            results.size(),
            searchTime,
            complexity,
            distribution
        );
        
        return new SearchResults(results, stats, regexPattern);
    }
    
    /**
     * Highlight matched portion of text
     */
    private String highlightMatch(String text, Matcher matcher) {
        int start = matcher.start();
        int end = matcher.end();
        return text.substring(0, start) + 
               "[" + text.substring(start, end) + "]" + 
               text.substring(end);
    }
    
    /**
     * Analyze regex complexity
     */
    private String analyzeRegexComplexity(String pattern) {
        // Simple complexity analysis
        if (pattern.contains(".*.*") || pattern.contains(".+.*") || pattern.contains(".*.+")) {
            return "O(n²) - May be slow for large datasets (nested quantifiers)";
        } else if (pattern.contains(".*") || pattern.contains(".+")) {
            return "O(n) - Linear complexity";
        } else {
            return "O(1) - Constant complexity";
        }
    }
    
    /**
     * Calculate distribution statistics
     */
    private Map<String, Integer> calculateDistribution(List<SearchResult> results, String fieldType) {
        Map<String, Integer> distribution = new HashMap<>();
        
        if (fieldType == null || fieldType.equals("email")) {
            // Email domain distribution
            for (SearchResult result : results) {
                if (result.getMatchedField().equals("email")) {
                    String email = result.getMatchedText();
                    int atIndex = email.indexOf('@');
                    if (atIndex > 0) {
                        String domain = email.substring(atIndex);
                        distribution.put(domain, distribution.getOrDefault(domain, 0) + 1);
                    }
                }
            }
        } else if (fieldType.equals("phone")) {
            // Phone area code distribution
            for (SearchResult result : results) {
                if (result.getMatchedField().equals("phone")) {
                    String phone = result.getMatchedText();
                    // Extract area code (simplified - assumes (XXX) format or XXX- format)
                    String areaCode = extractAreaCode(phone);
                    if (areaCode != null) {
                        distribution.put(areaCode, distribution.getOrDefault(areaCode, 0) + 1);
                    }
                }
            }
        }
        
        return distribution;
    }
    
    /**
     * Extract area code from phone number
     */
    private String extractAreaCode(String phone) {
        // Try different formats
        if (phone.startsWith("(") && phone.length() >= 5) {
            return phone.substring(1, 4); // (XXX)
        } else if (phone.matches("\\d{3}-.*")) {
            return phone.substring(0, 3); // XXX-
        } else if (phone.matches("\\+1-\\d{3}-.*")) {
            return phone.substring(3, 6); // +1-XXX-
        } else if (phone.matches("\\d{10}")) {
            return phone.substring(0, 3); // 10 digits
        }
        return null;
    }
    
    /**
     * SearchResults - Container for search results and statistics
     */
    public static class SearchResults {
        private final List<SearchResult> results;
        private final SearchStatistics statistics;
        private final String regexPattern;
        
        public SearchResults(List<SearchResult> results, SearchStatistics statistics, String regexPattern) {
            this.results = results;
            this.statistics = statistics;
            this.regexPattern = regexPattern;
        }
        
        public List<SearchResult> getResults() { return results; }
        public SearchStatistics getStatistics() { return statistics; }
        public String getRegexPattern() { return regexPattern; }
    }
}