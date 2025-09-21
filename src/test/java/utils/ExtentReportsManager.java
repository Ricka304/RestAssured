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
public class ExtentReportsManager {
    private static ExtentReports extent;
    private static String reportFilepath = System.getProperty("user.dir") + "/ExtentReports/";
    private static ExtentSparkReporter sparkReporter;
    public enum ReportStrategy {
        EVERY_EXECUTION,
        DAILY,
        WEEKLY,
        KEEP_LAST_N
    }
    // Change strategy if required
    private static final ReportStrategy STRATEGY = ReportStrategy.EVERY_EXECUTION;
    private static final int MAX_REPORTS_TO_KEEP = 10;


    public static ExtentReports createInstance() {
        String fileName = getReportNameWithTimeStamp();
        String reportPath = getReportFilePath();
        String reportFileLocation = reportPath + fileName;
        createReportPath(reportPath);
        handleReportCleanup(reportPath);


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


    private static void createReportPath (String path) {
        File testDirectory = new File(path);
        if (!testDirectory.exists()) {
            if (testDirectory.mkdirs()) {
                System.out.println("Directory: " + path + " is created!");
            } else {
                System.out.println("Failed to create directory: " + path);
            }
        }
    }
    private static String getReportFilePath () {
        return reportFilepath;
    }


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
    private static void handleReportCleanup(String reportPath) {
        if (STRATEGY == ReportStrategy.KEEP_LAST_N) {
            cleanupOldReports(reportPath, MAX_REPORTS_TO_KEEP);
        }
    }


    private static void cleanupOldReports(String reportPath, int maxReportsToKeep) {
        File reportDir = new File(reportPath);
        if (!reportDir.exists()) return;
        File[] reportFiles = reportDir.listFiles((dir, name) -> name.endsWith(".html"));
        if (reportFiles == null || reportFiles.length <= maxReportsToKeep) return;
        Arrays.sort(reportFiles, Comparator.comparingLong(File::lastModified));
        int filesToDelete = reportFiles.length - maxReportsToKeep;
        for (int i = 0; i < filesToDelete; i++) {
            if (reportFiles[i].delete()) {
                System.out.println("Deleted old report: " + reportFiles[i].getName());
            }
        }
    }


    public static ExtentReports getInstance() {
        if (extent == null) createInstance();
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
    private static void printReportSummary() {
        File reportDir = new File(reportFilepath);
        if (!reportDir.exists()) return;
        File[] reportFiles = reportDir.listFiles((dir, name) -> name.endsWith(".html"));
        if (reportFiles == null) return;
        System.out.println("📈 Report Strategy: " + STRATEGY);
        System.out.println("📄 Total reports available: " + reportFiles.length);
        if (reportFiles.length > 0) {
            Arrays.sort(reportFiles, Comparator.comparingLong(File::lastModified).reversed());
            System.out.println("🆕 Latest report: " + reportFiles[0].getName());
        }
    }
}