/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words.dictionary;

import static cz.webstones.words.dictionary.DictionaryStateEnum.*;

/**
 *
 * @author jaroslav_b
 */
public class ObserverTestHelper implements IObserver {
    
    private IDictionary dict;
    private int noChange;
    private int curWordChanged;
    private int curWordDeleted;
    private int curCategoryChanged;
    private int categoryListChanged;
    private int wordAdded;
    private int unknown;
    

    public ObserverTestHelper(IDictionary d) {
        super();
        dict = d;
        reset();
    }
    
    @Override
    public void updateObserver() {
        switch (dict.getSubjectState()) {
            case NO_CHANGE:
                noChange++;
                break;
            case CUR_WORD_CHANGED:
                curWordChanged++;
                break;
            case CUR_WORD_DELETED:
                curWordDeleted++;
                break;
            case CUR_CATEGORY_CHANGED:
                curCategoryChanged++;
                break;
            case CATEGORY_LIST_CHANGED:
                categoryListChanged++;
                break;
            case WORD_ADDED:
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
