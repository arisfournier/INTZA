package gr.aueb.budgetpm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;      // <-- added
import java.util.Map;         // <-- added

/*Προσωρινη budget με πραγματικα δεδομενα*/

public class Budget {

    private long totalRevenue = 0;
    private long totalExpenses = 0;

    // ---------------------- ADDED FIELDS ----------------------
    private int year;
    private String countryCode;
    private Map<String, Long> apiValues = new HashMap<>();
    // -----------------------------------------------------------

    public void loadFromApi() {
        JSONArray data = BudgetApiReader.fetchBudgetData("GR");
        if (data == null || data.length() < 2) return; // Ελεγχος για data

        JSONArray apiData = data.getJSONArray(1);

        for (int i = 0; i < data.length(); i++) {
            JSONObject record = apiData.getJSONObject(i);
            // Υποθέτουμε ότι το value περιέχει έσοδα/έξοδα
            double value = record.optDouble("value", 0);

            // ---------------------- ADDED: store values by category ----------------------
            String category = record.optString("category", "UNKNOWN");
            apiValues.put(category, (long) value);
            // ------------------------------------------------------------------------------

            String type = "revenue"; // Παιρνεις απο το record το ειδος
            if (type.equalsIgnoreCase("revenue")) {
                totalRevenue += (long)value;
            } else {
                totalExpenses += (long)value;
            }
        }
    }

    public long getTotalRevenue() {
        return totalRevenue;
    }

    public long getTotalExpenses() {
        return totalExpenses;
    }

    // ---------------------- ADDED METHODS ----------------------

    public Budget() {
        // default constructor
    }

    public Budget(int year, String countryCode) {
        this.year = year;
        this.countryCode = countryCode;
    }

    public int getYear() {
        return year;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public Map<String, Long> getApiValues() {
        return apiValues;
    }

    public void setTotals(long revenue, long expenses) {
        this.totalRevenue = revenue;
        this.totalExpenses = expenses;
    }

    // ------------------------------------------------------------
}
