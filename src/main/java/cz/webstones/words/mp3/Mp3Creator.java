/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words.mp3;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author jaroslav_b
 */
public class Mp3Creator {
    
    private static final Logger LOGGER = Logger.getLogger(Mp3Creator.class.getName());
    
    /* https://soundoftext.com/docs */
    private static String endpoint = "https://api.soundoftext.com";
    //private static boolean trustedInstalled = false;
    
    
    public static void createMp3(String text, String lang, String file) throws Mp3CreatorException {
        
        /* Install trusted manager to avoid problems with https certificate */
        //if (!trustedInstalled) {
        //    HttpUtils.installTrustManager();
        //    trustedInstalled = true;
        //}
        
        /* Send text and get voice ID */
        JSONObject object = new JSONObject();
        object.put("engine", "Google");
        JSONObject data = new JSONObject();
        data.put("text", text);
        data.put("voice", lang);
        object.put("data", data);

        JSONObject resp = HttpUtils.sendPost(endpoint + "/sounds", object);
        if (!resp.getBoolean("success")) {
            throw new Mp3CreatorException("Request to server returned no success!");
        }
        String id = resp.getString("id");
        
        /* Wait for MP3 creation */
        do {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                Thread.currentThread().interrupt();
            }
            resp = HttpUtils.sendGet(endpoint + "/sounds/" + id);
        } while (resp.getString("status").equals("Pending"));
        
        if (resp.getString("status").equals("Error")) {
            throw new Mp3CreatorException(resp.getString("message"));
        }
        
        /* Get MP3 file and store it */
        HttpUtils.downloadFile(resp.getString("location"), file);
    }
    
}
