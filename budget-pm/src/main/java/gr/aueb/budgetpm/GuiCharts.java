package gr.aueb.budgetpm;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Κλάση διαχείρισης γραφικών στοιχείων (Charts).
 * Περιλαμβάνει τα Custom Panels για την σχεδίαση ράβδων.
 */
public class GuiCharts {

    /**
     * Εσωτερική κλάση για το απλό γράφημα μπάρας ενός έτους.
     */
    public static class BarChartPanel extends JPanel {
        
        private final List<BudgetCategory> data;

        public BarChartPanel(List<BudgetCategory> data) {
            this.data = data;
            // Υπολογισμός ύψους ανάλογα με το πλήθος των κατηγοριών
            int height = (data.size() * 60) + 50;
            this.setPreferredSize(new Dimension(750, height));
            this.setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Ενεργοποίηση Antialiasing για πιο ομαλά γραφικά
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (data.isEmpty()) {
                return;
            }

            // Εύρεση της μέγιστης τιμής για την κλιμάκωση του γραφήματος
            long maxVal = 1;
            for (BudgetCategory c : data) {
                if (c.getAmount() > maxVal) {
                    maxVal = c.getAmount();
                }
            }

            int y = 20;
            int barHeight = 35;
            int maxBarWidth = getWidth() - 320; // Αφήνουμε χώρο για κείμενο δεξιά/αριστερά

            for (BudgetCategory c : data) {
                // Υπολογισμός μήκους μπάρας αναλογικά
                double ratio = (double) c.getAmount() / maxVal;
                int barWidth = (int) (ratio * maxBarWidth);

                // Σχεδίαση Ονόματος Κατηγορίας
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g2.drawString(c.getName(), 10, y + 22);

                // Επιλογή Χρώματος ανάλογα με τον τύπο του Υπουργείου
                Color barColor = getColorForCategory(c.getCode());
                g2.setColor(barColor);

                // Σχεδίαση Μπάρας (με στρογγυλεμένες γωνίες)
                g2.fillRoundRect(280, y, barWidth, barHeight, 10, 10);

                // Σχεδίαση Ποσού δίπλα στη μπάρα
                g2.setColor(Color.DARK_GRAY);
                String amountText = formatCompact(c.getAmount());
                g2.drawString(amountText, 280 + barWidth + 10, y + 22);

                // Μετακίνηση στην επόμενη γραμμή
                y += 55;
            }
        }
    }

    /**
     * Εσωτερική κλάση για το συγκριτικό γράφημα (Διπλές μπάρες).
     */
    public static class ComparisonChartPanel extends JPanel {
        
        private final List<String> labels;
        private final List<Long> values1;
        private final List<Long> values2;
        private final int year1;
        private final int year2;

        public ComparisonChartPanel(List<String> labels, List<Long> v1, List<Long> v2, int y1, int y2) {
            this.labels = labels;
            this.values1 = v1;
            this.values2 = v2;
            this.year1 = y1;
            this.year2 = y2;
            
            int height = (labels.size() * 80) + 60;
            this.setPreferredSize(new Dimension(850, height));
            this.setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (labels.isEmpty()) {
                return;
            }

            // Υπολογισμός μέγιστης τιμής και από τα δύο έτη
            long max1 = 0;
            for (Long v : values1) {
                if (v > max1) max1 = v;
            }
            
            long max2 = 0;
            for (Long v : values2) {
                if (v > max2) max2 = v;
            }
            
            long maxVal = Math.max(max1, max2);
            if (maxVal == 0) maxVal = 1;

            int startY = 40;
            int barHeight = 20;
            int maxBarWidth = getWidth() - 350;

            // --- Σχεδίαση Υπομνήματος (Legend) ---
            // Έτος 1 (Μπλε)
            g2.setColor(new Color(52, 152, 219));
            g2.fillRect(10, 10, 15, 15);
            g2.setColor(Color.BLACK);
            g2.drawString("Έτος " + year1, 30, 22);
            
            // Έτος 2 (Κόκκινο)
            g2.setColor(new Color(231, 76, 60));
            g2.fillRect(100, 10, 15, 15);
            g2.setColor(Color.BLACK);
            g2.drawString("Έτος " + year2, 120, 22);

            // --- Σχεδίαση Ράβδων ---
            for (int i = 0; i < labels.size(); i++) {
                String label = labels.get(i);
                long val1 = values1.get(i);
                long val2 = values2.get(i);

                int width1 = (int) ((double) val1 / maxVal * maxBarWidth);
                int width2 = (int) ((double) val2 / maxVal * maxBarWidth);

                // Όνομα Κατηγορίας
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2.drawString(label, 10, startY + 15);

                // Μπάρα 1 (Έτος 1)
                g2.setColor(new Color(52, 152, 219));
                g2.fillRoundRect(300, startY, width1, barHeight, 5, 5);
                g2.drawString(formatCompact(val1), 305 + width1, startY + 15);

                // Μπάρα 2 (Έτος 2) - Τοποθετείται ακριβώς από κάτω
                g2.setColor(new Color(231, 76, 60));
                g2.fillRoundRect(300, startY + barHeight + 2, width2, barHeight, 5, 5);
                g2.drawString(formatCompact(val2), 305 + width2, startY + barHeight + 17);

                // Προετοιμασία για την επόμενη ομάδα
                startY += 80;
            }
        }
    }

    /**
     * Βοηθητική μέθοδος: Επιλέγει χρώμα ανάλογα με τον κωδικό του Υπουργείου.
     */
    private static Color getColorForCategory(String code) {
        if (code.contains("HEALTH")) {
            return new Color(46, 204, 113); // Πράσινο
        } else if (code.contains("DEFENSE")) {
            return new Color(52, 152, 219); // Μπλε
        } else if (code.contains("LABOR")) {
            return new Color(230, 126, 34); // Πορτοκαλί
        } else if (code.contains("EDUCATION")) {
            return new Color(155, 89, 182); // Μωβ
        } else {
            return Color.GRAY;
        }
    }

    /**
     * Βοηθητική μέθοδος: Μορφοποίηση αριθμών σε σύντομη μορφή (π.χ. 1.5 δις).
     */
    private static String formatCompact(long amount) {
        if (amount >= 1_000_000_000L) {
            return String.format("%.1f δις €", amount / 1_000_000_000.0);
        } else {
            return String.format("%.1f εκ. €", amount / 1_000_000.0);
        }
    }
}