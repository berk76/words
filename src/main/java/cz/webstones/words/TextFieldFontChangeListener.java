/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
