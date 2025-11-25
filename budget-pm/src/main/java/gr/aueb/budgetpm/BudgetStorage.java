package gr.aueb.budgetpm;

import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

/**
 * Αποθήκευση και ανάκτηση Budget αντικειμένων σε/από JSON αρχεία.
 *
 * Απλό format, π.χ.:
 * {
 *   "countryCode": "GR",
 *   "year": 2020,
 *   "totalRevenue": 12345,
 *   "totalExpenses": 67890
 * }
 */
public class BudgetStorage {

    /**
     * Αποθηκεύει ένα Budget σε JSON αρχείο στο path που δίνουμε.
     */
    public static void saveBudget(Budget budget, Path file) throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("countryCode", budget.getCountryCode());
        obj.put("year", budget.getYear());
        obj.put("totalRevenue", budget.getTotalRevenue());
        obj.put("totalExpenses", budget.getTotalExpenses());

        String json = obj.toString(2); // pretty-print
        Files.writeString(file, json, StandardCharsets.UTF_8);
    }

    /**
     * Διαβάζει ένα Budget από JSON αρχείο.
     */
    public static Budget loadBudget(Path file) throws Exception {
        String json = Files.readString(file, StandardCharsets.UTF_8);
        JSONObject obj = new JSONObject(json);

        String countryCode = obj.getString("countryCode");
        int year = obj.getInt("year");
        long revenues = obj.optLong("totalRevenue", 0L);
        long expenses = obj.optLong("totalExpenses", 0L);

        Budget b = new Budget(year, countryCode);
        b.setTotals(revenues, expenses);

        return b;
    }
}
