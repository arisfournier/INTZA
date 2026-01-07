package gr.aueb.budgetpm;

import java.util.HashMap;
import java.util.Map;

/**
 * Ένα Scenario κρατάει "override" τιμές ανά ΚΑΤΗΓΟΡΙΑ (π.χ. HOSPITALS, PHARMA).
 * Δεν πειράζει το base budget· απλώς επιτρέπει σύγκριση baseline vs scenario.
 */
public class BudgetScenario {

    private final String name;
    private final Budget baseBudget;
    private final Map<String, Long> categoryOverrides = new HashMap<>();

    public BudgetScenario(String name, Budget baseBudget) {
        this.name = name;
        this.baseBudget = baseBudget;
    }

    public BudgetScenario(String name, Budget baseBudget, double percentChange) {
        this(name, baseBudget);

        //Εφαρμόζουμε ποσοστιαία μεταβολή σε όλες τις κατηγορίες baseline
        for (BudgetCategory c : baseBudget.getCategories()) {
            String catName = c.getName().toUpperCase();
            long base = c.getAmount();
            long modified = Math.round(base * (1 + percentChange / 100.0));
            if (modified < 0) modified = 0;
            categoryOverrides.put(catName, modified);
        }
    }


    public String getName() {
        return name;
    }

    public void setCategoryValue(String categoryName, long value) {
        categoryOverrides.put(categoryName.toUpperCase(), value);
    }

    /**
     * Επιστρέφει την τιμή της κατηγορίας στο scenario:
     * - αν υπάρχει override, το επιστρέφει
     * - αλλιώς επιστρέφει την baseline τιμή από το base budget (ΤΕΛΙΚΗ τιμή).
     */
    public long getCategoryValue(String categoryName) {
        String key = categoryName.toUpperCase();
        if (categoryOverrides.containsKey(key)) {
            return categoryOverrides.get(key);
        }
        return getBaselineCategoryValue(key);
    }

    //Επιστρέφει baseline (τελική) τιμή μιας κατηγορίας από το base budget.
    private long getBaselineCategoryValue(String categoryName) {
        long sum = 0;
        for (BudgetCategory c : baseBudget.getCategories()) {
            if (c.getName().equalsIgnoreCase(categoryName)) {
                sum += c.getAmount();
            }
        }
        return sum;
    }

    //Επιστρέφει ΟΛΕΣ τις κατηγορίες και τις τιμές τους στο scenario
    public Map<String, Long> getAllCategoryValues() {
        Map<String, Long> result = new HashMap<>();

        for (BudgetCategory c : baseBudget.getCategories()) {
            String name = c.getName().toUpperCase();
            result.put(name, getCategoryValue(name));
        }

        //Αν υπάρχουν overrides για κατηγορίες που δεν υπήρχαν στο base, τις προσθέτουμε
        for (var e : categoryOverrides.entrySet()) {
            result.putIfAbsent(e.getKey(), e.getValue());
        }

        return result;
    }

    public Map<String, Long> getOverrides() {
        return categoryOverrides;
    }
}
