# Gradr v2 - Advanced Student Grade Management System
A comprehensive Java-based grade management system demonstrating advanced programming concepts including collections, concurrency, I/O operations, design patterns, and performance optimization.

## Project Overview 
Gradr v2 is an enterprise-grade student grade management system built with Java 21 and Maven. The project demonstrates mastery of advanced Java concepts including:
- **Java Collections Framework** with optimized data structures (HashMap, TreeMap, LinkedList, HashSet, ConcurrentHashMap)
- **Thread-Safe Concurrent Operations** using ExecutorService, synchronized collections, and atomic operations
- **Modern File I/O** with NIO.2 API supporting CSV, JSON, and binary formats
- **Stream Processing** with functional programming and parallel streams
- **Design Patterns** (Facade, Strategy, Factory, Singleton)
- **Input Validation** with comprehensive regex patterns
- **Performance Optimization** with caching, LRU eviction, and monitoring
- **SOLID Principles** throughout the codebase

## Project Structure
```bash
gradr-v2/
├── pom.xml                          # Maven configuration
├── README.md                        # Project documentation
├── data/                            # Data storage
│   ├── binary/                      # Binary serialized data
│   ├── csv/                         # CSV data files
│   └── json/                        # JSON data files
├── imports/                         # Import data files
│   ├── bulk_students.csv
│   ├── term_one_grades.csv
│   └── test_case_grades.csv
├── logs/                            # Application logs
│   └── audit/                       # Audit trail logs
├── reports/                         # Generated reports
│   ├── binary/
│   ├── csv/
│   └── json/
└── src/
    ├── main/java/com/gradr/
    │   ├── Main.java                        # Application entry point
    │   ├── MenuHandler.java                 # Menu system coordinator
    │   ├── ApplicationContext.java          # Dependency injection container
    │   ├── ConsoleUI.java                   # User interface
    │   │
    │   ├── Student Management
    │   │   ├── Student.java                 # Abstract student base class
    │   │   ├── RegularStudent.java          # Regular student implementation
    │   │   ├── HonorsStudent.java           # Honors student implementation
    │   │   ├── StudentManager.java          # Facade for student operations
    │   │   ├── StudentRepository.java       # Student data persistence
    │   │   └── StudentFactory.java          # Factory pattern for students
    │   │
    │   ├── Grade Management
    │   │   ├── Grade.java                   # Grade entity
    │   │   ├── Subject.java                 # Abstract subject base
    │   │   ├── CoreSubject.java             # Core subject implementation
    │   │   ├── ElectiveSubject.java         # Elective subject implementation
    │   │   ├── GradeManager.java            # Facade for grade operations
    │   │   ├── GradeRepository.java         # Grade data persistence (TreeMap, LinkedList)
    │   │   ├── GradeCalculator.java         # Grade calculation logic
    │   │   └── GPACalculator.java           # GPA calculation
    │   │
    │   ├── File I/O & Data Processing
    │   │   ├── MultiFormatFileHandler.java  # NIO.2 file operations (CSV/JSON/Binary)
    │   │   ├── FileExporter.java            # Export coordination
    │   │   ├── FileExportStrategy.java      # Strategy pattern interface
    │   │   ├── CSVExportStrategy.java       # CSV export strategy
    │   │   ├── JSONExportStrategy.java      # JSON export strategy
    │   │   ├── BinaryExportStrategy.java    # Binary export strategy
    │   │   ├── CSVParser.java               # CSV parsing with validation
    │   │   └── StreamDataProcessor.java     # Stream API operations
    │   │
    │   ├── Concurrent Operations
    │   │   ├── BatchReportGenerator.java    # ExecutorService for batch processing
    │   │   ├── TaskScheduler.java           # Scheduled task management
    │   │   └── FileWatcherService.java      # File system monitoring
    │   │
    │   ├── Search & Validation
    │   │   └── PatternSearchService.java    # Regex-based pattern matching
    │   │
    │   ├── Statistics & Reporting
    │   │   ├── StatisticsService.java       # Statistics calculations
    │   │   ├── StatisticsCalculator.java    # Core statistics logic
    │   │   └── StatisticsDashboard.java     # Interactive dashboard
    │   │
    │   ├── Performance & Caching
    │   │   └── CacheManager.java            # LRU cache with ConcurrentHashMap
    │   │
    │   ├── Audit & Logging
    │   │   ├── GradeAuditService.java       # Audit trail management
    │   │   └── AuditLogger.java             # Logging operations
    │   │
    │   ├── Interfaces (SOLID - DIP)
    │   │   ├── IGradeCalculator.java
    │   │   ├── IGradeReader.java
    │   │   ├── IGradeWriter.java
    │   │   ├── IStudentReader.java
    │   │   └── IStudentWriter.java
    │   │
    │   └── exceptions/
    │       ├── StudentNotFoundException.java
    │       ├── FileExportException.java
    │       ├── CSVParseException.java
    │       └── InvalidMenuChoiceException.java
    │
    └── test/java/com/gradr/             # JUnit test suite
```

## Prerequisites

- **Java Development Kit (JDK) 21+**
    - Download from: https://www.oracle.com/java/technologies/downloads/
    - Verify installation: `java --version`

- **Apache Maven 3.6+**
    - Download from: https://maven.apache.org/download.cgi
    - Verify installation: `mvn --version`

- **Git** (Optional, for version control)
    - Download from: https://git-scm.com/

## Key Features

### Core Features
- **Student Management**: Add, view, search students (Regular & Honors)
- **Grade Management**: Record, calculate, and track grades for core and elective subjects
- **GPA Calculation**: Comprehensive GPA calculation with subject weighting
- **Bulk Import**: Import multiple students and grades from CSV files
- **Multi-Format Export**: Export reports in CSV, JSON, and binary formats

### Advanced Features
- **Concurrent Batch Processing**: Generate reports for multiple students in parallel (2-8 threads)
- **LRU Caching System**: 150-entry cache with hit/miss tracking and auto-refresh
- **Stream-Based Data Processing**: Filter, map, reduce operations on large datasets
- **Pattern-Based Search**: Regex search for email domains, phone numbers, student IDs
- **File System Monitoring**: Watch directories for automatic import
- **Scheduled Tasks**: Automated report generation and statistics updates
- **Performance Monitoring**: Track I/O operations, cache performance, thread utilization
- **Interactive Statistics Dashboard**: Real-time performance metrics and system stats
- **Audit Trail**: Complete logging of grade changes and system operations

### Collection Optimizations
- **ConcurrentHashMap**: Thread-safe O(1) student lookups
- **TreeMap**: O(log n) sorted subject grade access
- **LinkedList**: O(1) grade history insertions
- **HashSet**: O(1) unique course tracking
- **Synchronized Collections**: Thread-safe list operations

## Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/nksarps/gradr-v2.git
cd gradr-v2
```

### 2. Verify Prerequisites
```bash
java --version    # Should show Java 21+
mvn --version     # Should show Maven 3.6+
```

### 3. Build with Maven
```bash
mvn clean compile
```

### 4. Run the Application
```bash
mvn exec:java -Dexec.mainClass="com.gradr.Main"
```

Or compile and run directly:
```bash
# Compile
javac -d target/classes -sourcepath src/main/java src/main/java/com/gradr/*.java

# Run
java -cp target/classes com.gradr.Main
```

### 5. Run Tests
```bash
mvn test
```

## Usage Guide

### Main Menu
```bash
||=============================================||
||  STUDENT GRADE MANAGEMENT SYSTEM - v2.0     ||
||=============================================||
|| System Status: ✓ Running                    ||
|| Students: 3 | Grades: 15 | Cache: 89.2% HR  ||
||=============================================||

STUDENT MANAGEMENT
1.  Add Student
2.  Bulk Student Import
3.  View Students

GRADE MANAGEMENT
4.  Record Grade
5.  View Grade Report
6.  Export Report (CSV/JSON/Binary)
7.  Import Grade Data
8.  Bulk Grade Import
9.  Calculate GPA

STATISTICS & ANALYSIS
10. Class Statistics
11. GPA Rankings
12. Statistics Dashboard

ADVANCED FEATURES
13. Pattern-Based Search (Regex)
14. Stream Data Processing
15. Batch Report Generation (Concurrent)

SYSTEM OPERATIONS
16. View Cache Statistics
17. Schedule Automated Tasks
18. File System Monitor
19. Audit Trail Viewer

20. Exit

Enter choice: 
```


### Add Student
```bash
Enter choice: 1

ADD STUDENT
_______________________________________________

Enter student name: Nana Kwaku
Enter student age: 12
Enter student email: nana.kwaku@school.edu
Enter student phone: 0123456789

Student type:
1. Regular Student (Passing grade: 50%)
2. Honors Student: (Passing grade: 60%, honors recognition)

Select type (1-2): 1

Student added successfully!
Student ID: STU001
Name: Nana Kwaku
Type: Regular
Age: 20
Email: nana.kwaku@school.edu
Passing Grade: 50%
Status: Active
```

### View Students
```bash
Enter choice: 2

STUDENT LISTING
----------------------------------------------------------------------------------------------------
STU ID   | NAME                    | TYPE               | AVG GRADE         | STATUS                
----------------------------------------------------------------------------------------------------
STU001   | Nana Kwaku              | Regular            | 94.00             |               Active
         | Enrolled Subjects: 2 | Passing Grade: 50.0
----------------------------------------------------------------------------------------------------
STU002   | Adwoa Mansa             | Honors             | 87.0             % | Active
         | Enrolled Subjects: Adwoa Mansa | Passing Grade: 60.0 | Honors Eligible
----------------------------------------------------------------------------------------------------
STU003   | Kofi Poku               | Regular            | 70.00             |               Active
         | Enrolled Subjects: 1 | Passing Grade: 50.0
----------------------------------------------------------------------------------------------------

Total Students: 3
Average Class Grade: 83.67%
```
### Record Grade
```bash
Enter Student ID: STU001

Student Details:
Name: Nana Kwaku
Type: Regular Student
Current Average: 98.0%

Subject type:
1. Core Subject (Mathematics, English, Science)
2. Elective Subject (Music, Art, Physical Education)

Select type (1-2): 2

Available Elective Subjects
1. Music
2. Art
3. Physical Education

Select subject: 2

Enter grade: 90
GRADE CONFIRMATION
_______________________________________________________
Grade ID: GRD003
Student: STU001 - Nana Kwaku
Subject: Art (Elective)
Grade: 90.0%
Date: 2025-11-29
______________________________________________________

Confirm grade? (Y/N): Y
Grade added successfully.
```

### View Grade Report
```bash
Enter choice: 4

VIEW GRADE REPORT
_______________________________________________

Enter Student ID: STU001

Student: STU001 - Nana Kwaku
Type: Regular Student
Current Average: 94.0%
Status: PASSING

GRADE HISTORY
-------------------------------------------------------------------------------------
GRD ID   | DATE       | SUBJECT          | TYPE       | GRADE
-------------------------------------------------------------------------------------
GRD001    | 2025-11-29 | English          | Core       | 98.0 %
GRD003    | 2025-11-29 | Art              | Elective   | 90.0 %

Total Grades: 2
Core Subjects Average: 98.0%
Elective Subjects Average: 90.0%
Overall Average: 94.0%
```

### Advanced Features Examples

#### Batch Report Generation (Concurrent Processing)
```bash
Enter choice: 15

BATCH REPORT GENERATION
_______________________________________________

Initialize thread pool:
Enter number of threads (2-8): 4

Thread pool initialized with 4 threads.

Select students for batch processing:
1. All students (3)
2. Select specific students

Enter choice: 1

Report type:
1. Detailed Report
2. Summary Report
3. Performance Analytics

Enter choice: 2

Export format:
1. CSV
2. JSON
3. Binary
4. All formats

Enter choice: 4

Generating batch reports...

[Thread-1] Processing: STU001 - Nana Kwaku
[Thread-2] Processing: STU002 - Adwoa Mansa
[Thread-3] Processing: STU003 - Kofi Poku

Progress: [████████████████████] 100% (3/3 completed)

BATCH PROCESSING COMPLETE
Total Reports: 9 (3 students × 3 formats)
Successful: 9 | Failed: 0
Total Time: 245ms | Avg: 27ms per report
Thread Pool Size: 4
```

#### Pattern-Based Search (Regex)
```bash
Enter choice: 13

PATTERN-BASED SEARCH
_______________________________________________

1. Email Domain Pattern (e.g., @university.edu)
2. Phone Area Code Pattern (e.g., 555)
3. Student ID Pattern (e.g., STU0**)
4. Name Pattern (regex)
5. Custom Regex Pattern

Select search type: 1
Enter email domain: @school.edu
Case insensitive? (Y/N): Y

Searching... (Regex: .*@school\.edu$)

SEARCH RESULTS (2 matches)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Match 1: STU001 - Nana Kwaku
  Field: email
  Value: nana.kwaku@school.edu
  Highlight: nana.kwaku[@SCHOOL.EDU]

Match 2: STU003 - Kofi Poku
  Field: email
  Value: kofi.poku@school.edu
  Highlight: kofi.poku[@SCHOOL.EDU]

SEARCH STATISTICS
Total Scanned: 3 students
Matches Found: 2 (66.67%)
Search Time: 12ms
Regex Complexity: Simple
```

#### Stream Data Processing
```bash
Enter choice: 14

STREAM DATA PROCESSING
_______________________________________________

1. Filter students by GPA
2. Find honors students with high GPA
3. Extract student emails
4. Group students by performance
5. Calculate statistics with streams
6. Performance comparison (Sequential vs Parallel)

Select operation: 2

Filtering honors students with GPA > 3.5...

Results (using stream pipeline):
students.stream()
  .filter(s -> s.getStudentType().equals("Honors"))
  .filter(s -> calculateGPA(s) > 3.5)
  .collect(Collectors.toList())

Found 1 student(s):
- STU002: Adwoa Mansa (GPA: 3.85)

Processing time: 8ms
```

#### Cache Statistics
```bash
Enter choice: 16

CACHE STATISTICS DASHBOARD
═══════════════════════════════════════════════

Cache Performance:
  Total Requests: 1,245
  Cache Hits: 1,111 (89.2%)
  Cache Misses: 134 (10.8%)
  
  Avg Hit Time: 0.3ms
  Avg Miss Time: 12.5ms
  
Cache Contents:
  Total Entries: 47 / 150 (31.3%)
  Student Cache: 15
  Grade Reports: 28
  Statistics: 4
  
  Evictions: 12 (LRU policy)
  
Memory Usage: 2.4 MB

Top Cached Items (by access count):
1. student:STU001 - 45 accesses
2. report:STU001:summary - 28 accesses
3. stats:class_average - 19 accesses
```

#### Statistics Dashboard
```bash
Enter choice: 12

INTERACTIVE STATISTICS DASHBOARD
═══════════════════════════════════════════════════════════════
Last Updated: 2025-12-18 14:32:15 | Auto-refresh: ON (30s)
═══════════════════════════════════════════════════════════════

SYSTEM OVERVIEW
┌─────────────────────────────────────────────────────────────┐
│ Total Students: 3                                            │
│ Total Grades: 15                                             │
│ Unique Courses: 8                                            │
│ Class Average: 83.67%                                        │
└─────────────────────────────────────────────────────────────┘

GRADE DISTRIBUTION
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
A (90-100): ████████████ 40.0% (6 grades)
B (80-89):  ████████ 26.7% (4 grades)
C (70-79):  ████ 13.3% (2 grades)
D (60-69):  ██ 6.7% (1 grade)
F (0-59):   ████ 13.3% (2 grades)

PERFORMANCE METRICS
  I/O Operations: 47 (Total: 1,245ms, Avg: 26ms)
  Cache Hit Rate: 89.2%
  Active Threads: 1 / 4 available
  
TOP PERFORMERS
1. Nana Kwaku (STU001) - 94.0%
2. Adwoa Mansa (STU002) - 87.0%
3. Kofi Poku (STU003) - 70.0%
```

### Exit
```bash
Enter choice: 20

Shutting down services...
✓ Cache cleared (47 entries)
✓ Thread pool shutdown (4 threads terminated)
✓ File watchers stopped
✓ Scheduled tasks cancelled
✓ Audit log saved

Thank you for using Student Grade Management System v2.0!
Goodbye!
```

## Technical Implementation Highlights

### 1. Type-Safe Data Structures
```bash
// ConcurrentHashMap for O(1) thread-safe lookups
private final Map<String, Student> studentsMap = new ConcurrentHashMap<>();

// TreeMap for O(log n) sorted access
private final Map<String, List<Grade>> subjectGrades = 
    Collections.synchronizedMap(new TreeMap<>());

// LinkedList for O(1) insertions at ends
private final List<Grade> gradeHistory = 
    Collections.synchronizedList(new LinkedList<>());

// HashSet for O(1) uniqueness checks
private final Set<String> uniqueCourses = 
    Collections.synchronizedSet(new HashSet<>());
```

### 2. Modern File I/O with NIO.2
```bash
// Try-with-resources for automatic resource management
try (BufferedWriter writer = Files.newBufferedWriter(
        filePath, StandardCharsets.UTF_8)) {
    
    // Stream-based file writing
    students.stream()
        .map(this::toCsvRow)
        .forEach(row -> writer.write(row));
        
} catch (IOException e) {
    throw new FileExportException("Export failed", e);
}

// File system validation
Files.exists(path);
Files.isReadable(path);
Files.isWritable(path);
```

### 3. Concurrent Operations with ExecutorService
```bash
// Fixed thread pool for batch processing
ExecutorService executor = Executors.newFixedThreadPool(threadCount);

List<Future<ReportResult>> futures = new ArrayList<>();

for (Student student : students) {
    Future<ReportResult> future = executor.submit(() -> {
        return generateReport(student);
    });
    futures.add(future);
}

// Wait for all tasks to complete
for (Future<ReportResult> future : futures) {
    ReportResult result = future.get();
    results.add(result);
}

executor.shutdown();
executor.awaitTermination(1, TimeUnit.MINUTES);
```

### 4. Stream Processing & Functional Programming
```bash
// Filter, map, and collect pipeline
List<String> emails = studentManager.getStudentsList().stream()
    .filter(s -> s.getStudentType().equals("Honors"))
    .filter(s -> calculateGPA(s.getStudentId()) > 3.5)
    .map(Student::getEmail)
    .collect(Collectors.toList());

// Parallel stream for large datasets
double average = grades.parallelStream()
    .mapToDouble(Grade::getValue)
    .average()
    .orElse(0.0);

// Grouping with Collectors
Map<String, List<Student>> byType = students.stream()
    .collect(Collectors.groupingBy(Student::getStudentType));
```

### 5. Regex-Based Input Validation
```bash
// Compile patterns for performance
private static final Pattern EMAIL_PATTERN = 
    Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
private static final Pattern STUDENT_ID_PATTERN = 
    Pattern.compile("^STU\\d{3,}$");
    
private static final Pattern PHONE_PATTERN = 
    Pattern.compile("^\\d{10,15}$");

// Validation with Matcher
public boolean validateEmail(String email) {
    Matcher matcher = EMAIL_PATTERN.matcher(email);
    return matcher.matches();
}
```

### 6. Thread-Safe Caching with LRU Eviction
```bash
public class CacheManager {
    private final ConcurrentHashMap<String, CacheEntry> cache;
    private final LinkedHashMap<String, Long> accessOrder; // LRU tracking
    private final int MAX_SIZE = 150;
    
    public synchronized Object get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            entry.touch(); // Update access time
            accessOrder.put(key, System.currentTimeMillis());
            cacheHits.incrementAndGet();
            return entry.getValue();
        }
        cacheMisses.incrementAndGet();
        return null;
    }
    
    public synchronized void put(String key, Object value) {
        if (cache.size() >= MAX_SIZE) {
            evictLRU(); // Remove least recently used
        }
        cache.put(key, new CacheEntry(key, value));
    }
}
```

### 7. Design Patterns Implementation

#### Facade Pattern
```bash
public class GradeManager {
    private final GradeRepository repository;
    private final GradeCalculator calculator;
    private final GradeAuditService auditService;
    
    // Unified interface to subsystems
    public void addGrade(Grade grade) {
        repository.addGrade(grade);
        auditService.logGradeAdded(grade);
        statisticsService.clearCache();
    }
}
```

#### Strategy Pattern
```bash
public interface FileExportStrategy {
    void export(List<Grade> grades, String filename);
}

public class CSVExportStrategy implements FileExportStrategy { }
public class JSONExportStrategy implements FileExportStrategy { }
public class BinaryExportStrategy implements FileExportStrategy { }

// Client code
FileExportStrategy strategy = switch(choice) {
    case 1 -> new CSVExportStrategy();
    case 2 -> new JSONExportStrategy();
    case 3 -> new BinaryExportStrategy();
};
strategy.export(grades, filename);
```

#### Factory Pattern
```bash
public class StudentFactory {
    public static Student createStudent(String type) {
        return switch(type.toLowerCase()) {
            case "regular" -> new RegularStudent();
            case "honors" -> new HonorsStudent();
            default -> throw new IllegalArgumentException("Unknown type");
        };
    }
}
```

### 8. SOLID Principles

**Single Responsibility Principle (SRP)**
- `StudentRepository`: Only handles student data storage
- `GradeCalculator`: Only handles grade calculations
- `FileExporter`: Only handles file export operations

**Open/Closed Principle (OCP)**
- Strategy pattern allows adding new export formats without modifying existing code

**Liskov Substitution Principle (LSP)**
- `RegularStudent` and `HonorsStudent` can substitute `Student` base class

**Interface Segregation Principle (ISP)**
- Fine-grained interfaces: `IGradeReader`, `IGradeWriter` instead of one large interface

**Dependency Inversion Principle (DIP)**
- Depend on abstractions: `Student` depends on `IGradeCalculator`, not concrete `GradeManager`

## Performance Characteristics

### Time Complexity Analysis
| Operation | Data Structure | Complexity |
|-----------|---------------|------------|
| Student Lookup | ConcurrentHashMap | O(1) average |
| Grade by Subject | TreeMap | O(log n) |
| Add Grade | LinkedList | O(1) |
| Check Unique Course | HashSet | O(1) average |
| Stream Filter | ArrayList | O(n) |
| Parallel Stream | ArrayList | O(n/p) where p = processors |

### Space Complexity
- Student Storage: O(n) where n = number of students
- Grade History: O(m) where m = number of grades
- Cache: O(150) - fixed maximum size
- Subject Grades Map: O(k × m) where k = unique subjects

## Testing
The project includes comprehensive JUnit tests:
```bash
mvn test

# Run specific test class
mvn test -Dtest=GradeCalculatorTest

# Run with coverage
mvn clean test jacoco:report
```

## Learning Objectives

By exploring this project, you will understand:

✓ **Type-Safe Data Structures**: Using Java Collections Framework with generics  
✓ **Modern File I/O**: NIO.2 API for efficient file operations  
✓ **Input Validation**: Regex patterns for data integrity  
✓ **Thread-Safe Concurrent Operations**: ExecutorService and synchronization  
✓ **Performance Optimization**: Caching, LRU eviction, parallel processing  
✓ **Design Patterns**: Facade, Strategy, Factory, Singleton  
✓ **SOLID Principles**: Clean, maintainable, extensible code  
✓ **Stream Processing**: Functional programming with Java Streams  

> **Note:** For detailed information about collection optimizations and performance improvements, see the inline documentation and JavaDoc comments throughout the codebase.

## License
This project is for educational purposes.

## Contact
For inquiries or feedback, please get in touch with the project owner.