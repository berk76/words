package cz.webstones.words.mp3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;


public class HttpUtils {
    
    private static final int GET = 0;
    private static final int POST = 1;
    
    private HttpUtils() {}
    
    
    public static JSONObject sendPost(String requestUrl, JSONObject requestData) throws Mp3CreatorException {
        return send(HttpUtils.POST, requestUrl, requestData);
    }
    
    
    public static JSONObject sendGet(String requestUrl) throws Mp3CreatorException {
        return send(HttpUtils.GET, requestUrl, new JSONObject());
    }
    
    
    private static JSONObject send(int method, String requestUrl, JSONObject requestData) throws Mp3CreatorException {
        JSONObject result = null;
        String output;
        HttpURLConnection conn = null;
        
        try {
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            
            switch (method) {
                case HttpUtils.POST:
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream()); 
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, StandardCharsets.UTF_8))) {
                        writer.write(requestData.toString());
                    }
                    break;
                case HttpUtils.GET:
                    conn.setRequestMethod("GET");
                    break;
                default:
                    throw new Mp3CreatorException("Bad method " + method);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    response = new StringBuilder();
                    while ((output = reader.readLine()) != null) {
                        response.append(output);
                    }
                }
                result = new JSONObject(response.toString());
            } else {
                throw new Mp3CreatorException("Request to server returned " + responseCode);
            }
            
        } catch (Mp3CreatorException | IOException | JSONException ex) {
            throw new Mp3CreatorException(ex.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
        
        return result;
    }
    
    
    public static void downloadFile(String requestUrl, String filePath) throws Mp3CreatorException {
        HttpURLConnection conn = null;
        File f = new File(filePath);
        
        if (f.exists()) {
            throw new Mp3CreatorException("File " + filePath + " already exists.");
        }
        
        try {
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                byte[] buffer = new byte[4096];
                int n;

                try (InputStream input = conn.getInputStream(); FileOutputStream output = new FileOutputStream(f)) {
                    while ((n = input.read(buffer)) != -1) {
                        output.write(buffer, 0, n);
                    }
                }
            } else {
                throw new Mp3CreatorException("Request to server returned " + responseCode);
            }
            
        } catch (Mp3CreatorException | IOException ex) {
            throw new Mp3CreatorException(ex.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
    }
    
}
