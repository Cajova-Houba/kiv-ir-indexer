package cz.zcu.kiv.nlp.ir.trec.gui;

import cz.zcu.kiv.nlp.ir.trec.Main;
import cz.zcu.kiv.nlp.ir.trec.data.Result;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Panel for search form and result display.
 */
public class SearchPanel extends JPanel {

    private JTextField queryField;

    private ResultTableDataModel resultModel;

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
        resultModel = new ResultTableDataModel();
        JTable resultTable = new JTable(resultModel);
        resultTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(resultTable);
        resultDisplay.setLayout(new BoxLayout(resultDisplay, BoxLayout.Y_AXIS));
        resultDisplay.add(new JLabel("Found documents"));
        resultDisplay.add(scrollPane);
        return resultDisplay;
    }

    private JPanel createSearchForm() {
        JPanel searchForm = new JPanel(new FlowLayout());
        queryField = new JTextField(20);
        searchForm.add(queryField);
        searchForm.add(new JButton(new AbstractAction("Search") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = queryField.getText();
                if (query.isEmpty()) {
                    showErrorMessage("No query!");
                    return;
                }

                try {
                    java.util.List<Result> results = Main.search(query, 10);
                    resultModel.clearResults();
                    resultModel.addResults(results);
                } catch (Exception ex) {
                    showErrorMessage("Unexpected exception occurred while performing search: "+ex.getMessage());
                    return;
                }
            }
        }));
        return searchForm;
    }

    /**
     * Opens error dialog with error message and OK button.
     * @param errorMsg Error message to be displayed.
     */
    protected void showErrorMessage(String errorMsg) {
        JOptionPane.showMessageDialog(SearchPanel.this.getParent(), errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Custom model for result table.
     */
    private class ResultTableDataModel extends AbstractTableModel {

        private java.util.List<Result> results;
        private String[] colNames = new String[] {"Document id", "Score", "Rank"};

        public ResultTableDataModel() {
            results = new ArrayList<>();
        }

        @Override
        public int getRowCount() {
            return results.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Result r = results.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return r.getDocumentID();
                case 1:
                    return r.getScore();
                case 2:
                    return r.getRank();
                default:
                    return "";
            }
        }

        @Override
        public String getColumnName(int column) {
            return colNames[column];
        }

        public void clearResults() {
            results.clear();
            fireTableDataChanged();
        }

        public void addResults(java.util.List<Result> res) {
            results.addAll(res);
            fireTableDataChanged();
        }
    }
}
