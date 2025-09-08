package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * TestDataCleanup - Comprehensive test data cleanup utility
 * 
 * Features:
 * - Database cleanup operations
 * - File system cleanup
 * - Test artifact management
 * - Cleanup scheduling and tracking
 * - Cleanup verification
 * - Rollback capabilities
 */
public class TestDataCleanup {
    
    private static final Map<String, List<CleanupTask>> cleanupTasks = new ConcurrentHashMap<>();
    private static final Map<String, CleanupResult> cleanupResults = new ConcurrentHashMap<>();
    private static final List<String> cleanupLog = Collections.synchronizedList(new ArrayList<>());
    
    // ====================
    // CLEANUP TASK DEFINITIONS
    // ====================
    
    public interface CleanupTask {
        void execute() throws Exception;
        String getDescription();
        boolean isReversible();
        void rollback() throws Exception;
    }
    
    public static class CleanupResult {
        public final boolean success;
        public final String message;
        public final LocalDateTime timestamp;
        public final Exception exception;
        
        public CleanupResult(boolean success, String message, Exception exception) {
            this.success = success;
            this.message = message;
            this.timestamp = LocalDateTime.now();
            this.exception = exception;
        }
    }
    
    // ====================
    // DATABASE CLEANUP TASKS
    // ====================
    
    /**
     * Add database table cleanup task
     */
    public static void addDatabaseTableCleanup(String testName, String tableName, String whereClause, Object... parameters) {
        addDatabaseTableCleanup(testName, "default", tableName, whereClause, parameters);
    }
    
    /**
     * Add database table cleanup task for specific data source
     */
    public static void addDatabaseTableCleanup(String testName, String dataSourceName, String tableName, 
                                             String whereClause, Object... parameters) {
        CleanupTask task = new CleanupTask() {
            private List<Map<String, Object>> backupData;
            
            @Override
            public void execute() throws Exception {
                // Backup data before deletion for rollback capability
                if (isReversible()) {
                    String selectQuery = "SELECT * FROM " + tableName;
                    if (whereClause != null && !whereClause.trim().isEmpty()) {
                        selectQuery += " WHERE " + whereClause;
                    }
                    backupData = DatabaseManager.executeQuery(dataSourceName, selectQuery, parameters);
                }
                
                // Perform cleanup
                int deletedRows = DatabaseManager.deleteRecord(dataSourceName, tableName, whereClause, parameters);
                logCleanup("Deleted " + deletedRows + " rows from table: " + tableName);
            }
            
            @Override
            public String getDescription() {
                return "Delete records from table: " + tableName + 
                       (whereClause != null ? " WHERE " + whereClause : "");
            }
            
            @Override
            public boolean isReversible() {
                return true;
            }
            
            @Override
            public void rollback() throws Exception {
                if (backupData != null && !backupData.isEmpty()) {
                    for (Map<String, Object> record : backupData) {
                        DatabaseManager.insertRecord(dataSourceName, tableName, record);
                    }
                    logCleanup("Restored " + backupData.size() + " rows to table: " + tableName);
                }
            }
        };
        
        addCleanupTask(testName, task);
    }
    
    /**
     * Add database truncate table cleanup task
     */
    public static void addDatabaseTruncateCleanup(String testName, String tableName) {
        addDatabaseTruncateCleanup(testName, "default", tableName);
    }
    
    /**
     * Add database truncate table cleanup task for specific data source
     */
    public static void addDatabaseTruncateCleanup(String testName, String dataSourceName, String tableName) {
        CleanupTask task = new CleanupTask() {
            private List<Map<String, Object>> backupData;
            
            @Override
            public void execute() throws Exception {
                // Backup all data before truncating
                if (isReversible()) {
                    backupData = DatabaseManager.executeQuery(dataSourceName, "SELECT * FROM " + tableName);
                }
                
                DatabaseManager.truncateTable(dataSourceName, tableName);
                logCleanup("Truncated table: " + tableName);
            }
            
            @Override
            public String getDescription() {
                return "Truncate table: " + tableName;
            }
            
            @Override
            public boolean isReversible() {
                return true;
            }
            
            @Override
            public void rollback() throws Exception {
                if (backupData != null && !backupData.isEmpty()) {
                    for (Map<String, Object> record : backupData) {
                        DatabaseManager.insertRecord(dataSourceName, tableName, record);
                    }
                    logCleanup("Restored " + backupData.size() + " rows to table: " + tableName);
                }
            }
        };
        
        addCleanupTask(testName, task);
    }
    
    // ====================
    // FILE SYSTEM CLEANUP TASKS
    // ====================
    
    /**
     * Add file deletion cleanup task
     */
    public static void addFileCleanup(String testName, String filePath) {
        CleanupTask task = new CleanupTask() {
            private byte[] backupContent;
            private boolean fileExisted;
            
            @Override
            public void execute() throws Exception {
                File file = new File(filePath);
                if (file.exists()) {
                    fileExisted = true;
                    // Backup file content for rollback
                    if (isReversible() && file.isFile()) {
                        backupContent = Files.readAllBytes(file.toPath());
                    }
                    
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                        logCleanup("Deleted directory: " + filePath);
                    } else {
                        boolean deleted = file.delete();
                        if (deleted) {
                            logCleanup("Deleted file: " + filePath);
                        } else {
                            throw new IOException("Failed to delete file: " + filePath);
                        }
                    }
                } else {
                    logCleanup("File not found (already cleaned): " + filePath);
                }
            }
            
            @Override
            public String getDescription() {
                return "Delete file/directory: " + filePath;
            }
            
            @Override
            public boolean isReversible() {
                return true;
            }
            
            @Override
            public void rollback() throws Exception {
                if (fileExisted && backupContent != null) {
                    Files.write(Paths.get(filePath), backupContent);
                    logCleanup("Restored file: " + filePath);
                }
            }
        };
        
        addCleanupTask(testName, task);
    }
    
    /**
     * Add directory cleanup task (delete all files in directory)
     */
    public static void addDirectoryCleanup(String testName, String directoryPath, String filePattern) {
        CleanupTask task = new CleanupTask() {
            private List<String> deletedFiles = new ArrayList<>();
            
            @Override
            public void execute() throws Exception {
                Path dir = Paths.get(directoryPath);
                if (Files.exists(dir) && Files.isDirectory(dir)) {
                    try (Stream<Path> files = Files.list(dir)) {
                        files.filter(path -> path.getFileName().toString().matches(filePattern))
                             .forEach(path -> {
                                 try {
                                     Files.delete(path);
                                     deletedFiles.add(path.toString());
                                 } catch (IOException e) {
                                     System.err.println("Failed to delete file: " + path + " - " + e.getMessage());
                                 }
                             });
                    }
                    logCleanup("Deleted " + deletedFiles.size() + " files from directory: " + directoryPath);
                }
            }
            
            @Override
            public String getDescription() {
                return "Delete files matching pattern '" + filePattern + "' from directory: " + directoryPath;
            }
            
            @Override
            public boolean isReversible() {
                return false; // File content recovery not implemented for bulk deletion
            }
            
            @Override
            public void rollback() throws Exception {
                // Not reversible for bulk file deletion
                logCleanup("Cannot rollback bulk file deletion from directory: " + directoryPath);
            }
        };
        
        addCleanupTask(testName, task);
    }
    
    /**
     * Add temporary file cleanup task
     */
    public static void addTempFileCleanup(String testName) {
        CleanupTask task = new CleanupTask() {
            @Override
            public void execute() throws Exception {
                String tempDir = System.getProperty("java.io.tmpdir");
                Path tempPath = Paths.get(tempDir);
                
                if (Files.exists(tempPath)) {
                    try (Stream<Path> files = Files.list(tempPath)) {
                        long deletedCount = files
                            .filter(path -> path.getFileName().toString().startsWith("test_"))
                            .filter(Files::isRegularFile)
                            .mapToLong(path -> {
                                try {
                                    Files.delete(path);
                                    return 1;
                                } catch (IOException e) {
                                    return 0;
                                }
                            })
                            .sum();
                        
                        logCleanup("Deleted " + deletedCount + " temporary test files");
                    }
                }
            }
            
            @Override
            public String getDescription() {
                return "Delete temporary test files";
            }
            
            @Override
            public boolean isReversible() {
                return false;
            }
            
            @Override
            public void rollback() throws Exception {
                // Not reversible
            }
        };
        
        addCleanupTask(testName, task);
    }
    
    // ====================
    // CUSTOM CLEANUP TASKS
    // ====================
    
    /**
     * Add custom cleanup task
     */
    public static void addCustomCleanup(String testName, String description, Runnable cleanupAction) {
        CleanupTask task = new CleanupTask() {
            @Override
            public void execute() throws Exception {
                cleanupAction.run();
                logCleanup("Executed custom cleanup: " + description);
            }
            
            @Override
            public String getDescription() {
                return description;
            }
            
            @Override
            public boolean isReversible() {
                return false;
            }
            
            @Override
            public void rollback() throws Exception {
                // Not reversible for custom actions
            }
        };
        
        addCleanupTask(testName, task);
    }
    
    /**
     * Add reversible custom cleanup task
     */
    public static void addReversibleCustomCleanup(String testName, String description, 
                                                 Runnable cleanupAction, Runnable rollbackAction) {
        CleanupTask task = new CleanupTask() {
            @Override
            public void execute() throws Exception {
                cleanupAction.run();
                logCleanup("Executed reversible custom cleanup: " + description);
            }
            
            @Override
            public String getDescription() {
                return description;
            }
            
            @Override
            public boolean isReversible() {
                return true;
            }
            
            @Override
            public void rollback() throws Exception {
                rollbackAction.run();
                logCleanup("Rolled back custom cleanup: " + description);
            }
        };
        
        addCleanupTask(testName, task);
    }
    
    // ====================
    // CLEANUP EXECUTION
    // ====================
    
    /**
     * Execute all cleanup tasks for a specific test
     */
    public static CleanupResult executeCleanup(String testName) {
        List<CleanupTask> tasks = cleanupTasks.get(testName);
        if (tasks == null || tasks.isEmpty()) {
            CleanupResult result = new CleanupResult(true, "No cleanup tasks found for test: " + testName, null);
            cleanupResults.put(testName, result);
            return result;
        }
        
        int successCount = 0;
        int totalCount = tasks.size();
        Exception lastException = null;
        
        logCleanup("Starting cleanup for test: " + testName + " (" + totalCount + " tasks)");
        
        for (CleanupTask task : tasks) {
            try {
                logCleanup("Executing: " + task.getDescription());
                task.execute();
                successCount++;
            } catch (Exception e) {
                lastException = e;
                logCleanup("Failed: " + task.getDescription() + " - " + e.getMessage());
            }
        }
        
        boolean success = successCount == totalCount;
        String message = String.format("Cleanup completed for %s: %d/%d tasks successful", 
                                       testName, successCount, totalCount);
        
        CleanupResult result = new CleanupResult(success, message, lastException);
        cleanupResults.put(testName, result);
        
        logCleanup(message);
        
        // Clear tasks after execution
        cleanupTasks.remove(testName);
        
        return result;
    }
    
    /**
     * Execute cleanup for all registered tests
     */
    public static Map<String, CleanupResult> executeAllCleanup() {
        Map<String, CleanupResult> results = new HashMap<>();
        
        for (String testName : cleanupTasks.keySet()) {
            CleanupResult result = executeCleanup(testName);
            results.put(testName, result);
        }
        
        return results;
    }
    
    /**
     * Rollback cleanup for a specific test
     */
    public static CleanupResult rollbackCleanup(String testName) {
        // Note: This would require storing executed tasks for rollback
        // For now, return not supported message
        CleanupResult result = new CleanupResult(false, "Rollback not implemented yet", 
                                               new UnsupportedOperationException("Rollback feature coming soon"));
        cleanupResults.put(testName + "_rollback", result);
        return result;
    }
    
    // ====================
    // UTILITY METHODS
    // ====================
    
    /**
     * Add cleanup task to the list
     */
    private static void addCleanupTask(String testName, CleanupTask task) {
        cleanupTasks.computeIfAbsent(testName, name -> new ArrayList<>()).add(task);
        logCleanup("Added cleanup task for " + testName + ": " + task.getDescription());
    }
    
    /**
     * Log cleanup activity
     */
    private static void logCleanup(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = "[" + timestamp + "] " + message;
        cleanupLog.add(logEntry);
        System.out.println("CLEANUP: " + logEntry);
    }
    
    /**
     * Delete directory recursively
     */
    private static void deleteDirectory(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        throw new IOException("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }
        if (!directory.delete()) {
            throw new IOException("Failed to delete directory: " + directory.getAbsolutePath());
        }
    }
    
    /**
     * Get cleanup tasks for a test
     */
    public static List<CleanupTask> getCleanupTasks(String testName) {
        return cleanupTasks.getOrDefault(testName, new ArrayList<>());
    }
    
    /**
     * Get cleanup result for a test
     */
    public static CleanupResult getCleanupResult(String testName) {
        return cleanupResults.get(testName);
    }
    
    /**
     * Get all cleanup results
     */
    public static Map<String, CleanupResult> getAllCleanupResults() {
        return new HashMap<>(cleanupResults);
    }
    
    /**
     * Get cleanup log
     */
    public static List<String> getCleanupLog() {
        return new ArrayList<>(cleanupLog);
    }
    
    /**
     * Clear all cleanup tasks and results
     */
    public static void clearAll() {
        cleanupTasks.clear();
        cleanupResults.clear();
        cleanupLog.clear();
        logCleanup("Cleared all cleanup tasks and results");
    }
    
    /**
     * Clear cleanup data for specific test
     */
    public static void clearTest(String testName) {
        cleanupTasks.remove(testName);
        cleanupResults.remove(testName);
        logCleanup("Cleared cleanup data for test: " + testName);
    }
    
    /**
     * Print cleanup summary
     */
    public static void printCleanupSummary() {
        System.out.println("\n=== CLEANUP SUMMARY ===");
        
        if (cleanupResults.isEmpty()) {
            System.out.println("No cleanup operations performed.");
            return;
        }
        
        int totalTests = cleanupResults.size();
        int successfulTests = (int) cleanupResults.values().stream().mapToInt(r -> r.success ? 1 : 0).sum();
        
        System.out.println("Total tests with cleanup: " + totalTests);
        System.out.println("Successful cleanups: " + successfulTests);
        System.out.println("Failed cleanups: " + (totalTests - successfulTests));
        
        for (Map.Entry<String, CleanupResult> entry : cleanupResults.entrySet()) {
            CleanupResult result = entry.getValue();
            String status = result.success ? "✓" : "✗";
            System.out.println(status + " " + entry.getKey() + ": " + result.message);
            
            if (!result.success && result.exception != null) {
                System.out.println("   Error: " + result.exception.getMessage());
            }
        }
        
        System.out.println("========================\n");
    }
}
