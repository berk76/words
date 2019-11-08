/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words.dictionary.impl;

import cz.webstones.words.LanguageDto;
import cz.webstones.words.Service;
import cz.webstones.words.Setup;
import static cz.webstones.words.dictionary.IDictionary.allCategoryName;
import static cz.webstones.words.dictionary.IDictionary.encoding;
import cz.webstones.words.dictionary.DictionaryException;
import cz.webstones.words.dictionary.DictionaryStateEnum;
import cz.webstones.words.dictionary.IObserver;
import cz.webstones.words.dictionary.WordDto;
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
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import cz.webstones.words.dictionary.IDictionary;

/**
 *
 * @author jarberan
 */
public class DictionaryImpl implements IDictionary {
    private Setup setup = null;
    private ArrayList<WordDto> dictAll = new ArrayList<>();
    private ArrayList<WordDto> dictFil = new ArrayList<>();
    private ArrayList<String> categoryList = new ArrayList<>();
    private final ArrayList<IObserver> observers = new ArrayList<>();
    private DictionaryStateEnum subjectState = DictionaryStateEnum.stateNoChabge;
    private int current = 0;
    private String currentCategory;
    
    public DictionaryImpl() throws DictionaryException {
        super();
        
        currentCategory = IDictionary.allCategoryName;
    }
    
    
    /* Observer subject interface */
    
    @Override
    public void attach(IObserver o) {
        observers.add(o);
    }
    
    @Override
    public void detach(IObserver o) {
        observers.remove(o);
    }
    
    private void notifyAllObservers() {
        for (IObserver o: observers) {
            o.updateObserver();
        }
    }
    
    @Override
    public DictionaryStateEnum getSubjectState() {
        return subjectState;
    }
    
    /* Load / Save Dictionary */
    
    @Override
    public void loadDictionary(Setup s)
            throws FileNotFoundException, UnsupportedEncodingException, IOException {

        setup = s;
        
        dictAll = new ArrayList<WordDto>();
        SimpleDateFormat sdf = new SimpleDateFormat(getSetup().getDictionaryDateFormat());

        InputStream is = new FileInputStream(getSetup().getFullDictionaryFilePath());
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, encoding));

        boolean first = true;
        String line;
        while ((line = reader.readLine()) != null) {

            if (first) {
                Service.bomPresent = false;
                if (Service.isUTF8BOMPresent(line)) {
                    line = Service.removeUTF8BOM(line);
                    Service.bomPresent = true;
                }
                first = false;
            }

            String arr[] = line.split(getSetup().getDictionarySeparator());

            if (arr.length < 2) {
                continue;
            }

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

            if (arr.length > 5) {
                w.setWrongHits(Integer.valueOf(arr[5]));
            }

            if (arr.length > 6) {
                try {
                    w.setLastWrongHit(sdf.parse(arr[6]));
                } catch (ParseException ex) {
                    w.setLastWrongHit(null);
                }
            }

            dictAll.add(w);

        }
        reader.close();
        is.close();
        
        setCategory(IDictionary.allCategoryName);
        updateCategoryList();
        current = -1;
        setCurrnet(0);

        subjectState = DictionaryStateEnum.stateDictionaryLoaded;
        notifyAllObservers();
    }

    @Override
    public void saveDictionary()
            throws FileNotFoundException, UnsupportedEncodingException, IOException {

        SimpleDateFormat sdf = new SimpleDateFormat(getSetup().getDictionaryDateFormat());
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;

        try {
            fos = new FileOutputStream(getSetup().getFullDictionaryFilePath());
            osw = new OutputStreamWriter(fos, encoding);
            bw = new BufferedWriter(osw);

            if (Service.bomPresent) {
                bw.write(Service.UTF8_BOM);
            }

            String separator = getSetup().getDictionarySeparator();
            
            for (WordDto w : dictAll) {
                bw.write(Service.cleanAndAddSeparator(w.getEn(), separator));
                bw.write(Service.cleanAndAddSeparator(w.getCz(), separator));
                bw.write(Service.cleanAndAddSeparator(w.getCategory(), separator));
                bw.write(Service.cleanAndAddSeparator(String.valueOf(w.getGoodHits()), separator));
                bw.write(Service.cleanAndAddSeparator(((w.getLastGoodHit() != null) ? sdf.format(w.getLastGoodHit()) : ""), separator));
                bw.write(Service.cleanAndAddSeparator(String.valueOf(w.getWrongHits()), separator));
                bw.write(Service.cleanAndAddSeparator(((w.getLastWrongHit() != null) ? sdf.format(w.getLastWrongHit()) : ""), separator));
                bw.newLine();
            }
        } finally {
            bw.close();
            osw.close();
            fos.close();
        }
    }
    
    @Override
    public String getDictionaryName() {
        String result = setup.getDataDir();
        int trimPos = result.lastIndexOf(File.separator);

        if (trimPos != -1) {
            result = result.substring(trimPos + 1);
        }

        return result;
    }
    
    @Override
    public LanguageDto getLanguage() throws IOException {
        LanguageDto result = null;
        
        String pron = setup.getLanguage();
        if (pron != null) {
            ArrayList<LanguageDto> lang = Service.getLanguageList();
            for (LanguageDto ldto: lang) {
                if (pron.equals(ldto.getCode())) {
                    result = ldto;
                    break;
                }
            }
        }
        
        return result;
    }

    /* Dictionary manipulation */
    
    @Override
    public int size() {
        return dictFil.size();
    }
    
    @Override
    public int sizeOfAll() {
        return dictAll.size();
    }
    
    @Override
    public List<WordDto> getDictionaryAsList() {
        return dictFil;
    }
    
    /* Word manipulation */
    
    @Override
    public void setCurrnet(int i) {

        if (dictFil.size() == 0) {
            current = 0;
            subjectState = DictionaryStateEnum.stateCurWordChanged;
            notifyAllObservers();
        }

        if (dictFil.size() > i) {
            if (current != i) {
                current = i;
                
                subjectState = DictionaryStateEnum.stateCurWordChanged;
                notifyAllObservers();
            }
        }
    }
    
    @Override
    public int getCurrnet() {
        return current;
    }
    
    @Override
    public boolean setCurrent(WordDto w) {
        for (int i = 0; i < dictFil.size(); i++) {
            WordDto cw = dictFil.get(i);
            if (cw.getEn().equals(w.getEn()) && cw.getCz().equals(w.getCz())) {
                setCurrnet(i);
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void validateWord(WordDto w) throws DictionaryException {
        
        if ((w.getEn() == null) || w.getEn().trim().equals("")) {
            throw new DictionaryException("Word cannot be empty.");
        }
        
        if ((w.getCz() == null) || w.getCz().trim().equals("")) {
            throw new DictionaryException("Word cannot be empty.");
        }
        
        if ((w.getCategory() == null) || w.getCategory().trim().equals("")) {
            throw new DictionaryException("Category cannot be empty.");
        }
        
        if (w.getCategory().equals(IDictionary.allCategoryName)) {
            throw new DictionaryException("Category name cannot be " + IDictionary.allCategoryName + " !");
        }
        
        w.setCz(w.getCz().trim());
        w.setEn(w.getEn().trim());
    }
    
    @Override
    public void addWord(WordDto w) throws DictionaryException {
        
        validateWord(w);
        
        WordDto dup = findDuplicity(w);
        if (dup != null) {
            setCategory(dup.getCategory());
            setCurrent(w);
            throw new DictionaryException("Word " + w.getEn() + " already exists.");
        }

        dictAll.add(w);
        
        if (currentCategory.equals(IDictionary.allCategoryName) || currentCategory.equals(w.getCategory())) {
            dictFil.add(w);
            setCurrent(w);
        }
        
        subjectState = DictionaryStateEnum.stateWordAdded;
        notifyAllObservers();
    }
    
    
    @Override
    public void updateWord(WordDto w) throws DictionaryException {
        
        validateWord(w);
        
        if (!currentCategory.equals(IDictionary.allCategoryName) && !currentCategory.equals(w.getCategory())) {
            dictFil.remove(w);
        }
        
        if (!currentCategory.equals(IDictionary.allCategoryName) && currentCategory.equals(w.getCategory()) && !dictFil.contains(w)) {
            dictFil.add(w);
            setCurrent(w);
        }

        subjectState = DictionaryStateEnum.stateCurWordChanged;
        notifyAllObservers();
    }

    
    @Override
    public WordDto findDuplicity(String s) {
        for (WordDto t : dictAll) {
            if (t.getCz().equals(s) || t.getEn().equals(s)) {
                return t;
            }
        }
        return null;
    }

    @Override
    public WordDto findDuplicity(WordDto w) {
        for (WordDto t : dictAll) {
            if (t.getCz().equals(w.getCz()) && t.getEn().equals(w.getEn())) {
                return t;
            }
        }
        return null;
    }

    @Override
    public void deleteCurrentWord() {
        WordDto w = getWord();
        
        File f = new File(w.getMp3FilenameEn(setup.getFullMp3Path()));
        f.delete();
        dictFil.remove(w);
        dictAll.remove(w);
        
        subjectState = DictionaryStateEnum.stateCurWordDeleted;
        notifyAllObservers();
    }
    
    @Override
    public WordDto getWord() {
        return dictFil.get(current);
    }
    
    @Override
    public WordDto getWord(int i) {
        return dictFil.get(i);
    }
    
    @Override
    public boolean searchInCurrentCategory(String what, boolean caseSensitive, boolean exactMatch) {
        
        if (what.equals("")) {
            return false;
        }
                
        int c = getCurrnet();
        
        for (int i = 0; i < size(); i++) {
            c++;
                    
            if (c >= size()) {
                c = 0;
            }
            
            if (c == getCurrnet()) {
                break;
            }

            WordDto w = getWord(c);
            String s1 = w.getCz();
            String s2 = w.getEn();
            
            if (!caseSensitive) {
                what = what.toLowerCase();
                s1 = s1.toLowerCase();
                s2 = s2.toLowerCase();
            }
            
            boolean found = false;

            if (exactMatch) {
                if (s1.equals(what) || s2.equals(what)) {
                    found = true;
                }
            } else {
                if (s1.contains(what) || s2.contains(what)) {
                    found = true;
                }
            }
            
            if (found) {
                setCurrnet(c);
                    return true;
            }
        }
        
        return false;
    }
    
    
    /* Category manipulation */
    
    @Override
    public void setCategory(String category) {
        dictFil = new ArrayList<WordDto>();
        Random rand = new Random();

        for (WordDto w : dictAll) {
            if (category.equals(IDictionary.allCategoryName) || category.equals(w.getCategory())) {
                dictFil.add(w);
            }
        }

        for (WordDto w : dictFil) {
            int p = 0; // lower number means higher priority

            p += (w.getGoodHits() - w.getWrongHits()) * 10000;
            /*
             p += (365 * 24 * 60) - w.getLastWrongHitInMinutes() * 100;
             p += (365 * 24 * 60) - w.getLastGoodHitInMinutes() * 10;
             */
            p += rand.nextInt(10);
            w.setOrder(p);
        }

        Collections.sort(dictFil, new Comparator<WordDto>() {
            @Override
            public int compare(WordDto a, WordDto b) {
                return a.getOrder() < b.getOrder() ? -1 : (a.getOrder() > b.getOrder()) ? 1 : 0;
            }
        });
        
        String oldCategory = currentCategory;
        currentCategory = category;
        current = -1;
        setCurrnet(0);
        
        if (!oldCategory.equals(currentCategory)) {
            subjectState = DictionaryStateEnum.stateCurCategoryChanged;
            notifyAllObservers();
        }
    }
    
    private void updateCategoryList() {
        categoryList = new ArrayList<String>();
        
        for (WordDto w : dictAll) {
            boolean alreadyExists = false;

            for (String s : categoryList) {
                if (s.equals(w.getCategory())) {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {
                categoryList.add(w.getCategory());
            }
        }
        
        sortCategoryList();
        
        subjectState = DictionaryStateEnum.stateCategoryListChanged;
        notifyAllObservers();
    }
    
    @Override
    public ArrayList<String> getCategoryList(){
        return categoryList;
    }
    
    @Override
    public void renameCategory(String oldCat, String newCat) {
        if ((newCat == null) || newCat.trim().equals("")) {
            return;
        }
        
        for (WordDto w: dictAll) {
            if (w.getCategory().equals(oldCat)) {
                w.setCategory(newCat);
            }
        }
        updateCategoryList();
    }       

    @Override
    public String getCurrentCategory() {
        return currentCategory;
    }
    
    @Override
    public void addCategory(String category) throws DictionaryException {
        
        if ((category == null) || category.trim().equals("")) {
            return;
        }
        
        if (category.equals(IDictionary.allCategoryName)) {
            throw new DictionaryException("Category name cannot be " + IDictionary.allCategoryName + " !");
        }
        
        for (String c: categoryList) {
            if (c.equals(category)) {
                throw new DictionaryException("Category " + category + " already exists.");
            }
        }
        
        categoryList.add(category);
        sortCategoryList();
        
        subjectState = DictionaryStateEnum.stateCategoryListChanged;
        notifyAllObservers();
    }
    
    @Override
    public void deleteCurrentCategory() throws DictionaryException {
        if (currentCategory.equals(allCategoryName)) {
            throw new DictionaryException("Cannot delete category: " + allCategoryName);
        }
        
        if (dictFil.size() != 0) {
            throw new DictionaryException("Category " + currentCategory + " is not empty.");
        }
        
        categoryList.remove(currentCategory);
        
        subjectState = DictionaryStateEnum.stateCategoryListChanged;
        notifyAllObservers();
        
        setCategory(allCategoryName);
    }
    
    private void sortCategoryList() {
        Collections.sort(categoryList, Collator.getInstance(new Locale("cs", "CS"))); 
    }

    /**
     * @return the setup
     */
    @Override
    public Setup getSetup() {
        return setup;
    }
}
