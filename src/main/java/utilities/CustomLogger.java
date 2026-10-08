package utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logs to both the console AND a timestamped .log file under target/logs/.
 * The log file is shared across all CustomLogger instances in the same JVM run
 * (static writer initialised once on first use).
 */
public class CustomLogger {

    private static final String INFO = "INFO";
    private static final String WARNING = "WARNING";
    private static final String ERROR = "ERROR";
    private static final String STEP = "STEP";

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static String logFilePath = "logs/";   // updated by initFileWriter
    // ── single shared log file for the whole test run ─────────────────────────
    private static final PrintWriter FILE_WRITER = initFileWriter();

    private static PrintWriter initFileWriter() {
        try {
            File dir = new File("logs");
            dir.mkdirs();
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File logFile = new File(dir, "T3WidgetTest_" + timestamp + ".log");
            logFilePath = logFile.getAbsolutePath();
            PrintWriter pw = new PrintWriter(new FileWriter(logFile, true), true /*auto-flush*/);
            pw.println("════════════════════════════════════════════════════════");
            pw.println("  T3 Widget Smoke Test Log  –  started at " + LocalDateTime.now().format(TS_FMT));
            pw.println("════════════════════════════════════════════════════════");
            System.out.println("INFO: Log file → " + logFile.getAbsolutePath());
            return pw;
        } catch (IOException e) {
            System.err.println("WARNING: Could not create log file: " + e.getMessage());
            return new PrintWriter(System.out, true);   // fallback – console only
        }
    }

    /**
     * Returns the absolute path of the current log file.
     */
    public static String getLogFilePath() {
        return logFilePath;
    }

    // ── internal write ────────────────────────────────────────────────────────
    private void log(String level, String message, boolean toErr) {
        String timestamp = LocalDateTime.now().format(TS_FMT);
        String formatted = "[" + timestamp + "] " + level + ": " + message;

        // Console
        if (toErr) {
            System.err.println(formatted);
        } else {
            System.out.println(formatted);
        }

        // File
        if (FILE_WRITER != null) {
            FILE_WRITER.println(formatted);
        }
    }

    // ── public API ────────────────────────────────────────────────────────────
    public void logInfo(String message) {
        log(INFO, message, false);
    }

    public void logError(String message) {
        log(ERROR, message, true);
    }

    public void logError(String message, Throwable throwable) {
        logError(message + " | " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
    }

    public void logWarning(String message) {
        log(WARNING, message, false);
    }

    public void logStep(String message) {
        log(STEP, message, false);
    }
}
