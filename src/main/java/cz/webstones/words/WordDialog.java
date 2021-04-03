/*
*       WordDialog.java
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
import cz.webstones.words.dictionary.IObserver;
import cz.webstones.words.dictionary.WordDto;
import cz.webstones.words.dictionary.DictionaryException;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import cz.webstones.words.dictionary.IDictionary;
import java.awt.Font;

/**
 *
 * @author jaroslav_b
 */
public class WordDialog extends JEscapeableDialog implements IObserver {
    
    private static final long serialVersionUID = -626717366048322278L;
    
    private JComboBox<String> cbbCategory;
    private JTextField txfForeignWord;
    private JTextField txfNativeWord;
    private JTextField txfLastGood;
    private JTextField txfLastWrong;
    private JTextField txfCounter;
    private JTextField txfSucessRate;
    
    private boolean commited;
    private WordDto word;
    private AddCategoryDialog addCatDialog;
    private IDictionary dict;

    /**
     * Creates new form WordDialog
     */
    public WordDialog(java.awt.Frame parent, boolean modal, AddCategoryDialog d, IDictionary dic) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(null);
        
        addCatDialog = d;
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
    
    public void setWord(WordDto w) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        int total = 0;
        double rate = 0;
        
        setCommited(false);
        this.word = w;
        
        txfNativeWord.setText(this.word.getCz());
        txfForeignWord.setText(this.word.getEn());

        updateCategoryCombo();
        cbbCategory.setSelectedItem(this.word.getCategory());
        
        txfLastGood.setText((this.word.getLastGoodHit() == null) ? "" : sdf.format(this.word.getLastGoodHit()));
        txfLastWrong.setText((this.word.getLastWrongHit() == null) ? "" : sdf.format(this.word.getLastWrongHit()));
        total = this.word.getGoodHits() + this.word.getWrongHits();
        rate = (total == 0) ? 0 : Service.round((double) this.word.getGoodHits() / total, 2);
        txfCounter.setText(String.valueOf(total));
        txfSucessRate.setText(String.valueOf(rate));
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
    
    private void addCategory() {
        addCatDialog.setCategoryText("");
        addCatDialog.setVisible(true);
        if (addCatDialog.isCommited()) {
            try {
                dict.addCategory(addCatDialog.getCategoryText());
            } catch (DictionaryException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
        cbbCategory.setSelectedItem(addCatDialog.getCategoryText());
    }

    @Override
    public void setVisible(boolean b) {
        if (dict.getCategoryList().isEmpty()) {
            addCategory();
        }
        super.setVisible(b);
        txfForeignWord.requestFocus();
    }
    
    /**
     * @return the commited
     */
    public boolean isCommited() {
        return commited;
    }

    /**
     * @param commited the commited to set
     */
    private void setCommited(boolean commited) {
        this.commited = commited;
    }
    
    private void btnOkActionPerformed() {
        WordDto tmp = new WordDto();
        tmp.setCz(txfNativeWord.getText().trim());
        tmp.setEn(txfForeignWord.getText().trim());
        tmp.setCategory(cbbCategory.getSelectedItem().toString());
        
        try {
            dict.validateWord(tmp);
            
            this.word.setCz(tmp.getCz());
            this.word.setEn(tmp.getEn());
            this.word.setCategory(tmp.getCategory());
            
            setCommited(true);
            this.setVisible(false);
        } catch (DictionaryException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
    
    private void btnCancelActionPerformed() {
        setCommited(false);
        this.setVisible(false);
    }

    private void btnAddCategoryActionPerformed() {
        addCategory();
    }
    
    private void initComponents() {
        
        JButton btnOk;
        JButton btnCancel;
        JButton btnAddCategory;
        JLabel lblForeignWord;
        JLabel lblNativeWord;
        JLabel lblCategory;
        JLabel lblLastGood;
        JLabel lblLastWrong;
        JLabel lblCounter;
        JLabel lblSuccessRate;

        lblForeignWord = new JLabel();
        txfForeignWord = new JTextField();
        lblNativeWord = new JLabel();
        txfNativeWord = new JTextField();
        lblCategory = new JLabel();
        cbbCategory = new JComboBox<>();
        btnOk = new JButton();
        btnCancel = new JButton();
        lblLastGood = new JLabel();
        txfLastGood = new JTextField();
        lblLastWrong = new JLabel();
        txfLastWrong = new JTextField();
        lblCounter = new JLabel();
        txfCounter = new JTextField();
        lblSuccessRate = new JLabel();
        txfSucessRate = new JTextField();
        btnAddCategory = new JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Edit word");

        lblForeignWord.setFont(Service.createFont());
        lblForeignWord.setText("Foreign word");

        txfForeignWord.setFont(Service.createFont());
        txfForeignWord.getDocument().addDocumentListener(new TextFieldFontChangeListener(txfForeignWord));

        lblNativeWord.setFont(Service.createFont());
        lblNativeWord.setText("Native word");

        txfNativeWord.setFont(Service.createFont());
        txfNativeWord.getDocument().addDocumentListener(new TextFieldFontChangeListener(txfNativeWord));

        lblCategory.setFont(Service.createFont());
        lblCategory.setText("Category");

        cbbCategory.setFont(Service.createFont());
        cbbCategory.setModel(new javax.swing.DefaultComboBoxModel<>());

        btnOk.setFont(Service.createFont());
        btnOk.setText("OK");
        btnOk.addActionListener(e -> btnOkActionPerformed());

        btnCancel.setFont(Service.createFont());
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(e -> btnCancelActionPerformed());

        lblLastGood.setText("Last good");

        txfLastGood.setEnabled(false);
        txfLastGood.setFocusable(false);

        lblLastWrong.setText("Last wrong");

        txfLastWrong.setEnabled(false);
        txfLastWrong.setFocusable(false);

        lblCounter.setText("Counter");

        txfCounter.setEnabled(false);
        txfCounter.setFocusable(false);

        lblSuccessRate.setText("Success rate");

        txfSucessRate.setEnabled(false);
        txfSucessRate.setFocusable(false);

        btnAddCategory.setFont(Service.createFont());
        btnAddCategory.setText("Add Category...");
        btnAddCategory.addActionListener(e -> btnAddCategoryActionPerformed());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txfForeignWord)
                    .addComponent(txfNativeWord)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 263, Short.MAX_VALUE)
                        .addComponent(btnOk)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblForeignWord)
                            .addComponent(lblNativeWord)
                            .addComponent(lblCategory)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cbbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAddCategory))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblLastWrong)
                                    .addComponent(lblLastGood)
                                    .addComponent(txfLastGood, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                                    .addComponent(txfLastWrong))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblCounter)
                                    .addComponent(lblSuccessRate)
                                    .addComponent(txfCounter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txfSucessRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblForeignWord)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txfForeignWord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblNativeWord)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txfNativeWord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCategory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddCategory))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblLastGood)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txfLastGood, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblLastWrong)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txfLastWrong, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblCounter)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txfCounter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSuccessRate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txfSucessRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(btnCancel))
                .addContainerGap())
        );

        pack();
    }
}
