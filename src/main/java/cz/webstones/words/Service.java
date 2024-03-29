/*
*       Service.java
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jaroslav_b
 */
public class Service {
    
    private static final Logger LOGGER = Logger.getLogger(Service.class.getName());

    public static final String VERSION = "Words 1.14.0 snapshot";
    public static final String SETUP_FNAME = "setup.properties";
    private static final String HISTORY_FNAME = "history.properties";
    private static final String APP_DATA_DIR = ".words_data";
    private static final String FONT_NAME = "Tahoma";
    
    
    private static String getDataDir() {
        String home = (System.getenv("HOME") != null) ? System.getenv("HOME") : System.getenv("HOMEPATH");
        String result = home + File.separator + APP_DATA_DIR;
        File f = new File(result);
        if (!f.exists()) {
            if (!f.mkdir()) {
                LOGGER.log(Level.WARNING, "Cannot create directory {}", f);
            }
        }
        return result;
    }

    public static String getHistory() throws IOException {
        String result = null;
        Properties p = new Properties();
        String path = getDataDir() + File.separator + HISTORY_FNAME;
        File f = new File(path);
        
        if (f.canRead()) {
            try (InputStream is = new FileInputStream(f)) {
                p.load(is);
                result = p.getProperty("dictPath");
            } catch(Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        
        if (result == null) {
            result = getDataDir() + File.separator + "Data";
            f = new File(result);
            if (!f.isDirectory()) {
                if (!f.mkdir()) {
                    LOGGER.log(Level.WARNING, "Cannot create directory {}", f);
                }
            }

            // Fix setup.properties location from previous version
            f = new File(getDataDir() + File.separator + SETUP_FNAME);
            if (f.canRead()) {
                if (!f.renameTo(new File(getDataDir() + File.separator + "Data" + File.separator + SETUP_FNAME))) {
                    LOGGER.log(Level.WARNING, "Cannot rename file {}", f);
                }
            }
        }
        
        return result;
    }
    
    public static void saveHistory(String dictPath) throws IOException {
        Properties p = new Properties();
        String path = getDataDir() + File.separator + HISTORY_FNAME;
        File f = new File(path);
        
        p.setProperty("dictPath", dictPath);
        
        try (OutputStream os = new FileOutputStream(f)) {
            p.store(os, null);
        } catch(Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public static Setup getSetup(boolean createLocalCopy, String dictPath) {
        Setup setup = null;
        
        try {
            /* Load user setup */
            File s = new File(dictPath + File.separator + SETUP_FNAME);
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
            LOGGER.log(Level.SEVERE, null, ex);
        }

        return setup;
    }

    private static Setup loadSetup(File f, String dictPath, Setup setup) throws IOException {
        Properties props = new Properties();

        if (setup == null) {
            setup = new Setup();
        }
        
        if (f != null) {
            try (InputStream is = new FileInputStream(f)) {
                props.load(is);
            }
        } else {
            Class<Setup> cls = Setup.class;
            ClassLoader cLoader = cls.getClassLoader();
            try (InputStream is = cLoader.getResourceAsStream("setup.properties")) {
                props.load(is);
            }
        }
        
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
        Properties props = new Properties();
        File f = new File(setup.getDataDir() + File.separator + SETUP_FNAME);

        props.setProperty("data.dir", setup.getDataDir());
        props.setProperty("mp3.dir", setup.getMp3Dir());
        props.setProperty("dictionary.file", setup.getDirectoryFile());
        props.setProperty("categories.file", setup.getCategoryFile());
        props.setProperty("dictionary.separator", setup.getDictionarySeparator());
        props.setProperty("dictionary.date.format", setup.getDictionaryDateFormat());
        props.setProperty("language.id", setup.getLanguage());

        try (OutputStream output = new FileOutputStream(f)) {
            props.store(output, null);
        }
    }

    public static void checkOrCreateDirectory(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            if (!f.mkdir()) {
                LOGGER.log(Level.WARNING, "Cannot create directory {}", f);
            }
        }
    }

    public static void checkOrCreateFile(String file) throws IOException {
        File f = new File(file);
        if (!f.isFile()) {
            if (!f.createNewFile()){
                LOGGER.log(Level.WARNING, "Cannot create file {}", f);
            }
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
    
    public static Font createFontSmall() {
        return new Font(FONT_NAME, 0, 12);
    }
    
    public static Font createFont() {
        return new Font(FONT_NAME, 0, 18);
    }
    
    public static Font createFontLarge() {
        return new Font(FONT_NAME, 0, 24);
    }

    public static Font findFont(String text, Font currentFont) {
        Font result = currentFont;
        
        if (text == null) {
            return result;
        }

        if (currentFont.canDisplayUpTo(text) == -1) {
            return result;
        }

        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (Font font : fonts) {
            if (font.canDisplayUpTo(text) == -1) {
                result = new Font(font.getName(), Font.PLAIN, currentFont.getSize());
                break;
            }
        }

        return result;
    }
    
    public static ArrayList<LanguageDto> getLanguageList() throws IOException {
        ArrayList<LanguageDto> result = new ArrayList<>();

        Class<Setup> cls = Setup.class;
        ClassLoader cLoader = cls.getClassLoader();
        try (InputStream is = cLoader.getResourceAsStream("languages.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
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
        }

        
        return result;
    }
    
    public static boolean validateLang(String lang) throws IOException {
        boolean result = false;
        
        if (lang == null)
            return false;
        
        List<LanguageDto> list = getLanguageList();
        for (LanguageDto l: list) {
            if (l.getCode().equals(lang)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
