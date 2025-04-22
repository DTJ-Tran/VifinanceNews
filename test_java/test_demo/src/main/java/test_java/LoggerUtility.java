package test_java;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtility {

    /**
     * Provides a logger instance tied to the class calling it.
     * 
     * @param clazz Class requesting the logger.
     * @return Logger instance.
     */
    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

    /**
     * Log an info message.
     */
    public static void logInfo(Class<?> clazz, String message) {
        Logger logger = getLogger(clazz);
        logger.log(Level.INFO, message);
    }

    /**
     * Log a severe error message.
     */
    public static void logError(Class<?> clazz, String message, Throwable throwable) {
        Logger logger = getLogger(clazz);
        logger.log(Level.SEVERE, message, throwable);
    }


}