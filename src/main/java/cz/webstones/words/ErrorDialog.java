/*
*       ErrorDialog.java
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;

/**
 *
 * @author jaroslav_b
 */
public class ErrorDialog extends JEscapeableDialog {

    private static final long serialVersionUID = -3226387220038560119L;
    private static final Logger LOGGER = Logger.getLogger(ErrorDialog.class.getName());
    
    private JLabel lblError;
    private JTextArea taTrace;
    
    /**
     * Creates new form ErrorDialog
     */
    public ErrorDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setMinimumSize(getSize());
        this.setLocationRelativeTo(null);
    }
    
    public void showError(String message, Exception ex) {
        this.setTitle(message);
        this.lblError.setText(message);
    
        String result = "";
        try {
            try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
                ex.printStackTrace(pw);
                result = sw.toString();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        this.taTrace.setText(result);
        this.taTrace.setCaretPosition(0);
        
        this.setVisible(true);
    }
    
    private void btnCloseActionPerformed() {
        this.setVisible(false);
    }

    private void initComponents() {
        JScrollPane spTrace;
        JButton btnClose;

        lblError = new JLabel();
        spTrace = new JScrollPane();
        taTrace = new JTextArea();
        btnClose = new JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lblError.setFont(Service.createFont());
        lblError.setText("lblError");

        taTrace.setEditable(false);
        taTrace.setColumns(20);
        taTrace.setRows(5);
        spTrace.setViewportView(taTrace);

        btnClose.setFont(Service.createFont());
        btnClose.setText("Close");
        btnClose.addActionListener(e -> btnCloseActionPerformed());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                        .addComponent(spTrace, GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblError)
                            .addGap(0, 565, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(0, 546, Short.MAX_VALUE)
                            .addComponent(btnClose)))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblError)
                    .addGap(18)
                    .addComponent(spTrace, GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                    .addGap(18)
                    .addComponent(btnClose)
                    .addContainerGap())
        );
        getContentPane().setLayout(layout);

        pack();
    }    
}
