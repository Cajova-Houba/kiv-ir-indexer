package cz.zcu.kiv.nlp.ir.trec.gui;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractGUIPanel extends JPanel {

    protected MainWindow mainWindow;

    public AbstractGUIPanel(LayoutManager2 layout, MainWindow mainWindow) {
        super(layout);
        this.mainWindow = mainWindow;
    }

    public AbstractGUIPanel(MainWindow mainWindow) {
        super();
        this.mainWindow = mainWindow;
    }

    public abstract void enableButtons();
    public abstract void disableButtons();
}
