/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import javax.swing.JOptionPane;

/**
 *
 * @author jaroslav_b
 */
public class Main extends javax.swing.JFrame implements IDictionary, ICategory {
    
    private ArrayList<WordDto> allDictionary;
    private ArrayList<WordDto> filteredDictionary;
    private ArrayList<String> categoryList;
    private int dictSize;
    private int dictCurrnt;
    private Setup setup;
    private AddCategoryDialog addCatDialog = new AddCategoryDialog(this, true);
    private WordDialog wordDialog = new WordDialog(this, true, addCatDialog, this);
    private AboutDialog aboutDialog = new AboutDialog(this, true);
    private FindDialog findDialog = new FindDialog(this, false);

    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();
        this.setTitle("Words");
        jLabel1.setText("");
        jLabel2.setText("");
        jLabel3.setText("");
        jTextField1.setText("");
        
        findDialog.setText("");
        findDialog.setDictionary(this);
        
        try {
            setup = Service.loadSetup();
            loadDictionary();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        KeyboardFocusManager ky = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        ky.addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent evt) {

                if (evt.isControlDown() && (evt.getKeyCode() == KeyEvent.VK_F)) {
                    showFindDialog();
                    return true;
                }
                return false;
            }
        });
        
        next(1);
    }
    
    private void loadDictionary() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        allDictionary = Service.loadDictionary(
                setup.getFullDictionaryFilePath(), 
                setup.getDictionarySeparator(), 
                setup.getDictionaryDateFormat());
        
        categoryList = Service.loadCategoryList(allDictionary);
        updateCategoryCombo();
        
        reorder();
    }
    
    public void addCategory(String category) {
        
        if ((category == null) || category.trim().equals("")) {
            return;
        }
        
        for (String c: categoryList) {
            if (c.equals(category)) {
                JOptionPane.showMessageDialog(null, "Category " + category + " already exists.");
                return;
            }
        }
        
        categoryList.add(category);
        updateCategoryCombo();

        wordDialog.setCategoryList(categoryList, category);
    }
    
    private void updateCategoryCombo() {
        String selected = jComboBox1.getSelectedItem().toString();
        
        int n = jComboBox1.getItemCount();
        for (int i = 1; i < n; i++) {
            jComboBox1.removeItemAt(n - i);
        }
        
        //jComboBox1.removeAllItems();
        //jComboBox1.addItem("All");
        
        Collections.sort(categoryList, Collator.getInstance(new Locale("cs", "CS"))); 
        for (String s: categoryList) {
            jComboBox1.addItem(s);
        }
        jComboBox1.setSelectedItem(selected);
    }
    
    private void saveDirectory() throws IOException {
        
        Service.saveDictionary(allDictionary, setup.getFullDictionaryFilePath(), 
            setup.getDictionarySeparator(), setup.getDictionaryDateFormat());
        
        Service.saveCategoryList(categoryList, setup.getFullCategoryFilePath());
    }

    public void setDictCurrnet(int i) {
        this.dictCurrnt = i;
        next(0);
    }
    
    public int getDictCurrnet() {
        return this.dictCurrnt;
    }
    
    private void next(int i) {
        if (this.dictSize == 0) {
            return;
        }
        
        this.dictCurrnt += i;
        if (this.dictCurrnt >= this.dictSize) {
            this.dictCurrnt = this.dictSize -1;
        }
        if (this.dictCurrnt < 0) {
            this.dictCurrnt = 0;
        }
        
        Font f;
        f = Service.findFont(filteredDictionary.get(this.dictCurrnt).getCz(), this.jLabel1.getFont());
        this.jLabel1.setFont(f);
        f = Service.findFont(filteredDictionary.get(this.dictCurrnt).getEn(), this.jLabel3.getFont());
        this.jLabel3.setFont(f);
        f = Service.findFont(filteredDictionary.get(this.dictCurrnt).getEn(), this.jTextField1.getFont());
        this.jTextField1.setFont(f);
        
        this.jLabel1.setText(filteredDictionary.get(this.dictCurrnt).getCz());
        this.jLabel3.setText("");
        this.jTextField1.setText("");
        this.jTextField1.grabFocus();
        updateStatus();
    }
    
    private void reorder() {
        filteredDictionary = Service.createReorderedList(
                allDictionary, jComboBox1.getSelectedItem().toString());
        
        this.dictCurrnt = -1;
        this.dictSize = filteredDictionary.size();
    }
    
    private void play() {
        disableControls(true);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                String fName = String.format("%s/%s.mp3", setup.getFullMp3Path(), removeBadChars(filteredDictionary.get(dictCurrnt).getEn()));
                File f = new File(fName);
                if (f.exists()) {
                    AudioFilePlayer.playFile(fName);
                }
                disableControls(false);
            }
        }).start();
    }
    
    private void showFindDialog() {
        findDialog.setDict(filteredDictionary);
        findDialog.setLabel("Searching in category " + jComboBox1.getSelectedItem());
        findDialog.setVisible(true);
    }
    
    private String removeBadChars(String w) {
        return w.replaceAll("\\?", "").replaceAll("\\.", "").replaceAll("'", "").replaceAll(",", "");
    }
    
    
    private void updateStatus() {
        this.jLabel2.setText(String.valueOf(this.dictCurrnt + 1) + " / " + String.valueOf(filteredDictionary.size()) + " words");
    }
    
    private void disableControls(boolean b) {
        jButton1.setEnabled(!b);
        jButton2.setEnabled(!b);
        jButton3.setEnabled(!b);
        jButton4.setEnabled(!b);
        jButton5.setEnabled(!b);
        jButton6.setEnabled(!b);
        jButton7.setEnabled(!b);
        jButton8.setEnabled(!b);
        jComboBox1.setEnabled(!b);
        this.revalidate();
    }
    
    private boolean compareTexts() {
        if (jTextField1.getText().trim().isEmpty()) {
            return true;
        }
        if (jTextField1.getText().trim().equals(jLabel3.getText().trim())) {
            return true;
        }
        JOptionPane.showMessageDialog(null, "Texts doesn't match. Correct it or delete it.");
        return false;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("jLabel1");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jTextField1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField1.setText("jTextField1");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel2.setText("jLabel2");

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton1.setText("Good");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton2.setText("Wrong");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton3.setText("Play");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jComboBox1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("jLabel3");
        jLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jButton4.setLabel("<");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setLabel(">");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("Edit...");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setText("About...");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText("Find...");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton2)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jButton4)
                    .addComponent(jButton5)
                    .addComponent(jButton6)
                    .addComponent(jButton7)
                    .addComponent(jButton8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (compareTexts()) {
            filteredDictionary.get(this.dictCurrnt).setGoodHits(filteredDictionary.get(this.dictCurrnt).getGoodHits() + 1);
            filteredDictionary.get(this.dictCurrnt).setLastGoodHit(new Date());
            next(1);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (compareTexts()) {
            filteredDictionary.get(this.dictCurrnt).setWrongHits(filteredDictionary.get(this.dictCurrnt).getWrongHits() + 1);
            filteredDictionary.get(this.dictCurrnt).setLastWrongHit(new Date());
            next(1);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        this.jLabel3.setText(filteredDictionary.get(this.dictCurrnt).getEn());
        play();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        try {
            saveDirectory();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_formWindowClosing

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // Do not reorder if category has been added
        if (!wordDialog.isVisible()) {
            findDialog.setVisible(false);
            reorder();
            next(1);
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        next(-1);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        next(1);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        wordDialog.setWord(filteredDictionary.get(dictCurrnt), categoryList);
        wordDialog.setVisible(true);
        this.jLabel1.setText(filteredDictionary.get(this.dictCurrnt).getCz());
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        showFindDialog();
    }//GEN-LAST:event_jButton8ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
