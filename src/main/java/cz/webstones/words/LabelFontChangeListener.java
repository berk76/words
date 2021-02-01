/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

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
    
    public LabelFontChangeListener(JLabel c) {
        super();
        this.c = c;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("text".equals(evt.getPropertyName())) {
            Font f = Service.findFont(c.getText(), c.getFont());
            if (c.getFont() != f) {
                c.setFont(f);
            }
        }
    }
}
