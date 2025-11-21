package gr.aueb.budgetpm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    @Test
    void computeBalance_returnsNegativeWhenExpensesHigher() {
        long revenues = 35_000_000_000L;
        long expenses = 40_000_000_000L;

        long balance = App.computeBalance(revenues, expenses);

        assertEquals(-5_000_000_000L, balance);
    }

    @Test
    void computeBalance_returnsPositiveWhenRevenuesHigher() {
        long balance = App.computeBalance(10, 3);
        assertEquals(7, balance);
    }

    @Test
    void computeBalance_returnsZeroWhenEqual() {
        long balance = App.computeBalance(100, 100);
        assertEquals(0, balance);
    }
}
