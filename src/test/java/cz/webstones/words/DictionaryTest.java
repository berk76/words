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
        ObserverTestHelper o = new ObserverTestHelper(d);
        d.attach(o);
        
        /* Test initial values */
        assertEquals("Fil dictionary size:", 0, d.size());
        assertEquals("All dictionary size:", 0, d.sizeOfAll());
        assertEquals("Category list size:", 0, d.getCategoryList().size());
        
        /* Fill category list */
        o.reset();
        d.addCategory("TestCat1");
        d.addCategory("TestCat2");
        d.addCategory("TestCat3");
        
        assertEquals("Observer getNoChange:", 0, o.getNoChange());
        assertEquals("Observer getCurWordChanged:", 0, o.getCurWordChanged());
        assertEquals("Observer getCurWordDeleted:", 0, o.getCurWordDeleted());
        assertEquals("Observer getCurCategoryChanged:", 0, o.getCurCategoryChanged());
        assertEquals("Observer getCategoryListChanged:", 3, o.getCategoryListChanged());
        assertEquals("Observer getWordAdded:", 0, o.getWordAdded());
        assertEquals("Observer getUnknown:", 0, o.getUnknown());
        
        assertEquals("Fil dictionary size:", 0, d.size());
        assertEquals("All dictionary size:", 0, d.sizeOfAll());
        assertEquals("Category list size:", 3, d.getCategoryList().size());
        
        /* Fill some words */
        o.reset();
        d.addWord(new WordDto("test1cz", "test1en", "TestCat1"));
        d.addWord(new WordDto("test2cz", "test2en", "TestCat1"));
        d.addWord(new WordDto("test3cz", "test3en", "TestCat2"));
        
        assertEquals("Observer getNoChange:", 0, o.getNoChange());
        assertEquals("Observer getCurWordChanged:", 0, o.getCurWordChanged());
        assertEquals("Observer getCurWordDeleted:", 0, o.getCurWordDeleted());
        assertEquals("Observer getCurCategoryChanged:", 0, o.getCurCategoryChanged());
        assertEquals("Observer getCategoryListChanged:", 0, o.getCategoryListChanged());
        assertEquals("Observer getWordAdded:", 3, o.getWordAdded());
        assertEquals("Observer getUnknown:", 0, o.getUnknown());
        
        assertEquals("Fil dictionary size:", 3, d.size());
        assertEquals("All dictionary size:", 3, d.sizeOfAll());
        assertEquals("Category list size:", 3, d.getCategoryList().size());
        assertEquals("Curr cat:", Dictionary.allCategoryName, d.getCurrentCategory());
        
        /* Filter category */
        o.reset();
        d.setCategory("TestCat1");
        
        assertEquals("Observer getNoChange:", 0, o.getNoChange());
        assertEquals("Observer getCurWordChanged:", 1, o.getCurWordChanged());
        assertEquals("Observer getCurWordDeleted:", 0, o.getCurWordDeleted());
        assertEquals("Observer getCurCategoryChanged:", 1, o.getCurCategoryChanged());
        assertEquals("Observer getCategoryListChanged:", 0, o.getCategoryListChanged());
        assertEquals("Observer getWordAdded:", 0, o.getWordAdded());
        assertEquals("Observer getUnknown:", 0, o.getUnknown());
        
        assertEquals("Fil dictionary size:", 2, d.size());
        assertEquals("All dictionary size:", 3, d.sizeOfAll());
        assertEquals("Category list size:", 3, d.getCategoryList().size());
        assertEquals("Curr cat:", "TestCat1", d.getCurrentCategory());
        
        /* Delete word */
        o.reset();
        d.deleteCurrentWord();
        
        assertEquals("Observer getNoChange:", 0, o.getNoChange());
        assertEquals("Observer getCurWordChanged:", 0, o.getCurWordChanged());
        assertEquals("Observer getCurWordDeleted:", 1, o.getCurWordDeleted());
        assertEquals("Observer getCurCategoryChanged:", 0, o.getCurCategoryChanged());
        assertEquals("Observer getCategoryListChanged:", 0, o.getCategoryListChanged());
        assertEquals("Observer getWordAdded:", 0, o.getWordAdded());
        assertEquals("Observer getUnknown:", 0, o.getUnknown());
        
        assertEquals("Fil dictionary size:", 1, d.size());
        assertEquals("All dictionary size:", 2, d.sizeOfAll());
        assertEquals("Category list size:", 3, d.getCategoryList().size());
        assertEquals("Curr cat:", "TestCat1", d.getCurrentCategory());
        
        /* Unfilter category */
        o.reset();
        d.setCategory(Dictionary.allCategoryName);
        
        assertEquals("Observer getNoChange:", 0, o.getNoChange());
        assertEquals("Observer getCurWordChanged:", 1, o.getCurWordChanged());
        assertEquals("Observer getCurWordDeleted:", 0, o.getCurWordDeleted());
        assertEquals("Observer getCurCategoryChanged:", 1, o.getCurCategoryChanged());
        assertEquals("Observer getCategoryListChanged:", 0, o.getCategoryListChanged());
        assertEquals("Observer getWordAdded:", 0, o.getWordAdded());
        assertEquals("Observer getUnknown:", 0, o.getUnknown());
        
        assertEquals("Fil dictionary size:", 2, d.size());
        assertEquals("All dictionary size:", 2, d.sizeOfAll());
        assertEquals("Category list size:", 3, d.getCategoryList().size());
        assertEquals("Curr cat:", Dictionary.allCategoryName, d.getCurrentCategory());
        
        /* Delete last word in category */
        /* updateCategory list will remove all unused categories */
        d.setCategory("TestCat1");
        o.reset();
        d.deleteCurrentWord();
        
        assertEquals("Observer getNoChange:", 0, o.getNoChange());
        assertEquals("Observer getCurWordChanged:", 1, o.getCurWordChanged());
        assertEquals("Observer getCurWordDeleted:", 1, o.getCurWordDeleted());
        assertEquals("Observer getCurCategoryChanged:", 1, o.getCurCategoryChanged());
        assertEquals("Observer getCategoryListChanged:", 1, o.getCategoryListChanged());
        assertEquals("Observer getWordAdded:", 0, o.getWordAdded());
        assertEquals("Observer getUnknown:", 0, o.getUnknown());
        
        assertEquals("Fil dictionary size:", 1, d.size());
        assertEquals("All dictionary size:", 1, d.sizeOfAll());
        assertEquals("Category list size:", 1, d.getCategoryList().size());
        assertEquals("Curr cat:", Dictionary.allCategoryName, d.getCurrentCategory());
        
    }
    
    @Test
    public void testCategories() throws DictionaryException {
        Dictionary d = new Dictionary();
        ObserverTestHelper o = new ObserverTestHelper(d);
        d.attach(o);
        
        assertEquals("Curr cat:", Dictionary.allCategoryName, d.getCurrentCategory());
        
        /* Fill category list */
        o.reset();
        d.addCategory("TestCat1");
        d.addCategory("TestCat2");
        d.addCategory("TestCat3");
        
        assertEquals("Observer getNoChange:", 0, o.getNoChange());
        assertEquals("Observer getCurWordChanged:", 0, o.getCurWordChanged());
        assertEquals("Observer getCurWordDeleted:", 0, o.getCurWordDeleted());
        assertEquals("Observer getCurCategoryChanged:", 0, o.getCurCategoryChanged());
        assertEquals("Observer getCategoryListChanged:", 3, o.getCategoryListChanged());
        assertEquals("Observer getWordAdded:", 0, o.getWordAdded());
        assertEquals("Observer getUnknown:", 0, o.getUnknown());
        
        assertEquals("Category list size:", 3, d.getCategoryList().size());
        
        /* Fill some words */
        o.reset();
        d.addWord(new WordDto("test1cz", "test1en", "TestCat1"));
        d.addWord(new WordDto("test2cz", "test2en", "TestCat1"));
        d.addWord(new WordDto("test3cz", "test3en", "TestCat2"));
        
        assertEquals("Observer getNoChange:", 0, o.getNoChange());
        assertEquals("Observer getCurWordChanged:", 0, o.getCurWordChanged());
        assertEquals("Observer getCurWordDeleted:", 0, o.getCurWordDeleted());
        assertEquals("Observer getCurCategoryChanged:", 0, o.getCurCategoryChanged());
        assertEquals("Observer getCategoryListChanged:", 0, o.getCategoryListChanged());
        assertEquals("Observer getWordAdded:", 3, o.getWordAdded());
        assertEquals("Observer getUnknown:", 0, o.getUnknown());
        
        /* Rename category */
        o.reset();
        d.renameCategory("TestCat1", "TestCat4");
        
        assertEquals("Observer getNoChange:", 0, o.getNoChange());
        assertEquals("Observer getCurWordChanged:", 0, o.getCurWordChanged());
        assertEquals("Observer getCurWordDeleted:", 0, o.getCurWordDeleted());
        assertEquals("Observer getCurCategoryChanged:", 0, o.getCurCategoryChanged());
        assertEquals("Observer getCategoryListChanged:", 1, o.getCategoryListChanged());
        assertEquals("Observer getWordAdded:", 0, o.getWordAdded());
        assertEquals("Observer getUnknown:", 0, o.getUnknown());
        
        assertEquals("Category list size:", 2, d.getCategoryList().size());
        
        d.setCategory("TestCat4");
        assertEquals("Fil dictionary size:", 2, d.size());
        assertEquals("All dictionary size:", 3, d.sizeOfAll());
    }
}
