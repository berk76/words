/*
*       CustFileView.java
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

import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

/**
 *
 * @author jaroslav_b
 */
public class CustFileView extends FileView {
    
    private ImageIcon bookIcon = new ImageIcon(this.getClass().getClassLoader().getResource("open_book.png"));
    
    @Override
    public Icon getIcon(File f) {
        
        if (f.isDirectory()) {
            File fi = new File(f.getAbsoluteFile() + File.separator + Service.SETUP_FNAME);
            if (fi.exists()) {
                return bookIcon;
            }
        }
        
        return FileSystemView.getFileSystemView().getSystemIcon(f);
        
    }
}
