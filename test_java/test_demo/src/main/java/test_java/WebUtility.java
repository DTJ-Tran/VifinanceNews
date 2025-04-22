package test_java;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class WebUtility {

    // ✅ Initialize logger ONCE for the entire class
    private static final Logger log = LoggerUtility.getLogger(WebUtility.class);

    public static String encodeQueryParam(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            log.info(String.format("Successfully encoded query: %s to %s", query, encoded));            
            return encoded;
        } catch (UnsupportedEncodingException e) {
            log.severe(String.format("Failed to encode query parameter: \"%s\". Error: %s", query, e.getMessage()));
            return "";
        }
    };
    
    public static void getResponseAndPrint(HttpURLConnection conn) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            // Print the raw response
            System.out.println("Response from server:");
            System.out.println(response.toString());
        }
    }

    public static LinkedHashMap<Integer, JSONObject> getResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            String resStr = response.toString();
            while (StringCheckUtil.needsDecoding(resStr)) {
                resStr = WebUtility.removeSurroundingQuotes(resStr);
                resStr = WebUtility.decodeUnicodeString(resStr);
            }
            JSONArray jsonArr = new JSONArray(resStr);
            LinkedHashMap<Integer, JSONObject> result = new LinkedHashMap<>();
            for (int iter = 0; iter < jsonArr.length(); iter++) {
                JSONObject obj = jsonArr.getJSONObject(iter); // Cast directly to JSONObject
                result.put(iter, obj);
            }
        
            return result;
        }
        
    };

    private static String decodeUnicodeString(String input) throws IOException {
        Properties prop = new Properties();
        prop.load(new StringReader("key=" + input));
        String decoded = prop.getProperty("key");

        // Remove quotes again if double-escaped
        decoded = removeSurroundingQuotes(decoded);

        // Decode again if necessary
        prop.load(new StringReader("key=" + decoded));
        return prop.getProperty("key");
    }

    private static String removeSurroundingQuotes(String str) {
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    public static void main(String[] args) {
        //  For testing purpose
        log.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        log.addHandler(handler);

        // Test Case 1
        String query1 = "Giá Cổ Phiếu VinGroup đang như thế nào ?";
        String encoded1 = encodeQueryParam(query1);
        System.out.println("Encoded Query 1: " + encoded1);

        // Test Case 2
        String query2 = "Java Logging Test @123";
        String encoded2 = encodeQueryParam(query2);
        System.out.println("Encoded Query 2: " + encoded2);
    }
}