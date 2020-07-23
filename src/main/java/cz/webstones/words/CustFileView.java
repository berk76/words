/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
