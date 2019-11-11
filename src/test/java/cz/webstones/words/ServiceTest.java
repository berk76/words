/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jaroslav_b
 */
public class ServiceTest {
    
    @Test
    @DisplayName("Setup")
    public void testSetup() throws IOException {
        Setup s = Service.getSetup(false, "Data");
        assertEquals("Data", s.getDataDir(), "Data dir");
        assertEquals("MP3", s.getMp3Dir(), "MP3 dir");
        assertEquals("Dictionary.txt", s.getDirectoryFile(), "Dictionary file");
        assertEquals("Categories.txt", s.getCategoryFile(), "Category file");
        assertEquals(";", s.getDictionarySeparator(), "Dictionary separator");
        assertEquals("dd.MM.yyyy HH:mm", s.getDictionaryDateFormat(), "Dictionary date format");
        assertEquals("Data/Dictionary.txt", s.getFullDictionaryFilePath(), "Full dictionary path");
        assertEquals("Data/Categories.txt", s.getFullCategoryFilePath(), "Full category path");
        assertEquals("Data/MP3", s.getFullMp3Path(), "Full MP3 dir path");
    }
    
    @Test
    @DisplayName("Sanity data string (removal of separator)")
    public void testCleanAndAddSeparator() throws IOException {
        assertEquals("Ahoj;", Service.cleanAndAddSeparator(" Ah;oj ", ";"), "Bad string 1");
        assertEquals("Ahoj;", Service.cleanAndAddSeparator("Ahoj ;", ";"), "Bad string 2");
        assertEquals("Ahoj;", Service.cleanAndAddSeparator(" ;Ahoj", ";"), "Bad string 3");
        assertEquals("Ahoj;", Service.cleanAndAddSeparator(";Ah;;o;j; ; ", ";"), "Bad string 4");
    }
}
