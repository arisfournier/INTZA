package gr.aueb.budgetpm;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Η κεντρική κλάση της εφαρμογής (GUI).
 * Διαχειρίζεται το παράθυρο, τα events και τη ροή δεδομένων.
 */
public class GuiApp extends JFrame {

    private final BudgetYearManager yearManager;
    private Budget currentBudget;
    private int currentYear = 2020;

    private JTextField txtYear;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblStatus;
    private JLabel lblTotal;
    
    private boolean showOnlyMinistries = true;

    public GuiApp() {
        setupLookAndFeel();

        this.yearManager = new BudgetYearManager("GR");

        //Βασικές ρυθμίσεις παραθύρου
        this.setTitle("Πρωθυπουργός για μια μέρα - Dashboard Pro");
        this.setSize(1200, 800);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());

        //Στοιχεια UI
        
        //Header
        JPanel headerPanel = createHeaderPanel();
        this.add(headerPanel, BorderLayout.NORTH);

        //Sidebar Menu
        JPanel sidebarPanel = createSidebarPanel();
        this.add(sidebarPanel, BorderLayout.WEST);

        //Table & Toolbar
        JPanel mainContentPanel = createMainContentPanel();
        this.add(mainContentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(new Color(44, 62, 80));
        
        JLabel title = new JLabel("Σύστημα Διαχείρισης Κρατικού Προϋπολογισμού");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        
        header.add(title);
        return header;
    }

    //Menu
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(236, 240, 241));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidebar.setPreferredSize(new Dimension(240, 0));

        // Πεδίο Έτους
        sidebar.add(createSidebarLabel("Επιλογή Έτους:"));
        txtYear = new JTextField(String.valueOf(currentYear));
        txtYear.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        sidebar.add(txtYear);
        sidebar.add(Box.createVerticalStrut(5));
        
        // Κουμπί Φόρτωσης
        JButton btnLoad = createStyledButton("📥 Φόρτωση Έτους", new Color(52, 152, 219));
        btnLoad.addActionListener(e -> {
            loadData();
        });
        sidebar.add(btnLoad);
        
        // Διαχωριστικό
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(new JSeparator());
        sidebar.add(Box.createVerticalStrut(20));

        // Κουμπιά Γραφημάτων & Εργαλείων
        JButton btnChart = createStyledButton("📊 Γράφημα Έτους", new Color(46, 204, 113));
        btnChart.addActionListener(e -> {
            showChartDialog();
        });
        sidebar.add(btnChart);
        sidebar.add(Box.createVerticalStrut(10));

        JButton btnCompareChart = createStyledButton("📉 Συγκριτικό Γράφημα", new Color(230, 126, 34));
        btnCompareChart.addActionListener(e -> {
            showComparisonChartDialog();
        });
        sidebar.add(btnCompareChart);
        sidebar.add(Box.createVerticalStrut(10));

        JButton btnCompareTable = createStyledButton("⚖️ Πίνακας Σύγκρισης", new Color(243, 156, 18));
        btnCompareTable.addActionListener(e -> {
            showCompareDialog();
        });
        sidebar.add(btnCompareTable);
        sidebar.add(Box.createVerticalStrut(10));
        
        JButton btnScenario = createStyledButton("Νέο Σενάριο", new Color(155, 89, 182));
        btnScenario.addActionListener(e -> {
            showScenarioDialog();
        });
        sidebar.add(btnScenario);
        
        sidebar.add(Box.createVerticalGlue());
        
        JButton btnSave = createStyledButton("💾 Αποθήκευση", new Color(192, 57, 43));
        btnSave.addActionListener(e -> {
            saveData();
        });
        sidebar.add(btnSave);

        return sidebar;
    }

    //Κεντρικό Πάνελ
    private JPanel createMainContentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        //Εργαλεία
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        JButton btnToggle = new JButton("👁️ Εναλλαγή Προβολής (Σύνοψη/Όλα)");
        btnToggle.addActionListener(e -> {
            showOnlyMinistries = !showOnlyMinistries;
            refreshTable();
        });
        
        toolbar.add(btnToggle);
        toolbar.addSeparator();
        toolbar.add(new JLabel(" 💡 Tip: Διπλό κλικ σε Υπουργείο για ανάλυση | Διπλό κλικ σε ποσό για αλλαγή"));
        mainPanel.add(toolbar, BorderLayout.NORTH);

        String[] columnNames = {"HiddenCode", "Υπουργείο", "Ποσό (€)"};
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setAutoCreateRowSorter(true);
        
        TableColumnModel tcm = table.getColumnModel();
        tcm.removeColumn(tcm.getColumn(0));
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

        //Listener για το διπλό κλικ
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick();
                }
            }
        });

        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        //Status bar
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.LIGHT_GRAY);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        lblStatus = new JLabel("Έτοιμο.");
        lblTotal = new JLabel("Σύνολο: 0 €");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        footer.add(lblStatus, BorderLayout.WEST);
        footer.add(lblTotal, BorderLayout.EAST);
        
        mainPanel.add(footer, BorderLayout.SOUTH);

        return mainPanel;
    }

    /**
     *          Λογικη
     * Φορτώνει τα δεδομένα για το επιλεγμένο έτος.
     * Χρησιμοποιεί SwingWorker για να μην "παγώνει" το περιβάλλον.
     */
    private void loadData() {
        try {
            String inputYear = txtYear.getText().trim();
            int year = Integer.parseInt(inputYear);
            currentYear = year;
            
            lblStatus.setText("Φόρτωση δεδομένων...");
            
            new SwingWorker<Budget, Void>() {
                @Override
                protected Budget doInBackground() throws Exception {
                    return yearManager.getOrLoad(currentYear);
                }

                @Override
                protected void done() {
                    try {
                        currentBudget = get();
                        refreshTable();
                        lblStatus.setText("Φορτώθηκε επιτυχώς: " + currentYear);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(GuiApp.this, "Σφάλμα κατά τη φόρτωση: " + e.getMessage());
                        lblStatus.setText("Σφάλμα.");
                    }
                }
            }.execute();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Παρακαλώ εισάγετε έγκυρο έτος.");
        }
    }

    private void refreshTable() {
        if (currentBudget == null) {
            return;
        }

        tableModel.setRowCount(0);
        long grandTotal = 0;

        List<BudgetCategory> categories = currentBudget.getCategories();
        
        //Ταξινόμηση λίστας κατά όνομα
        categories.sort((c1, c2) -> {
            return c1.getName().compareTo(c2.getName());
        });

        for (BudgetCategory c : categories) {
            boolean isMinistry = c.getCode().startsWith("MIN_") || c.getCode().startsWith("OTHER");
            boolean isSubCategory = c.getName().startsWith(" -");

            //Φιλτράρισμα
            if (showOnlyMinistries) {
                if (!isSubCategory) {
                    addCategoryToTable(c);
                    if (isMinistry) {
                        grandTotal += c.getAmount();
                    }
                }
            } else {
                addCategoryToTable(c);
                if (isMinistry) {
                    grandTotal += c.getAmount();
                }
            }
        }
        
        lblTotal.setText("Σύνολο: " + formatMoney(grandTotal));
    }

    private void addCategoryToTable(BudgetCategory c) {
        Object[] rowData = {
            c.getCode(), 
            c.getName(), 
            formatMoney(c.getAmount())
        };
        tableModel.addRow(rowData);
    }

    /**
     * Διαχειρίζεται το διπλό κλικ στον πίνακα.
     * -Υπουργείο - Drill Down.
     * -Υποκατηγορία - Edit Value.
     */
    private void handleDoubleClick() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(viewRow);

        String code = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        long amount = currentBudget.getFinalValue(code);

        if (code.startsWith("MIN_")) {
            showMinistryDetails(code, name);
        } else {
            askToEditValue(code, name, amount);
        }
    }

    //Παραθυρο υποκατηγοριων
    private void showMinistryDetails(String ministryCode, String ministryName) {
        String filterKey = getFilterKey(ministryCode);
        
        JDialog dialog = new JDialog(this, "Ανάλυση: " + ministryName, true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        String[] cols = {"Κατηγορία", "Ποσό (€)"};
        DefaultTableModel detailModel = new DefaultTableModel(cols, 0);

        long sum = 0;
        for (BudgetCategory c : currentBudget.getCategories()) {
            if (c.getCode().contains(filterKey) && !c.getCode().equals(ministryCode)) {
                detailModel.addRow(new Object[]{c.getName(), formatMoney(c.getAmount())});
                sum += c.getAmount();
            }
        }
        
        detailModel.addRow(new Object[]{"---------------", "---------------"});
        detailModel.addRow(new Object[]{"ΣΥΝΟΛΟ ΥΠΟΚΑΤΗΓΟΡΙΩΝ", formatMoney(sum)});

        JTable detailTable = new JTable(detailModel);
        detailTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        detailTable.setRowHeight(25);
        
        dialog.add(new JScrollPane(detailTable));
        dialog.setVisible(true);
    }

    private String getFilterKey(String minCode) {
        if (minCode.contains("HEALTH")) return "HEALTH";
        if (minCode.contains("EDUCATION")) return "EDU";
        if (minCode.contains("DEFENSE")) return "DEF";
        if (minCode.contains("PROTECTION")) return "PROT";
        if (minCode.contains("FOREIGN")) return "FOR";
        if (minCode.contains("INTERIOR")) return "INT";
        if (minCode.contains("LABOR")) return "LABOR";
        return "OTHER";
    }

    private void askToEditValue(String code, String name, long currentVal) {
        String input = JOptionPane.showInputDialog(this, "Επεξεργασία ποσού για:\n" + name, currentVal);
        
        if (input != null) {
            try {
                String cleanInput = input.replace(".", "").replace(",", "");
                long newVal = Long.parseLong(cleanInput);
                
                currentBudget.setUserValue(code, newVal);
                refreshTable();
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Παρακαλώ δώστε έγκυρο ακέραιο αριθμό.");
            }
        }
    }

    //Αποθήκευση δεδομένων σε αρχείο JSON.
    private void saveData() {
        if (currentBudget == null) {
            JOptionPane.showMessageDialog(this, "Δεν υπάρχουν δεδομένα για αποθήκευση.");
            return;
        }
        
        try {
            Path path = Paths.get("data", "all-budgets.json");

            if (!java.nio.file.Files.exists(path.getParent())) {
                java.nio.file.Files.createDirectories(path.getParent());
            }
            
            yearManager.saveAll(path);
            JOptionPane.showMessageDialog(this, "Η αποθήκευση ολοκληρώθηκε επιτυχώς!");
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Σφάλμα αποθήκευσης: " + e.getMessage());
        }
    }

    //Παραθυρα διαλογων

    private void showChartDialog() {
        if (currentBudget == null) {
            JOptionPane.showMessageDialog(this, "Φορτώστε πρώτα δεδομένα.");
            return;
        }
        
        JDialog d = new JDialog(this, "Γράφημα " + currentYear, true);
        d.setSize(800, 600);
        d.setLocationRelativeTo(this);
        
        List<BudgetCategory> data = new ArrayList<>();
        for (BudgetCategory c : currentBudget.getCategories()) {
            if (c.getCode().startsWith("MIN_") || c.getCode().startsWith("OTHER")) {
                data.add(c);
            }
        }
        
        GuiCharts.BarChartPanel chartPanel = new GuiCharts.BarChartPanel(data);
        d.add(new JScrollPane(chartPanel));
        d.setVisible(true);
    }

    private void showComparisonChartDialog() {
        if (currentBudget == null) {
            return;
        }
        
        String input = JOptionPane.showInputDialog(this, "Σύγκριση με ποιο έτος;");
        if (input == null) {
            return;
        }
        
        try {
            int targetYear = Integer.parseInt(input);
            Budget b2 = yearManager.getOrLoad(targetYear);
            
            JDialog d = new JDialog(this, "Συγκριτικό: " + currentYear + " vs " + targetYear, true);
            d.setSize(900, 600);
            d.setLocationRelativeTo(this);

            List<String> labels = new ArrayList<>();
            List<Long> v1 = new ArrayList<>();
            List<Long> v2 = new ArrayList<>();

            for (BudgetCategory c : currentBudget.getCategories()) {
                if (c.getCode().startsWith("MIN_") || c.getCode().startsWith("OTHER")) {
                    labels.add(c.getName());
                    v1.add(c.getAmount());
                    v2.add(b2.getFinalValue(c.getCode()));
                }
            }
            
            GuiCharts.ComparisonChartPanel chart = new GuiCharts.ComparisonChartPanel(labels, v1, v2, currentYear, targetYear);
            d.add(new JScrollPane(chart));
            d.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Σφάλμα: " + e.getMessage());
        }
    }

    private void showCompareDialog() {
        if (currentBudget == null) return;
        
        String input = JOptionPane.showInputDialog(this, "Σύγκριση με ποιο έτος;");
        if (input == null) return;
        
        try {
            int targetYear = Integer.parseInt(input);
            Budget b2 = yearManager.getOrLoad(targetYear);
            
            Map<String, BudgetComparator.ComparisonResult> results = BudgetComparator.compare(currentBudget, b2);
            
            String[] cols = {"Κατηγορία", String.valueOf(currentYear), String.valueOf(targetYear), "Διαφορά"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            
            for (String key : results.keySet()) {
                if (key.startsWith("MIN_") || key.equals("GC.XPN.TOTL.GD.ZS")) {
                     String name = key;
                     for (BudgetCategory c : currentBudget.getCategories()) {
                         if (c.getCode().equals(key)) {
                             name = c.getName();
                             break;
                         }
                     }
                     
                     BudgetComparator.ComparisonResult res = results.get(key);
                     String diffSign = (res.diff > 0) ? "+" : "";
                     
                     Object[] row = {
                         name, 
                         formatMoney(res.oldValue), 
                         formatMoney(res.newValue), 
                         diffSign + formatMoney(res.diff)
                     };
                     model.addRow(row);
                }
            }
            
            JTable t = new JTable(model);
            t.setFont(new Font("SansSerif", Font.PLAIN, 14));
            t.setRowHeight(25);
            
            JDialog d = new JDialog(this, "Πίνακας Σύγκρισης", true);
            d.setSize(900, 500);
            d.setLocationRelativeTo(this);
            d.add(new JScrollPane(t));
            d.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Σφάλμα: " + e.getMessage());
        }
    }

    private void showScenarioDialog() {
        if (currentBudget == null) return;
        
        String input = JOptionPane.showInputDialog(this, "Ποσοστό αλλαγής (π.χ. 5):");
        if (input == null) return;
        
        try {
            double percent = Double.parseDouble(input);
            BudgetScenario scenario = new BudgetScenario("Scenario 1", currentBudget, percent);
            Map<String, Long> vals = scenario.getAllCategoryValues();
            
            String[] cols = {"Κατηγορία", "Τρέχον", "Σενάριο (" + percent + "%)"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            
            for (BudgetCategory c : currentBudget.getCategories()) {
                if (c.getCode().startsWith("MIN_")) {
                    long scenarioVal = vals.getOrDefault(c.getName().toUpperCase(), 0L);
                    
                    Object[] row = {
                        c.getName(), 
                        formatMoney(c.getAmount()), 
                        formatMoney(scenarioVal)
                    };
                    model.addRow(row);
                }
            }
            
            JTable t = new JTable(model);
            t.setFont(new Font("SansSerif", Font.PLAIN, 14));
            t.setRowHeight(25);
            
            JDialog d = new JDialog(this, "Αποτέλεσμα Σεναρίου", true);
            d.setSize(600, 500);
            d.setLocationRelativeTo(this);
            d.add(new JScrollPane(t));
            d.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Μη έγκυρος αριθμός.");
        }
    }

    //Βοηθητικες κλασεις

    private String formatMoney(long amount) {
        return NumberFormat.getInstance(Locale.GERMANY).format(amount) + " €";
    }

    private void setupLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            //Αγνοηση σφαλματος
        }
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return btn;
    }

    private JLabel createSidebarLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GuiApp().setVisible(true);
        });
    }
}