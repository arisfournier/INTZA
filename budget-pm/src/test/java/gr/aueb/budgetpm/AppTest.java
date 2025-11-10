package gr.aueb.budgetpm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    @Test
    void basicMathCheck() {
        long revenues = 35_000_000_000L;
        long expenses = 40_000_000_000L;
        long balance = revenues - expenses;
        assertEquals(-5_000_000_000L, balance);
    }
}
