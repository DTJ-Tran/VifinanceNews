package test_java;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import org.json.JSONObject;


public class SearchArticle {
    private String baseURL;
    private static final Logger logger = Logger.getLogger(SearchArticle.class.getName()); // Create a logger to log the error to trace the error

    private void setBaseURL(String inputBaseURL) {
        this.baseURL = inputBaseURL;
    };

    public String getBaseURL() {
        return this.baseURL;
    };

    public SearchArticle(String inBaseURL) {
        this.setBaseURL(inBaseURL);
       
    };

    public LinkedHashMap<Integer, JSONObject> getSearchResult(String userQuery) {
        try {
            String encodedQuery = WebUtility.encodeQueryParam(userQuery);
            String userRequestURL = this.baseURL + "/search_result/" + encodedQuery;
            URL url = new URL(userRequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
    
            int responseCode = conn.getResponseCode();
            System.out.println("GET Response Code: " + responseCode);
            System.out.println("Check the repsonse");
            LinkedHashMap<Integer, JSONObject> responseResult = WebUtility.getResponse(conn);
            /*
             * Each JSON has the properties: date_downloaded, date_publish, author, image_url, main_text,title, url
             * The Database accept the properties: date_publish, author, image_url,title, url
             */
            return responseResult; // ✅ Also return the result instead of null
        } catch (Exception e) {
            logger.severe(String.format("Failed to get articles for query: \"%s\". Error: %s", userQuery, e.getMessage()));        
            return null;
        }
    };

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        String baseUrl = "http://127.0.0.1:5000"; // Flask default localhost
        SearchArticle testSA = new SearchArticle(baseUrl);
        String userQuery = "Giá cổ phiếu VinGroup đang như thế nào ?";
        LinkedHashMap<Integer, JSONObject> result =  testSA.getSearchResult(userQuery);
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) + " ms");
        JSONObject exampleObj = result.get(1);
        System.out.println(exampleObj.keySet().toString()); 
        // [date_download, date_publish, author, image_url, description, main_text, title, url, tags]
        System.out.println(exampleObj.get("date_publish") + "\n");
        System.out.println(exampleObj.get("author") + "\n");
        System.out.println(exampleObj.get("image_url") + "\n");
        System.out.println(exampleObj.get("description")+ "\n");
        System.out.println(exampleObj.get("title") + "\n");
        System.out.println(exampleObj.get("url")+ "\n");
        System.out.println(exampleObj.get("tags")+ "\n");

        // System.out.println(result.toString());
    }
}
