/*
*       Main.java
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
import cz.webstones.words.mp3.AudioFilePlayer;
import cz.webstones.words.dictionary.IObserver;
import cz.webstones.words.dictionary.WordDto;
import cz.webstones.words.dictionary.DictionaryException;
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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileView;
import cz.webstones.words.dictionary.IDictionary;
import java.awt.Font;
import java.nio.file.Files;

/**
 *
 * @author jaroslav_b
 */
public class Main extends javax.swing.JFrame implements IObserver {
    
    private static final long serialVersionUID = 5213584438445872718L;
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    private JLabel lblNativeWordValue;
    private JLabel lblForeignWordValue;
    private JLabel lblStatus;
    private JTextField txfTryToWrite;
    private JButton btnGood;
    private JButton btnToolAdd;
    private JButton btnToolEdit;
    private JButton btnToolDelete;
    private JButton btnRewind;
    private JButton btnWrong;
    private JButton btnShowAndPlay;
    private JButton btnBack;
    private JButton btnForward;
    private JButton btnToolNew;
    private JButton btnToolOpen;
    private JButton btnToolSave;
    private JButton btnToolSearch;
    private JComboBox<String> cbbCategory;

    private IDictionary dict;
    private AddCategoryDialog addCatDialog;
    private RenameCategoryDialog renameCatDialog;
    private WordDialog wordDialog;
    private AboutDialog aboutDialog;
    private SearchDialog findDialog;
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
        initComponents();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        
        dict = new DictionaryImpl();
        addCatDialog = new AddCategoryDialog(this, true);
        renameCatDialog = new RenameCategoryDialog(this, true, dict);
        wordDialog = new WordDialog(this, true, addCatDialog, dict);
        aboutDialog = new AboutDialog(this, true, dict);
        findDialog = new SearchDialog(this, false, dict);
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

            case WORD_ADDED:
            case CUR_WORD_CHANGED:
            case CUR_WORD_DELETED:
                nextRelative(0);
                break;

            case CUR_CATEGORY_CHANGED:
                if (!dict.getCurrentCategory().equals(cbbCategory.getSelectedItem().toString())) {
                    cbbCategory.setSelectedItem(dict.getCurrentCategory());
                }
                break;

            case CATEGORY_LIST_CHANGED:
                disableCategotyChange = true;
                updateCategoryCombo();
                disableCategotyChange = false;
                break;

            case DICTIONARY_LOADED:
                try {
                    this.setTitleText(Service.VERSION, dict.getDictionaryName(), dict.getLanguage());
                } catch (IOException ex) {
                    errorDialog.showError("Error: Cannot get language.", ex);
                }
                break;
            default:
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
                    lblStatus.setIcon(loadingIcon);
                    lblStatus.setText("Downloading ...");

                    try {
                        Mp3Creator.createMp3(wordToPlay.getEn(), dict.getSetup().getLanguage(), wordToPlay.getMp3FilenameEn(dict.getSetup().getFullMp3Path()));
                    } catch (Mp3CreatorException ex) {
                        errorDialog.showError("Error: Cannot download pronunciation.", ex);
                        mp3DownloadConfirmed = false;
                    }

                    disableControls(false);
                    disableToolbarControls(false);
                    lblStatus.setIcon(null);
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
        int n = cbbCategory.getItemCount();
        for (int i = 1; i < n; i++) {
            cbbCategory.removeItemAt(n - i);
        }

        for (String s : dict.getCategoryList()) {
            cbbCategory.addItem(s);
            if (cbbCategory.getFont().canDisplayUpTo(s) != -1) {
                Font f = findFont(s, cbbCategory.getFont());
                cbbCategory.setFont(f);
            }
        }
        cbbCategory.setSelectedItem(dict.getCurrentCategory());
    }

    private void loadDirectory(String dictPath) throws IOException {
        Setup setup = Service.getSetup(true, dictPath);
        if (!Service.validateLang(setup.getLanguage())) {
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
            this.lblNativeWordValue.setText("<category is empty>");
            this.lblForeignWordValue.setText("");
            this.lblStatus.setText("0 / 0 words");
            disableControls(true);
            cbbCategory.setEnabled(true);
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

        this.lblNativeWordValue.setText(w.getCz());
        if (findDialog.isShowing()) {
            this.lblForeignWordValue.setText(w.getEn());
        } else {
            this.lblForeignWordValue.setText("");
        }
        this.txfTryToWrite.setText("");
        this.txfTryToWrite.grabFocus();

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
            lblStatus.setText(String.format("%d / %s words", dict.getCurrnet() + 1, dict.size()));
        }
    }

    private void disableControls(boolean b) {
        controlsEnabled = !b;
        btnGood.setEnabled(!b);
        btnWrong.setEnabled(!b);
        btnShowAndPlay.setEnabled(!b);
        btnBack.setEnabled(!b);
        btnForward.setEnabled(!b);
        btnRewind.setEnabled(!b);
        cbbCategory.setEnabled(!b);
        revalidate();
    }
    
    private void disableToolbarControls(boolean b) {
        btnToolNew.setEnabled(!b);
        btnToolOpen.setEnabled(!b);
        btnToolSave.setEnabled(!b);
        btnToolSearch.setEnabled(!b);
        btnToolAdd.setEnabled(!b);
        btnToolEdit.setEnabled(!b);
        btnToolDelete.setEnabled(!b);
    }

    private void disableGoodWrong(boolean b) {
        btnGood.setEnabled(!b);
        btnWrong.setEnabled(!b);
    }

    private boolean compareTexts() {
        if (txfTryToWrite.getText().trim().isEmpty()) {
            return true;
        }
        if (txfTryToWrite.getText().trim().equals(lblForeignWordValue.getText().trim())) {
            return true;
        }
        JOptionPane.showMessageDialog(this, "Texts don't match. Correct it or delete it.");
        return false;
    }
    
    private void formWindowClosing() {
        try {
            saveDirectory();
            Service.saveHistory(dict.getSetup().getDataDir());
        } catch (IOException ex) {
            errorDialog.showError("Error: Cannot save dictionary.", ex);
        } finally {
            onFinish();
        }        
    }

    private void editWord() {
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
        WordDto w = new WordDto();
        w.setCategory(cbbCategory.getSelectedItem().toString());
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
            this.lblForeignWordValue.setText(w.getEn());
            
            play(w);
        }
    }
    
    private void deleteWord() {
        int dialogResult = JOptionPane.showConfirmDialog(this, "Do you want to delete word: " + dict.getWord().getCz() + "?", "Question", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            dict.deleteCurrentWord();
        }
    }
    
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
        try {
            saveDirectory();
            JOptionPane.showMessageDialog(this, "Dictionary saved.", "Info", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            errorDialog.showError("Error: Cannot save dictionary.", ex);
        }
    }

    private void initComponents() {
        
        JLabel lblNativeWord;
        JLabel lblForeignWord;
        JLabel lblTryToWrite;
        JLabel lblCategory;
        JMenu menWord;
        JMenu menCategory;
        JMenu menDictionary;
        JMenu menHelp;
        JMenuBar mbMenu;
        JMenuItem meiWordSearch;
        JMenuItem meiNew;
        JMenuItem meiOpen;
        JMenuItem meiSave;
        JMenuItem meiWordEdit;
        JMenuItem meiCatAdd;
        JMenuItem meiCatRename;
        JMenuItem meiAbout;
        JMenuItem meiWordAdd;
        JMenuItem meiWordDelete;
        JMenuItem meiCatDelete;
        JPanel jPanel1;
        JToolBar jToolBar1;

        jPanel1 = new JPanel();
        lblNativeWordValue = new JLabel();
        txfTryToWrite = new JTextField();
        lblStatus = new JLabel();
        btnGood = new JButton();
        btnWrong = new JButton();
        btnShowAndPlay = new JButton();
        cbbCategory = new JComboBox<>();
        lblForeignWordValue = new JLabel();
        btnBack = new JButton();
        btnForward = new JButton();
        lblNativeWord = new JLabel();
        lblForeignWord = new JLabel();
        lblTryToWrite = new JLabel();
        lblCategory = new JLabel();
        btnRewind = new JButton();
        jToolBar1 = new JToolBar();
        btnToolNew = new JButton();
        btnToolOpen = new JButton();
        btnToolSave = new JButton();
        btnToolSearch = new JButton();
        btnToolAdd = new JButton();
        btnToolEdit = new JButton();
        btnToolDelete = new JButton();
        mbMenu = new JMenuBar();
        menDictionary = new JMenu();
        meiNew = new JMenuItem();
        meiOpen = new JMenuItem();
        meiSave = new JMenuItem();
        menWord = new JMenu();
        meiWordSearch = new JMenuItem();
        meiWordAdd = new JMenuItem();
        meiWordEdit = new JMenuItem();
        meiWordDelete = new JMenuItem();
        menCategory = new JMenu();
        meiCatAdd = new JMenuItem();
        meiCatRename = new JMenuItem();
        meiCatDelete = new JMenuItem();
        menHelp = new JMenu();
        meiAbout = new JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                // no action required
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing();
            }
        });
        setTitleText(Service.VERSION, "", null);

        lblNativeWordValue.setFont(Service.createFontLarge());
        lblNativeWordValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNativeWordValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblNativeWordValue.setText("");
        lblNativeWordValue.addPropertyChangeListener(new LabelFontChangeListener(lblNativeWordValue));

        txfTryToWrite.setFont(Service.createFontLarge());
        txfTryToWrite.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txfTryToWrite.setText("");
        txfTryToWrite.getDocument().addDocumentListener(new TextFieldFontChangeListener(txfTryToWrite));

        lblStatus.setFont(Service.createFont());
        lblStatus.setText("");

        btnGood.setFont(Service.createFont());
        btnGood.setForeground(new java.awt.Color(51, 153, 0));
        btnGood.setText("Good");
        btnGood.addActionListener(e -> btnGoodActionPerformed());

        btnWrong.setFont(Service.createFont());
        btnWrong.setForeground(new java.awt.Color(204, 0, 51));
        btnWrong.setText("Wrong");
        btnWrong.addActionListener(e -> btnWrongActionPerformed());

        btnShowAndPlay.setFont(Service.createFont());
        btnShowAndPlay.setText("Show & Play");
        btnShowAndPlay.addActionListener(e -> btnShowAndPlayActionPerformed());

        cbbCategory.setFont(Service.createFont());
        cbbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All" }));
        cbbCategory.addActionListener(e -> cbbActionPerformed());

        lblForeignWordValue.setFont(Service.createFontLarge());
        lblForeignWordValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblForeignWordValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblForeignWordValue.setText("");
        lblForeignWordValue.addPropertyChangeListener(new LabelFontChangeListener(lblForeignWordValue));

        btnBack.setToolTipText("Back");
        btnBack.setText("<");
        btnBack.addActionListener(e -> btnBackActionPerformed());

        btnForward.setToolTipText("Next");
        btnForward.setText(">");
        btnForward.addActionListener(e -> btnForwardActionPerformed());

        lblNativeWord.setText("Native word");

        lblForeignWord.setText("Foreign word");

        lblTryToWrite.setText("Try to write it down");

        lblCategory.setText("Choose category");

        btnRewind.setText("|<");
        btnRewind.setToolTipText("Rewind");
        btnRewind.addActionListener(e -> btnRewindActionPerformed());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNativeWordValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txfTryToWrite, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblForeignWordValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnShowAndPlay)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnGood)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnWrong)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                        .addComponent(btnRewind)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBack)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnForward))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNativeWord)
                            .addComponent(lblForeignWord)
                            .addComponent(lblTryToWrite)
                            .addComponent(lblCategory)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(cbbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblStatus)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(lblNativeWord)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblNativeWordValue, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblForeignWord)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblForeignWordValue, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTryToWrite)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txfTryToWrite, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCategory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnShowAndPlay, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGood, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnWrong, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnBack)
                    .addComponent(btnForward)
                    .addComponent(btnRewind))
                .addContainerGap())
        );

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        btnToolNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/page.png")));
        btnToolNew.setToolTipText("New dictionary");
        btnToolNew.setFocusable(false);
        btnToolNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnToolNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnToolNew.addActionListener(e -> btnToolNewActionPerformed());
        jToolBar1.add(btnToolNew);

        btnToolOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/folder.png")));
        btnToolOpen.setToolTipText("Open dictionary");
        btnToolOpen.setFocusable(false);
        btnToolOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnToolOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnToolOpen.addActionListener(e -> btnToolOpenActionPerformed());
        jToolBar1.add(btnToolOpen);

        btnToolSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disk.png")));
        btnToolSave.setToolTipText("Save dictionary");
        btnToolSave.setFocusable(false);
        btnToolSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnToolSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnToolSave.addActionListener(e -> btnToolSaveActionPerformed());
        jToolBar1.add(btnToolSave);

        btnToolSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/find.png")));
        btnToolSearch.setToolTipText("Find word");
        btnToolSearch.setFocusable(false);
        btnToolSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnToolSearch.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnToolSearch.addActionListener(e -> btnToolSearchActionPerformed());
        jToolBar1.add(btnToolSearch);

        btnToolAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/application_add.png")));
        btnToolAdd.setToolTipText("Add word");
        btnToolAdd.setFocusable(false);
        btnToolAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnToolAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnToolAdd.addActionListener(e -> btnToolAddActionPerformed());
        jToolBar1.add(btnToolAdd);

        btnToolEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/application_edit.png")));
        btnToolEdit.setToolTipText("Edit word");
        btnToolEdit.setFocusable(false);
        btnToolEdit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnToolEdit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnToolEdit.addActionListener(e -> btnToolEditActionPerformed());
        jToolBar1.add(btnToolEdit);

        btnToolDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/application_delete.png")));
        btnToolDelete.setToolTipText("Delete word");
        btnToolDelete.setFocusable(false);
        btnToolDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnToolDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnToolDelete.addActionListener(e -> btnToolDeleteActionPerformed());
        jToolBar1.add(btnToolDelete);

        mbMenu.setFont(Service.createFont());

        menDictionary.setText("Dictionary");
        menDictionary.setFont(Service.createFont());

        meiNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        meiNew.setFont(Service.createFont());
        meiNew.setText("New...");
        meiNew.addActionListener(e -> meiNewActionPerformed());
        menDictionary.add(meiNew);

        meiOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        meiOpen.setFont(Service.createFont());
        meiOpen.setText("Open...");
        meiOpen.addActionListener(e -> meiOpenActionPerformed());
        menDictionary.add(meiOpen);

        meiSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        meiSave.setFont(Service.createFont());
        meiSave.setText("Save");
        meiSave.addActionListener(e -> meiSaveActionPerformed());
        menDictionary.add(meiSave);

        mbMenu.add(menDictionary);

        menWord.setText("Word");
        menWord.setFont(Service.createFont());

        meiWordSearch.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        meiWordSearch.setFont(Service.createFont());
        meiWordSearch.setText("Find...");
        meiWordSearch.addActionListener(e -> meiWordSearchActionPerformed());
        menWord.add(meiWordSearch);

        meiWordAdd.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        meiWordAdd.setFont(Service.createFont());
        meiWordAdd.setText("Add...");
        meiWordAdd.addActionListener(e -> meiWordAddActionPerformed());
        menWord.add(meiWordAdd);

        meiWordEdit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        meiWordEdit.setFont(Service.createFont());
        meiWordEdit.setText("Edit...");
        meiWordEdit.addActionListener(e -> meiWordEditActionPerformed());
        menWord.add(meiWordEdit);

        meiWordDelete.setFont(Service.createFont());
        meiWordDelete.setText("Delete");
        meiWordDelete.addActionListener(e -> meiWordDeleteActionPerformed());
        menWord.add(meiWordDelete);

        mbMenu.add(menWord);

        menCategory.setText("Category");
        menCategory.setFont(Service.createFont());

        meiCatAdd.setFont(Service.createFont());
        meiCatAdd.setText("Add...");
        meiCatAdd.addActionListener(e -> meiCatAddActionPerformed());
        menCategory.add(meiCatAdd);

        meiCatRename.setFont(Service.createFont());
        meiCatRename.setText("Rename...");
        meiCatRename.addActionListener(e -> meiCatRenameActionPerformed());
        menCategory.add(meiCatRename);

        meiCatDelete.setFont(Service.createFont());
        meiCatDelete.setText("Delete");
        meiCatDelete.addActionListener(e -> meiCatDeleteActionPerformed());
        menCategory.add(meiCatDelete);

        mbMenu.add(menCategory);

        menHelp.setText("Help");
        menHelp.setFont(Service.createFont());

        meiAbout.setFont(Service.createFont());
        meiAbout.setText("About...");
        meiAbout.addActionListener(e -> meiAboutActionPerformed());
        menHelp.add(meiAbout);

        mbMenu.add(menHelp);

        setJMenuBar(mbMenu);

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
    }

    private void btnGoodActionPerformed() {
        if (compareTexts()) {
            dict.getWord().incGoodHits();
            dict.getWord().setLastGoodHit(new Date());
            nextRelative(1);
        }
    }

    private void btnWrongActionPerformed() {
        if (compareTexts()) {
            dict.getWord().incWrongHits();
            dict.getWord().setLastWrongHit(new Date());
            nextRelative(1);
        }
    }

    private void btnShowAndPlayActionPerformed() {
        this.lblForeignWordValue.setText(dict.getWord().getEn());
        play(dict.getWord());
    }

    private void cbbActionPerformed() {
        if (!disableCategotyChange) {
            if (!dict.getCurrentCategory().equals(cbbCategory.getSelectedItem().toString())) {
                dict.setCategory(cbbCategory.getSelectedItem().toString());
            }
        }
    }

    private void btnBackActionPerformed() {
        nextRelative(-1);
    }

    private void btnForwardActionPerformed() {
        nextRelative(1);
    }

    private void meiWordSearchActionPerformed() {
        showFindDialog();
    }

    private void meiAboutActionPerformed() {
        aboutDialog.setVisible(true);
    }

    private void meiWordEditActionPerformed() {
        editWord();
    }

    private void meiCatAddActionPerformed() {
        addCatDialog.setCategoryText("");
        addCatDialog.setVisible(true);
        if (addCatDialog.isCommited()) {
            addCategory(addCatDialog.getCategoryText());
        }
    }

    private void meiCatRenameActionPerformed() {
        renameCatDialog.setNewCategoryText("");
        renameCatDialog.setVisible(true);
        if (renameCatDialog.isCommited()) {
            renameCategory(renameCatDialog.getOldCategoryText(), renameCatDialog.getNewCategoryText());
        }
    }
    
    private void meiWordAddActionPerformed() {
        addWord();
    }

    private void meiWordDeleteActionPerformed() {
        deleteWord();
    }

    private void meiCatDeleteActionPerformed() {
        int dialogResult = JOptionPane.showConfirmDialog(this, "Do you want to delete category: " + dict.getCurrentCategory() + "?", "Question", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            try {
                dict.deleteCurrentCategory();
            } catch (DictionaryException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void meiOpenActionPerformed() {
        openDictionary();
    }
    
    private void meiNewActionPerformed() {
        newDictionary();
    }

    private void meiSaveActionPerformed() {
        saveDictionary();
    }

    private void btnToolNewActionPerformed() {
        newDictionary();
    }

    private void btnToolOpenActionPerformed() {
        openDictionary();
    }

    private void btnToolSaveActionPerformed() {
        saveDictionary();
    }

    private void btnToolSearchActionPerformed() {
        showFindDialog();
    }

    private void btnToolAddActionPerformed() {
        addWord();
    }

    private void btnToolEditActionPerformed() {
        editWord();
    }

    private void btnToolDeleteActionPerformed() {
        deleteWord();
    }

    private void btnRewindActionPerformed() {
        nextAbsolute(0);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
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
}
