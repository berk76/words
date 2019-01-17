/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import java.io.IOException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jaroslav_b
 */
public class DictionaryTest {
    
    @Test
    public void testWords() throws IOException, DictionaryException {
        Dictionary d = new Dictionary();
        
        /* Test initial values */
        assertEquals("Fil dictionary size:", 0, d.size());
        assertEquals("All dictionary size:", 0, d.sizeOfAll());
        assertEquals("Category list size:", 0, d.getCategoryList().size());
        
        /* Fill category list */
        d.addCategory("TestCat1");
        d.addCategory("TestCat2");
        d.addCategory("TestCat3");
        
        assertEquals("Fil dictionary size:", 0, d.size());
        assertEquals("All dictionary size:", 0, d.sizeOfAll());
        assertEquals("Category list size:", 3, d.getCategoryList().size());
        
        /* Fill some words */
        d.addWord(new WordDto("test1cz", "test1en", "TestCat1"));
        d.addWord(new WordDto("test2cz", "test2en", "TestCat1"));
        d.addWord(new WordDto("test3cz", "test3en", "TestCat2"));
        
        assertEquals("Fil dictionary size:", 3, d.size());
        assertEquals("All dictionary size:", 3, d.sizeOfAll());
        assertEquals("Category list size:", 3, d.getCategoryList().size());
        assertEquals("Curr cat:", Dictionary.allCategoryName, d.getCurrentCategory());
        
        /* Filter category */
        d.setCategory("TestCat1");
        
        assertEquals("Fil dictionary size:", 2, d.size());
        assertEquals("All dictionary size:", 3, d.sizeOfAll());
        assertEquals("Category list size:", 3, d.getCategoryList().size());
        assertEquals("Curr cat:", "TestCat1", d.getCurrentCategory());
        
        /* Delete word */
        d.deleteCurrentWord();
        
        assertEquals("Fil dictionary size:", 1, d.size());
        assertEquals("All dictionary size:", 2, d.sizeOfAll());
        assertEquals("Category list size:", 3, d.getCategoryList().size());
        assertEquals("Curr cat:", "TestCat1", d.getCurrentCategory());
        
        /* Unfilter category */
        
        d.setCategory(Dictionary.allCategoryName);
        
        assertEquals("Fil dictionary size:", 2, d.size());
        assertEquals("All dictionary size:", 2, d.sizeOfAll());
        assertEquals("Category list size:", 3, d.getCategoryList().size());
        assertEquals("Curr cat:", Dictionary.allCategoryName, d.getCurrentCategory());
        
        /* Delete last word in category */
        /* updateCategory list will remove all unused categories */
        d.setCategory("TestCat1");
        d.deleteCurrentWord();
        
        assertEquals("Fil dictionary size:", 1, d.size());
        assertEquals("All dictionary size:", 1, d.sizeOfAll());
        assertEquals("Category list size:", 1, d.getCategoryList().size());
        assertEquals("Curr cat:", Dictionary.allCategoryName, d.getCurrentCategory());
        
    }
    
    @Test
    public void testCategories() throws DictionaryException {
        Dictionary d = new Dictionary();
        
        assertEquals("Curr cat:", Dictionary.allCategoryName, d.getCurrentCategory());
        
        /* Fill category list */
        d.addCategory("TestCat1");
        d.addCategory("TestCat2");
        d.addCategory("TestCat3");
        
        assertEquals("Category list size:", 3, d.getCategoryList().size());
        
        /* Fill some words */
        d.addWord(new WordDto("test1cz", "test1en", "TestCat1"));
        d.addWord(new WordDto("test2cz", "test2en", "TestCat1"));
        d.addWord(new WordDto("test3cz", "test3en", "TestCat2"));
        
        /* Rename category */
        d.renameCategory("TestCat1", "TestCat4");
        
        assertEquals("Category list size:", 2, d.getCategoryList().size());
        
        d.setCategory("TestCat4");
        assertEquals("Fil dictionary size:", 2, d.size());
        assertEquals("All dictionary size:", 3, d.sizeOfAll());
    }
}
