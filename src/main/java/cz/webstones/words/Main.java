/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import static cz.webstones.words.Service.findFont;
import cz.webstones.words.mp3.AudioFilePlayer;
import cz.webstones.words.dictionary.IObserver;
import cz.webstones.words.dictionary.WordDto;
import cz.webstones.words.dictionary.DictionaryException;
import static cz.webstones.words.dictionary.DictionaryStateEnum.stateCurWordChanged;
import cz.webstones.words.dictionary.impl.DictionaryImpl;
import cz.webstones.words.mp3.Mp3Creator;
import cz.webstones.words.mp3.Mp3CreatorException;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileView;
import cz.webstones.words.dictionary.IDictionary;
import java.awt.Font;
import java.nio.file.Files;

/**
 *
 * @author jaroslav_b
 */
public class Main extends javax.swing.JFrame implements IObserver {
    
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private IDictionary dict;
    private AddCategoryDialog addCatDialog;
    private RenameCategoryDialog renameCatDialog;
    private WordDialog wordDialog;
    private AboutDialog aboutDialog;
    private FindDialog findDialog;
    private LanguageDialog langDialog;
    private ErrorDialog errorDialog;
    private WordExistsDialog wordExistsDialog;

    private boolean disableCategotyChange = false;
    private ImageIcon loadingIcon = new ImageIcon(this.getClass().getClassLoader().getResource("ajax-loader.gif"));
    protected WordDto wordToPlay = null;
    private boolean controlsEnabled = true;
    private boolean mp3DownloadConfirmed = true;

    /**
     * Creates new form Main
     */
    public Main() throws DictionaryException {
        
        super();
        dict = new DictionaryImpl();
        addCatDialog = new AddCategoryDialog(this, true);
        renameCatDialog = new RenameCategoryDialog(this, true, dict);
        wordDialog = new WordDialog(this, true, addCatDialog, dict);
        aboutDialog = new AboutDialog(this, true, dict);
        findDialog = new FindDialog(this, false, dict);
        wordExistsDialog = new WordExistsDialog(this, true, dict);

        Point p = findDialog.getLocation();
        findDialog.setLocation(p.x + 500, p.y - 100);

        langDialog = new LanguageDialog(this, true);
        errorDialog = new ErrorDialog(this, true);

        if (isRunning()) {
            String txt = "You should not run this application in more instances, otherwise you may lost some changes in your dictionary.\nDo you want to run it anyway?";
            int dialogResult = JOptionPane.showConfirmDialog(this, txt, "Question", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
        } else {
            onStart();
        }

        initComponents();
        this.setLocationRelativeTo(null);
        this.setMinimumSize(this.getSize());
        setTitleText(Service.VERSION, "", null);
        jLabel1.setText("");
        jLabel1.addPropertyChangeListener(new LabelFontChangeListener(jLabel1));
        jLabel2.setText("");
        jLabel3.setText("");
        jLabel3.addPropertyChangeListener(new LabelFontChangeListener(jLabel3));
        jTextField1.setText("");
        jTextField1.getDocument().addDocumentListener(new TextFieldFontChangeListener(jTextField1));

        findDialog.setText("");

        try {
            dict.attach(this);
            String dictPath = Service.getHistory();
            loadDirectory(dictPath);
        } catch (Exception ex) {
            errorDialog.showError("Error: Cannot init and load dictionary.", ex);
            onFinish();
            System.exit(-1);
        }
    }

    private void onStart() {
        Preferences prefs;
        prefs = Preferences.userRoot().node(this.getClass().getName());
        prefs.put("RUNNING", "true");
    }

    private void onFinish() {
        Preferences prefs;
        prefs = Preferences.userRoot().node(this.getClass().getName());
        prefs.put("RUNNING", "false");
    }

    private boolean isRunning() {
        Preferences prefs;
        prefs = Preferences.userRoot().node(this.getClass().getName());
        return prefs.get("RUNNING", null) != null ? Boolean.valueOf(prefs.get("RUNNING", null)) : false;
    }

    public void updateObserver() {
        switch (dict.getSubjectState()) {

            case stateWordAdded:
            case stateCurWordChanged:
            case stateCurWordDeleted:
                nextRelative(0);
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

            case stateDictionaryLoaded:
                try {
                    this.setTitleText(Service.VERSION, dict.getDictionaryName(), dict.getLanguage());
                } catch (IOException ex) {
                    errorDialog.showError("Error: Cannot get language.", ex);
                }
                break;
        }
    }

    private void setTitleText(String name, String dictionary, LanguageDto lang) {
        String langCode = "";
        if (lang != null) {
            langCode = lang.getCode();
        }
        setTitle(String.format("%s - %s [%s]", name, dictionary, langCode));
    }

    private void play(WordDto w) {
        String fName = w.getMp3FilenameEn(dict.getSetup().getFullMp3Path());
        File f = new File(fName);

        if (!f.exists() && !mp3DownloadConfirmed) {
            String txt = "Pronunciation for this word is missing.\nDo you want downloading it?";
            int dialogResult = JOptionPane.showConfirmDialog(this, txt, "Question", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                mp3DownloadConfirmed = true;
            }
        }

        disableControls(true);
        disableToolbarControls(true);
        wordToPlay = w;

        new Thread(new Runnable() {
            @Override
            public void run() {

                String fName = wordToPlay.getMp3FilenameEn(dict.getSetup().getFullMp3Path());
                File f = new File(fName);

                if (!f.exists() && mp3DownloadConfirmed) {
                    jLabel2.setIcon(loadingIcon);
                    jLabel2.setText("Downloading ...");

                    try {
                        Mp3Creator.createMp3(wordToPlay.getEn(), dict.getSetup().getLanguage(), wordToPlay.getMp3FilenameEn(dict.getSetup().getFullMp3Path()));
                    } catch (Mp3CreatorException ex) {
                        errorDialog.showError("Error: Cannot download pronunciation.", ex);
                        mp3DownloadConfirmed = false;
                    }

                    disableControls(false);
                    disableToolbarControls(false);
                    jLabel2.setIcon(null);
                    updateStatus();
                    disableControls(true);
                    disableToolbarControls(true);
                }

                if (f.exists()) {
                    try {
                        AudioFilePlayer.playFile(fName);
                    } catch (Exception ex) {
                        errorDialog.showError("Error: Cannot play pronunciation.", ex);
                    }
                }

                disableControls(false);
                disableToolbarControls(false);

            }
        }).start();
    }

    private void addCategory(String category) {
        try {
            dict.addCategory(category);
            dict.setCategory(category);
        } catch (DictionaryException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        for (String s : dict.getCategoryList()) {
            jComboBox1.addItem(s);
            if (jComboBox1.getFont().canDisplayUpTo(s) != -1) {
                Font f = findFont(s, jComboBox1.getFont());
                jComboBox1.setFont(f);
            }
        }
        jComboBox1.setSelectedItem(dict.getCurrentCategory());
    }

    private void loadDirectory(String dictPath) throws IOException {
        Setup setup = Service.getSetup(true, dictPath);
        if ((setup.getLanguage() == null) || setup.getLanguage().equals("")) {
            do {
                langDialog.setVisible(true);
            } while (langDialog.getLangCode().equals(""));
            setup.setLanguage(langDialog.getLangCode());
            Service.saveSetup(setup);
        }

        dict.loadDictionary(setup);
    }
    
    private void saveDirectory() throws IOException {
        dict.saveDictionary();
    }

    private void nextAbsolute(int i) {
            if (dict.size() == 0) {
            this.jLabel1.setText("<category is empty>");
            this.jLabel3.setText("");
            this.jLabel2.setText("0 / 0 words");
            disableControls(true);
            jComboBox1.setEnabled(true);
            return;
        }
        disableControls(false);

        int dictCurrnt = i;

        if (dictCurrnt >= dict.size()) {
            dictCurrnt = dict.size() - 1;
        }
        if (dictCurrnt < 0) {
            dictCurrnt = 0;
        }
        dict.setCurrnet(dictCurrnt);

        WordDto w = dict.getWord();

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
    
    private void nextRelative(int i) {
        nextAbsolute(dict.getCurrnet() + i);
    }

    private void showFindDialog() {
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
        jButton14.setEnabled(!b);
        jComboBox1.setEnabled(!b);
        this.revalidate();
    }
    
    private void disableToolbarControls(boolean b) {
        jButton6.setEnabled(!b);
        jButton7.setEnabled(!b);
        jButton8.setEnabled(!b);
        jButton9.setEnabled(!b);
        jButton10.setEnabled(!b);
        jButton11.setEnabled(!b);
        jButton12.setEnabled(!b);
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
        jButton14 = new javax.swing.JButton();
        jToolBar1 = new javax.swing.JToolBar();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();

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

        jButton4.setToolTipText("Back");
        jButton4.setLabel("<");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setToolTipText("Next");
        jButton5.setLabel(">");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jLabel4.setText("Native word");

        jLabel5.setText("Foreign word");

        jLabel6.setText("Try to write it down");

        jLabel7.setText("Choose category");

        jButton14.setText("|<");
        jButton14.setToolTipText("Rewind");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                        .addComponent(jButton14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4)
                    .addComponent(jButton5)
                    .addComponent(jButton14))
                .addContainerGap())
        );

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/page.png"))); // NOI18N
        jButton6.setToolTipText("New dictionary");
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton6);

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/folder.png"))); // NOI18N
        jButton7.setToolTipText("Open dictionary");
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton7);

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disk.png"))); // NOI18N
        jButton8.setToolTipText("Save dictionary");
        jButton8.setFocusable(false);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton8);

        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/find.png"))); // NOI18N
        jButton9.setToolTipText("Find word");
        jButton9.setFocusable(false);
        jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton9.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton9);

        jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/application_add.png"))); // NOI18N
        jButton10.setToolTipText("Add word");
        jButton10.setFocusable(false);
        jButton10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton10.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton10);

        jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/application_edit.png"))); // NOI18N
        jButton11.setToolTipText("Edit word");
        jButton11.setFocusable(false);
        jButton11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton11.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton11);

        jButton12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/application_delete.png"))); // NOI18N
        jButton12.setToolTipText("Delete word");
        jButton12.setFocusable(false);
        jButton12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton12.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton12);

        jMenuBar1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jMenu3.setText("Dictionary");
        jMenu3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jMenuItem10.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItem10.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jMenuItem10.setText("New...");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem10);

        jMenuItem12.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItem12.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jMenuItem12.setText("Open...");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem12);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItem2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jMenuItem2.setText("Save");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem2);

        jMenuBar1.add(jMenu3);

        jMenu1.setText("Word");
        jMenu1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItem1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jMenuItem1.setText("Find...");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem7.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItem7.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jMenuItem7.setText("Add...");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem7);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
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

        jMenuItem9.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jMenuItem9.setText("Delete");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem9);

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
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // Button Good
        if (compareTexts()) {
            dict.getWord().incGoodHits();
            dict.getWord().setLastGoodHit(new Date());
            nextRelative(1);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // Button Wrong
        if (compareTexts()) {
            dict.getWord().incWrongHits();
            dict.getWord().setLastWrongHit(new Date());
            nextRelative(1);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // Button Show&Play
        this.jLabel3.setText(dict.getWord().getEn());
        play(dict.getWord());
    }//GEN-LAST:event_jButton3ActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        try {
            saveDirectory();
            Service.saveHistory(dict.getSetup().getDataDir());
        } catch (IOException ex) {
            errorDialog.showError("Error: Cannot save dictionary.", ex);
        } finally {
            onFinish();
        }        
    }//GEN-LAST:event_formWindowClosing

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        if (!disableCategotyChange) {
            if (!dict.getCurrentCategory().equals(jComboBox1.getSelectedItem().toString())) {
                dict.setCategory(jComboBox1.getSelectedItem().toString());
            }
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        nextRelative(-1);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        nextRelative(1);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        showFindDialog();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        editWord();
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
        renameCatDialog.setVisible(true);
        if (renameCatDialog.isCommited()) {
            renameCategory(renameCatDialog.getOldCategoryText(), renameCatDialog.getNewCategoryText());
        }
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void editWord() {
        // Edit Word
        WordDto w = dict.getWord();
        String oldWordPath = w.getMp3FilenameEn(dict.getSetup().getFullMp3Path());
        wordDialog.setWord(w);
        wordDialog.setVisible(true);
        
        try {
            dict.updateWord(w);
        } catch (DictionaryException ex) {
            errorDialog.showError("Error: Cannot modify word.", ex);
        }
        
        if (!oldWordPath.equals(w.getMp3FilenameEn(dict.getSetup().getFullMp3Path()))) {
            File f = new File(oldWordPath);
            try {
                Files.delete(f.toPath());
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
            play(w);
        }
    }
    
    private void addWord() {
        // Add Word
        WordDto w = new WordDto();
        w.setCategory(jComboBox1.getSelectedItem().toString());
        wordDialog.setWord(w);
        wordDialog.setVisible(true);
        
        if (wordDialog.isCommited()) {

            WordDto wOld;

            if ((wOld = dict.findDuplicity(w)) != null) {
                wordExistsDialog.showDialog(w, wOld);
                if (!wordExistsDialog.isCancelled())
                    play(w);
                return;
            }

            if ((wOld = dict.findDuplicity(w.getCz())) != null) {
                wordExistsDialog.showDialog(w, wOld);
                if (!wordExistsDialog.isCancelled())
                    play(w);
                return;
            }

            if ((wOld = dict.findDuplicity(w.getEn())) != null) {
                wordExistsDialog.showDialog(w, wOld);
                if (!wordExistsDialog.isCancelled())
                    play(w);
                return;
            }

            try {
                dict.addWord(w);
            } catch (DictionaryException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            dict.setCategory(w.getCategory());
            dict.setCurrent(w);
            this.jLabel3.setText(w.getEn());
            
            play(w);
        }
    }
    
    private void deleteWord() {
        // Delete Word
        int dialogResult = JOptionPane.showConfirmDialog(this, "Do you want to delete word: " + dict.getWord().getCz() + "?", "Question", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            dict.deleteCurrentWord();
        }
    }
    
    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        addWord();
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        deleteWord();
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        // Delete Category
        int dialogResult = JOptionPane.showConfirmDialog(this, "Do you want to delete category: " + dict.getCurrentCategory() + "?", "Question", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            try {
                dict.deleteCurrentCategory();
            } catch (DictionaryException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private String getPathToDataDir() {
        String result = dict.getSetup().getDataDir();
        int trimPos = result.lastIndexOf(File.separator);
        
        if (trimPos != -1) {
            result = result.substring(0, trimPos);
        }
        
        return result;
    }
    
    private void newDictionary() {
        // Create New Dictionary
        JFileChooser fileChooser = new JFileChooser(getPathToDataDir());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("New dictionary");
        FileView fv = new CustFileView();
        fileChooser.setFileView(fv);

        int option = fileChooser.showOpenDialog(this);
        if(option == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            
            try {
                saveDirectory();
            } catch (IOException ex) {
                errorDialog.showError("Error: Cannot save dictionary.", ex);
            }
            
            try {
                if (!file.exists()) {
                    file.mkdir();
                }
                loadDirectory(file.getAbsolutePath());
            } catch (IOException ex) {
                errorDialog.showError("Error: Cannot load dictionary.", ex);
            }
        }
    }
    
    private void openDictionary() {
        // Open Dictionary
        JFileChooser fileChooser = new JFileChooser(getPathToDataDir());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Open dictionary");
        FileView fv = new CustFileView();
        fileChooser.setFileView(fv);

        int option = fileChooser.showOpenDialog(this);
        if(option == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            
            try {
                saveDirectory();
            } catch (IOException ex) {
                errorDialog.showError("Error: Cannot save dictionary.", ex);
            }
            
            try {
                loadDirectory(file.getAbsolutePath());
            } catch (IOException ex) {
                errorDialog.showError("Error: Cannot load dictionary.", ex);
            }
        }
    }
    
    private void saveDictionary() {
        // Save Dictionary
        try {
            saveDirectory();
            JOptionPane.showMessageDialog(this, "Dictionary saved.", "Info", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            errorDialog.showError("Error: Cannot save dictionary.", ex);
        }
    }
    
    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        openDictionary();
    }//GEN-LAST:event_jMenuItem12ActionPerformed
    
    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        newDictionary();
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        saveDictionary();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        newDictionary();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        openDictionary();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        saveDictionary();
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        showFindDialog();
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        addWord();
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        editWord();
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        deleteWord();
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        nextAbsolute(0);
    }//GEN-LAST:event_jButton14ActionPerformed

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
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
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
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables
}
