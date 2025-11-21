package gr.aueb.budgetpm;

import org.json.JSONArray;
import org.json.JSONObject;

/*Προσωρινη budget με πραγματικα δεδομενα*/

public class Budget {

    private long totalRevenue = 0;
    private long totalExpenses = 0;

    public void loadFromApi() {
        JSONArray data = BudgetApiReader.fetchBudgetData("GR");
        if (data == null || data.length() < 2) return; // Ελεγχος για data

        JSONArray apiData = data.getJSONArray(1);

        for (int i = 0; i < data.length(); i++) {
            JSONObject record = apiData.getJSONObject(i);
            // Υποθέτουμε ότι το value περιέχει έσοδα/έξοδα
            double value = record.optDouble("value", 0);

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

}
