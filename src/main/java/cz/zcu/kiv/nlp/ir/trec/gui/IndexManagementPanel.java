package cz.zcu.kiv.nlp.ir.trec.gui;

import javax.swing.*;

/**
 * Panel for managing search index.
 */
public class IndexManagementPanel extends JPanel {

    private JLabel indexedCodumentsCountDisplay;

    public IndexManagementPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Index management"),
                BorderFactory.createEmptyBorder(10,10,10,10)
                ));

        addComponents();
    }

    private void addComponents() {
        add(createIndexedDocumentsNumDisplay());
        add(createSaveIndexBtn());
        add(createLoadIndexBtn());
    }

    private JPanel createIndexedDocumentsNumDisplay() {
        JPanel panel = new JPanel();
        panel.setAlignmentX(0.5f);
        panel.setSize(50,20);
        indexedCodumentsCountDisplay = new JLabel("0");
        panel.add(new JLabel("# of indexed documents: "));
        panel.add(indexedCodumentsCountDisplay);
        return panel;
    }

    private JButton createSaveIndexBtn() {
        JButton btn = new JButton("Save index");
        btn.setAlignmentX(0.5f);

        return btn;
    }

    private JButton createLoadIndexBtn() {
        JButton btn = new JButton("Load index");
        btn.setAlignmentX(0.5f);

        return btn;
    }

    public void setIndexedDocumentsCount(int count) {
        indexedCodumentsCountDisplay.setText(Integer.toString(count));
    }
}
