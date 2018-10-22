/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

/**
 *
 * @author jaroslav_b
 */
public class DictionaryException extends Exception {

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
