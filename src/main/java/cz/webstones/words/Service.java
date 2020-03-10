/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * @author jaroslav_b
 */
public class Service {

    public static final String version = "Words 1.10.0 snapshot";
    public static final String setupFName = "setup.properties";
    private static final String historyFName = "history.properties";
    private static final String appDataDir = "WordsData";

    
    private static String getDataDir() {

        //Is current dir writable?
        String result = System.getProperty("user.dir");
        File f = new File(result + File.separator + appDataDir + "test.txt");
        try {
            f.createNewFile();
            f.delete();
            return result;
        } catch (Exception ex) {
        }

        //If not use local app data dir
        result = System.getenv("LOCALAPPDATA") + File.separator + appDataDir;
        f = new File(result);
        if (!f.exists()) {
            f.mkdir();
        }
        return result;
    }

    public static String getHistory() throws IOException {
        String result = null;
        Properties p = new Properties();
        String path = getDataDir() + File.separator + historyFName;
        File f = new File(path);
        
        if (f.canRead()) {
            InputStream is = null;
            try {
                is = new FileInputStream(f);
                p.load(is);
                result = p.getProperty("dictPath");
            } catch(Exception ex) {
                ex.printStackTrace();
            } finally {
                if (is != null)
                    is.close();
            }
        }
        
        if (result == null) {
            result = getDataDir() + File.separator + "Data";
            f = new File(result);
            if (!f.isDirectory()) {
                f.mkdir();
            }

            // Fix setup.properties location from previous version
            f = new File(getDataDir() + File.separator + setupFName);
            if (f.canRead()) {
                f.renameTo(new File(getDataDir() + File.separator + "Data" + File.separator + setupFName));
            }
        }
        
        return result;
    }
    
    public static void saveHistory(String dictPath) throws IOException {
        Properties p = new Properties();
        String path = getDataDir() + File.separator + historyFName;
        File f = new File(path);
        
        p.setProperty("dictPath", dictPath);
        
        OutputStream os = null;
        try {
            os = new FileOutputStream(f);
            p.store(os, null);
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            if (os != null)
                os.close();
        }
    }
    
    public static Setup getSetup(boolean createLocalCopy, String dictPath) {
        Setup setup = null;
        
        try {
            /* Load user setup */
            File s = new File(dictPath + File.separator + setupFName);
            if (s.canRead()) {
                setup = loadSetup(s, dictPath, setup);
            }

            /* Load default setup */
            setup = loadSetup(null, dictPath, setup);

            /* Save user setup if doesn't exist */
            if (!s.exists() && createLocalCopy) {
                saveSetup(setup);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return setup;
    }

    private static Setup loadSetup(File f, String dictPath, Setup setup) throws IOException {
        InputStream is = null;
        Properties props = new Properties();

        if (setup == null) {
            setup = new Setup();
        }
        
        if (f != null) {
            is = new FileInputStream(f);
        } else {
            Class cls = Setup.class;
            ClassLoader cLoader = cls.getClassLoader();
            is = cLoader.getResourceAsStream("setup.properties");
        }

        props.load(is);
        is.close();
        
        setup.setDataDir(dictPath);

        if (setup.getDataDir() == null) {
            setup.setDataDir(props.getProperty("data.dir"));
        }

        if (setup.getMp3Dir() == null) {
            setup.setMp3Dir(props.getProperty("mp3.dir"));
        }

        if (setup.getDirectoryFile() == null) {
            setup.setDirectoryFile(props.getProperty("dictionary.file"));
        }

        if (setup.getCategoryFile() == null) {
            setup.setCategoryFile(props.getProperty("categories.file"));
        }

        if (setup.getDictionarySeparator() == null) {
            setup.setDictionarySeparator(props.getProperty("dictionary.separator"));
        }

        if (setup.getDictionaryDateFormat() == null) {
            setup.setDictionaryDateFormat(props.getProperty("dictionary.date.format"));
        }

        if (setup.getLanguage() == null) {
            setup.setLanguage(props.getProperty("language.id"));
        }
        
        return setup;
    }

    public static void saveSetup(Setup setup) throws IOException {
        OutputStream output = null;
        Properties props = new Properties();
        File f = new File(setup.getDataDir() + File.separator + setupFName);

        props.setProperty("data.dir", setup.getDataDir());
        props.setProperty("mp3.dir", setup.getMp3Dir());
        props.setProperty("dictionary.file", setup.getDirectoryFile());
        props.setProperty("categories.file", setup.getCategoryFile());
        props.setProperty("dictionary.separator", setup.getDictionarySeparator());
        props.setProperty("dictionary.date.format", setup.getDictionaryDateFormat());
        props.setProperty("language.id", setup.getLanguage());

        try {
            output = new FileOutputStream(f);
            props.store(output, null);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void checkOrCreateDirectory(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            f.mkdir();
        }
    }

    public static void checkOrCreateFile(String file) throws IOException {
        File f = new File(file);
        if (!f.isFile()) {
            f.createNewFile();
        }
    }

    public static String cleanAndAddSeparator(String s, String separator) {
        String result;

        if (s == null) {
            result = "";
        } else {
            result = s.replaceAll(separator, "").trim();
        }
        result += separator;

        return result;
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    // http://www.rgagnon.com/javadetails/java-handle-utf8-file-with-bom.html
    // FEFF because this is the Unicode char represented by the UTF-8 byte order mark (EF BB BF).
    public static final String UTF8_BOM = "\uFEFF";
    public static boolean bomPresent = false;

    public static boolean isUTF8BOMPresent(String s) {
        boolean result = false;
        if (s.startsWith(UTF8_BOM)) {
            result = true;
        }
        return result;
    }

    public static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }

    public static Font findFont(String text, Font currentFont) {
        Font result = currentFont;
        boolean ok = true;

        if (text == null) {
            return result;
        }

        for (int n = 0; n < text.length(); n++) {
            if (!currentFont.canDisplay(text.charAt(n))) {
                ok = false;
                break;
            }
        }

        if (!ok) {

            Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
            for (Font font : fonts) {

                for (int n = 0; n < text.length(); n++) {
                    if (!font.canDisplay(text.charAt(n))) {
                        ok = false;
                        break;
                    }
                    ok = true;
                }

                if (ok) {
                    result = new Font(font.getName(), Font.PLAIN, currentFont.getSize());
                    break;
                }
            }
        }

        return result;
    }
    
    public static ArrayList<LanguageDto> getLanguageList() throws UnsupportedEncodingException, IOException {
        ArrayList<LanguageDto> result = new ArrayList<LanguageDto>();

        Class cls = Setup.class;
        ClassLoader cLoader = cls.getClassLoader();
        InputStream is = cLoader.getResourceAsStream("languages.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

        String line;
        while ((is != null) && ((line = reader.readLine()) != null)) {
            String a[] = line.split(";");
            if (a.length == 2) {
                LanguageDto l = new LanguageDto();
                l.setCode(a[0]);
                l.setName(a[1]);
                result.add(l);
            }
        }
        reader.close();
        if (is != null) {
            is.close();
        }
        
        return result;
    }
}
