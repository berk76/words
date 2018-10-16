/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words.mp3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONObject;

/**
 *
 * @author jaroslav_b
 */
public class HttpUtils {

    
    public static void installTrustManager() throws Mp3CreatorException {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }
            }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception ex) {
            throw new Mp3CreatorException(ex.getMessage());
        }
    }
    
    
    public static JSONObject sendPost(String requestUrl, JSONObject requestData) throws Mp3CreatorException {
        JSONObject result = null;
        String output;
        HttpURLConnection conn = null;
        
        try {
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));
            writer.write(requestData.toString());
            writer.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                while ((output = reader.readLine()) != null) {
                    response.append(output);
                }
                reader.close();
                result = new JSONObject(response.toString());
            } else {
                throw new Mp3CreatorException("Request to server returned " + responseCode);
            }
            
        } catch (Exception ex) {
            throw new Mp3CreatorException(ex.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
        
        return result;
    }
    
    
    public static JSONObject sendGet(String requestUrl) throws Mp3CreatorException {
        JSONObject result = null;
        
        String output;
        HttpURLConnection conn = null;
        
        try {
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                while ((output = reader.readLine()) != null) {
                    response.append(output);
                }
                reader.close();

                result = new JSONObject(response.toString());
            } else {
                throw new Mp3CreatorException("Request to server returned " + responseCode);
            }
            
        } catch (Exception ex) {
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
                InputStream input = conn.getInputStream();
               
                FileOutputStream output = new FileOutputStream(f);
                while ((n = input.read(buffer)) != -1) {
                    output.write(buffer, 0, n);
                }
                output.close();
            } else {
                throw new Mp3CreatorException("Request to server returned " + responseCode);
            }
            
        } catch (Exception ex) {
            throw new Mp3CreatorException(ex.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
    }
    
}
