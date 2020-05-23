package cz.zcu.kiv.nlp.ir.trec.gui;

import cz.zcu.kiv.nlp.ir.trec.Configuration;
import cz.zcu.kiv.nlp.ir.trec.Main;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Panel for search form and result display.
 */
public class SearchPanel extends JPanel {

    private static Logger log = LoggerFactory.getLogger(SearchPanel.class);

    private static String FOUND_DOC_LABEL = "Found documents";

    private JTextField queryField;
    private SpinnerNumberModel topResultsModel;
    private JLabel foundDocumentsCount;

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
        foundDocumentsCount = new JLabel(FOUND_DOC_LABEL);
        resultDisplay.add(foundDocumentsCount);
        resultDisplay.add(scrollPane);
        return resultDisplay;
    }

    private JPanel createSearchForm() {
        JPanel searchForm = new JPanel(new FlowLayout());
        prepareSpinnerModel();
        queryField = new JTextField(20);
        searchForm.add(queryField);
        searchForm.add(new JSpinner(topResultsModel));
        searchForm.add(new JButton(new AbstractAction("Search") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = queryField.getText();
                log.info("Executing query \"{}\"", query);
                if (query.isEmpty()) {
                    log.warn("Query is empty.");
                    showErrorMessage("No query!");
                    return;
                }

                try {
                    log.debug("Performing search.");
                    java.util.List<Result> results = Main.search(query, topResultsModel.getNumber().intValue());
                    log.debug("Done, {} results found.", results.size());

                    updateFoundCount(results.size());
                    resultModel.clearResults();
                    resultModel.addResults(results);
                } catch (Exception ex) {
                    log.error("Unexpected error while executing the query: ", ex);
                    showErrorMessage("Unexpected exception occurred while performing search: "+ex.getMessage());
                }
            }
        }));
        return searchForm;
    }

    private void prepareSpinnerModel() {
        topResultsModel = new SpinnerNumberModel(Configuration.getMinTopKResults(), Configuration.getMinTopKResults(), Configuration.getMaxTopKResults(), 1);
    }

    private void updateFoundCount(int size) {
        foundDocumentsCount.setText(FOUND_DOC_LABEL + "("+size+")");
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
