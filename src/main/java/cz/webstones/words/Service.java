/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.util.Random;

/**
 *
 * @author jaroslav_b
 */
public class Service {
    
    public static Setup loadSetup() throws IOException {
        Setup result = new Setup();
        InputStream is = null;
        Properties props = new Properties();
        Class cls = Setup.class;
        ClassLoader cLoader = cls.getClassLoader();
        
        is = cLoader.getResourceAsStream("setup.properties");
        props.load(is);
        is.close();
        
        result.setDataDir(props.getProperty("data.dir"));
        result.setMp3Dir(props.getProperty("mp3.dir"));
        result.setDirectoryFile(props.getProperty("dictionary.file"));
        result.setCategoryFile(props.getProperty("categories.file"));
        result.setDictionarySeparator(props.getProperty("dictionary.separator"));
        result.setDictionaryDateFormat(props.getProperty("dictionary.date.format"));
        return result;
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
    
    
    public static ArrayList<WordDto> loadDictionary(String file, 
            String separator, String dateFormat) 
            throws FileNotFoundException, UnsupportedEncodingException, IOException {
        
        ArrayList<WordDto> result = new ArrayList<WordDto>();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        
        InputStream is = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        
        boolean first = true;
        String line;
        while ((is != null) && ((line = reader.readLine()) != null)) {
            
            if (first) {
                bomPresent = false;
                if (isUTF8BOMPresent(line)) {
                    line = removeUTF8BOM(line);
                    bomPresent = true;
                }
                first = false;
            }
            
            String arr[] = line.split(separator);
            
            if (arr.length < 2)
                continue;
            
            WordDto w = new WordDto();
            w.setEn(arr[0]);
            w.setCz(arr[1]);
            w.setCategory(arr[2]);
            
            if (arr.length > 3) {
                w.setGoodHits(Integer.valueOf(arr[3]));
            }
            
            if (arr.length > 4) {
                try {
                    w.setLastGoodHit(sdf.parse(arr[4]));
                } catch (ParseException ex) {
                    w.setLastGoodHit(null);
                }
            }
            
            if (arr.length > 5)
                w.setWrongHits(Integer.valueOf(arr[5]));
            
            if (arr.length > 6) {
                try {
                    w.setLastWrongHit(sdf.parse(arr[6]));
                } catch (ParseException ex) {
                    w.setLastWrongHit(null);
                }
            }
            
            result.add(w);
            
        }
        reader.close();
        is.close();
        
        return result;
    }
    
    
    public static void saveDictionary(ArrayList<WordDto> dict, String file, 
            String separator, String dateFormat) 
            throws FileNotFoundException, UnsupportedEncodingException, IOException {
        
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        
        try {
            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos, "UTF-8");
            bw = new BufferedWriter(osw);
            
            if (bomPresent) {
                bw.write(UTF8_BOM);
            }
            
            for (WordDto w: dict) {
                bw.write(w.getEn() + separator);
                bw.write(w.getCz() + separator);
                bw.write(w.getCategory() + separator);
                bw.write(w.getGoodHits() + separator);
                bw.write(((w.getLastGoodHit() != null) ? sdf.format(w.getLastGoodHit()) : "") + separator);
                bw.write(w.getWrongHits() + separator);
                bw.write(((w.getLastWrongHit() != null) ? sdf.format(w.getLastWrongHit()) : "") + separator);
                bw.newLine();
            }
        } finally {
            bw.close();
            osw.close();
            fos.close();
        }
    }

            
    public static ArrayList<String> loadCategoryList(ArrayList<WordDto> dict) {
        ArrayList<String> result = new ArrayList<String>();
        
        for (WordDto w : dict) {
            boolean alreadyExists = false;
            
            for (String s: result) {
                if (s.equals(w.getCategory())) {
                    alreadyExists = true;
                    break;
                }
            }
            
            if (!alreadyExists)
                result.add(w.getCategory());
        }
        
        return result;
    }
    
    
    public static void saveCategoryList(ArrayList<String> cat, String file) 
            throws FileNotFoundException, UnsupportedEncodingException, IOException {
        
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        
        try {
            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos, "UTF-8");
            bw = new BufferedWriter(osw);
            
            for (String s: cat) {
                bw.write(s);
                bw.newLine();
            }
        } finally {
            bw.close();
            osw.close();
            fos.close();
        }
    }
    
    
    public static ArrayList<WordDto> createReorderedList(ArrayList<WordDto> dict, String category) {
        ArrayList<WordDto> result = new ArrayList<WordDto>();
        Random rand = new Random();
        
        for (WordDto w: dict) {
            if (category.equals("All") ||category.equals(w.getCategory()))
                result.add(w);
        }
        
        for (WordDto w: result) {
            int p = 0; // lower number means higher priority
            
            p += (w.getGoodHits() - w.getWrongHits()) * 10000;
            /*
            p += (365 * 24 * 60) - w.getLastWrongHitInMinutes() * 100;
            p += (365 * 24 * 60) - w.getLastGoodHitInMinutes() * 10;
            */
            p += rand.nextInt(10);
            w.setOrder(p);
        }
        
        Collections.sort(result, new Comparator<WordDto>() {
            @Override
            public int compare(WordDto a, WordDto b) {
                return a.getOrder() < b.getOrder() ? -1 : (a.getOrder() > b.getOrder()) ? 1 : 0;
            }
        });
        
        return result;
    }
    
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    // http://www.rgagnon.com/javadetails/java-handle-utf8-file-with-bom.html
    
    // FEFF because this is the Unicode char represented by the UTF-8 byte order mark (EF BB BF).
    public static final String UTF8_BOM = "\uFEFF";
    public static boolean bomPresent = false;
    
    private static boolean isUTF8BOMPresent(String s) {
        boolean result = false;
        if (s.startsWith(UTF8_BOM)) {
            result = true;
        }
        return result;
    }
    
    private static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }
    
}
