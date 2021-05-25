/*
*       ServiceTest.java
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

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jaroslav_b
 */
class ServiceTest {
    
    @Test
    @DisplayName("Setup")
    void testSetup() throws IOException {
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
    void testCleanAndAddSeparator() throws IOException {
        assertEquals("Ahoj;", Service.cleanAndAddSeparator(" Ah;oj ", ";"), "Bad string 1");
        assertEquals("Ahoj;", Service.cleanAndAddSeparator("Ahoj ;", ";"), "Bad string 2");
        assertEquals("Ahoj;", Service.cleanAndAddSeparator(" ;Ahoj", ";"), "Bad string 3");
        assertEquals("Ahoj;", Service.cleanAndAddSeparator(";Ah;;o;j; ; ", ";"), "Bad string 4");
    }
}
