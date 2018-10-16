/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words.mp3;

/**
 *
 * @author jaroslav_b
 */
public class Mp3CreatorException extends Exception {

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
