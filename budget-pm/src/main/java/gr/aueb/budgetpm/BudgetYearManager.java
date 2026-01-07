package gr.aueb.budgetpm;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

/**
 * Διαχειρίζεται Budget αντικείμενα ανά έτος για μια συγκεκριμένη χώρα.
 *
 * - Φορτώνει από API όταν ζητηθεί ένα έτος πρώτη φορά.
 * - Κρατάει cache ώστε να μην κάνουμε άσκοπα πολλά API calls.
 */
public class BudgetYearManager {

    private final String countryCode;
    private final Map<Integer, Budget> budgetsByYear = new HashMap<>();

    public BudgetYearManager(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Επιστρέφει το Budget για το συγκεκριμένο έτος.
     * Αν δεν υπάρχει, το δημιουργεί, το φορτώνει από το API και το αποθηκεύει.
     */
    public Budget getOrLoad(int year) {
        Budget b = budgetsByYear.get(year);
        if (b == null) {
            b = new Budget(year, countryCode);
            b.loadFromApi();
            budgetsByYear.put(year, b);
        }
        return b;
    }

    /**
     * Επιστρέφει τα έτη που έχουν ήδη φορτωθεί.
     */
    public Set<Integer> getLoadedYears() {
        return Collections.unmodifiableSet(budgetsByYear.keySet());
    }

    /**
     * Επιτρέπει να βάλουμε χειροκίνητα ένα budget για ένα έτος
     * (π.χ. από αποθήκευση σε αρχείο).
     */
    public void putBudget(int year, Budget budget) {
        budgetsByYear.put(year, budget);
    }

    /**
 * Αποθηκεύει ΟΛΑ τα budgets (όλα τα έτη) σε ένα JSON αρχείο,
 * μαζί με τις αλλαγές χρήστη (userChanges).
 */
    public void saveAll(Path file) throws Exception {
        JSONObject root = new JSONObject();
        root.put("country", countryCode);

        JSONArray arr = new JSONArray();

        for (var entry : budgetsByYear.entrySet()) {
            int year = entry.getKey();
            Budget b = entry.getValue();

            JSONObject obj = new JSONObject();
            obj.put("year", year);
            obj.put("totalRevenue", b.getTotalRevenue());
            obj.put("totalExpenses", b.getTotalExpenses());

            // Αποθήκευση αλλαγών χρήστη
            JSONObject changes = new JSONObject();
            for (var ch : b.getUserChanges().entrySet()) {
                changes.put(ch.getKey(), ch.getValue());
            }
            obj.put("userChanges", changes);

            arr.put(obj);
        }

        root.put("budgets", arr);

        Files.writeString(file, root.toString(2), StandardCharsets.UTF_8);
    }



    //Φορτώνει τα budgets από ένα JSON αρχείο και τα περνάει στον manager
    public void loadAll(Path file) throws Exception {
        String json = Files.readString(file, StandardCharsets.UTF_8);
        JSONObject root = new JSONObject(json);

        JSONArray arr = root.getJSONArray("budgets");

        budgetsByYear.clear();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);

            int year = obj.getInt("year");
            long rev = obj.getLong("totalRevenue");
            long exp = obj.getLong("totalExpenses");

            Budget b = new Budget(year, countryCode);
            b.setTotals(rev, exp);

            // Φόρτωση αλλαγών χρήστη
            if (obj.has("userChanges")) {
                JSONObject changes = obj.getJSONObject("userChanges");
                for (String key : changes.keySet()) {
                    long value = changes.getLong(key);
                    b.setUserValue(key, value);
                }
            }

            this.budgetsByYear.put(year, b);
        }
    }

}
