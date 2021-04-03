/*
*       HttpUtils.java
*
*       This file is part of Words project.
*       https://github.com/berk76/words
*
*       Words is free software; you can redistribute it and/or modify
*       it under the terms of the GNU General Public License as published by
*       the Free Software Foundation; either version 3 of the License, or
*       (at your option) any later version. <http://www.gnu.org/licenses/>
*
*       Written by Jaroslav Beran <jaroslav.beran@gmail.com>
*/
package cz.webstones.words.mp3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;


public class HttpUtils {
    
    private static final int GET = 0;
    private static final int POST = 1;
    
    private HttpUtils() {}
    
    
    public static String sendPost(String requestUrl, String requestData, String contentType) throws Mp3CreatorException {
        return send(HttpUtils.POST, requestUrl, requestData, contentType);
    }
    
    
    public static String sendGet(String requestUrl) throws Mp3CreatorException {
        return send(HttpUtils.GET, requestUrl, null, null);
    }
    
    
    private static String send(int method, String requestUrl, String requestData, String contentType) throws Mp3CreatorException {
        String result = null;
        String output;
        HttpURLConnection conn = null;
        
        try {
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            
            switch (method) {
                case HttpUtils.POST:
                    conn.setRequestProperty("Content-Type", contentType);
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream()); 
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, StandardCharsets.UTF_8))) {
                        writer.write(requestData);
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
                result = response.toString();
            } else {
                throw new Mp3CreatorException("Request to server returned " + responseCode);
            }
            
        } catch (Mp3CreatorException | IOException | JSONException ex) {
            throw new Mp3CreatorException(ex.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        
        return result;
    }    
}
