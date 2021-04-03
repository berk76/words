/*
*       IDictionary.java
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

import cz.webstones.words.LanguageDto;
import cz.webstones.words.Setup;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author jaroslav_b
 */
public interface IDictionary extends Serializable {
    
    public static final String ALL_CATEGORY = "All";
    
    /* Observer subject interface */
    
    public void attach(IObserver o);
    public void detach(IObserver o);
    public DictionaryStateEnum getSubjectState();
    
    /* Load / Save Dictionary */
    
    public void loadDictionary(Setup s) throws FileNotFoundException, UnsupportedEncodingException, IOException;
    public void saveDictionary() throws FileNotFoundException, UnsupportedEncodingException, IOException;
    
    public String getDictionaryName();
    public LanguageDto getLanguage() throws IOException;

    /* Dictionary manipulation */
    
    public int size();
    public int sizeOfAll();
    public List<WordDto> getDictionaryAsList();
    
    /* Word manipulation */
    
    public void setCurrnet(int i);
    public int getCurrnet();
    public boolean setCurrent(WordDto w);
    
    public void validateWord(WordDto w) throws DictionaryException;
    public void addWord(WordDto w) throws DictionaryException;
    public void updateWord(WordDto w) throws DictionaryException;
    public void deleteCurrentWord();
    
    public boolean searchInCurrentCategory(String what, boolean caseSensitive, boolean exactMatch);
    
    public WordDto findDuplicity(String s);
    public WordDto findDuplicity(WordDto w);
    
    public WordDto getWord();
    public WordDto getWord(int i);
    
    /* Category manipulation */
    
    public void setCategory(String category);
    public ArrayList<String> getCategoryList();
    public void renameCategory(String oldCat, String newCat);
    public String getCurrentCategory();
    public void addCategory(String category) throws DictionaryException;
    public void deleteCurrentCategory() throws DictionaryException;
    
    /* Other */
    
    public Setup getSetup();
}
