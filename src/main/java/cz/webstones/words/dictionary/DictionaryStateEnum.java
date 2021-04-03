/*
*       DictionaryStateEnum.java
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
