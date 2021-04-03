/*
*       TextFieldFontChangeListener.java
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
package cz.webstones.words;

import static cz.webstones.words.Service.findFont;
import java.awt.Font;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 *
 * @author jarberan
 */
public class TextFieldFontChangeListener implements DocumentListener {
    private JTextComponent c;
    private Font originalFont;
    
    public TextFieldFontChangeListener(JTextComponent c) {
        super();
        this.c = c;
        this.originalFont = c.getFont();
    }
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        setFont(c);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        setFont(c);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        setFont(c);
    }
    
    private void setFont(JTextComponent c) {
        if ((originalFont.canDisplayUpTo(c.getText()) == -1) && (c.getFont() != originalFont)) {
            c.setFont(originalFont);
            return;
        }
        if (c.getFont().canDisplayUpTo(c.getText()) == -1) {
            return;
        }
        Font f = findFont(c.getText(), c.getFont());
        if (c.getFont() != f) {
            c.setFont(f);
        }
    }
}
