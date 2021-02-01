/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import static cz.webstones.words.Service.findFont;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;

/**
 *
 * @author jarberan
 */
public class LabelFontChangeListener implements PropertyChangeListener {
    private JLabel c;
    private Font originalFont;
    
    public LabelFontChangeListener(JLabel c) {
        super();
        this.c = c;
        this.originalFont = c.getFont();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("text".equals(evt.getPropertyName())) {
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
}
