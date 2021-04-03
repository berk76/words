/*
*       Mp3CreatorException.java
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
package cz.webstones.words.mp3;

/**
 *
 * @author jaroslav_b
 */
public class Mp3CreatorException extends Exception {

    private static final long serialVersionUID = 3865708760717019296L;

    /**
     * Creates a new instance of
     * <code>Mp3CreatorException</code> without detail message.
     */
    public Mp3CreatorException() {
    }

    /**
     * Constructs an instance of
     * <code>Mp3CreatorException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public Mp3CreatorException(String msg) {
        super(msg);
    }
}
