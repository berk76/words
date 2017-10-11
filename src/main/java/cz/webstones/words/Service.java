/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
}
