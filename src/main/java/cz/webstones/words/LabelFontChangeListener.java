/*
*       LabelFontChangeListener.java
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
