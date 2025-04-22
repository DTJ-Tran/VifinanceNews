package test_java;
public class StringCheckUtil {

    // Method to check if string is wrapped with double quotes
    public static boolean hasSurroundingQuotes(String str) {
        return str != null && str.startsWith("\"") && str.endsWith("\"");
    }

    // Method to check if string still contains Unicode escape sequences like \\u_XXXX
    public static boolean hasUnicodeEscapes(String str) {
        if (str == null) return false;
        return str.matches(".*\\\\u[0-9a-fA-F]{4}.*"); // Regex: looks for \\u followed by 4 hex digits
    }

    // Combined method: checks for both conditions
    public static boolean needsDecoding(String str) {
        return hasSurroundingQuotes(str) || hasUnicodeEscapes(str);
    }

    // Testing
    public static void main(String[] args) {
        String test1 = "\"\\u0048\\u0065llo\""; // Has quotes and Unicode
        String test2 = "\\u0048ello";          // Only Unicode
        String test3 = "\"Hello\"";            // Only quotes
        String test4 = "Hello";                // Clean

        System.out.println(needsDecoding(test1)); // true
        System.out.println(needsDecoding(test2)); // true
        System.out.println(needsDecoding(test3)); // true
        System.out.println(needsDecoding(test4)); // false
    }
}