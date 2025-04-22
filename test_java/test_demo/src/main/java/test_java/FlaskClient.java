package test_java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class FlaskClient {
    private static final String BASE_URL_SEARCH = "http://127.0.0.1:5001";
    private static final String BASE_URL_SUM = "http://127.0.0.1:5002";
    private static final String BASE_URL_ANA = "http://127.0.0.1:5003";

    public static void main(String[] args) {
        try {
            // Step 1: Generate Query and Get Articles
            String userQuery = "ảnh hưởng của lệnh cấm vận Mỹ lên Việt Nam"; // Example query
            // String testURL = "https://vneconomy.vn/bi-my-cam-van-huawei-mat-ngoi-hang-dien-thoai-ban-chay-so-mot-the-gioi.htm";
            JSONArray articles = getCachedResults(userQuery);
            String jsonString = articles.toString();
            Gson gson = new Gson();
            List<String> list = gson.fromJson(jsonString, new TypeToken<List<String>>() {}.getType());
            if (articles.length() > 0) {
                
                JSONObject toxicityCheck = getToxicityCheck(list.get(0));
                System.out.println("Bias Check:\n" + toxicityCheck.toString(4));
            } else {
                System.out.println("No articles found for query: " + userQuery);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to send a GET request to Flask API
    private static JSONArray getCachedResults(String query) throws IOException {
        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        URL url = new URL(BASE_URL_SEARCH + "/get_cached_result/" + encodedQuery);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            String response = readResponse(conn);
            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getJSONArray("data"); // Extract article list
        } else {
            System.out.println("Failed to fetch articles, HTTP Code: " + responseCode);
            return new JSONArray();
        }
    }

    // Method to send a GET request to Flask API
    private static JSONObject getSummarizer(String articleUrl) throws IOException {
        URL sendUrl = new URL(BASE_URL_SUM + "/summarize/");

        HttpURLConnection conn = (HttpURLConnection) sendUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Send JSON payload
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("url", articleUrl);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }
    
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            String response = readResponse(conn);
            return new JSONObject(response); // Return JSON response
        } else {
            System.out.println("Failed to summarize, HTTP Code: " + responseCode);
            return new JSONObject();
        }
    }

    private static JSONObject getFactCheck(String articleUrl) throws IOException {
        URL sendUrl = new URL(BASE_URL_ANA + "/factcheck/");

        HttpURLConnection conn = (HttpURLConnection) sendUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Send JSON payload
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("url", articleUrl);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }
    
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            String response = readResponse(conn);
            return new JSONObject(response); // Return JSON response
        } else {
            System.out.println("Failed to fact-check, HTTP Code: " + responseCode);
            return new JSONObject();
        }
    }

    private static JSONObject getBiasCheck(String articleUrl) throws IOException {
        URL sendUrl = new URL(BASE_URL_ANA + "/biascheck/");

        HttpURLConnection conn = (HttpURLConnection) sendUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Send JSON payload
        JSONObject jsonPayload = new JSONObject();
        
        jsonPayload.put("url", articleUrl);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }
    
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            String response = readResponse(conn);
            return new JSONObject(response); // Return JSON response
        } else {
            System.out.println("Failed to bias-check, HTTP Code: " + responseCode);
            return new JSONObject();
        }
    }

    private static JSONObject getToxicityCheck(String articleUrl) throws IOException {
        URL sendUrl = new URL(BASE_URL_ANA + "/toxicity_analysis/");

        HttpURLConnection conn = (HttpURLConnection) sendUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Send JSON payload
        JSONObject jsonPayload = new JSONObject();
        
        jsonPayload.put("url", articleUrl);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }
    
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            String response = readResponse(conn);
            return new JSONObject(response); // Return JSON response
        } else {
            System.out.println("Failed to toxicity-check, HTTP Code: " + responseCode);
            return new JSONObject();
        }
    }
    
    private static JSONObject getSentimentAna(String articleUrl) throws IOException {
        URL sendUrl = new URL(BASE_URL_ANA + "/sentiment_analysis/");

        HttpURLConnection conn = (HttpURLConnection) sendUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Send JSON payload
        JSONObject jsonPayload = new JSONObject();
        
        jsonPayload.put("url", articleUrl);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }
    
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            String response = readResponse(conn);
            return new JSONObject(response); // Return JSON response
        } else {
            System.out.println("Failed to sentimemnt-analysis, HTTP Code: " + responseCode);
            return new JSONObject();
        }
    }

    // Method to send a POST request to Flask API (SUCCESS)
    private static void saveArticle(String articleUrl) throws IOException {
        URL url = new URL(BASE_URL_SEARCH + "/save");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // JSON payload
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("url", articleUrl);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            System.out.println("Article successfully saved to the database.");
        } else {
            System.out.println("Failed to save article, HTTP Code: " + responseCode);
        }
    }


    // Method to send a POST request to Flask API (SUCCESS)
    private static JSONObject getSynthesis(List<String> articleUrls) throws IOException {
        JSONObject responseJson = new JSONObject();
        try {
            URL url = new URL(BASE_URL_SUM + "/synthesis/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set request properties
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Convert list of URLs to JSON array
            JSONArray jsonPayload = new JSONArray(articleUrls);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Handle response
            int responseCode = conn.getResponseCode();
            String responseBody = readResponse(conn);

            responseJson.put("status", responseCode);
            responseJson.put("response", new JSONObject(responseBody));
           
        } catch (IOException e) {
            responseJson.put("status", 500);
            responseJson.put("error", e.getMessage());
        }
        return responseJson;
    };

    // Helper method to read API response
    private static String readResponse(HttpURLConnection conn) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
        return response.toString();
    }
}