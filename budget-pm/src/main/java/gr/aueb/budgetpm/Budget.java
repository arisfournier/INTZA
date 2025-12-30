package gr.aueb.budgetpm;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Κλάση Budget με φόρτωση δεδομένων από το WorldBank API.
 * Υποστηρίζει:
 * - χώρα (countryCode)
 * - έτος (year) – προς το παρόν δεν το χρησιμοποιούμε στο API query,
 *   αλλά το κρατάμε για μελλοντική χρήση.
 * -indicator για την φορτωση δυο δεικτων(revenues και expenses)
 */

public class Budget {

    private final int year;
    private final String countryCode;
    
    // Συνολικά ποσά
    private long totalRevenue;
    private long totalExpenses;

    // Αποθήκευση τιμών από το API (Base values)
    private final Map<String, Long> apiValues = new HashMap<>();

    // Αποθήκευση αλλαγών χρήστη (User overrides)
    private final Map<String, Long> userChanges = new HashMap<>();

    // Σταθερές API
    private static final String INDICATOR_EXPENSES = "GC.XPN.TOTL.GD.ZS";
    private static final String INDICATOR_REVENUE  = "GC.REV.XGRT.GD.ZS";
    // Εκτίμηση ΑΕΠ (σταθερό για απλοποίηση, ~200 δις)
    private static final long ESTIMATED_GDP = 200_000_000_000L; 

    // Ορισμός ονομάτων για το CLI - ΠΛΗΡΗΣ ΕΛΛΗΝΙΚΗ ΜΕΤΑΦΡΑΣΗ
    private static final Map<String, String> CATEGORY_MAP = Map.ofEntries(
            // Γενικό Σύνολο
            Map.entry("GC.XPN.TOTL.GD.ZS", "ΣΥΝΟΛΟ ΚΡΑΤΙΚΩΝ ΔΑΠΑΝΩΝ"),

            // 1. ΥΠΟΥΡΓΕΙΟ ΥΓΕΙΑΣ
            Map.entry("MIN_HEALTH",       "ΥΠΟΥΡΓΕΙΟ ΥΓΕΙΑΣ"),
            Map.entry("HEALTH_SALARIES",  " - ΜΙΣΘΟΙ ΠΡΟΣΩΠΙΚΟΥ"),
            Map.entry("HEALTH_HOSPITALS", " - ΛΕΙΤΟΥΡΓΙΚΑ ΝΟΣΟΚΟΜΕΙΩΝ"),
            Map.entry("HEALTH_MEDS",      " - ΦΑΡΜΑΚΑ & ΕΜΒΟΛΙΑ"),
            Map.entry("HEALTH_EQUIP",     " - ΙΑΤΡΙΚΟΣ ΕΞΟΠΛΙΣΜΟΣ"),

            // 2. ΥΠΟΥΡΓΕΙΟ ΠΑΙΔΕΙΑΣ
            Map.entry("MIN_EDUCATION",    "ΥΠΟΥΡΓΕΙΟ ΠΑΙΔΕΙΑΣ"),
            Map.entry("EDU_SALARIES",     " - ΜΙΣΘΟΙ ΕΚΠ/ΚΩΝ"),
            Map.entry("EDU_MAINTENANCE",  " - ΣΥΝΤΗΡΗΣΗ ΚΤΙΡΙΩΝ"),
            Map.entry("EDU_RESEARCH",     " - ΕΡΕΥΝΑ"),

            // 3. ΥΠΟΥΡΓΕΙΟ ΑΜΥΝΑΣ
            Map.entry("MIN_DEFENSE",      "ΥΠΟΥΡΓΕΙΟ ΑΜΥΝΑΣ"),
            Map.entry("DEF_SALARIES",     " - ΜΙΣΘΟΔΟΣΙΑ"),
            Map.entry("DEF_EQUIPMENT",    " - ΕΞΟΠΛΙΣΜΟΙ"),
            Map.entry("DEF_TRAINING",     " - ΑΣΚΗΣΕΙΣ"),

            // 4. ΥΠΟΥΡΓΕΙΟ ΠΡΟΣΤΑΣΙΑΣ ΠΟΛΙΤΗ
            Map.entry("MIN_PROTECTION",   "ΥΠ. ΠΡΟΣΤΑΣΙΑΣ ΠΟΛΙΤΗ"),
            Map.entry("PROT_SALARIES",    " - ΜΙΣΘΟΙ ΣΩΜΑΤΩΝ"),
            Map.entry("PROT_EQUIP",       " - ΟΧΗΜΑΤΑ/ΜΕΣΑ"),
            Map.entry("PROT_BORDERS",     " - ΦΥΛΑΞΗ ΣΥΝΟΡΩΝ"),

            // 5. ΥΠΟΥΡΓΕΙΟ ΕΞΩΤΕΡΙΚΩΝ
            Map.entry("MIN_FOREIGN",      "ΥΠΟΥΡΓΕΙΟ ΕΞΩΤΕΡΙΚΩΝ"),
            Map.entry("FOR_DIPLOMACY",    " - ΔΙΠΛΩΜΑΤΙΑ"),
            Map.entry("FOR_EMBASSIES",    " - ΠΡΕΣΒΕΙΕΣ"),
            Map.entry("FOR_AID",          " - ΑΝΘΡΩΠΙΣΤΙΚΗ ΒΟΗΘΕΙΑ"),

            // 6. ΥΠΟΥΡΓΕΙΟ ΕΣΩΤΕΡΙΚΩΝ
            Map.entry("MIN_INTERIOR",     "ΥΠΟΥΡΓΕΙΟ ΕΣΩΤΕΡΙΚΩΝ"),
            Map.entry("INT_MUNICIPAL",    " - ΕΠΙΧΟΡΗΓΗΣΗ ΔΗΜΩΝ"),
            Map.entry("INT_ELECTIONS",    " - ΕΚΛΟΓΕΣ/ΤΑΥΤΟΤΗΤΕΣ"),

            // 7. ΝΕΟ: ΥΠΟΥΡΓΕΙΟ ΕΡΓΑΣΙΑΣ
            Map.entry("MIN_LABOR",        "ΥΠ. ΕΡΓΑΣΙΑΣ & ΑΣΦΑΛΙΣΗΣ"),
            Map.entry("LABOR_PENSIONS",   " - ΣΥΝΤΑΞΕΙΣ"),
            Map.entry("LABOR_BENEFITS",   " - ΕΠΙΔΟΜΑΤΑ ΑΝΕΡΓΙΑΣ"),
            Map.entry("LABOR_SUPPORT",    " - ΚΟΙΝΩΝΙΚΗ ΠΡΟΝΟΙΑ"),

            // Λοιπά
            Map.entry("MIN_OTHER",        "ΛΟΙΠΕΣ ΚΡΑΤΙΚΕΣ ΔΑΠΑΝΕΣ")
    );

    public Budget(int year, String countryCode) {
        this.year = year;
        this.countryCode = countryCode;
    }

    /**
     * Φορτώνει δεδομένα από API και κάνει την κατανομή.
     */
    public void loadFromApi() {
        this.totalRevenue = 0L;
        this.totalExpenses = 0L;
        apiValues.clear();

        long expenses = fetchMetric(INDICATOR_EXPENSES);
        long revenues = fetchMetric(INDICATOR_REVENUE);

        // Αν δεν βρει δεδομένα, σταματάει (θα μείνουν 0)
        if (expenses == 0 && revenues == 0) {
            System.out.println("Προσοχή: Δεν βρέθηκαν δεδομένα WorldBank για το έτος " + this.year);
            System.out.println("   (Δοκίμασε ένα παλαιότερο έτος, π.χ. 2020)");
            return;
        }

        this.totalExpenses = expenses;
        this.totalRevenue = revenues;
        apiValues.put(INDICATOR_EXPENSES, totalExpenses);

        // --- ΚΑΤΑΝΟΜΗ ---
        
        // 1. ΥΓΕΙΑ (15%)
        long healthTotal = Math.round(totalExpenses * 0.15);
        apiValues.put("MIN_HEALTH",       healthTotal);
        apiValues.put("HEALTH_SALARIES",  Math.round(healthTotal * 0.45));
        apiValues.put("HEALTH_HOSPITALS", Math.round(healthTotal * 0.30));
        apiValues.put("HEALTH_MEDS",      Math.round(healthTotal * 0.15));
        apiValues.put("HEALTH_EQUIP",     Math.round(healthTotal * 0.10));

        // 2. ΠΑΙΔΕΙΑ (12%)
        long eduTotal = Math.round(totalExpenses * 0.12);
        apiValues.put("MIN_EDUCATION",    eduTotal);
        apiValues.put("EDU_SALARIES",     Math.round(eduTotal * 0.75));
        apiValues.put("EDU_MAINTENANCE",  Math.round(eduTotal * 0.15));
        apiValues.put("EDU_RESEARCH",     Math.round(eduTotal * 0.10));

        // 3. ΑΜΥΝΑ (10%)
        long defTotal = Math.round(totalExpenses * 0.10);
        apiValues.put("MIN_DEFENSE",      defTotal);
        apiValues.put("DEF_EQUIPMENT",    Math.round(defTotal * 0.45));
        apiValues.put("DEF_SALARIES",     Math.round(defTotal * 0.40));
        apiValues.put("DEF_TRAINING",     Math.round(defTotal * 0.15));

        // 4. ΠΡΟΣΤΑΣΙΑ ΠΟΛΙΤΗ (5%)
        long protTotal = Math.round(totalExpenses * 0.05);
        apiValues.put("MIN_PROTECTION",   protTotal);
        apiValues.put("PROT_SALARIES",    Math.round(protTotal * 0.70));
        apiValues.put("PROT_EQUIP",       Math.round(protTotal * 0.20));
        apiValues.put("PROT_BORDERS",     Math.round(protTotal * 0.10));

        // 5. ΕΞΩΤΕΡΙΚΩΝ (3%)
        long forTotal = Math.round(totalExpenses * 0.03);
        apiValues.put("MIN_FOREIGN",      forTotal);
        apiValues.put("FOR_EMBASSIES",    Math.round(forTotal * 0.50));
        apiValues.put("FOR_DIPLOMACY",    Math.round(forTotal * 0.30));
        apiValues.put("FOR_AID",          Math.round(forTotal * 0.20));

        // 6. ΕΣΩΤΕΡΙΚΩΝ (4%)
        long intTotal = Math.round(totalExpenses * 0.04);
        apiValues.put("MIN_INTERIOR",     intTotal);
        apiValues.put("INT_MUNICIPAL",    Math.round(intTotal * 0.80));
        apiValues.put("INT_ELECTIONS",    Math.round(intTotal * 0.20));

        // 7. ΕΡΓΑΣΙΑΣ (30%)
        long labTotal = Math.round(totalExpenses * 0.30);
        apiValues.put("MIN_LABOR",        labTotal);
        apiValues.put("LABOR_PENSIONS",   Math.round(labTotal * 0.60));
        apiValues.put("LABOR_BENEFITS",   Math.round(labTotal * 0.25));
        apiValues.put("LABOR_SUPPORT",    Math.round(labTotal * 0.15));

        // 8. ΛΟΙΠΑ
        long otherTotal = totalExpenses - healthTotal - eduTotal - defTotal - protTotal - forTotal - intTotal - labTotal;
        if (otherTotal < 0) otherTotal = 0;
        apiValues.put("MIN_OTHER", otherTotal);
    }

    /**
     * Επιστρέφει τη λίστα κατηγοριών για το App.
     * Συνδυάζει API values και User changes.
     */
    public List<BudgetCategory> getCategories() {
        List<BudgetCategory> list = new ArrayList<>();

        // Σημαντικό: Διατρέχουμε το Map των ονομάτων, όχι τις τιμές του API τυχαία.
        for (Map.Entry<String, String> entry : CATEGORY_MAP.entrySet()) {
            String code = entry.getKey();      // π.χ. MIN_HEALTH (Αυτό θέλουμε για Code)
            String name = entry.getValue();    // π.χ. ΥΠΟΥΡΓΕΙΟ ΥΓΕΙΑΣ (Αυτό θέλουμε για Name)

            // Ελέγχουμε αν υπάρχει ποσό για αυτόν τον κωδικό
            if (apiValues.containsKey(code) || userChanges.containsKey(code)) {
                long amount = getFinalValue(code);
                
                // Δημιουργία κατηγορίας με ΣΩΣΤΟ Κωδικό και Όνομα
                list.add(new BudgetCategory(code, name, amount));
            }
        }
        return list;
    }

    /**
     * Επιστρέφει την τελική τιμή μιας κατηγορίας.
     * Αν ο χρήστης έχει κάνει αλλαγή, επιστρέφει αυτήν. Αλλιώς την API τιμή.
     */
    public long getFinalValue(String key) {
        if (userChanges.containsKey(key)) {
            return userChanges.get(key);
        }
        return apiValues.getOrDefault(key, 0L);
    }

    // --- Βοηθητικές μέθοδοι (Getters/Setters) για το BudgetYearManager ---

    public int getYear() { return year; }
    public String getCountryCode() { return countryCode; }
    public long getTotalRevenue() { return totalRevenue; }
    public long getTotalExpenses() { return totalExpenses; }
    
    public void setTotals(long rev, long exp) {
        this.totalRevenue = rev;
        this.totalExpenses = exp;
    }

    public Map<String, Long> getUserChanges() {
        return userChanges;
    }
    
    public Map<String, Long> getApiValues() {
        return apiValues;
    }

    public void setUserValue(String key, long value) {
        userChanges.put(key, value);
    }

    // --- Κώδικας επικοινωνίας με API ---

    private long fetchMetric(String indicator) {
        try {
            // Κλήση της βοηθητικής κλάσης (BudgetApiReader)
            // Αν δεν έχεις την BudgetApiReader ως ξεχωριστό αρχείο ή θες να την ενσωματώσεις,
            // μπορείς να βάλεις τη λογική εδώ. Αλλά υποθέτω ότι υπάρχει το BudgetApiReader.java.
            
            // Εναλλακτικά, μια απλή υλοποίηση εδώ για σιγουριά:
            String urlStr = "https://api.worldbank.org/v2/country/" + countryCode 
                          + "/indicator/" + indicator + "?format=json&date=" + year;
            
            URL url = new URI(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() != 200) return 0;

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) content.append(line);
            in.close();

            JSONArray jsonArray = new JSONArray(content.toString());
            // Το API επιστρέφει [ {page info}, [ {data} ] ]
            if (jsonArray.length() < 2) return 0;
            
            Object dataObj = jsonArray.get(1);
            if (dataObj instanceof JSONArray) {
                JSONArray data = (JSONArray) dataObj;
                if (data.isEmpty()) return 0;
                
                // Παίρνουμε το πρώτο στοιχείο
                var item = data.getJSONObject(0);
                if (item.isNull("value")) return 0;
                
                double percent = item.getDouble("value"); // Ποσοστό του ΑΕΠ
                // Μετατροπή σε απόλυτο ποσό (Ποσοστό * ΑΕΠ)
                return Math.round((percent / 100.0) * ESTIMATED_GDP);
            }

            return 0;
        } catch (Exception e) {
            // System.err.println("API error: " + e.getMessage());
            return 0;
        }
    }

    public long getFinalValueFromCategoryName(String categoryName) {
        // Διατρέχουμε όλες τις κατηγορίες που έχει δημιουργήσει το budget
        for (BudgetCategory c : getCategories()) {
            // Αν το όνομα ταιριάζει (αγνοώντας κεφαλαία/μικρά)
            if (c.getName().equalsIgnoreCase(categoryName)) {
                return c.getAmount();
            }
        }
        return 0; // Αν δεν βρεθεί, επιστρέφει 0
    }
}