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
    stateNoChabge,
    stateCurWordChanged,
    stateCurWordDeleted,
    stateCurCategoryChanged,
    stateCategoryListChanged,
    stateWordAdded,
    stateDictionaryLoaded
}
