/*
*       AboutDialog.java
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

import cz.webstones.words.dictionary.DictionaryStateEnum;
import cz.webstones.words.dictionary.IObserver;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import cz.webstones.words.dictionary.IDictionary;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle.ComponentPlacement;

/**
 *
 * @author jaroslav_b
 */
public class AboutDialog extends JEscapeableDialog implements IObserver {
    
    private static final long serialVersionUID = -2183131500093783064L;
    private static final Logger LOGGER = Logger.getLogger(AboutDialog.class.getName());
    
    private javax.swing.JButton btnClose;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JLabel lblWrittenBy;
    private javax.swing.JLabel lblWeb;
    private javax.swing.JLabel lblWebLink;
    private javax.swing.JLabel lblJvmVendor;
    private javax.swing.JLabel lblJvmVersion;
    private javax.swing.JLabel lblDictionary;
    private javax.swing.JLabel lblLang;
    private javax.swing.JScrollPane spLicense;
    private javax.swing.JTextArea taLicense;
    
    private static final String URL_GITHUB = "https://berk76.github.io/words/";
    private IDictionary dict;
    private ErrorDialog errorDialog;

    /**
     * Creates new form AboutDialog
     */
    public AboutDialog(java.awt.Frame parent, boolean modal, IDictionary dict) {
        super(parent, modal);
        initComponents();
        
        this.dict = dict;
        dict.attach(this);
        errorDialog = new ErrorDialog(parent, true);
    }
    
    @Override
    public void updateObserver() {
        if (dict.getSubjectState() == DictionaryStateEnum.DICTIONARY_LOADED) {
            try {
                setPronunciation(dict.getLanguage());
            } catch (IOException ex) {
                errorDialog.showError("Error: Cannot download pronunciation.", ex);
            }
            setDictionaryName(dict.getDictionaryName());
        }
    }

    private void setPronunciation(LanguageDto lang) {
        if (lang != null) {
            String p = "Pronunciation: %s [%s]";
            lblLang.setText(String.format(p, lang.getName(), lang.getCode()));
        }
    }
    
    private void setDictionaryName(String dictName) {
        if (dictName != null) {
            lblDictionary.setText(String.format("Dictionary: %s", dictName));
        }
    }
    
    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {
        this.setVisible(false);
    }

    private void initComponents() {
        lblVersion = new javax.swing.JLabel();
        spLicense = new javax.swing.JScrollPane();
        taLicense = new javax.swing.JTextArea();
        lblWrittenBy = new javax.swing.JLabel();
        lblWeb = new javax.swing.JLabel();
        btnClose = new javax.swing.JButton();
        lblWebLink = new javax.swing.JLabel();
        lblJvmVendor = new javax.swing.JLabel();
        lblJvmVersion = new javax.swing.JLabel();
        lblDictionary = new javax.swing.JLabel();
        lblLang = new javax.swing.JLabel();

        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About");

        lblVersion.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblVersion.setText(Service.VERSION);

        taLicense.setEditable(false);
        taLicense.setColumns(20);
        taLicense.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        taLicense.setLineWrap(true);
        taLicense.setRows(5);
        taLicense.setText("Words is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version. <http://www.gnu.org/licenses/>");
        taLicense.setWrapStyleWord(true);
        spLicense.setViewportView(taLicense);

        lblWrittenBy.setText("Written by Jaroslav Beran");

        lblWeb.setText("Web:");
        lblWebLink.setText(URL_GITHUB);
        lblWebLink.setForeground(Color.BLUE.darker());
        lblWebLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblWebLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(URL_GITHUB));
                } catch (IOException | URISyntaxException e1) {
                    LOGGER.log(Level.SEVERE, null, e1);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // the mouse has entered the label
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // the mouse has exited the label
            }
        });

        btnClose.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        lblJvmVendor.setText("JVM Vendor: " + System.getProperty("java.vm.vendor"));
        lblJvmVersion.setText("JVM Version: " + System.getProperty("java.version"));
        lblDictionary.setText("Dictionary:");
        lblLang.setText("Language:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(lblVersion, GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblWeb)
                            .addGap(18)
                            .addComponent(lblWebLink, GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblWrittenBy)
                            .addGap(39, 175, Short.MAX_VALUE))
                        .addComponent(lblJvmVendor)
                        .addComponent(lblJvmVersion)
                        .addComponent(lblDictionary)
                        .addComponent(lblLang)
                        .addComponent(spLicense, GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                        .addComponent(btnClose, Alignment.TRAILING))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblVersion, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(spLicense, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                    .addGap(18)
                    .addComponent(lblWrittenBy)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblWeb)
                        .addComponent(lblWebLink))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(lblJvmVendor)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(lblJvmVersion)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(lblDictionary)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(lblLang)
                    .addPreferredGap(ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                    .addComponent(btnClose)
                    .addContainerGap())
        );
        getContentPane().setLayout(layout);

        pack();
    }
}
