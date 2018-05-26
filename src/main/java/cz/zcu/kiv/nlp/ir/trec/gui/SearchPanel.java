package cz.zcu.kiv.nlp.ir.trec.gui;

import cz.zcu.kiv.nlp.ir.trec.data.Result;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for search form and result display.
 */
public class SearchPanel extends JPanel {

    private JTextField queryField;

    private JList<Result> resultList;

    public SearchPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        addComponents();
    }

    private void addComponents() {
        add(createSearchForm(), BorderLayout.NORTH);
        add(createResultDisplay(), BorderLayout.CENTER);
    }

    private JPanel createResultDisplay() {
        JPanel resultDisplay = new JPanel();
        resultList = new JList<>();
        resultDisplay.setLayout(new BoxLayout(resultDisplay, BoxLayout.Y_AXIS));
        resultDisplay.add(new JLabel("Found documents"));
        resultDisplay.add(resultList);
        return resultDisplay;
    }

    private JPanel createSearchForm() {
        JPanel searchForm = new JPanel(new FlowLayout());
        queryField = new JTextField(20);
        searchForm.add(queryField);
        searchForm.add(new JButton("Search"));
        return searchForm;
    }
}
