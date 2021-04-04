/*
*       AddCategoryDialog.java
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

import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.GroupLayout;

/**
 *
 * @author jaroslav_b
 */
public class AddCategoryDialog extends JEscapeableDialog {
    
    private static final long serialVersionUID = -8086361529678177201L;
    private boolean commited;
    
    private JTextField txfCategory;

    /**
     * Creates new form AddCategoryDialog
     */
    public AddCategoryDialog(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setMinimumSize(getSize());
        this.setLocationRelativeTo(null);
    }
    
    public void setCategoryText(String s) {
        txfCategory.setText(s);
    }
    
    public String getCategoryText() {
        return txfCategory.getText();
    }
    
    public boolean isCommited() {
        return commited;
    }
    
    private void btnOkActionPerformed() {
        commited = true;
        setVisible(false);
    }

    private void btnCancelActionPerformed() {
        commited = false;
        setVisible(false);
    }

    private void initComponents() {
        javax.swing.JLabel lblCategory;

        txfCategory = new javax.swing.JTextField();
        JButton btnCancel = new javax.swing.JButton();
        JButton btnOk = new javax.swing.JButton();
        lblCategory = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add Category");

        txfCategory.setFont(Service.createFont());
        txfCategory.getDocument().addDocumentListener(new TextFieldFontChangeListener(txfCategory));

        btnCancel.setFont(Service.createFont());
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(e -> btnCancelActionPerformed());

        btnOk.setFont(Service.createFont());
        btnOk.setText("OK");
        btnOk.addActionListener(e -> btnOkActionPerformed());

        lblCategory.setFont(Service.createFont());
        lblCategory.setText("New category name");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                        .addComponent(txfCategory, GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblCategory)
                            .addGap(0, 187, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(0, 196, Short.MAX_VALUE)
                            .addComponent(btnOk)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnCancel)))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblCategory)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(txfCategory, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(btnCancel)
                        .addComponent(btnOk))
                    .addContainerGap())
        );
        getContentPane().setLayout(layout);

        pack();
    }
}
