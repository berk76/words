/*
*       DictionaryException.java
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
public class DictionaryException extends Exception {

    private static final long serialVersionUID = -3669403811937096882L;

    /**
     * Creates a new instance of
     * <code>DictionaryException</code> without detail message.
     */
    public DictionaryException() {
    }

    /**
     * Constructs an instance of
     * <code>DictionaryException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public DictionaryException(String msg) {
        super(msg);
    }
}
