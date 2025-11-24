package gr.aueb.budgetpm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BudgetTest {

    @Test
    void newBudget_hasZeroTotals() {
        // 1. Δημιουργώ ένα καινούριο Budget
        Budget budget = new Budget();

        // 2. Ελέγχω ότι ξεκινάει με 0
        assertEquals(0, budget.getTotalRevenue());
        assertEquals(0, budget.getTotalExpenses());
    }

    @Test
    void loadFromApi_keepsTotalsNonNegative() {
        
        Budget budget = new Budget();

        // Καλώ τη μέθοδο που φορτώνει δεδομένα από το API
        budget.loadFromApi();

        // Ελέγχω ότι τα ποσά ΔΕΝ είναι αρνητικά
        assertTrue(budget.getTotalRevenue() >= 0);
        assertTrue(budget.getTotalExpenses() >= 0);
    }
}