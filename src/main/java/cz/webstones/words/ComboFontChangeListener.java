/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import static cz.webstones.words.Service.findFont;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;

/**
 *
 * @author jarberan
 */
public class ComboFontChangeListener implements ItemListener {
    private JComboBox c;
    private Font originalFont;
    
    public ComboFontChangeListener(JComboBox c) {
        super();
        this.c = c;
        this.originalFont = c.getFont();
    }
    
    @Override
    public void itemStateChanged(ItemEvent e) {
        String text = e.getItem().toString();
        
        if (c.getFont().canDisplayUpTo(text) == -1) {
            return;
        }
        Font f = findFont(text, c.getFont());
        if (c.getFont() != f) {
            c.setFont(f);
        }
    }
}
