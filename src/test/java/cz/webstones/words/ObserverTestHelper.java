/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import static cz.webstones.words.DictionaryStateEnum.*;

/**
 *
 * @author jaroslav_b
 */
public class ObserverTestHelper implements IObserver {
    
    private Dictionary dict;
    private int noChange;
    private int curWordChanged;
    private int curWordDeleted;
    private int curCategoryChanged;
    private int categoryListChanged;
    private int wordAdded;
    private int unknown;
    

    public ObserverTestHelper(Dictionary d) {
        super();
        dict = d;
        reset();
    }
    
    @Override
    public void updateObserver() {
        switch (dict.getSubjectState()) {
            case stateNoChabge:
                noChange++;
                break;
            case stateCurWordChanged:
                curWordChanged++;
                break;
            case stateCurWordDeleted:
                curWordDeleted++;
                break;
            case stateCurCategoryChanged:
                curCategoryChanged++;
                break;
            case stateCategoryListChanged:
                categoryListChanged++;
                break;
            case stateWordAdded:
                wordAdded++;
                break;
            default:
                unknown++;
        }
    }
    
    public void reset() {
        noChange = 0;
        curWordChanged = 0;
        curWordDeleted = 0;
        curCategoryChanged = 0;
        categoryListChanged = 0;
        wordAdded = 0;
        unknown = 0;
    }

    /**
     * @return the noChange
     */
    public int getNoChange() {
        return noChange;
    }

    /**
     * @return the curWordChanged
     */
    public int getCurWordChanged() {
        return curWordChanged;
    }

    /**
     * @return the curWordDeleted
     */
    public int getCurWordDeleted() {
        return curWordDeleted;
    }

    /**
     * @return the curCategoryChanged
     */
    public int getCurCategoryChanged() {
        return curCategoryChanged;
    }

    /**
     * @return the categoryListChanged
     */
    public int getCategoryListChanged() {
        return categoryListChanged;
    }

    /**
     * @return the wordAdded
     */
    public int getWordAdded() {
        return wordAdded;
    }

    /**
     * @return the unknown
     */
    public int getUnknown() {
        return unknown;
    }
    
}
