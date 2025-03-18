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


     public static LinkedHashMap<Integer, JSONObject> getResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            String resStr = response.toString();
            String removeQuote = resStr.substring(1,resStr.length()-1);
            Properties prop = new Properties();
            prop.load(new StringReader("key=" + removeQuote));
            String decodedString = prop.getProperty("key");

            String cleanStr = decodedString.replace("\n", System.lineSeparator());
            JSONArray jsonArr = new JSONArray(cleanStr);
            LinkedHashMap<Integer, JSONObject> result = new LinkedHashMap<>();
            for (int iter = 0; iter < jsonArr.length(); iter++) {
                JSONObject obj = jsonArr.getJSONObject(iter); // Cast directly to JSONObject
                result.put(iter, obj);
            }
            
            return result;
        }
        
    };



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