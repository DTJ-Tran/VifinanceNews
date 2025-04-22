package test_java;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FlaskApiTest {
    private static final String BASE_URL = "http://127.0.0.1:5001";
    
    private static final OkHttpClient client = new OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS) // Increase timeout to 30s
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build();

    public static void main(String[] args) {
        String query = "Gía vàng 2025";
        
        // Test GET request
        testGetArticles(query);

        // // Test POST request
        // testMoveToDatabase("https://example.com/article-1");
    }

    public static void testGetArticles(String userQuery) {
        try {
            // Encode query
            String encodedQuery = URLEncoder.encode(userQuery, StandardCharsets.UTF_8.toString());
            String url = BASE_URL + "/get_cached_result/" + encodedQuery;

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                System.out.println("GET Response: " + response.body().string());
            } else {
                System.out.println("GET Request Failed: " + response.code() + " - " + response.message());
            }
        } catch (IOException e) {
            System.err.println("Error in GET request: " + e.getMessage());
        }
    }

    public static void testMoveToDatabase(String articleUrl) {
        try {
            // JSON body
            String jsonBody = "{\"url\": \"" + articleUrl + "\"}";

            RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(BASE_URL + "/save")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                System.out.println("POST Response: " + response.body().string());
            } else {
                System.out.println("POST Request Failed: " + response.code() + " - " + response.message());
            }
        } catch (IOException e) {
            System.err.println("Error in POST request: " + e.getMessage());
        }
    }
}