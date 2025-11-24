package gr.aueb.budgetpm;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class App {

    private static final String BANNER = """
            =========================================
               Πρωθυπουργός για μια μέρα (CLI v0.2)
            =========================================
            Εντολές:
              help            - βοήθεια
              show summary    - εμφάνιση συνοπτικών στοιχείων (από API)
              exit            - έξοδος
            """;

    // Κρατάει τα σύνολα που φέρνουμε από το API (κλάση Budget της ομάδας σας)
    private static Budget currentBudget = new Budget();

    public static void main(String[] args) throws Exception {
        System.out.println("Φόρτωση δεδομένων προϋπολογισμού από API για GR...");
        try {
            currentBudget.loadFromApi();   // Μέθοδος που ήδη υπάρχει στην Budget.java
            System.out.println("Ολοκληρώθηκε η φόρτωση.\n");
        } catch (Exception e) {
            System.out.println("Αποτυχία φόρτωσης δεδομένων από API: " + e.getMessage());
            System.out.println("Θα χρησιμοποιηθούν μηδενικές τιμές.\n");
        }

        System.out.println(BANNER);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");
            String line = br.readLine();
            if (line == null) break; // EOF

            String cmd = line.trim().toLowerCase();

            switch (cmd) {
                case "help" -> System.out.println(BANNER);
                case "show summary" -> showSummary();
                case "exit", "quit" -> {
                    System.out.println("Αντίο!");
                    return;
                }
                case "" -> { /* αγνόησε κενές γραμμές */ }
                default -> System.out.println("Άγνωστη εντολή. Γράψε 'help'.");
            }
        }
    }

    /** Υπολογίζει το ισοζύγιο (έσοδα - έξοδα).
     *  Την κρατάμε ξεχωριστή επειδή ήδη έχουμε tests για αυτή.
     */
    public static long computeBalance(long revenues, long expenses) {
        return revenues - expenses;
    }

    private static void showSummary() {
        long revenues = currentBudget.getTotalRevenue();
        long expenses = currentBudget.getTotalExpenses();
        long balance  = computeBalance(revenues, expenses);

        System.out.println("— Σύνοψη προϋπολογισμού (WorldBank API, GR) —");
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
