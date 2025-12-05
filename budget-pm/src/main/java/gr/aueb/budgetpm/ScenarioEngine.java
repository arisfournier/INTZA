package gr.aueb.budgetpm;

import java.util.HashMap;
import java.util.Map;

/**
 * ScenarioEngine: Εφαρμόζει αλλαγές (σενάρια) σε ένα Budget,
 * όπως αύξηση δαπανών, μείωση, αλλαγή ΦΠΑ κτλ.
 */
public class ScenarioEngine {

    private final Map<String, Double> percentageChanges = new HashMap<>();

    /**
     * Ορίζει αλλαγή ποσοστού για μια κατηγορία.
     * Παράδειγμα: setChange("HOSPITALS", +10)
     */
    public void setChange(String category, double percent) {
        percentageChanges.put(category, percent);
    }

    /**
     * Εφαρμόζει όλα τα σενάρια πάνω σε ένα Budget και επιστρέφει
     * ένα νέο Budget αντικείμενο που αντιπροσωπεύει το “after scenario”.
     */
    public Budget applyTo(Budget original) {
        Budget modified = new Budget(original.getYear(), original.getCountryCode());
        modified.setTotals(original.getTotalRevenue(), original.getTotalExpenses());

        Map<String, Long> values = original.getApiValues();

        for (var entry : values.entrySet()) {
            String key = entry.getKey();
            long value = entry.getValue();

            if (percentageChanges.containsKey(key)) {
                double percent = percentageChanges.get(key);
                long newValue = (long) (value * (1 + percent / 100));
                modified.getApiValues().put(key, newValue);
            } else {
                modified.getApiValues().put(key, value);
            }
        }

        return modified;
    }
}
