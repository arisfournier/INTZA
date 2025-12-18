package gr.aueb.budgetpm;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Σύγκριση δύο budgets (συνήθως διαφορετικών χωρών) ανά κατηγορία.
 * Η σύγκριση γίνεται σε τελικές τιμές κατηγορίας (όπως τις βλέπει ο χρήστης στο CLI).
 */
public final class CountryComparator {

    private CountryComparator() {}

    public static Map<String, ComparisonRow> compare(Budget a, Budget b) {
        if (a == null) throw new IllegalArgumentException("budget a is null");
        if (b == null) throw new IllegalArgumentException("budget b is null");

        // Παίρνουμε union κατηγοριών από τα δύο budgets
        TreeSet<String> cats = new TreeSet<>();
        for (BudgetCategory c : a.getCategories()) cats.add(c.getName().toUpperCase());
        for (BudgetCategory c : b.getCategories()) cats.add(c.getName().toUpperCase());

        Map<String, ComparisonRow> res = new LinkedHashMap<>();
        for (String cat : cats) {
            long av = getCategoryAmount(a, cat);
            long bv = getCategoryAmount(b, cat);
            res.put(cat, new ComparisonRow(av, bv, bv - av));
        }
        return res;
    }

    /**
     * Επιστρέφει ποσό κατηγορίας από το budget. Αν δεν υπάρχει, 0.
     */
    private static long getCategoryAmount(Budget budget, String categoryUpper) {
        long sum = 0;
        for (BudgetCategory c : budget.getCategories()) {
            if (c.getName().equalsIgnoreCase(categoryUpper)) {
                sum += c.getAmount();
            }
        }
        return sum;
    }

    public static final class ComparisonRow {
        public final long aValue;
        public final long bValue;
        public final long diff;

        public ComparisonRow(long aValue, long bValue, long diff) {
            this.aValue = aValue;
            this.bValue = bValue;
            this.diff = diff;
        }
    }
}
