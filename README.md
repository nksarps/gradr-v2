# Gradr
A Java-based console application for managing student grades. 

## Project Overview 
Gradr is a Java-based console application for managing student grades. The project shows key OOP principles like abstraction, inheritance, polymorphism, and encapsulation, while providing a structured and efficient way to handle student grades.

## Project Structure
```bash
gradr/
├── .idea/
│   ├── .gitignore
│   ├── misc.xml
│   ├── modules.xml
│   └── vcs.xml
│
├── src/
│   ├── Grade.java
│   ├── Main.java
│   ├── Student.java
│   └── Subject.java
│
├── .gitignore
├── README.md
└── StudentGradeManagement.iml

```

## Prerequisites

- **Java Development Kit (JDK)**
    - Download from: https://www.oracle.com/java/technologies/downloads/

- **Git**: For version control
    - Download from: https://git-scm.com/

## Key Features
- **Add Student**: Register new students in the system
- **View Students**: Display all students with their details
- **Record Grade**: Add grades for students in different subjects
- **View Grade Report**: Display grade history for a student
- **Simple Menu**: Navigate through options

## Setup Steps
### 1. Clone the Repository

```bash
git clone https://github.com/nksarps/gradr
```

### 2. Verify Prerequisites
Check that Java is installed:

```bash
java --version
```

### 3. Compile the Project

```bash
javac src/*.java
```

### 4. Run the Project
```bash
java src/Main
```

## Usage Examples
### Main Menu
```bash
||=============================================||
||     STUDENT GRADE MANAGEMENT - MAIN MENU    ||
||=============================================||

1. Add Student
2. View Students
3. Record Grade
4. View Grade Report
5. Exit

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

### Exit
```bash
Enter choice: 5

Thank you for using Student Grade Management System!
Goodbye!
```

## License
This project is for educational purposes.

## Contact
For inquiries or feedback, please get in touch with the project owner.