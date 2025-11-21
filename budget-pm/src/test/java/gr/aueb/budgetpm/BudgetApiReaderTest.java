package gr.aueb.budgetpm;

import org.junit.jupiter.api.Test;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;

public class BudgetApiReaderTest {

    @Test
    void fetchBudgetData_returnsNotNullForValidCountry() {
        JSONArray result = BudgetApiReader.fetchBudgetData("GR");

        assertNotNull(result, "Το API δεν πρέπει να επιστρέφει null για valid country");
        assertTrue(result.length() > 0, "Το API πρέπει να επιστρέφει δεδομένα");
    }

    @Test
    void fetchBudgetData_returnsNullForInvalidCountry() {
        JSONArray result = BudgetApiReader.fetchBudgetData("XXX");

        // Η WorldBank επιστρέφει JSON με error message, όχι null.
        // Άρα αλλάζουμε το test να ελέγχει το error message.
        assertNotNull(result, "Το API πρέπει να επιστρέφει error JSON array, όχι null");

        // Το πρώτο JSON object έχει μέσα "message"
        JSONObject obj = result.getJSONObject(0);

        assertTrue(obj.has("message"), "Το object πρέπει να περιέχει error 'message'");
    }
}
