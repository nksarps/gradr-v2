package com.gradr;

import java.util.*;

/**
 * TaskSchedulerService - Responsible for task scheduling and management
 * Adheres to Single Responsibility Principle by focusing only on task scheduling
 * 
 * Responsibilities:
 * - Schedule tasks with priority
 * - Process tasks in priority order
 * - Manage task queue
 * 
 * Thread Safety:
 * - Synchronized PriorityQueue for task scheduling
 */
public class TaskSchedulerService {
    // Synchronized PriorityQueue for task scheduling based on priority
    // Time Complexity: O(log n) for offer/poll operations, O(1) for peek
    private final PriorityQueue<Task> taskQueue = new PriorityQueue<>();
    
    /**
     * Add task to priority queue
     * Time Complexity: O(log n) where n is the number of tasks in queue
     * Thread-safe: Synchronized to prevent concurrent modification
     */
    public synchronized void scheduleTask(Task task) {
        taskQueue.offer(task);
    }
    
    /**
     * Get and remove the highest priority task
     * Time Complexity: O(log n) where n is the number of tasks in queue
     * Thread-safe: Synchronized to prevent concurrent modification
     */
    public synchronized Task processNextTask() {
        return taskQueue.poll();
    }
    
    /**
     * Peek at the highest priority task without removing it
     * Time Complexity: O(1) - PriorityQueue peek operation
     * Thread-safe: Synchronized to prevent concurrent modification
     */
    public synchronized Task peekNextTask() {
        return taskQueue.peek();
    }
    
    /**
     * Get the number of pending tasks
     * Time Complexity: O(1) - Queue size operation
     */
    public synchronized int getPendingTaskCount() {
        return taskQueue.size();
    }
    
    /**
     * Check if there are pending tasks
     * Time Complexity: O(1) - Queue isEmpty operation
     */
    public synchronized boolean hasPendingTasks() {
        return !taskQueue.isEmpty();
    }
    
    /**
     * Clear all pending tasks
     * Time Complexity: O(1) - Queue clear operation
     */
    public synchronized void clearTasks() {
        taskQueue.clear();
    }
}
