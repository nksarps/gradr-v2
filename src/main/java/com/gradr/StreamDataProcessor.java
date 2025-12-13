package com.gradr;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * StreamDataProcessor - Stream-based data processing using Java Streams API
 * 
 * Features:
 * - Filter operations for student criteria
 * - Map operations for data transformation
 * - Reduce operations for aggregations
 * - Collect with Collectors for grouping/partitioning
 * - Parallel streams for large datasets
 * - Chained operations (filter → map → sort → collect)
 * - Search operations (findFirst, findAny, anyMatch, allMatch, noneMatch)
 * - CSV file processing with Files.lines() stream
 * - Performance comparison (sequential vs parallel)
 */
public class StreamDataProcessor {
    
    private final StudentManager studentManager;
    private final GradeManager gradeManager;
    
    public StreamDataProcessor(StudentManager studentManager, GradeManager gradeManager) {
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
    }
    
    /**
     * Filter students by GPA using streams
     * Example: students.stream().filter(s -> s.getGPA() > 3.5)
     */
    public List<Student> filterStudentsByGPA(double minGPA) {
        return studentManager.getStudentsList().stream()
            .filter(s -> {
                try {
                    GPACalculator calc = new GPACalculator(gradeManager);
                    double gpa = calc.calculateCumulativeGPA(s.getStudentId());
                    return gpa > minGPA;
                } catch (Exception e) {
                    return false;
                }
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Filter honors students with GPA > 3.5
     */
    public List<Student> findHonorsStudentsWithHighGPA() {
        return studentManager.getStudentsList().stream()
            .filter(s -> s.getStudentType().equals("Honors"))
            .filter(s -> {
                try {
                    GPACalculator calc = new GPACalculator(gradeManager);
                    double gpa = calc.calculateCumulativeGPA(s.getStudentId());
                    return gpa > 3.5;
                } catch (Exception e) {
                    return false;
                }
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Map to extract all student emails
     * Example: students.stream().map(Student::getEmail)
     */
    public List<String> extractStudentEmails() {
        return studentManager.getStudentsList().stream()
            .map(Student::getEmail)
            .filter(email -> email != null && !email.isEmpty())
            .collect(Collectors.toList());
    }
    
    /**
     * Map to extract student names
     */
    public List<String> extractStudentNames() {
        return studentManager.getStudentsList().stream()
            .map(Student::getName)
            .collect(Collectors.toList());
    }
    
    /**
     * Reduce for aggregations - calculate total grades
     */
    public int calculateTotalGrades() {
        return gradeManager.getGradeCount();
    }
    
    /**
     * Reduce to calculate sum of all grade values
     */
    public double calculateTotalGradeSum() {
        return Arrays.stream(gradeManager.getGrades())
            .filter(Objects::nonNull)
            .mapToDouble(Grade::getGrade)
            .reduce(0.0, Double::sum);
    }
    
    /**
     * Collect with Collectors.groupingBy - Group students by grade range
     */
    public Map<String, List<Student>> groupStudentsByGradeRange() {
        return studentManager.getStudentsList().stream()
            .collect(Collectors.groupingBy(s -> {
                try {
                    GPACalculator calc = new GPACalculator(gradeManager);
                    double gpa = calc.calculateCumulativeGPA(s.getStudentId());
                    if (gpa >= 3.5) return "A (3.5-4.0)";
                    else if (gpa >= 3.0) return "B (3.0-3.49)";
                    else if (gpa >= 2.5) return "C (2.5-2.99)";
                    else if (gpa >= 2.0) return "D (2.0-2.49)";
                    else return "F (<2.0)";
                } catch (Exception e) {
                    return "Unknown";
                }
            }));
    }
    
    /**
     * Collect with Collectors.partitioningBy - Partition by student type
     */
    public Map<Boolean, List<Student>> partitionByStudentType() {
        return studentManager.getStudentsList().stream()
            .collect(Collectors.partitioningBy(s -> s.getStudentType().equals("Honors")));
    }
    
    /**
     * Calculate average grade per subject using streams
     */
    public Map<String, Double> calculateAverageGradePerSubject() {
        return Arrays.stream(gradeManager.getGrades())
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                g -> g.getSubject().getSubjectName(),
                Collectors.averagingDouble(Grade::getGrade)
            ));
    }
    
    /**
     * Extract unique course codes
     */
    public Set<String> extractUniqueCourseCodes() {
        return Arrays.stream(gradeManager.getGrades())
            .filter(Objects::nonNull)
            .map(g -> g.getSubject().getSubjectName())
            .collect(Collectors.toSet());
    }
    
    /**
     * Find top 5 students by average grade
     */
    public List<Student> findTop5StudentsByAverage() {
        return studentManager.getStudentsList().stream()
            .map(s -> {
                try {
                    GPACalculator calc = new GPACalculator(gradeManager);
                    double gpa = calc.calculateCumulativeGPA(s.getStudentId());
                    return new AbstractMap.SimpleEntry<>(s, gpa);
                } catch (Exception e) {
                    return new AbstractMap.SimpleEntry<>(s, 0.0);
                }
            })
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(5)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Chain operations: filter → map → sort → collect
     */
    public List<String> chainOperationsExample() {
        return studentManager.getStudentsList().stream()
            .filter(s -> s.getStudentType().equals("Honors"))  // Filter
            .map(Student::getName)                              // Map
            .sorted()                                          // Sort
            .collect(Collectors.toList());                     // Collect
    }
    
    /**
     * findFirst() - Find first student with GPA > 3.5
     */
    public Optional<Student> findFirstHighGPAStudent() {
        return studentManager.getStudentsList().stream()
            .filter(s -> {
                try {
                    GPACalculator calc = new GPACalculator(gradeManager);
                    double gpa = calc.calculateCumulativeGPA(s.getStudentId());
                    return gpa > 3.5;
                } catch (Exception e) {
                    return false;
                }
            })
            .findFirst();
    }
    
    /**
     * findAny() - Find any honors student
     */
    public Optional<Student> findAnyHonorsStudent() {
        return studentManager.getStudentsList().stream()
            .filter(s -> s.getStudentType().equals("Honors"))
            .findAny();
    }
    
    /**
     * anyMatch() - Check if any student has GPA > 3.5
     */
    public boolean hasAnyHighGPAStudent() {
        return studentManager.getStudentsList().stream()
            .anyMatch(s -> {
                try {
                    GPACalculator calc = new GPACalculator(gradeManager);
                    double gpa = calc.calculateCumulativeGPA(s.getStudentId());
                    return gpa > 3.5;
                } catch (Exception e) {
                    return false;
                }
            });
    }
    
    /**
     * allMatch() - Check if all students have GPA > 2.0
     */
    public boolean allStudentsHavePassingGPA() {
        return studentManager.getStudentsList().stream()
            .allMatch(s -> {
                try {
                    GPACalculator calc = new GPACalculator(gradeManager);
                    double gpa = calc.calculateCumulativeGPA(s.getStudentId());
                    return gpa > 2.0;
                } catch (Exception e) {
                    return false;
                }
            });
    }
    
    /**
     * noneMatch() - Check if no student has GPA < 1.0
     */
    public boolean noStudentHasFailingGPA() {
        return studentManager.getStudentsList().stream()
            .noneMatch(s -> {
                try {
                    GPACalculator calc = new GPACalculator(gradeManager);
                    double gpa = calc.calculateCumulativeGPA(s.getStudentId());
                    return gpa < 1.0;
                } catch (Exception e) {
                    return false;
                }
            });
    }
    
    /**
     * Process large CSV file line-by-line using Files.lines() stream
     */
    public List<String[]> processCSVFile(Path filePath) {
        try (Stream<String> lines = Files.lines(filePath, StandardCharsets.UTF_8)) {
            return lines
                .skip(1) // Skip header
                .filter(line -> !line.trim().isEmpty())
                .map(line -> line.split(","))
                .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * Compare performance: sequential vs parallel stream processing
     */
    public PerformanceComparison compareSequentialVsParallel(int iterations) {
        List<Student> students = studentManager.getStudentsList();
        
        // Sequential processing
        long sequentialStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            students.stream()
                .filter(s -> s.getStudentType().equals("Honors"))
                .map(Student::getName)
                .sorted()
                .collect(Collectors.toList());
        }
        long sequentialTime = System.nanoTime() - sequentialStart;
        
        // Parallel processing
        long parallelStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            students.parallelStream()
                .filter(s -> s.getStudentType().equals("Honors"))
                .map(Student::getName)
                .sorted()
                .collect(Collectors.toList());
        }
        long parallelTime = System.nanoTime() - parallelStart;
        
        return new PerformanceComparison(
            sequentialTime / 1_000_000.0, // Convert to milliseconds
            parallelTime / 1_000_000.0,
            students.size()
        );
    }
    
    /**
     * PerformanceComparison - Results of sequential vs parallel comparison
     */
    public static class PerformanceComparison {
        private final double sequentialTime;
        private final double parallelTime;
        private final int datasetSize;
        
        public PerformanceComparison(double sequentialTime, double parallelTime, int datasetSize) {
            this.sequentialTime = sequentialTime;
            this.parallelTime = parallelTime;
            this.datasetSize = datasetSize;
        }
        
        public double getSequentialTime() { return sequentialTime; }
        public double getParallelTime() { return parallelTime; }
        public int getDatasetSize() { return datasetSize; }
        
        public double getSpeedup() {
            return sequentialTime > 0 ? sequentialTime / parallelTime : 0.0;
        }
        
        public boolean isParallelFaster() {
            return parallelTime < sequentialTime;
        }
    }
    
    /**
     * Execute stream operation with timing
     */
    public <T> TimedResult<T> executeWithTiming(java.util.function.Supplier<T> operation, String operationName) {
        long startTime = System.nanoTime();
        T result = operation.get();
        long executionTime = System.nanoTime() - startTime;
        return new TimedResult<>(result, executionTime / 1_000_000.0, operationName);
    }
    
    /**
     * TimedResult - Result with execution time
     */
    public static class TimedResult<T> {
        private final T result;
        private final double executionTime;
        private final String operationName;
        
        public TimedResult(T result, double executionTime, String operationName) {
            this.result = result;
            this.executionTime = executionTime;
            this.operationName = operationName;
        }
        
        public T getResult() { return result; }
        public double getExecutionTime() { return executionTime; }
        public String getOperationName() { return operationName; }
    }
}

