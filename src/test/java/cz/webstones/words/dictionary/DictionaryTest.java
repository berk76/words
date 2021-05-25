/*
*       DictionaryTest.java
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
package cz.webstones.words.dictionary;

import cz.webstones.words.Service;
import cz.webstones.words.dictionary.impl.DictionaryImpl;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jaroslav_b
 */
class DictionaryTest {
    
    @Test
    @DisplayName("Words (add, filter, delete, unfilter, delete last)")
    void testWords() throws IOException, DictionaryException {
        IDictionary d = new DictionaryImpl();
        String dictPath = Service.getHistory();
        d.loadDictionary(Service.getSetup(true, dictPath));
        ObserverTestHelper o = new ObserverTestHelper(d);
        d.attach(o);
        
        /* Test initial values */
        assertEquals(0, d.size(), "Fil dictionary size");
        assertEquals(0, d.sizeOfAll(), "All dictionary size");
        assertEquals(0, d.getCategoryList().size(), "Category list size");
        
        /* Fill category list */
        o.reset();
        d.addCategory("TestCat1");
        d.addCategory("TestCat2");
        d.addCategory("TestCat3");
        
        assertEquals(0, o.getNoChange(), "Observer getNoChange");
        assertEquals(0, o.getCurWordChanged(), "Observer getCurWordChanged");
        assertEquals(0, o.getCurWordDeleted(), "Observer getCurWordDeleted");
        assertEquals(0, o.getCurCategoryChanged(), "Observer getCurCategoryChanged");
        assertEquals(3, o.getCategoryListChanged(), "Observer getCategoryListChanged");
        assertEquals(0, o.getWordAdded(), "Observer getWordAdded");
        assertEquals(0, o.getUnknown(), "Observer getUnknown");
        
        assertEquals(0, d.size(), "Fil dictionary size");
        assertEquals(0, d.sizeOfAll(), "All dictionary size");
        assertEquals(3, d.getCategoryList().size(), "Category list size");
        
        /* Fill some words */
        o.reset();
        d.addWord(new WordDto("test1cz", "test1en", "TestCat1"));
        d.addWord(new WordDto("test2cz", "test2en", "TestCat1"));
        d.addWord(new WordDto("test3cz", "test3en", "TestCat2"));
        
        assertEquals(0, o.getNoChange(), "Observer getNoChange");
        assertEquals(2, o.getCurWordChanged(), "Observer getCurWordChanged");
        assertEquals(0, o.getCurWordDeleted(), "Observer getCurWordDeleted");
        assertEquals(0, o.getCurCategoryChanged(), "Observer getCurCategoryChanged");
        assertEquals(0, o.getCategoryListChanged(), "Observer getCategoryListChanged");
        assertEquals(3, o.getWordAdded(), "Observer getWordAdded");
        assertEquals(0, o.getUnknown(), "Observer getUnknown");
        
        assertEquals(3, d.size(), "Fil dictionary size");
        assertEquals(3, d.sizeOfAll(), "All dictionary size");
        assertEquals(3, d.getCategoryList().size(), "Category list size");
        assertEquals(IDictionary.ALL_CATEGORY, d.getCurrentCategory(), "Curr cat");
        
        /* Filter category */
        o.reset();
        d.setCategory("TestCat1");
        
        assertEquals(0, o.getNoChange(), "Observer getNoChange");
        assertEquals(1, o.getCurWordChanged(), "Observer getCurWordChanged");
        assertEquals(0, o.getCurWordDeleted(), "Observer getCurWordDeleted");
        assertEquals(1, o.getCurCategoryChanged(), "Observer getCurCategoryChanged");
        assertEquals(0, o.getCategoryListChanged(), "Observer getCategoryListChanged");
        assertEquals(0, o.getWordAdded(), "Observer getWordAdded");
        assertEquals(0, o.getUnknown(), "Observer getUnknown");
        
        assertEquals(2, d.size(), "Fil dictionary size");
        assertEquals(3, d.sizeOfAll(), "All dictionary size");
        assertEquals(3, d.getCategoryList().size(), "Category list size");
        assertEquals("TestCat1", d.getCurrentCategory(), "Curr cat");
        
        /* Delete word */
        o.reset();
        d.deleteCurrentWord();
        
        assertEquals(0, o.getNoChange(), "Observer getNoChange");
        assertEquals(0, o.getCurWordChanged(), "Observer getCurWordChanged");
        assertEquals(1, o.getCurWordDeleted(), "Observer getCurWordDeleted");
        assertEquals(0, o.getCurCategoryChanged(), "Observer getCurCategoryChanged");
        assertEquals(0, o.getCategoryListChanged(), "Observer getCategoryListChanged");
        assertEquals(0, o.getWordAdded(), "Observer getWordAdded");
        assertEquals(0, o.getUnknown(), "Observer getUnknown");
        
        assertEquals(1, d.size(), "Fil dictionary size");
        assertEquals(2, d.sizeOfAll(), "All dictionary size");
        assertEquals(3, d.getCategoryList().size(), "Category list size");
        assertEquals("TestCat1", d.getCurrentCategory(), "Curr cat");
        
        /* Unfilter category */
        o.reset();
        d.setCategory(IDictionary.ALL_CATEGORY);
        
        assertEquals(0, o.getNoChange(), "Observer getNoChange");
        assertEquals(1, o.getCurWordChanged(), "Observer getCurWordChanged");
        assertEquals(0, o.getCurWordDeleted(), "Observer getCurWordDeleted");
        assertEquals(1, o.getCurCategoryChanged(), "Observer getCurCategoryChanged");
        assertEquals(0, o.getCategoryListChanged(), "Observer getCategoryListChanged");
        assertEquals(0, o.getWordAdded(), "Observer getWordAdded");
        assertEquals(0, o.getUnknown(), "Observer getUnknown");
        
        assertEquals(2, d.size(), "Fil dictionary size");
        assertEquals(2, d.sizeOfAll(), "All dictionary size");
        assertEquals(3, d.getCategoryList().size(), "Category list size");
        assertEquals(IDictionary.ALL_CATEGORY, d.getCurrentCategory(), "Curr cat");
        
        /* Delete last word in category */
        /* updateCategory list will remove all unused categories */
        d.setCategory("TestCat1");
        o.reset();
        d.deleteCurrentWord();
        
        assertEquals(0, o.getNoChange(), "Observer getNoChange");
        assertEquals(0, o.getCurWordChanged(), "Observer getCurWordChanged");
        assertEquals(1, o.getCurWordDeleted(), "Observer getCurWordDeleted");
        assertEquals(0, o.getCurCategoryChanged(), "Observer getCurCategoryChanged");
        assertEquals(0, o.getCategoryListChanged(), "Observer getCategoryListChanged");
        assertEquals(0, o.getWordAdded(), "Observer getWordAdded");
        assertEquals(0, o.getUnknown(), "Observer getUnknown");
        
        assertEquals(0, d.size(), "Fil dictionary size");
        assertEquals(1, d.sizeOfAll(), "All dictionary size");
        assertEquals(3, d.getCategoryList().size(), "Category list size");
        assertEquals("TestCat1", d.getCurrentCategory(), "Curr cat");
        
    }
    
    @Test
    @DisplayName("Categories (add, rename)")
    void testCategories() throws DictionaryException {
        IDictionary d = new DictionaryImpl();
        ObserverTestHelper o = new ObserverTestHelper(d);
        d.attach(o);
        
        assertEquals(IDictionary.ALL_CATEGORY, d.getCurrentCategory(), "Curr cat");
        
        /* Fill category list */
        o.reset();
        d.addCategory("TestCat1");
        d.addCategory("TestCat2");
        d.addCategory("TestCat3");
        
        assertEquals(0, o.getNoChange(), "Observer getNoChange");
        assertEquals(0, o.getCurWordChanged(), "Observer getCurWordChanged");
        assertEquals(0, o.getCurWordDeleted(), "Observer getCurWordDeleted");
        assertEquals(0, o.getCurCategoryChanged(), "Observer getCurCategoryChanged");
        assertEquals(3, o.getCategoryListChanged(), "Observer getCategoryListChanged");
        assertEquals(0, o.getWordAdded(), "Observer getWordAdded");
        assertEquals(0, o.getUnknown(), "Observer getUnknown");
        
        assertEquals(3, d.getCategoryList().size(), "Category list size");
        
        /* Fill some words */
        o.reset();
        d.addWord(new WordDto("test1cz", "test1en", "TestCat1"));
        d.addWord(new WordDto("test2cz", "test2en", "TestCat1"));
        d.addWord(new WordDto("test3cz", "test3en", "TestCat2"));
        
        assertEquals(0, o.getNoChange(), "Observer getNoChange");
        assertEquals(2, o.getCurWordChanged(), "Observer getCurWordChanged");
        assertEquals(0, o.getCurWordDeleted(), "Observer getCurWordDeleted");
        assertEquals(0, o.getCurCategoryChanged(), "Observer getCurCategoryChanged");
        assertEquals(0, o.getCategoryListChanged(), "Observer getCategoryListChanged");
        assertEquals(3, o.getWordAdded(), "Observer getWordAdded");
        assertEquals(0, o.getUnknown(), "Observer getUnknown");
        
        /* Rename category */
        o.reset();
        d.renameCategory("TestCat1", "TestCat4");
        
        assertEquals(0, o.getNoChange(), "Observer getNoChange");
        assertEquals(0, o.getCurWordChanged(), "Observer getCurWordChanged");
        assertEquals(0, o.getCurWordDeleted(), "Observer getCurWordDeleted");
        assertEquals(0, o.getCurCategoryChanged(), "Observer getCurCategoryChanged");
        assertEquals(1, o.getCategoryListChanged(), "Observer getCategoryListChanged");
        assertEquals(0, o.getWordAdded(), "Observer getWordAdded");
        assertEquals(0, o.getUnknown(), "Observer getUnknown");
        
        assertEquals(2, d.getCategoryList().size(), "Category list size");
        
        d.setCategory("TestCat4");
        assertEquals(2, d.size(), "Fil dictionary size");
        assertEquals(3, d.sizeOfAll(), "All dictionary size");
    }
    
    @Test
    @DisplayName("Search case sensitive")
    void testSearchCSensitive() throws DictionaryException {    
        IDictionary d = new DictionaryImpl();
        
        d.addCategory("TestCat1");
        d.addWord(new WordDto("maminka", "mum", "TestCat1"));
        d.addWord(new WordDto("Maminka", "Mum", "TestCat1"));
        d.addWord(new WordDto("tatínek", "dad", "TestCat1"));
        d.addWord(new WordDto("Tatínek", "Dad", "TestCat1"));
        
        boolean result = d.searchInCurrentCategory("dědeček", true, false);
        assertEquals(false, result, "Not found");
        
        /* CZ */
        result = d.searchInCurrentCategory("tatínek", true, false);
        assertEquals(true, result, "Found tatínek");
        assertEquals("tatínek", d.getWord().getCz(), "Found tatínek");
        
        result = d.searchInCurrentCategory("tatínek", true, false);
        assertEquals(false, result, "Not found tatínek");
        
        /* EN */
        result = d.searchInCurrentCategory("mum", true, false);
        assertEquals(true, result, "Found mum");
        assertEquals("mum", d.getWord().getEn(), "Found mum");
        
        result = d.searchInCurrentCategory("mum", true, false);
        assertEquals(false, result, "Not found mum");
    }
    
    @Test
    @DisplayName("Search case insensitive")
    void testSearchCInSensitive() throws DictionaryException {    
        IDictionary d = new DictionaryImpl();
        
        d.addCategory("TestCat1");
        d.addWord(new WordDto("maminka", "mum", "TestCat1"));
        d.addWord(new WordDto("Maminka", "Mum", "TestCat1"));
        d.addWord(new WordDto("tatínek", "dad", "TestCat1"));
        d.addWord(new WordDto("Tatínek", "Dad", "TestCat1"));
        
        boolean result = d.searchInCurrentCategory("dědeček", true, false);
        assertEquals(false, result, "Not found");
        
        /* CZ */
        result = d.searchInCurrentCategory("tatínek", false, false);
        assertEquals(true, result, "Found tatínek");
        assertEquals("tatínek", d.getWord().getCz(), "Found tatínek");
        
        result = d.searchInCurrentCategory("tatínek", false, false);
        assertEquals(true, result, "Found Tatínek");
        assertEquals("Tatínek", d.getWord().getCz(), "Found Tatínek");
        
        result = d.searchInCurrentCategory("tatínek", false, false);
        assertEquals(true, result, "Found tatínek");
        assertEquals("tatínek", d.getWord().getCz(), "Found tatínek");
        
        /* EN */
        result = d.searchInCurrentCategory("mum", false, false);
        assertEquals(true, result, "Found mum");
        assertEquals("mum", d.getWord().getEn(), "Found mum");
        
        result = d.searchInCurrentCategory("mum", false, false);
        assertEquals(true, result, "Found Mum");
        assertEquals("Mum", d.getWord().getEn(), "Found Mum");
        
        result = d.searchInCurrentCategory("mum", false, false);
        assertEquals(true, result, "Found mum");
        assertEquals("mum", d.getWord().getEn(), "Found mum");
    }
    
    @Test
    @DisplayName("Search exact match")
    void testSearchExactMatch() throws DictionaryException {    
        IDictionary d = new DictionaryImpl();
        
        d.addCategory("TestCat1");
        d.addWord(new WordDto("maminka", "mum", "TestCat1"));
        d.addWord(new WordDto("Maminka", "Mum", "TestCat1"));
        d.addWord(new WordDto("Maminka je červená.", "Mum is red.", "TestCat1"));
        d.addWord(new WordDto("tatínek", "dad", "TestCat1"));
        d.addWord(new WordDto("Tatínek", "Dad", "TestCat1"));
        d.addWord(new WordDto("Tatínek je bílý.", "Dad is white.", "TestCat1"));
        
        /* CZ */
        boolean result = d.searchInCurrentCategory("tatínek", false, true);
        assertEquals(true, result, "Found tatínek");
        assertEquals("tatínek", d.getWord().getCz(), "Found tatínek");
        
        result = d.searchInCurrentCategory("tatínek", false, true);
        assertEquals(true, result, "Found Tatínek");
        assertEquals("Tatínek", d.getWord().getCz(), "Found Tatínek");
        
        result = d.searchInCurrentCategory("tatínek", false, true);
        assertEquals(true, result, "Found tatínek");
        assertEquals("tatínek", d.getWord().getCz(), "Found tatínek");
        
        /* EN */
        result = d.searchInCurrentCategory("mum", false, true);
        assertEquals(true, result, "Found mum");
        assertEquals("mum", d.getWord().getEn(), "Found mum");
        
        result = d.searchInCurrentCategory("mum", false, true);
        assertEquals(true, result, "Found Mum");
        assertEquals("Mum", d.getWord().getEn(), "Found Mum");
        
        result = d.searchInCurrentCategory("mum", false, true);
        assertEquals(true, result, "Found mum");
        assertEquals("mum", d.getWord().getEn(), "Found mum");
    }

    @Test
    @DisplayName("Search not exact match")
    void testSearchNotExactMatch() throws DictionaryException {    
        IDictionary d = new DictionaryImpl();
        
        d.addCategory("TestCat1");
        d.addWord(new WordDto("maminka", "mum", "TestCat1"));
        d.addWord(new WordDto("Maminka", "Mum", "TestCat1"));
        d.addWord(new WordDto("Maminka je červená.", "Mum is red.", "TestCat1"));
        d.addWord(new WordDto("tatínek", "dad", "TestCat1"));
        d.addWord(new WordDto("Tatínek", "Dad", "TestCat1"));
        d.addWord(new WordDto("Tatínek je bílý.", "Dad is white.", "TestCat1"));
        
        /* CZ */
        boolean result = d.searchInCurrentCategory("tatínek", false, false);
        assertEquals(true, result, "Found tatínek");
        assertEquals("tatínek", d.getWord().getCz(), "Found tatínek");
        
        result = d.searchInCurrentCategory("tatínek", false, false);
        assertEquals(true, result, "Found Tatínek");
        assertEquals("Tatínek", d.getWord().getCz(), "Found Tatínek");
        
        result = d.searchInCurrentCategory("tatínek", false, false);
        assertEquals(true, result, "Found Tatínek");
        assertEquals("Tatínek je bílý.", d.getWord().getCz(), "Found Tatínek");
        
        result = d.searchInCurrentCategory("tatínek", false, false);
        assertEquals(true, result, "Found tatínek");
        assertEquals("tatínek", d.getWord().getCz(), "Found tatínek");
        
        /* EN */
        result = d.searchInCurrentCategory("mum", false, false);
        assertEquals(true, result, "Found mum");
        assertEquals("mum", d.getWord().getEn(), "Found mum");
        
        result = d.searchInCurrentCategory("mum", false, false);
        assertEquals(true, result, "Found Mum");
        assertEquals("Mum", d.getWord().getEn(), "Found Mum");
        
        result = d.searchInCurrentCategory("mum", false, false);
        assertEquals(true, result, "Found Mum");
        assertEquals("Mum is red.", d.getWord().getEn(), "Found Mum");
        
        result = d.searchInCurrentCategory("mum", false, false);
        assertEquals(true, result, "Found mum");
        assertEquals("mum", d.getWord().getEn(), "Found mum");
    }

}
