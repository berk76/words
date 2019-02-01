/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import static cz.webstones.words.DictionaryStateEnum.stateCurWordChanged;
import cz.webstones.words.mp3.Mp3Creator;
import cz.webstones.words.mp3.Mp3CreatorException;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author jaroslav_b
 */
public class Main extends javax.swing.JFrame implements IObserver {
    
    private Dictionary dict;
    private Setup setup;
    private AddCategoryDialog addCatDialog;
    private RenameCategoryDialog renameCatDialog;
    private WordDialog wordDialog;
    private AboutDialog aboutDialog;
    private FindDialog findDialog;
    private LanguageDialog langDialog;
    private boolean disableCategotyChange = false;
    private ImageIcon loadingIcon = new ImageIcon(this.getClass().getClassLoader().getResource("ajax-loader.gif"));
    protected WordDto wordToPlay = null;
    private boolean controlsEnabled = true;
    private boolean mp3DownloadConfirmed = false;

    /**
     * Creates new form Main
     */
    public Main() throws DictionaryException {
        
        dict = new Dictionary();
        addCatDialog = new AddCategoryDialog(this, true);
        renameCatDialog = new RenameCategoryDialog(this, true);
        wordDialog = new WordDialog(this, true, addCatDialog, dict);
        aboutDialog = new AboutDialog(this, true);
        findDialog = new FindDialog(this, false, dict);
        langDialog = new LanguageDialog(this, true);
        
        if (isRunning()){
            String txt = "You should not run this application in more instances, otherwise you may lost some changes in your dictionary.\nDo you want to run it anyway?";
            int dialogResult = JOptionPane.showConfirmDialog (this, txt,"Question", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.NO_OPTION){
                System.exit(0);
            }
        } else {
            onStart();
        }
        
        initComponents();
        this.setTitle(Service.version);
        jLabel1.setText("");
        jLabel2.setText("");
        jLabel3.setText("");
        jTextField1.setText("");
        
        findDialog.setText("");
        
        try {
            setup = Service.getSetup(true);
            if ((setup.getLanguage() == null) || setup.getLanguage().equals("")) {
                langDialog.setVisible(true);
                setup.setLanguage(langDialog.getLangCode());
                Service.saveSetup();
            }

            dict.attach(this);
            dict.loadDictionary(
                setup.getFullDictionaryFilePath(), 
                setup.getDictionarySeparator(), 
                setup.getDictionaryDateFormat());
            
        } catch (Exception ex) {
            ex.printStackTrace();
            onFinish();
            System.exit(-1);
        }
    }
    
    private void onStart(){
        Preferences prefs;
        prefs = Preferences.userRoot().node(this.getClass().getName());
        prefs.put("RUNNING", "true");
    }

    private void onFinish(){
        Preferences prefs;
        prefs = Preferences.userRoot().node(this.getClass().getName());
        prefs.put("RUNNING", "false");
    }

    private boolean isRunning(){
        Preferences prefs;
        prefs = Preferences.userRoot().node(this.getClass().getName());
        return prefs.get("RUNNING", null) != null ? Boolean.valueOf(prefs.get("RUNNING", null)) : false;
    }
    
    public void updateObserver() {
        switch (dict.getSubjectState()) {
            
            case stateCurWordChanged:
            case stateCurWordDeleted:
                next(0);
                break;
                
            case stateCurCategoryChanged:
                if (!dict.getCurrentCategory().equals(jComboBox1.getSelectedItem().toString())) {
                    jComboBox1.setSelectedItem(dict.getCurrentCategory());
                }
                break;
                
            case stateCategoryListChanged:
                disableCategotyChange = true;
                updateCategoryCombo();
                disableCategotyChange = false;
                break;
                
            case stateWordAdded:
                break;
        }
    }
    
    private void addWord(WordDto w) {
        try {
            dict.addWord(w);
        } catch (DictionaryException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
            return;
        }
        checkIfSoundExistsAndPlay(w);
        
        dict.setCategory(w.getCategory());
        dict.setCurrent(w);
        this.jLabel3.setText(w.getEn());
    }
    
    private void checkIfSoundExistsAndPlay(WordDto w) {
        String fName = w.getMp3FilenameEn();
        File f = new File(fName);
        
        if (f.exists()) {
            wordToPlay = w;
            _play();
            return;
        }
        
        /* Get MP3 */
        if (!mp3DownloadConfirmed) {
            int dialogResult = JOptionPane.showConfirmDialog (this, "Pronunciation for this word is missing.\nDo you want downloading it?","Question", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                mp3DownloadConfirmed = true;
            }
        }
        
        if (mp3DownloadConfirmed) {
            disableControls(true);
            wordToPlay = w;
            jLabel2.setIcon(loadingIcon);
            jLabel2.setText("Downloading ...");
                
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Mp3Creator.createMp3(wordToPlay.getEn(), setup.getLanguage(), wordToPlay.getMp3FilenameEn());
                    } catch (Mp3CreatorException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                        mp3DownloadConfirmed = false;
                    }    
                    disableControls(false);
                    jLabel2.setIcon(null);
                    updateStatus();
                    
                    _play();
                }
            }).start();
            
        }
    }
    
    private void addCategory(String category) {
        try {
            dict.addCategory(category);
        } catch (DictionaryException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
    
    private void renameCategory(String oldCat, String newCat) {
        dict.renameCategory(oldCat, newCat);
        dict.setCategory(newCat);
    }
    
    private void updateCategoryCombo() {
        int n = jComboBox1.getItemCount();
        for (int i = 1; i < n; i++) {
            jComboBox1.removeItemAt(n - i);
        }
        
        for (String s: dict.getCategoryList()) {
            jComboBox1.addItem(s);
        }
        jComboBox1.setSelectedItem(dict.getCurrentCategory());
    }
    
    private void saveDirectory() throws IOException {
        dict.saveDictionary(setup.getFullDictionaryFilePath(), 
            setup.getDictionarySeparator(), setup.getDictionaryDateFormat());
    }
    
    private void next(int i) {
        if (dict.size() == 0) {
            return;
        }
        
        int dictCurrnt = dict.getCurrnet();
        
        dictCurrnt += i;
        if (dictCurrnt >= dict.size()) {
            dictCurrnt = dict.size() -1;
        }
        if (dictCurrnt < 0) {
            dictCurrnt = 0;
        }
        dict.setCurrnet(dictCurrnt);
        
        Font f;
        WordDto w = dict.getWord();
        f = Service.findFont(w.getCz(), this.jLabel1.getFont());
        this.jLabel1.setFont(f);
        f = Service.findFont(w.getEn(), this.jLabel3.getFont());
        this.jLabel3.setFont(f);
        f = Service.findFont(w.getEn(), this.jTextField1.getFont());
        this.jTextField1.setFont(f);
        
        this.jLabel1.setText(w.getCz());
        if (findDialog.isShowing()) {
            this.jLabel3.setText(w.getEn());
        } else {
            this.jLabel3.setText("");
        }
        this.jTextField1.setText("");
        this.jTextField1.grabFocus();
        
        updateStatus();
        disableGoodWrong(true);
    }
    
    private void _play() {
        disableControls(true);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                String fName = wordToPlay.getMp3FilenameEn();
                File f = new File(fName);
                if (f.exists()) {
                    AudioFilePlayer.playFile(fName);
                }
                disableControls(false);
            }
        }).start();
    }
    
    private void showFindDialog() {
        findDialog.setLabel("Searching in category " + jComboBox1.getSelectedItem());
        findDialog.setVisible(true);
    }
    
    private void updateStatus() {
        if (controlsEnabled) {
            this.jLabel2.setText(String.valueOf(dict.getCurrnet() + 1) + " / " + String.valueOf(dict.size()) + " words");
        }
    }
    
    private void disableControls(boolean b) {
        controlsEnabled = !b;
        jButton1.setEnabled(!b);
        jButton2.setEnabled(!b);
        jButton3.setEnabled(!b);
        jButton4.setEnabled(!b);
        jButton5.setEnabled(!b);
        jComboBox1.setEnabled(!b);
        this.revalidate();
    }
    
    private void disableGoodWrong(boolean b) {
        jButton1.setEnabled(!b);
        jButton2.setEnabled(!b);
    }
    
    private boolean compareTexts() {
        if (jTextField1.getText().trim().isEmpty()) {
            return true;
        }
        if (jTextField1.getText().trim().equals(jLabel3.getText().trim())) {
            return true;
        }
        JOptionPane.showMessageDialog(this, "Texts don't match. Correct it or delete it.");
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

        jMenuItem2 = new javax.swing.JMenuItem();
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
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();

        jMenuItem2.setText("jMenuItem2");

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
        jButton1.setForeground(new java.awt.Color(51, 153, 0));
        jButton1.setText("Good");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton2.setForeground(new java.awt.Color(204, 0, 51));
        jButton2.setText("Wrong");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton3.setText("Show & Play");
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

        jLabel4.setText("Native word");

        jLabel5.setText("Foreign word");

        jLabel6.setText("Try to write down");

        jLabel7.setText("Choose category");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel2)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4)
                    .addComponent(jButton5))
                .addContainerGap())
        );

        jMenuBar1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jMenu1.setText("Word");
        jMenu1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jMenuItem1.setText("Find...");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem7.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem7.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jMenuItem7.setText("Add...");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem7);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jMenuItem3.setText("Edit...");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuItem8.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jMenuItem8.setText("Delete");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem8);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Category");
        jMenu2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jMenuItem4.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jMenuItem4.setText("Add...");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem4);

        jMenuItem5.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jMenuItem5.setText("Rename...");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        jMenuBar1.add(jMenu2);

        jMenu4.setText("Help");
        jMenu4.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jMenuItem6.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jMenuItem6.setText("About...");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem6);

        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // Button Good
        if (compareTexts()) {
            dict.getWord().incGoodHits();
            dict.getWord().setLastGoodHit(new Date());
            next(1);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // Button Wrong
        if (compareTexts()) {
            dict.getWord().incWrongHits();
            dict.getWord().setLastWrongHit(new Date());
            next(1);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // Button Show&Play
        this.jLabel3.setText(dict.getWord().getEn());
        checkIfSoundExistsAndPlay(dict.getWord());
    }//GEN-LAST:event_jButton3ActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        try {
            saveDirectory();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            onFinish();
        }
    }//GEN-LAST:event_formWindowClosing

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        findDialog.setVisible(false);
        if (!disableCategotyChange) {
            if (!dict.getCurrentCategory().equals(jComboBox1.getSelectedItem().toString())) {
                dict.setCategory(jComboBox1.getSelectedItem().toString());
            }
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        next(-1);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        next(1);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        showFindDialog();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // Edit Word
        String oldWord = dict.getWord().getMp3FilenameEn();
        wordDialog.setWord(dict.getWord());
        //wordDialog.setForeignWordEditable(false);
        wordDialog.setVisible(true);
        this.jLabel1.setText(dict.getWord().getCz());
        this.jLabel3.setText(dict.getWord().getEn());
        if (!oldWord.equals(dict.getWord().getMp3FilenameEn())) {
            File f = new File(oldWord);
            f.delete();
        }
        checkIfSoundExistsAndPlay(dict.getWord());
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        // Add Category
        addCatDialog.setCategoryText("");
        addCatDialog.setVisible(true);
        if (addCatDialog.isCommited()) {
            addCategory(addCatDialog.getCategoryText());
        }
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        // Rename Category
        renameCatDialog.setNewCategoryText("");
        renameCatDialog.setCategoryList(dict.getCategoryList(), jComboBox1.getSelectedItem().toString());
        renameCatDialog.setVisible(true);
        if (renameCatDialog.isCommited()) {
            renameCategory(renameCatDialog.getOldCategoryText(), renameCatDialog.getNewCategoryText());
        }
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        // Add Word
        WordDto w = new WordDto();
        w.setCategory(jComboBox1.getSelectedItem().toString());
        wordDialog.setWord(w);
        wordDialog.setForeignWordEditable(true);
        wordDialog.setVisible(true);
        if (wordDialog.isCommited()) {
            wordDialog.setVisible(false);
            addWord(w);
        }
        
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        int dialogResult = JOptionPane.showConfirmDialog (this, "Do you want to delete word: " + dict.getWord().getCz() + " ?","Question", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION){
            dict.deleteCurrentWord();
        }
    }//GEN-LAST:event_jMenuItem8ActionPerformed

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
                try {
                    new Main().setVisible(true);
                } catch (DictionaryException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
