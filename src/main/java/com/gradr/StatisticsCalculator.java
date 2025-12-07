package com.gradr;

import com.gradr.exceptions.StudentNotFoundException;

public class StatisticsCalculator {
    private GradeManager gradeManager;
    private StudentManager studentManager;

    public StatisticsCalculator(GradeManager gradeManager, StudentManager studentManager) {
        this.gradeManager = gradeManager;
        this.studentManager = studentManager;
    }

    /**
     * Generates a comprehensive class statistics report
     * @return Formatted string containing all class statistics
     */
    public String generateClassStatistics() throws StudentNotFoundException {
        StringBuilder report = new StringBuilder();

        report.append("CLASS STATISTICS\n");
        report.append("_______________________________________________\n\n");

        // Basic counts
        int totalStudents = studentManager.getStudentCount();
        int totalGrades = gradeManager.getGradeCount();

        report.append(String.format("Total Students: %d\n", totalStudents));
        report.append(String.format("Total Grades Recorded: %d\n\n", totalGrades));

        // If there are no grades recorded for student, return this and exit
        if (totalGrades == 0) {
            report.append("No grades recorded yet. Statistics unavailable.\n");
            return report.toString();
        }

        // Grade Distribution
        report.append("GRADE DISTRIBUTION\n");
        report.append("_______________________________________________\n");
        report.append(generateGradeDistribution());
        report.append("\n");

        // Statistical Analysis
        report.append("STATISTICAL ANALYSIS\n");
        report.append("_______________________________________________\n");
        report.append(generateStatisticalAnalysis());
        report.append("\n");

        // Highest and Lowest Grades
        report.append(generateHighestLowestGrades());
        report.append("\n");

        // Subject Performance
        report.append("SUBJECT PERFORMANCE\n");
        report.append("_______________________________________________\n");
        report.append(generateSubjectPerformance());
        report.append("\n");

        // Student Type Comparison
        report.append("STUDENT TYPE COMPARISON\n");
        report.append("_______________________________________________\n");
        report.append(generateStudentTypeComparison());

        return report.toString();
    }

    /**
     * Generates grade distribution with ASCII bar chart
     */
    private String generateGradeDistribution() {
        StringBuilder dist = new StringBuilder();

        // Count grades in each range
        int countA = 0;
        int countB = 0;
        int countC = 0;
        int countD = 0;
        int countF = 0;
        int totalGrades = 0;

        for (Grade grade : gradeManager.getGrades()) {
            if (grade == null) continue;

            double gradeValue = grade.getGrade();
            totalGrades++;

            if (gradeValue >= 90) countA++;
            else if (gradeValue >= 80) countB++;
            else if (gradeValue >= 70) countC++;
            else if (gradeValue >= 60) countD++;
            else countF++;
        }

        // Generate bars and percentages
        dist.append(generateDistributionLine("90-100% (A):", countA, totalGrades));
        dist.append(generateDistributionLine("80-89%  (B):", countB, totalGrades));
        dist.append(generateDistributionLine("70-79%  (C):", countC, totalGrades));
        dist.append(generateDistributionLine("60-69%  (D):", countD, totalGrades));
        dist.append(generateDistributionLine("0-59%   (F):", countF, totalGrades));

        return dist.toString();
    }

    /**
     * Generates a single line of the distribution chart
     */
    private String generateDistributionLine(String label, int count, int total) {
        double percentage = (count * 100.0) / total;

        // Create ASCII bar (each █ represents ~2%)
        int barLength = (int) (percentage / 2);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLength; i++) {
            bar.append("|");
        }

        // Pad the bar to maintain alignment (max 50 characters for 100%)
        while (bar.length() < 25) {
            bar.append(" ");
        }

        return String.format("%-13s %s %.1f%% (%d grades)\n",
                label, bar.toString(), percentage, count);
    }

    /**
     * Generates statistical analysis (mean, median, mode, standard deviation, range)
     */
    private String generateStatisticalAnalysis() {
        StringBuilder stats = new StringBuilder();

        double[] allGrades = getAllGradesInArray();

        if (allGrades.length == 0) {
            return "No data available\n";
        }

        // Calculate statistics
        double mean = calculateMean(allGrades);
        double median = calculateMedian(allGrades);
        double mode = calculateMode(allGrades);
        double stdDev = calculateStandardDeviation(allGrades);
        double min = findMin(allGrades);
        double max = findMax(allGrades);
        double range = max - min;

        stats.append(String.format("Mean (Average):       %.1f%%\n", mean));
        stats.append(String.format("Median:               %.1f%%\n", median));
        stats.append(String.format("Mode:                 %.1f%%\n", mode));
        stats.append(String.format("Standard Deviation:   %.1f%%\n", stdDev));
        stats.append(String.format("Range:                %.1f%% (%.0f%% - %.0f%%)\n",
                range, min, max));

        return stats.toString();
    }

    /**
     * Generates highest and lowest grade information with student names
     */
    private String generateHighestLowestGrades() throws StudentNotFoundException {
        StringBuilder result = new StringBuilder();

        Grade highestGrade = null;
        Grade lowestGrade = null;

        for (Grade grade : gradeManager.getGrades()) {
            if (grade == null) continue;

            if (highestGrade == null || grade.getGrade() > highestGrade.getGrade()) {
                highestGrade = grade;
            }
            if (lowestGrade == null || grade.getGrade() < lowestGrade.getGrade()) {
                lowestGrade = grade;
            }
        }

        if (highestGrade != null) {
            Student student = studentManager.findStudent(highestGrade.getStudentId());
            result.append(String.format("Highest Grade:        %.0f%% (%s - %s)\n",
                    highestGrade.getGrade(),
                    highestGrade.getStudentId(),
                    highestGrade.getSubject().getSubjectName()));
        }

        if (lowestGrade != null) {
            Student student = studentManager.findStudent(lowestGrade.getStudentId());
            result.append(String.format("Lowest Grade:         %.0f%% (%s - %s)\n",
                    lowestGrade.getGrade(),
                    lowestGrade.getStudentId(),
                    lowestGrade.getSubject().getSubjectName()));
        }

        return result.toString();
    }

    /**
     * Generates subject performance averages (Core and Elective)
     */
    private String generateSubjectPerformance() {
        StringBuilder perf = new StringBuilder();

        // Core subjects
        double mathAvg = calculateSubjectAverage("Mathematics");
        double englishAvg = calculateSubjectAverage("English");
        double scienceAvg = calculateSubjectAverage("Science");
        double coreAvg = calculateCoreSubjectsAverage();

        perf.append(String.format("Core Subjects:        %.1f%% average\n", coreAvg));
        perf.append(String.format("  Mathematics:        %.1f%%\n", mathAvg));
        perf.append(String.format("  English:            %.1f%%\n", englishAvg));
        perf.append(String.format("  Science:            %.1f%%\n\n", scienceAvg));

        // Elective subjects
        double musicAvg = calculateSubjectAverage("Music");
        double artAvg = calculateSubjectAverage("Art");
        double peAvg = calculateSubjectAverage("Physical Education");
        double electiveAvg = calculateElectiveSubjectsAverage();

        perf.append(String.format("Elective Subjects:    %.1f%% average\n", electiveAvg));
        perf.append(String.format("  Music:              %.1f%%\n", musicAvg));
        perf.append(String.format("  Art:                %.1f%%\n", artAvg));
        perf.append(String.format("  Physical Ed:        %.1f%%\n", peAvg));

        return perf.toString();
    }

    /**
     * Generates student type comparison (Regular vs Honors)
     */
    private String generateStudentTypeComparison() {
        StringBuilder comparison = new StringBuilder();

        int regularCount = 0;
        int honorsCount = 0;
        double regularTotal = 0.0;
        double honorsTotal = 0.0;

        for (int i = 0; i < studentManager.getStudentCount(); i++) {
            Student student = studentManager.getStudents()[i];
            if (student == null) continue;

            double avgGrade = student.calculateAverageGrade();

            if (student.getStudentType().equals("Regular")) {
                regularCount++;
                regularTotal += avgGrade;
            } else if (student.getStudentType().equals("Honors")) {
                honorsCount++;
                honorsTotal += avgGrade;
            }
        }

        double regularAvg = (regularCount > 0) ? regularTotal / regularCount : 0.0;
        double honorsAvg = (honorsCount > 0) ? honorsTotal / honorsCount : 0.0;

        comparison.append(String.format("Regular Students:     %d students, %.1f%% average\n",
                regularCount, regularAvg));
        comparison.append(String.format("Honors Students:      %d students, %.1f%% average\n",
                honorsCount, honorsAvg));

        return comparison.toString();
    }

    // Helper methods for statistical calculations

    private double[] getAllGradesInArray() {
        int count = 0;
        // Getting the total number of grades in the array
        for (Grade grade : gradeManager.getGrades()) {
            if (grade != null) count++;
        }

        double[] grades = new double[count];
        int index = 0;
        // Saving only the grades in the grades array (in grade manager) to a grades
        // array defined here
        for (Grade grade : gradeManager.getGrades()) {
            if (grade != null) {
                grades[index++] = grade.getGrade();
            }
        }
        return grades;
    }

    private double calculateMean(double[] grades) {
        if (grades.length == 0) return 0.0;
        double sum = 0;
        for (double grade : grades) {
            sum += grade;
        }
        return sum / grades.length;
    }

    private double calculateMedian(double[] grades) {
        if (grades.length == 0) return 0.0;

        // Sort array
        double[] sorted = grades.clone();
        java.util.Arrays.sort(sorted);

        if (sorted.length % 2 == 0) {
            return (sorted[sorted.length / 2 - 1] + sorted[sorted.length / 2]) / 2.0;
        } else {
            return sorted[sorted.length / 2];
        }
    }

    private double calculateMode(double[] grades) {
        if (grades.length == 0) return 0.0;

        // Round grades to nearest integer for mode calculation
        int[] roundedGrades = new int[grades.length];
        for (int i = 0; i < grades.length; i++) {
            roundedGrades[i] = (int) Math.round(grades[i]);
        }

        int maxCount = 0;
        int mode = roundedGrades[0];

        for (int i = 0; i < roundedGrades.length; i++) {
            int count = 0;
            for (int j = 0; j < roundedGrades.length; j++) {
                if (roundedGrades[j] == roundedGrades[i]) {
                    count++;
                }
            }
            if (count > maxCount) {
                maxCount = count;
                mode = roundedGrades[i];
            }
        }

        return (double) mode;
    }

    private double calculateStandardDeviation(double[] grades) {
        if (grades.length == 0) return 0.0;

        double mean = calculateMean(grades);
        double sumSquaredDiff = 0;

        for (double grade : grades) {
            sumSquaredDiff += Math.pow(grade - mean, 2);
        }

        return Math.sqrt(sumSquaredDiff / grades.length);
    }

    private double findMin(double[] grades) {
        if (grades.length == 0) return 0.0;
        double min = grades[0];
        for (double grade : grades) {
            if (grade < min) min = grade;
        }
        return min;
    }

    private double findMax(double[] grades) {
        if (grades.length == 0) return 0.0;
        double max = grades[0];
        for (double grade : grades) {
            if (grade > max) max = grade;
        }
        return max;
    }

    private double calculateSubjectAverage(String subjectName) {
        double sum = 0;
        int count = 0;

        for (Grade grade : gradeManager.getGrades()) {
            if (grade == null) continue;
            if (grade.getSubject().getSubjectName().equals(subjectName)) {
                sum += grade.getGrade();
                count++;
            }
        }

        return (count > 0) ? sum / count : 0.0;
    }

    private double calculateCoreSubjectsAverage() {
        double sum = 0;
        int count = 0;

        for (Grade grade : gradeManager.getGrades()) {
            if (grade == null) continue;
            if (grade.getSubject().getSubjectType().equals("Core")) {
                sum += grade.getGrade();
                count++;
            }
        }

        return (count > 0) ? sum / count : 0.0;
    }

    private double calculateElectiveSubjectsAverage() {
        double sum = 0;
        int count = 0;

        for (Grade grade : gradeManager.getGrades()) {
            if (grade == null) continue;
            if (grade.getSubject().getSubjectType().equals("Elective")) {
                sum += grade.getGrade();
                count++;
            }
        }

        return (count > 0) ? sum / count : 0.0;
    }
}