package gr.aueb.budgetpm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Κλάση Budget με φόρτωση δεδομένων από το WorldBank API.
 * Υποστηρίζει:
 * - χώρα (countryCode)
 * - έτος (year) – προς το παρόν δεν το χρησιμοποιούμε στο API query,
 *   αλλά το κρατάμε για μελλοντική χρήση.
 * -indicator για την φορτωση δυο δεικτων(revenues και expenses)
 */
public class Budget {
    // Αλλαγές που έκανε ο χρήστης (overrides στις API τιμές)
    private Map<String, Long> userChanges = new HashMap<>();

    private final String countryCode;
    private final int year;

    private long totalRevenue = 0L;
    private long totalExpenses = 0L;

    //Οι δεικτες που παιρνουμε απο το API
    private static final long ESTIMATED_GDP = 200_000_000_000L; 
    private static final String INDICATOR_EXPENSES = "GC.XPN.TOTL.GD.ZS";
    private static final String INDICATOR_REVENUE  = "GC.REV.XGRT.GD.ZS";

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
        this.totalRevenue = 0L;
        this.totalExpenses = 0L;
        apiValues.clear();

        long expenses = fetchMetric(INDICATOR_EXPENSES);
        long revenues = fetchMetric(INDICATOR_REVENUE);

        // Αν και τα δύο είναι 0, κάτι πήγε στραβά ή δεν υπάρχουν δεδομένα
        if (expenses == 0 && revenues == 0) {
            System.out.println("Δεν βρέθηκαν δεδομένα (Έσοδα/Έξοδα) για το έτος " + this.year);
            return;
        }

        // Ενημερωση των μεταβλητές της κλάσης
        this.totalExpenses = expenses;
        this.totalRevenue = revenues;

        // Αποθηκευση των εξοδων στο map
        apiValues.put(INDICATOR_EXPENSES, totalExpenses);

        //Υπολογισμός υπο-κατηγοριών Υγείας (με βάση τα έξοδα)
        long healthTotal = totalExpenses;
        
        long hospitals  = healthTotal * 50 / 100; //50% νοσοκομεια
        long pharma     = healthTotal * 20 / 100; //20% φαρμακευτικο υλικο
        long prevention = healthTotal * 15 / 100; // 10% προληψη
        long staff      = healthTotal - hospitals - pharma - prevention;

        apiValues.put("HEALTH_TOTAL", healthTotal);
        apiValues.put("HOSPITALS",    hospitals);
        apiValues.put("PHARMA",       pharma);
        apiValues.put("PREVENTION",   prevention);
        apiValues.put("STAFF",        staff);
        
    }

    private long fetchMetric(String indicator) {
        try {
            // Κλήση στο API με κωδικο χωρας, ετος, δεικτη
            JSONArray data = BudgetApiReader.fetchBudgetData(countryCode, this.year, indicator);
            
            if (data == null || data.length() < 2) return 0;
            
            JSONArray observations = data.getJSONArray(1);
            if (observations.length() == 0) return 0;

            for (int i = 0; i < observations.length(); i++) {
                
                JSONObject obj = observations.getJSONObject(i); 
                
                if (!obj.isNull("value")) {
                    double percentage = obj.getDouble("value");
                    
                    return Math.round((Math.abs(percentage) / 100.0) * ESTIMATED_GDP);
                }
            }
        } catch (Exception e) {
            System.out.println("Σφάλμα κατά τη λήψη του δείκτη " + indicator + ": " + e.getMessage());
        }
        return 0; // Αν αποτύχει ή δεν βρει τιμή, επιστρέφει 0
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
