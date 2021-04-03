/*
*       RenameCategoryDialog.java
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
import cz.webstones.words.dictionary.IDictionary;
import cz.webstones.words.dictionary.IObserver;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 *
 * @author jaroslav_b
 */
public class RenameCategoryDialog extends JEscapeableDialog implements IObserver {

    private static final long serialVersionUID = -1602447504641992950L;
    
    private JComboBox<String> cbbCategory;
    private JTextField txfNewCategory;
    
    private boolean commited;
    private IDictionary dict;
    
    /**
     * Creates new form RenameCategoryDialog
     */
    public RenameCategoryDialog(java.awt.Frame parent, boolean modal, IDictionary dic) {
        super(parent, modal);
        initComponents();
        dict = dic;
        dict.attach(this);
    }
    
    public void updateObserver() {
        switch (dict.getSubjectState()) {
            case CATEGORY_LIST_CHANGED:
                updateCategoryCombo();
                break;
            case CUR_CATEGORY_CHANGED:
                if (!dict.getCurrentCategory().equals(cbbCategory.getSelectedItem().toString())) {
                    cbbCategory.setSelectedItem(dict.getCurrentCategory());
                }
                break;
            default:
        }
    }
    
    public void setNewCategoryText(String s) {
        txfNewCategory.setText(s);
    }
    
    public String getNewCategoryText() {
        return txfNewCategory.getText();
    }
    
    public String getOldCategoryText() {
        return cbbCategory.getSelectedItem().toString();
    }
    
    public boolean isCommited() {
        return commited;
    }
    
    private void updateCategoryCombo() {
        cbbCategory.removeAllItems();
        for (String s: dict.getCategoryList()) {
            cbbCategory.addItem(s);
            if (cbbCategory.getFont().canDisplayUpTo(s) != -1) {
                Font f = findFont(s, cbbCategory.getFont());
                cbbCategory.setFont(f);
            }
        }
        cbbCategory.setSelectedItem(dict.getCurrentCategory());
    }
    
    private void btnCancelActionPerformed() {
        commited = false;
        this.setVisible(false);
    }

    private void btnOkActionPerformed() {
        commited = true;
        this.setVisible(false);
    }

    private void initComponents() {
        JButton btnOk;
        JButton btnCancel;
        JLabel lblCategory;
        JLabel lblNewCategory;

        cbbCategory = new JComboBox<>();
        lblCategory = new JLabel();
        lblNewCategory = new JLabel();
        txfNewCategory = new JTextField();
        btnOk = new JButton();
        btnCancel = new JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Rename Category");
        setLocationRelativeTo(null);

        cbbCategory.setFont(Service.createFont());
        cbbCategory.setModel(new javax.swing.DefaultComboBoxModel<>());

        lblCategory.setFont(Service.createFont());
        lblCategory.setText("Choose category");

        lblNewCategory.setFont(Service.createFont());
        lblNewCategory.setText("New category name");

        txfNewCategory.setFont(Service.createFont());
        txfNewCategory.getDocument().addDocumentListener(new TextFieldFontChangeListener(txfNewCategory));

        btnOk.setFont(Service.createFont());
        btnOk.setText("Ok");
        btnOk.addActionListener(e -> btnOkActionPerformed());

        btnCancel.setFont(Service.createFont());
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(e -> btnCancelActionPerformed());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnOk)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel))
                    .addComponent(cbbCategory, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCategory)
                            .addComponent(lblNewCategory))
                        .addGap(0, 194, Short.MAX_VALUE))
                    .addComponent(txfNewCategory))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCategory)
                .addGap(4, 4, 4)
                .addComponent(cbbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblNewCategory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txfNewCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(btnCancel))
                .addContainerGap())
        );

        pack();
    }    
}
