/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 *
 * @author jaroslav_b
 */
public class Dictionary {
    
    private ArrayList<WordDto> dict = new ArrayList<WordDto>();
    private ArrayList<IObserver> observers = new ArrayList<IObserver>();
    private int current = 0;
    

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
    
    /* Observer subject end */
    
    public void setDictCurrnet(int i) {
        if (dict.size() > i) {
            if (current != i) {
                current = i;
                notifyAllObservers();
            }
        }
    }
    
    public int getDictCurrnet() {
        return current;
    }
    
    public boolean setWordCurrent(WordDto w) {
        for (int i = 0; i < dict.size(); i++) {
            WordDto cw = dict.get(i);
            if (cw.getEn().equals(w.getEn()) && cw.getCz().equals(w.getCz())) {
                setDictCurrnet(i);
                return true;
            }
        }
        
        return false;
    }
    
    public int size() {
        return dict.size();
    }
    
    public void addWord(WordDto w) {
        dict.add(w);
    }
    
    public boolean isDuplicityEn (WordDto w) {
        for (WordDto t : dict) {
            if (t.getEn().equals(w.getEn())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isDuplicityCz (WordDto w) {
        for (WordDto t : dict) {
            if (t.getCz().equals(w.getCz())) {
                return true;
            }
        }
        return false;
    }
    
    public WordDto findDuplicity(WordDto w) {
        for (WordDto t : dict) {
            if (t.getCz().equals(w.getCz()) && t.getEn().equals(w.getEn())) {
                return t;
            }
        }
        return null;
    }
    
    public WordDto getWord() {
        return dict.get(current);
    }
    
    public WordDto getWord(int i) {
        return dict.get(i);
    }
    
    public List<WordDto> getDictionaryAsList() {
        return dict;
    }
    
    public ArrayList<String> getCategoryList(){
        ArrayList<String> result = new ArrayList<String>();

        for (WordDto w : dict) {
            boolean alreadyExists = false;

            for (String s : result) {
                if (s.equals(w.getCategory())) {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {
                result.add(w.getCategory());
            }
        }

        return result;
    }
    
    public void renameCategory(String oldCat, String newCat) {
        for (WordDto w: dict) {
            if (w.getCategory().equals(oldCat)) {
                w.setCategory(newCat);
            }
        }
    }       
    
    public Dictionary createReorderedDictionary(String category) {
        Dictionary result = new Dictionary();
        Random rand = new Random();

        for (WordDto w : dict) {
            if (category.equals("All") || category.equals(w.getCategory())) {
                result.addWord(w);
            }
        }

        for (WordDto w : result.getDictionaryAsList()) {
            int p = 0; // lower number means higher priority

            p += (w.getGoodHits() - w.getWrongHits()) * 10000;
            /*
             p += (365 * 24 * 60) - w.getLastWrongHitInMinutes() * 100;
             p += (365 * 24 * 60) - w.getLastGoodHitInMinutes() * 10;
             */
            p += rand.nextInt(10);
            w.setOrder(p);
        }

        Collections.sort(result.getDictionaryAsList(), new Comparator<WordDto>() {
            @Override
            public int compare(WordDto a, WordDto b) {
                return a.getOrder() < b.getOrder() ? -1 : (a.getOrder() > b.getOrder()) ? 1 : 0;
            }
        });

        return result;
    }
    
}
