package gr.aueb.budgetpm;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;

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
}
