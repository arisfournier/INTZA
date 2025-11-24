package gr.aueb.budgetpm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Προσωρινή κλάση Budget με φόρτωση δεδομένων από το WorldBank API.
 * Κρατάει συνολικά έσοδα / έξοδα και επιτρέπει ομαδοποίηση σε κατηγορίες.
 *
 * Σκοπός μας εδώ είναι:
 * - να ΜΗΝ πετάει exceptions όταν το API έχει πρόβλημα
 * - να έχουμε πάντα μη αρνητικές τιμές
 * - να μπορούμε να πάρουμε categories μέσω getCategories()
 */
public class Budget {

    private long totalRevenue = 0L;
    private long totalExpenses = 0L;

    // Αποθηκεύει τις "ακατέργαστες" τιμές που προέρχονται από το API
    // π.χ. apiValues.put("GC.XPN.TOTL.GD.ZS", 123456L);
    private final Map<String, Long> apiValues = new HashMap<>();

    // Απλή αντιστοίχιση API κωδικών σε κατηγορίες. Προς το παρόν ενδεικτική.
    private static final Map<String, String> CATEGORY_MAP = Map.of(
            "GC.XPN.TOTL.GD.ZS", "TAXES"   // δικό μας προσωρινό label
    );

    public Budget() {
    }

    /**
     * Φορτώνει δεδομένα από το WorldBank API για την Ελλάδα (GR).
     * Σε περίπτωση αποτυχίας, τα σύνολα παραμένουν 0 και δεν πετάγεται exception.
     */
    public void loadFromApi() {
        totalRevenue = 0L;
        totalExpenses = 0L;
        apiValues.clear();

        try {
            JSONArray data = BudgetApiReader.fetchBudgetData("GR");
            if (data == null || data.length() < 2) {
                // Δεν έχουμε επαρκή δεδομένα
                return;
            }

            // Στα responses της WorldBank, το index 1 είναι array με τις παρατηρήσεις.
            JSONArray observations = data.getJSONArray(1);
            if (observations.length() == 0) {
                return;
            }

            // Θα πάρουμε την πιο πρόσφατη μη-κενή τιμή "value"
            Long latestValue = null;
            for (int i = 0; i < observations.length(); i++) {
                JSONObject obj = observations.getJSONObject(i);
                if (!obj.isNull("value")) {
                    double v = obj.getDouble("value");
                    latestValue = Math.round(Math.abs(v));
                    break;
                }
            }

            if (latestValue == null) {
                // Όλες οι τιμές ήταν null
                return;
            }

            // Για την ώρα θεωρούμε ότι αυτό είναι "σύνολο εξόδων"
            totalExpenses = latestValue;
            // Μπορούμε να θέσουμε τα έσοδα ίσα, ή 0. Κρατάμε 0 για ασφάλεια.
            totalRevenue = 0L;

            // Γεμίζουμε τον χάρτη raw τιμών για grouping
            apiValues.put("GC.XPN.TOTL.GD.ZS", totalExpenses);

        } catch (Exception e) {
            System.out.println("API parsing error in Budget.loadFromApi: " + e.getMessage());
            // Σε οποιοδήποτε σφάλμα, κρατάμε μηδενικά και κενό map
            totalRevenue = 0L;
            totalExpenses = 0L;
            apiValues.clear();
        }
    }

    public long getTotalRevenue() {
        return totalRevenue;
    }

    public long getTotalExpenses() {
        return totalExpenses;
    }

    /**
     * Επιστρέφει λίστα με κατηγορίες προϋπολογισμού, βασισμένη στις τιμές του apiValues.
     */
    public List<BudgetCategory> getCategories() {
        Map<String, BudgetCategory> categories = new HashMap<>();

        for (Map.Entry<String, Long> entry : apiValues.entrySet()) {
            String apiCode = entry.getKey();
            long amount = entry.getValue() != null ? entry.getValue() : 0L;

            String categoryCode = CATEGORY_MAP.getOrDefault(apiCode, "OTHER");

            BudgetCategory cat = categories.get(categoryCode);
            if (cat == null) {
                cat = new BudgetCategory(categoryCode, categoryCode, amount);
            } else {
                cat.addAmount(amount);
            }
            categories.put(categoryCode, cat);
        }

        return new ArrayList<>(categories.values());
    }
}
