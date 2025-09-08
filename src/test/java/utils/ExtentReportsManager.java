package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import config.ConfigManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * ExtentReports Manager with multiple report storage strategies
 * 
 * REPORT STRATEGIES:
 * 
 * 1. EVERY_EXECUTION (Default):
 *    - Creates: Test_Report_2025_09_04_18_18_03.html
 *    - Behavior: New report for every test run
 *    - Use case: Complete history of all test executions
 * 
 * 2. DAILY:
 *    - Creates: Daily_Report_2025_09_04.html
 *    - Behavior: One report per day (overwrites same day runs)
 *    - Use case: Daily summary of all test runs
 * 
 * 3. WEEKLY:
 *    - Creates: Weekly_Report_2025_W36.html
 *    - Behavior: One report per week
 *    - Use case: Weekly test summary
 * 
 * 4. KEEP_LAST_N:
 *    - Creates: Test_Report_2025_09_04_18_18_03.html
 *    - Behavior: Keeps only last N reports, deletes older ones
 *    - Use case: Limited storage but detailed execution history
 * 
 * TO CHANGE STRATEGY: 
 * Modify the STRATEGY constant below (line ~29)
 */
public class ExtentReportsManager {
    
    private static ExtentReports extent;
    private static String reportFilepath = System.getProperty("user.dir") + "/ExtentReports/";
    private static ExtentSparkReporter sparkReporter;
    
    // Report storage strategy
    public enum ReportStrategy {
        EVERY_EXECUTION,    // Creates new report for each test run
        DAILY,              // One report per day (overwrites)
        WEEKLY,             // One report per week
        KEEP_LAST_N         // Keep only last N reports
    }
    
    // Configuration - Change these as needed
    private static final ReportStrategy STRATEGY = ReportStrategy.EVERY_EXECUTION;
    private static final int MAX_REPORTS_TO_KEEP = 10; // Only used with KEEP_LAST_N strategy

    public static ExtentReports createInstance() {
        String fileName = getReportNameWithTimeStamp();
        String reportFilepath = getReportFilePath();
        String reportFileLocation = reportFilepath + fileName;
        createReportPath(reportFilepath);
        
        // Handle report cleanup based on strategy
        handleReportCleanup(reportFilepath);
        
        sparkReporter = new ExtentSparkReporter(reportFileLocation);
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("GSS API Test Report");
        sparkReporter.config().setEncoding("utf-8");
        sparkReporter.config().setReportName("GSS API Automation Test Results");
        sparkReporter.config().setTimelineEnabled(true);
        
        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        extent.setSystemInfo("Application", "GSS Application");
        extent.setSystemInfo("Environment", ConfigManager.getEnvironment().toUpperCase());
        extent.setSystemInfo("Base URL", ConfigManager.getBaseUri());
        extent.setSystemInfo("User", System.getProperty("user.name"));
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        
        return extent;
    }

    //Create the report path if it does not exist
    private static void createReportPath (String path) {
        File testDirectory = new File(path);
        if (!testDirectory.exists()) {
            if (testDirectory.mkdirs()) {
                System.out.println("Directory: " + path + " is created!" );
            } else {
                System.out.println("Failed to create directory: " + path);
            }
        } else {
            System.out.println("Directory already exists: " + path);
        }
    }

    //Get the report file path
    private static String getReportFilePath () {
        return reportFilepath;
    }

    //Get the report name based on strategy
    private static String getReportNameWithTimeStamp() {
        SimpleDateFormat sdf;
        String reportFileName;
        
        switch (STRATEGY) {
            case DAILY:
                sdf = new SimpleDateFormat("yyyy_MM_dd");
                reportFileName = "Daily_Report_" + sdf.format(new Date()) + ".html";
                break;
            case WEEKLY:
                sdf = new SimpleDateFormat("yyyy_'W'ww");
                reportFileName = "Weekly_Report_" + sdf.format(new Date()) + ".html";
                break;
            case KEEP_LAST_N:
            case EVERY_EXECUTION:
            default:
                sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                reportFileName = "Test_Report_" + sdf.format(new Date()) + ".html";
                break;
        }
        
        return reportFileName;
    }
    
    //Handle report cleanup based on strategy
    private static void handleReportCleanup(String reportPath) {
        if (STRATEGY == ReportStrategy.KEEP_LAST_N) {
            cleanupOldReports(reportPath, MAX_REPORTS_TO_KEEP);
        }
    }
    
    //Clean up old reports keeping only the last N reports
    private static void cleanupOldReports(String reportPath, int maxReportsToKeep) {
        File reportDir = new File(reportPath);
        if (!reportDir.exists()) return;
        
        File[] reportFiles = reportDir.listFiles((dir, name) -> name.endsWith(".html"));
        if (reportFiles == null || reportFiles.length <= maxReportsToKeep) return;
        
        // Sort files by last modified date (oldest first)
        Arrays.sort(reportFiles, Comparator.comparingLong(File::lastModified));
        
        // Delete oldest files, keeping only the last N
        int filesToDelete = reportFiles.length - maxReportsToKeep;
        for (int i = 0; i < filesToDelete; i++) {
            if (reportFiles[i].delete()) {
                System.out.println("Deleted old report: " + reportFiles[i].getName());
            }
        }
    }

    public static ExtentReports getInstance() {
        if (extent == null)
            createInstance();
        return extent;
    }

    public static void flushReports() {
        if (extent != null) {
            extent.flush();
            System.out.println("📊 ExtentReport generated successfully!");
            System.out.println("📁 Report location: " + reportFilepath);
            printReportSummary();
        }
    }
    
    //Print summary of available reports
    private static void printReportSummary() {
        File reportDir = new File(reportFilepath);
        if (!reportDir.exists()) return;
        
        File[] reportFiles = reportDir.listFiles((dir, name) -> name.endsWith(".html"));
        if (reportFiles == null) return;
        
        System.out.println("📈 Report Strategy: " + STRATEGY);
        System.out.println("📄 Total reports available: " + reportFiles.length);
        
        if (reportFiles.length > 0) {
            // Sort by last modified (newest first)
            Arrays.sort(reportFiles, Comparator.comparingLong(File::lastModified).reversed());
            System.out.println("🆕 Latest report: " + reportFiles[0].getName());
        }
    }
    
    //Utility method to change report strategy programmatically
    public static void setReportStrategy(ReportStrategy strategy) {
        // Note: This would require making STRATEGY non-final and adding proper synchronization
        System.out.println("⚠️ To change report strategy, modify STRATEGY constant in ExtentReportsManager.java");
        System.out.println("Current strategy: " + STRATEGY);
        System.out.println("Available strategies: " + Arrays.toString(ReportStrategy.values()));
    }
}
