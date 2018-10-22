/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

/**
 *
 * @author jaroslav_b
 */
public class Dictionary {
    
    public static final int stateNoChabge = 0;
    public static final int stateCurWordChanged = 1;
    public static final int stateCurCategoryChanged = 2;
    public static final int stateCategoryListChanged = 3;
    public static final int stateWordAdded = 4;
    
    public static final String allCategoryName = "All";
    
    private ArrayList<WordDto> dictAll = new ArrayList<WordDto>();
    private ArrayList<WordDto> dictFil = new ArrayList<WordDto>();
    private ArrayList<String> categoryList = new ArrayList<String>();
    private ArrayList<IObserver> observers = new ArrayList<IObserver>();
    private int subjectState = Dictionary.stateNoChabge;
    private int current = 0;
    private String currentCategory = Dictionary.allCategoryName;
    
    
    /* Observer subject */
    
    public void attach(IObserver o) {
        observers.add(o);
    }
    
    public void detach(IObserver o) {
        observers.remove(o);
    }
    
    private void notifyAllObservers() {
        for (IObserver o: observers) {
            o.updateObserver();
        }
    }
    
    public int getSubjectState() {
        return subjectState;
    }
    
    /* Load / Save Dictionary */
    
    public void loadDictionary(String file,
            String separator, String dateFormat)
            throws FileNotFoundException, UnsupportedEncodingException, IOException {

        dictAll = new ArrayList<WordDto>();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

        InputStream is = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

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

            String arr[] = line.split(separator);

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
        
        setCategory(Dictionary.allCategoryName);
        updateCategoryList();
        current = -1;
        setDictCurrnet(0);
    }

    public void saveDictionary(String file,
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

            if (Service.bomPresent) {
                bw.write(Service.UTF8_BOM);
            }

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
    
    /* Dictionary manipulation */
    
    public int size() {
        return dictFil.size();
    }
    
    public List<WordDto> getDictionaryAsList() {
        return dictFil;
    }
    
    public void setCategory(String category) {
        dictFil = new ArrayList<WordDto>();
        Random rand = new Random();

        for (WordDto w : dictAll) {
            if (category.equals(Dictionary.allCategoryName) || category.equals(w.getCategory())) {
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
        setDictCurrnet(0);
        
        if (!oldCategory.equals(currentCategory)) {
            subjectState = Dictionary.stateCurCategoryChanged;
            notifyAllObservers();
        }
    }
    
    /* Word manipulation */
    
    public void setDictCurrnet(int i) {
        if (dictFil.size() > i) {
            if (current != i) {
                current = i;
                
                subjectState = Dictionary.stateCurWordChanged;
                notifyAllObservers();
            }
        }
    }
    
    public int getDictCurrnet() {
        return current;
    }
    
    public boolean setWordCurrent(WordDto w) {
        for (int i = 0; i < dictFil.size(); i++) {
            WordDto cw = dictFil.get(i);
            if (cw.getEn().equals(w.getEn()) && cw.getCz().equals(w.getCz())) {
                setDictCurrnet(i);
                return true;
            }
        }
        
        return false;
    }
    
    public void addWord(WordDto w) throws DictionaryException {
        
        if ((w.getEn() == null) || w.getEn().trim().equals("")) {
            throw new DictionaryException("Word cannot be empty.");
        }
        
        if ((w.getCz() == null) || w.getCz().trim().equals("")) {
            throw new DictionaryException("Word cannot be empty.");
        }
        
        if ((w.getCategory() == null) || w.getCategory().trim().equals("")) {
            throw new DictionaryException("Category cannot be empty.");
        }
        
        w.setCz(w.getCz().trim());
        w.setEn(w.getEn().trim());
        
        WordDto dup = findDuplicity(w);
        if (dup != null) {
            setCategory(dup.getCategory());
            setWordCurrent(w);
            throw new DictionaryException("Word " + w.getEn() + " already exists.");
        }

        dictAll.add(w);
        
        subjectState = Dictionary.stateWordAdded;
        notifyAllObservers();
    }
    
    public boolean isDuplicityEn (WordDto w) {
        for (WordDto t : dictAll) {
            if (t.getEn().equals(w.getEn())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isDuplicityCz (WordDto w) {
        for (WordDto t : dictAll) {
            if (t.getCz().equals(w.getCz())) {
                return true;
            }
        }
        return false;
    }
    
    public WordDto findDuplicity(WordDto w) {
        for (WordDto t : dictAll) {
            if (t.getCz().equals(w.getCz()) && t.getEn().equals(w.getEn())) {
                return t;
            }
        }
        return null;
    }
    
    public WordDto getWord() {
        return dictFil.get(current);
    }
    
    public WordDto getWord(int i) {
        return dictFil.get(i);
    }
    
    
    /* Category manipulation */
    
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
        
        subjectState = Dictionary.stateCategoryListChanged;
        notifyAllObservers();
    }
    
    public ArrayList<String> getCategoryList(){
        return categoryList;
    }
    
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

    public String getCurrentCategory() {
        return currentCategory;
    }
    
    public void addCategory(String category) throws DictionaryException {
        
        if ((category == null) || category.trim().equals("")) {
            return;
        }
        
        for (String c: categoryList) {
            if (c.equals(category)) {
                throw new DictionaryException("Category " + category + " already exists.");
            }
        }
        
        categoryList.add(category);
        sortCategoryList();
        
        subjectState = Dictionary.stateCategoryListChanged;
        notifyAllObservers();
    }
    
    private void sortCategoryList() {
        Collections.sort(categoryList, Collator.getInstance(new Locale("cs", "CS"))); 
    }

}
