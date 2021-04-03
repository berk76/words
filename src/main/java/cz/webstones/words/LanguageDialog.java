/*
*       LanguageDialog.java
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

import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 *
 * @author jaroslav_b
 */
public class LanguageDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = -6326504443963785153L;
    
    private JComboBox<String> cbbSelectLang;
    private ArrayList<LanguageDto> langs;
    private boolean selected = false;
    
    /**
     * Creates new form LanguageDialog
     */
    public LanguageDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        
        cbbSelectLang.removeAllItems();
        try {
            langs = Service.getLanguageList();
            for (LanguageDto l : langs) {
                cbbSelectLang.addItem(l.getName());
            }
        } catch (Exception ex) {
            langs = new ArrayList<LanguageDto>();
        }
    }
    
    public String getLangCode() {
        String result = "";
        
        if (selected) {
            String option = (String) cbbSelectLang.getSelectedItem();

            for (LanguageDto l : langs) {
                if (option.equals(l.getName())) {
                    result = l.getCode();
                    break;
                }
            }
        }
        return result;
    }
    
    private void btnSelectActionPerformed() {
        this.selected = true;
        this.setVisible(false);
    }

    private void initComponents() {
        JButton btnSelect;
        JLabel lblSelectLang;

        lblSelectLang = new JLabel();
        cbbSelectLang = new JComboBox<>();
        btnSelect = new JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Language");

        lblSelectLang.setFont(Service.createFont());
        lblSelectLang.setText("Select language for pronunciation");

        cbbSelectLang.setFont(Service.createFont());
        cbbSelectLang.setModel(new javax.swing.DefaultComboBoxModel<>());

        btnSelect.setFont(Service.createFont());
        btnSelect.setText("Select");
        btnSelect.addActionListener(e -> btnSelectActionPerformed());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSelectLang)
                    .addComponent(cbbSelectLang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(54, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSelect)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSelectLang)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbbSelectLang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSelect)
                .addContainerGap())
        );

        pack();
    }
}
