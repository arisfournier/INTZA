package gr.aueb.budgetpm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Προσωρινή κλάση Budget με φόρτωση δεδομένων από το WorldBank API.
 * Υποστηρίζει:
 * - χώρα (countryCode)
 * - έτος (year) – προς το παρόν δεν το χρησιμοποιούμε στο API query,
 *   αλλά το κρατάμε για μελλοντική χρήση.
 */
public class Budget {
    // Αλλαγές που έκανε ο χρήστης (overrides στις API τιμές)
    private Map<String, Long> userChanges = new HashMap<>();

    private final String countryCode;
    private final int year;

    private long totalRevenue = 0L;
    private long totalExpenses = 0L;

    // Αποθηκεύει τις "ακατέργαστες" τιμές που προέρχονται από το API
    private final Map<String, Long> apiValues = new HashMap<>();

    // - τον δείκτη GC.XPN.TOTL.GD.ZS ως "συνολικά έξοδα"
    // - εσωτερικούς κωδικούς HEALTH_TOTAL, HOSPITALS, PHARMA, PREVENTION, STAFF
    private static final Map<String, String> CATEGORY_MAP = Map.of(
            "GC.XPN.TOTL.GD.ZS", "TOTAL_EXPENSES",
            "HEALTH_TOTAL",      "HEALTH",
            "HOSPITALS",         "HOSPITALS",
            "PHARMA",            "PHARMA",
            "PREVENTION",        "PREVENTION",
            "STAFF",             "STAFF"
    );

    /**
     * Default budget: Ελλάδα, χωρίς συγκεκριμένο έτος (0).
     * Το χρησιμοποιεί η App σήμερα.
     */
    public Budget() {
        this(0, "GR");
    }

    /**
     * Budget για συγκεκριμένη χώρα & έτος.
     */
    public Budget(int year, String countryCode) {
        this.year = year;
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public int getYear() {
        return year;
    }

    /**
     * Φορτώνει δεδομένα από το WorldBank API για τη χώρα.
     * (Προς το παρόν αγνοεί το year, θα το χρησιμοποιήσουμε αργότερα
     * όταν φτιάξουμε πιο εξειδικευμένο API query.)
     */
    public void loadFromApi() {
        totalRevenue = 0L;
        totalExpenses = 0L;
        apiValues.clear();

        try {
            JSONArray data = BudgetApiReader.fetchBudgetData(countryCode);
            if (data == null || data.length() < 2) {
                return;
            }

            JSONArray observations = data.getJSONArray(1);
            if (observations.length() == 0) {
                return;
            }

            // Παίρνουμε την πρώτη μη-κενή τιμή "value"
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
                return;
            }

            // Προς το παρόν θεωρούμε ότι η τιμή αυτή είναι "έξοδα"
            totalExpenses = latestValue;
            totalRevenue = 0L;

            apiValues.put("GC.XPN.TOTL.GD.ZS", totalExpenses);

            // Θεωρούμε ότι ΟΛΑ τα έξοδα ανήκουν στο "HEALTH_TOTAL"
            long healthTotal = totalExpenses;

            // Σπάμε το healthTotal σε υπο-κατηγορίες:
            long hospitals  = healthTotal * 50 / 100;  // 50% νοσοκομεία
            long pharma     = healthTotal * 20 / 100;  // 20% φάρμακα
            long prevention = healthTotal * 15 / 100;  // 15% πρόληψη
            long staff      = healthTotal - hospitals - pharma - prevention; // υπόλοιπο σε προσωπικό

            apiValues.put("HEALTH_TOTAL", healthTotal);
            apiValues.put("HOSPITALS",    hospitals);
            apiValues.put("PHARMA",       pharma);
            apiValues.put("PREVENTION",   prevention);
            apiValues.put("STAFF",        staff);

        } catch (Exception e) {
            System.out.println("API parsing error in Budget.loadFromApi: " + e.getMessage());
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
 * Επιστρέφει τις "ακατέργαστες" τιμές από το API
 * (πριν από αλλαγές χρήστη).
 */
    public Map<String, Long> getApiValues() {
        return apiValues;
    }


    //Θέτει χειροκίνητα τα σύνολα εσόδων/εξόδων.
    // Χρησιμοποιείται κυρίως από το BudgetStorage.loadBudget().
    public void setTotals(long revenues, long expenses) {
        this.totalRevenue = revenues;
        this.totalExpenses = expenses;
    }

    /**
     * Επιστρέφει λίστα με κατηγορίες προϋπολογισμού, βασισμένη στις τιμές του apiValues.
     */
    public List<BudgetCategory> getCategories() {
        Map<String, BudgetCategory> categories = new HashMap<>();

        // 1) Περνάμε όλα τα κλειδιά του API, αλλά χρησιμοποιούμε ΤΕΛΙΚΗ τιμή
        for (String apiCode : apiValues.keySet()) {
            long amount = getFinalValue(apiCode); // αν υπάρχει userChange, το παίρνει αυτό
            String categoryCode = CATEGORY_MAP.getOrDefault(apiCode, "OTHER");

            BudgetCategory cat = categories.get(categoryCode);
            if (cat == null) {
                cat = new BudgetCategory(categoryCode, categoryCode, amount);
            } else {
                cat.addAmount(amount);
            }
            categories.put(categoryCode, cat);
        }

        // 2) Περνάμε κλειδιά που υπάρχουν ΜΟΝΟ στα userChanges (όχι στο API)
        for (String apiCode : userChanges.keySet()) {
            if (apiValues.containsKey(apiCode)) {
                continue; // τα έχουμε ήδη χειριστεί παραπάνω
            }

            long amount = getFinalValue(apiCode);
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

    /**
 * Επιστρέφει την τελική τιμή (API + userChanges) βάση της ΟΝΟΜΑΣΙΑΣ κατηγορίας,
 * δηλαδή PHARMA, HOSPITALS κλπ.
 */
    public long getFinalValueFromCategoryName(String categoryName) {
        long sum = 0;

        for (BudgetCategory c : getCategories()) {
            if (c.getName().equals(categoryName)) {
                sum += c.getAmount();
            }
        }

        return sum;
    }



    /**
 * Ορίζει μια αλλαγή από τον χρήστη για μια κατηγορία.
 */
    public void setUserValue(String category, long value) {
        userChanges.put(category, value);
    }

    /**
 * Επιστρέφει όλες τις αλλαγές που έκανε ο χρήστης.
 */
    public Map<String, Long> getUserChanges() {
        return userChanges;
    }

    public long getFinalValue(String category) {
        if (userChanges.containsKey(category)) {
            return userChanges.get(category);   // override
        }
        return apiValues.getOrDefault(category, 0L);
    }

}
