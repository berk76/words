package cz.webstones.words.mp3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Base64;
import org.json.JSONArray;
import org.json.JSONException;


public class Mp3Creator {
    
    private Mp3Creator() {}
    
    /*
    *   https://github.com/ncpierson/google-translate-tts
    *   https://github.com/Boudewijn26/gTTS-token/blob/master/docs/november-2020-translate-changes.md
    */
    
    private static final String URL = "https://translate.google.com/_/TranslateWebserverUi/data/batchexecute";
    private static final String DATA = "[[[\"jQ1olc\",\"[\\\"%s\\\",\\\"%s\\\",null,\\\"null\\\"]\",null,\"generic\"]]]";
    private static final String PAYLOAD = "f.req=%s";
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    
    public static void createMp3(String text, String lang, String file) throws Mp3CreatorException {
        try {
            String value = String.format(DATA, text, lang);
            String payload = String.format(PAYLOAD, URLEncoder.encode(value, "UTF-8"));
            String resp = HttpUtils.sendPost(URL, payload, CONTENT_TYPE);
            resp = resp.replaceFirst("\\)]}'", "");
            JSONArray a = new JSONArray(resp);
            a = new JSONArray(a.getJSONArray(0).getString(2));
            byte[] buffer = Base64.getDecoder().decode(a.getString(0));
            try (FileOutputStream output = new FileOutputStream(new File(file))) {
                output.write(buffer);
            }
        } catch(Mp3CreatorException | IOException | JSONException ex) {
            throw new Mp3CreatorException(ex.getMessage());
        }
    }
}
