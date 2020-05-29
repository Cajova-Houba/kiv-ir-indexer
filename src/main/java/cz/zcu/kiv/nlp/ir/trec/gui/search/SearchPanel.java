package cz.zcu.kiv.nlp.ir.trec.gui.search;

import cz.zcu.kiv.nlp.ir.trec.Configuration;
import cz.zcu.kiv.nlp.ir.trec.core.SearchMode;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.gui.AbstractGUIPanel;
import cz.zcu.kiv.nlp.ir.trec.gui.MainWindow;
import cz.zcu.kiv.nlp.ir.trec.gui.search.actions.SearchIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for search form and result display.
 */
public class SearchPanel extends AbstractGUIPanel {

    private static Logger log = LoggerFactory.getLogger(SearchPanel.class);

    private static String FOUND_DOC_LABEL = "Found documents";

    private JTextField queryField;
    private SpinnerNumberModel topResultsModel;
    private JLabel foundDocumentsCount;
    private JProgressBar progressBar;
    private JButton searchButton;

    private ComboBoxModel<SearchMode> searchModeSelection;

    private ResultTableDataModel resultModel;

    public SearchPanel(MainWindow mainWindow) {
        super(new BorderLayout(), mainWindow);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        addComponents();
    }

    private void addComponents() {
        add(createProgressBar(), BorderLayout.SOUTH);
        add(createSearchForm(), BorderLayout.NORTH);
        add(createResultDisplay(), BorderLayout.CENTER);
    }

    private Component createProgressBar() {
        progressBar = new JProgressBar(0,Configuration.getMaxProgress());
        resetProgressBar();
        return progressBar;
    }

    private void resetProgressBar() {
        progressBar.setValue(0);
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
        searchForm.add(createSearchModeSelection());
        searchForm.add(queryField);
        searchForm.add(new JSpinner(topResultsModel));
        searchButton = new JButton(new SearchIndex("Search", progressBar, this) {
            @Override
            public void onBeforeSearch() {
                resetProgressBar();
                mainWindow.disableButtons();
            }

            @Override
            public void onError(String message) {
                showErrorMessage(message);
                mainWindow.enableButtons();
            }

            @Override
            public void onSearchFinished(List<Result> results, int totalDocumentCount) {
                updateFoundCount(results.size(), totalDocumentCount);
                resultModel.clearResults();
                resultModel.addResults(results);
                mainWindow.enableButtons();
            }
        });
        searchForm.add(searchButton);
        return searchForm;
    }

    private Component createSearchModeSelection() {
        searchModeSelection = new DefaultComboBoxModel<>(SearchMode.values());
        searchModeSelection.setSelectedItem(SearchMode.BOOLEAN);
        JComboBox<SearchMode> comboBox = new JComboBox<>(searchModeSelection);
        comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value.name));
        return comboBox;
    }

    private void prepareSpinnerModel() {
        topResultsModel = new SpinnerNumberModel(Configuration.getMinTopKResults(), Configuration.getMinTopKResults(), Configuration.getMaxTopKResults(), 1);
        topResultsModel.setValue(10);
    }

    private void updateFoundCount(int documentCount, int totalDocumentCount) {
        foundDocumentsCount.setText(String.format("%s (%d/%d)", FOUND_DOC_LABEL, documentCount, totalDocumentCount));
    }

    /**
     * Opens error dialog with error message and OK button.
     * @param errorMsg Error message to be displayed.
     */
    protected void showErrorMessage(String errorMsg) {
        JOptionPane.showMessageDialog(SearchPanel.this.getParent(), errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void enableButtons() {
        searchButton.setEnabled(true);
    }

    public void disableButtons() {
        searchButton.setEnabled(false);
    }

    public String getQuery() {
        return queryField.getText();
    }

    public int getTopK() {
        return topResultsModel.getNumber().intValue();
    }

    public SearchMode getSearchMode() {
        return (SearchMode) searchModeSelection.getSelectedItem();
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
