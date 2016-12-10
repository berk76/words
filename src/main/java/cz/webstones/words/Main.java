/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;

/**
 *
 * @author jaroslav_b
 */
public class Main extends javax.swing.JFrame {
    
    private ArrayList<WordDto> dictionary = new ArrayList<WordDto>();
    private int dictSize;
    private int dictCurrnt;
    private Random rand = new Random();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    private String dictionaryFileName = "C:\\Users\\jaroslav_b\\Documents\\Projekty\\Projects_Private\\Java\\Words\\Data\\Dictionary.txt";
    private String dictionarySeparator = ";";

    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();
        this.setTitle("Words");
        try {
            loadDictionary();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        next();
    }
    
    private void loadDictionary() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        dictionary = new ArrayList<WordDto>();
        
        InputStream is = new FileInputStream(dictionaryFileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

        String line;
        int order = 0;
        while ((is != null) && ((line = reader.readLine()) != null)) {
            String arr[] = line.split(dictionarySeparator);
            if (arr.length < 2)
                continue;
            WordDto w = new WordDto();
            w.setOriginalOrder(order++);
            w.setEn(arr[0]);
            w.setCz(arr[1]);
            if (arr.length == 6) {
                w.setGoodHits(Integer.valueOf(arr[2]));
                try {
                    w.setLastGoodHit(sdf.parse(arr[3]));
                } catch (ParseException ex) {
                    w.setLastGoodHit(null);
                }
                w.setWrongHits(Integer.valueOf(arr[4]));
                try {
                    w.setLastWrongHit(sdf.parse(arr[5]));
                } catch (ParseException ex) {
                    w.setLastWrongHit(null);
                }
            }
            dictionary.add(w);
        }
        reader.close();
        is.close();
        
        this.dictCurrnt = -1;
        this.dictSize = dictionary.size();
        reorder();
    }
    
    private void saveDirectory() throws IOException {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        
        try {
            fos = new FileOutputStream(dictionaryFileName);
            osw = new OutputStreamWriter(fos, "UTF-8");
            bw = new BufferedWriter(osw);
            
            // copy dictionary
            ArrayList<WordDto> d = new ArrayList<WordDto>();
            for (WordDto w: dictionary) {
                d.add(w);
            }
            
            // reorder dictionary for write
            Collections.sort(d, new Comparator<WordDto>() {
                @Override
                public int compare(WordDto a, WordDto b) {
                    return a.getOriginalOrder() < b.getOriginalOrder() ? -1 : (a.getOriginalOrder() > b.getOriginalOrder()) ? 1 : 0;
                }
            });
            
            // write file
            for (WordDto w: d) {
                bw.write(w.getEn() + dictionarySeparator);
                bw.write(w.getCz() + dictionarySeparator);
                bw.write(w.getGoodHits() + dictionarySeparator);
                bw.write(((w.getLastGoodHit() != null) ? sdf.format(w.getLastGoodHit()) : "") + dictionarySeparator);
                bw.write(w.getWrongHits() + dictionarySeparator);
                bw.write(((w.getLastWrongHit() != null) ? sdf.format(w.getLastWrongHit()) : "") + dictionarySeparator);
                bw.newLine();
            }
        } finally {
            bw.close();
            osw.close();
            fos.close();
        }
    }
    
    private void next() {
        if (this.dictSize == 0) {
            return;
        }
        
        this.dictCurrnt++;
        if (this.dictCurrnt >= this.dictSize) {
            this.dictCurrnt = 0;
            reorder();
        }
        this.jLabel1.setText(dictionary.get(this.dictCurrnt).getCz());
        updateStatus();
    }
    
    private void reorder() {
        for (WordDto w: dictionary) {
            w.setOrder(rand.nextInt(1000));
        }
        
        Collections.sort(dictionary, new Comparator<WordDto>() {
            @Override
            public int compare(WordDto a, WordDto b) {
                return a.getOrder() < b.getOrder() ? -1 : (a.getOrder() > b.getOrder()) ? 1 : 0;
            }
        });
    }
    
    private void play() {
        String fName = "Data\\MP3\\" + dictionary.get(this.dictCurrnt).getEn() + ".mp3";
        File f = new File(fName);
        if (f.exists()) {
            AudioFilePlayer.playFile(fName);
        }
    }
    
    private void updateStatus() {
        this.jLabel2.setText(String.valueOf(this.dictCurrnt + 1) + " / " + String.valueOf(dictionary.size()) + " words");
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

        jTextField1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel2.setText("jLabel2");

        jButton1.setText("Good");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Wrong");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Play");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
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
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton3)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dictionary.get(this.dictCurrnt).setGoodHits(dictionary.get(this.dictCurrnt).getGoodHits() + 1);
        dictionary.get(this.dictCurrnt).setLastGoodHit(new Date());
        this.jTextField1.setText("");
        next();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        dictionary.get(this.dictCurrnt).setWrongHits(dictionary.get(this.dictCurrnt).getWrongHits() + 1);
        dictionary.get(this.dictCurrnt).setLastWrongHit(new Date());
        this.jTextField1.setText("");
        next();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        this.jTextField1.setText(dictionary.get(this.dictCurrnt).getEn());
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
