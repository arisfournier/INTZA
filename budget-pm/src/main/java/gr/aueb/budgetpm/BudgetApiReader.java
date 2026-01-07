package gr.aueb.budgetpm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
/*import org.json.JSONObject;*/
import org.json.JSONArray;

public class BudgetApiReader {

    private static final String API_URL = "https://api.worldbank.org/v2/country/{COUNTRY}/indicator/{INDICATOR}?format=json&date={YEAR}";
    //Τωρα μπορει να παιρνει διαφορετικα indicators(εσοδα εξοδα)

    public static JSONArray fetchBudgetData(String countryCode, int year, String indicator) {
        try {
            String fullUrl = API_URL.replace("{COUNTRY}", countryCode).replace("{INDICATOR}", indicator).replace("{YEAR}", String.valueOf(year));
            URI uri = URI.create(fullUrl);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            //To API της WorldBank επιστρέφει JSON array
            JSONArray jsonArray = new JSONArray(response.toString());

            return jsonArray;

        } catch (Exception e) {
            System.out.println("API error: " + e.getMessage());
            return null;
        }
    }
}
