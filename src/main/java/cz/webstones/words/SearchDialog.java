/*
*       SearchDialog.java
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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import cz.webstones.words.dictionary.IDictionary;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.GroupLayout;

/**
 *
 * @author jaroslav_b
 */
public class SearchDialog extends JEscapeableDialog implements IObserver {

    private static final long serialVersionUID = 667637337584380718L;
    private IDictionary dict = null;
    
    private JTextField txfSearch;
    private JLabel lblSearch;
    private JCheckBox chkCaseSensitive;
    private JCheckBox chkExactMatch;

    /**
     * Creates new form FindDialog
     */
    public SearchDialog(java.awt.Frame parent, boolean modal, IDictionary d) {
        super(parent, modal);
        initComponents();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        
        dict = d;
        dict.attach(this);
        setLabel(d.getCurrentCategory());
    }
    
    @Override
    public void updateObserver() {
        if (dict.getSubjectState() == DictionaryStateEnum.CUR_CATEGORY_CHANGED) {
            setLabel(dict.getCurrentCategory());
        }
    }

    public void setLabel(String s) {
        String text = "Searching in category %s";
        lblSearch.setText(String.format(text, s));
    }
    
    public void setText(String s) {
        txfSearch.setText(s);
    }
    
    private void search(boolean caseSensitive, boolean exactMatch) {
        String what = txfSearch.getText();
        
        if (what.equals("")) {
            return;
        }
                
        if (dict == null) {
            JOptionPane.showMessageDialog(this, "No dictionary provided!");
            return;
        }
        
        if (!dict.searchInCurrentCategory(what, caseSensitive, exactMatch)) {
            JOptionPane.showMessageDialog(this, "Nothing found");
        }

        this.toFront();
        txfSearch.requestFocus();
    }
    
    private void btnCloseActionPerformed() {
        this.setVisible(false);
    }

    private void btnSearchActionPerformed() {
        search(chkCaseSensitive.isSelected(), chkExactMatch.isSelected());
    }

    private void enterKeyPressed(java.awt.event.KeyEvent evt) {
        if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            search(chkCaseSensitive.isSelected(), chkExactMatch.isSelected());
        }
    }

    private void initComponents() {
        JButton btnClose;
        JButton btnFind;

        txfSearch = new JTextField();
        btnClose = new JButton();
        btnFind = new JButton();
        lblSearch = new JLabel();
        chkCaseSensitive = new JCheckBox();
        chkExactMatch = new JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Search");

        lblSearch.setFont(Service.createFont());
        lblSearch.setText("lblSearch");
        
        txfSearch.setFont(Service.createFont());
        txfSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                enterKeyPressed(evt);
            }
        });
        txfSearch.addFocusListener(new FocusListener()
        {
            public void focusLost(FocusEvent e)
            {
                JTextField field = (JTextField)e.getSource();
                field.select(0, 0);
            }
 
            public void focusGained(FocusEvent e)
            {
                JTextField field = (JTextField)e.getSource();
                field.selectAll();
            }
        });
        txfSearch.getDocument().addDocumentListener(new TextFieldFontChangeListener(txfSearch));
        
        chkCaseSensitive.setText("Case Sensitive");
        chkCaseSensitive.setFont(Service.createFontSmall());
        chkExactMatch.setText("Exact Match");
        chkExactMatch.setFont(Service.createFontSmall());

        btnClose.setFont(Service.createFont());
        btnClose.setText("Close");
        btnClose.addActionListener(e -> btnCloseActionPerformed());

        btnFind.setFont(Service.createFont());
        btnFind.setText("Find");
        btnFind.addActionListener(e -> btnSearchActionPerformed());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(txfSearch, 350, 350, 350)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(chkExactMatch)
                                    .addPreferredGap(ComponentPlacement.RELATED, 180, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(btnFind)
                                    .addPreferredGap(ComponentPlacement.RELATED)))
                            .addComponent(btnClose))
                        .addComponent(chkCaseSensitive)
                        .addComponent(lblSearch))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblSearch)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(txfSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addComponent(chkCaseSensitive)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(chkExactMatch)
                            .addContainerGap(25, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(btnClose)
                                .addComponent(btnFind))
                            .addContainerGap())))
        );
        getContentPane().setLayout(layout);
        
        pack();
    }
}
