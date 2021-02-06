/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words.dictionary;

/**
 *
 * @author jaroslav_b
 */
public enum DictionaryStateEnum {
    NO_CHANGE,
    CUR_WORD_CHANGED,
    CUR_WORD_DELETED,
    CUR_CATEGORY_CHANGED,
    CATEGORY_LIST_CHANGED,
    WORD_ADDED,
    DICTIONARY_LOADED
}
