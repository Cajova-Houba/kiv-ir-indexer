package cz.zcu.kiv.nlp.ir.trec.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Main window for GUI.
 */
public class MainWindow extends JFrame {

    public MainWindow() {
        setTitle("KIV/IR Indexer");
        setSize(800,400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        addComponents();
        setVisible(true);
    }

    private void addComponents() {
        add(new IndexManagementPanel(), BorderLayout.EAST);
        add(new SearchPanel(), BorderLayout.CENTER);
    }
}
