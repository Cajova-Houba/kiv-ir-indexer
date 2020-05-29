package cz.zcu.kiv.nlp.ir.trec.gui;

import cz.zcu.kiv.nlp.ir.trec.gui.indexmgmt.IndexManagementPanel;
import cz.zcu.kiv.nlp.ir.trec.gui.search.SearchPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Main window for GUI.
 */
public class MainWindow extends JFrame {

    private AbstractGUIPanel indexManagementPanel, searchPanel;

    public MainWindow() {
        setTitle("KIV/IR Indexer");
        setSize(950,800);
        setMinimumSize(new Dimension(800,800));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(true);

        addComponents();
        setVisible(true);
    }

    private void addComponents() {
        indexManagementPanel = new IndexManagementPanel(this);
        searchPanel = new SearchPanel(this);
        add(indexManagementPanel, BorderLayout.EAST);
        add(searchPanel, BorderLayout.CENTER);
    }

    public void enableButtons() {
        indexManagementPanel.enableButtons();
        searchPanel.enableButtons();
    }

    public void disableButtons() {
        indexManagementPanel.disableButtons();
        searchPanel.disableButtons();
    }
}
