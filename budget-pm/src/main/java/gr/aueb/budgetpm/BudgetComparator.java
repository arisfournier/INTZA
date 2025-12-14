package gr.aueb.budgetpm;

import java.util.LinkedHashMap;
import java.util.Map;

public class BudgetComparator {

    public static Map<String, ComparisonResult> compare(Budget a, Budget b) {
        Map<String, ComparisonResult> results = new LinkedHashMap<>();

        // Παίρνουμε όλα τα κλειδιά που υπάρχουν είτε στο API είτε στα user changes
        var keys = new java.util.TreeSet<String>();
        keys.addAll(a.getApiValues().keySet());
        keys.addAll(b.getApiValues().keySet());
        keys.addAll(a.getUserChanges().keySet());
        keys.addAll(b.getUserChanges().keySet());

        for (String key : keys) {
            long oldValue = a.getFinalValue(key); // FINAL = API + changes
            long newValue = b.getFinalValue(key); // FINAL = API + changes
            long diff = newValue - oldValue;

            results.put(key, new ComparisonResult(oldValue, newValue, diff));
        }

        return results;
    }


    public static class ComparisonResult {
        public long oldValue;
        public long newValue;
        public long diff;

        public ComparisonResult(long oldValue, long newValue, long diff) {
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.diff = diff;
        }
    }
}
