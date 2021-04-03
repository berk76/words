/*
*       WordExistsDialog.java
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

import cz.webstones.words.dictionary.WordDto;
import cz.webstones.words.dictionary.DictionaryException;
import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import cz.webstones.words.dictionary.IDictionary;

/**
 *
 * @author jarberan
 */
public class WordExistsDialog extends JEscapeableDialog {

    private static final long serialVersionUID = -1842961190834694712L;
    
    private JButton btnCreate;
    private JButton btnMove;
    private JLabel lblNativeWordValue;
    private JLabel lblForeignWordValue;
    private JLabel lblCategoryValue;
    
    private IDictionary dict;
    private WordDto newWord;
    private WordDto oldWord;
    private boolean cancelled;
    
    /**
     * Creates new form WordExistsDialog
     */
    public WordExistsDialog(java.awt.Frame parent, boolean modal, IDictionary dictionary) {
        super(parent, modal);
        initComponents();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        
        this.dict = dictionary;
        
    }
    
    public void showDialog(WordDto newWord, WordDto oldWord) {
        this.newWord = newWord;
        this.oldWord = oldWord;
        this.cancelled = false;
        
        this.lblNativeWordValue.setText(oldWord.getCz());
        this.lblForeignWordValue.setText(oldWord.getEn());
        this.lblCategoryValue.setText(oldWord.getCategory());
        
        if (oldWord.getCz().equals(newWord.getCz())) {
            this.lblNativeWordValue.setForeground(Color.RED);
        } else {
            this.lblNativeWordValue.setForeground(Color.BLACK);
        }
        
        if (oldWord.getEn().equals(newWord.getEn())) {
            this.lblForeignWordValue.setForeground(Color.RED);
        } else {
            this.lblForeignWordValue.setForeground(Color.BLACK);
        }
        
        if (oldWord.getCategory().equals(newWord.getCategory())) {
            this.lblCategoryValue.setForeground(Color.RED);
            this.btnMove.setEnabled(false);
        } else {
            this.lblCategoryValue.setForeground(Color.BLACK);
            this.btnMove.setEnabled(true);
        }

        if (oldWord.getCz().equals(newWord.getCz()) && oldWord.getEn().equals(newWord.getEn())) {
            this.btnCreate.setEnabled(false);
        } else {
            this.btnCreate.setEnabled(true);
        }
        
        this.setVisible(true);
    }
    
    /**
     * @return the play
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    private void btnCancelActionPerformed() {
        cancelled = true;
        this.setVisible(false);
    }

    private void btnCreateActionPerformed() {
        try {
            dict.addWord(newWord);
        } catch (DictionaryException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        this.setVisible(false);
    }

    private void btnMoveActionPerformed() {
        oldWord.setCategory(newWord.getCategory());
        try {
            dict.updateWord(oldWord);
        } catch (DictionaryException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        this.setVisible(false);
    }

    private void initComponents() {
        
        JButton btnCancel;
        JLabel lblAlreadyExists;
        JLabel lblNativeWord;
        JLabel lblForeignWord;
        JLabel lblCategory;

        lblAlreadyExists = new JLabel();
        lblNativeWord = new JLabel();
        lblNativeWordValue = new JLabel();
        lblForeignWord = new JLabel();
        lblForeignWordValue = new JLabel();
        lblCategory = new JLabel();
        lblCategoryValue = new JLabel();
        btnCreate = new JButton();
        btnMove = new JButton();
        btnCancel = new JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Word already exists");        

        lblAlreadyExists.setFont(Service.createFont());
        lblAlreadyExists.setText("Word already exists");

        lblNativeWord.setText("Native word");

        lblNativeWordValue.setFont(Service.createFont());
        lblNativeWordValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNativeWordValue.setText("lblNativeWordValue");
        lblNativeWordValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblNativeWordValue.addPropertyChangeListener(new LabelFontChangeListener(lblNativeWordValue));

        lblForeignWord.setText("Foreign word");

        lblForeignWordValue.setFont(Service.createFont());
        lblForeignWordValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblForeignWordValue.setText("lblForeignWordValue");
        lblForeignWordValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblForeignWordValue.addPropertyChangeListener(new LabelFontChangeListener(lblForeignWordValue));

        lblCategory.setText("Category");

        lblCategoryValue.setFont(Service.createFont());
        lblCategoryValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCategoryValue.setText("lblCategoryValue");
        lblCategoryValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnCreate.setFont(Service.createFont());
        btnCreate.setText("Create");
        btnCreate.addActionListener(e -> btnCreateActionPerformed());

        btnMove.setFont(Service.createFont());
        btnMove.setText("Move to current category");
        btnMove.addActionListener(e -> btnMoveActionPerformed());

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
                    .addComponent(lblCategoryValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 18, Short.MAX_VALUE)
                        .addComponent(btnCreate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMove)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel))
                    .addComponent(lblNativeWordValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAlreadyExists)
                            .addComponent(lblNativeWord)
                            .addComponent(lblForeignWord)
                            .addComponent(lblCategory))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(lblForeignWordValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblAlreadyExists)
                .addGap(18, 18, 18)
                .addComponent(lblNativeWord)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblNativeWordValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblForeignWord)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblForeignWordValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCategory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCategoryValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreate)
                    .addComponent(btnMove)
                    .addComponent(btnCancel))
                .addContainerGap())
        );

        pack();
    }
}
