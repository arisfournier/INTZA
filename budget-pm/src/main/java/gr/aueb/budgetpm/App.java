package gr.aueb.budgetpm;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class App {

    private static final String BANNER = """
            =========================================
               Πρωθυπουργός για μια μέρα (CLI v0.1)
            =========================================
            Εντολές:
              help            - βοήθεια
              show summary    - εμφάνιση συνοπτικών στοιχείων (dummy)
              exit            - έξοδος
            """;

    public static void main(String[] args) throws Exception {
        System.out.println(BANNER);

        // ΔΗΜΙΟΥΡΓΙΑ ΚΑΙ ΦΟΡΤΩΣΗ BUDGET
        Budget budget = new Budget();
        budget.loadFromApi();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");
            String line = br.readLine();
            if (line == null) break; // EOF

            String cmd = line.trim().toLowerCase();

            switch (cmd) {
                case "help" -> System.out.println(BANNER);
                case "show summary" -> showSummary(budget);
                case "exit", "quit" -> {
                    System.out.println("Αντίο!");
                    return;
                }
                case "" -> {} // αγνόησε κενές γραμμές
                default -> System.out.println("Άγνωστη εντολή. Γράψε 'help'.");
            }
        }
    }
        /**
     * Υπολογίζει το ισοζύγιο (έσοδα - έξοδα).
     * Προς το παρόν απλή αφαίρεση· αργότερα μπορεί να γίνει πιο έξυπνη.
     */
    public static long computeBalance(long revenues, long expenses) {
        return revenues - expenses;
    }

    private static void showSummary(Budget budget) {
        long revenues = budget.getTotalRevenue();      //Το νεο
        long expenses = budget.getTotalExpenses();
        long balance  = computeBalance(revenues, expenses);
        /*long revenues = 35_000_000_000L; // προσωρινά δεδομένα    Τα προιγουμενα προσωρινα
        long expenses = 40_000_000_000L; // προσωρινά δεδομένα
        long balance  = computeBalance(revenues, expenses);*/

        System.out.println("— Σύνοψη προϋπολογισμού (προσωρινά δεδομένα) —");
        System.out.printf("  Έσοδα : %,d €%n", revenues);
        System.out.printf("  Έξοδα : %,d €%n", expenses);
        System.out.printf("  Ισοζύγιο: %,d €%n", balance);
        if (balance < 0) {
            System.out.println("  (Έλλειμμα)");
        } else if (balance > 0) {
            System.out.println("  (Πλεόνασμα)");
        } else {
            System.out.println("  (Ισοσκελισμένο)");
        }
    }
    
}
