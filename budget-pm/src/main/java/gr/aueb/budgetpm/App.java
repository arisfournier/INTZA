package gr.aueb.budgetpm;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Κεντρική εφαρμογή CLI.
 * Τώρα υποστηρίζει επιλογή έτους μέσω BudgetYearManager.
 */
public class App {

    private static final String BANNER = """
            =========================================
               Πρωθυπουργός για μια μέρα (CLI v0.3)
            =========================================
            Εντολές:
              help               - βοήθεια
              show summary       - εμφάνιση συνοπτικών στοιχείων
              show categories    - εμφάνιση κατηγοριών
              set year <έτος>    - επιλογή έτους (π.χ. set year 2020)
              list years         - εμφάνιση φορτωμένων ετών
              exit               - έξοδος
            """;

    private static BudgetYearManager yearManager = new BudgetYearManager("GR");
    private static int currentYear = 2020;

    public static void main(String[] args) throws Exception {
        System.out.println(BANNER);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");
            String input = reader.readLine().trim().toLowerCase();

            if (input.equals("help")) {
                System.out.println(BANNER);
            } else if (input.equals("show summary")) {
                showSummary();
            } else if (input.equals("show categories")) {
                showCategories();
            } else if (input.startsWith("set year")) {
                handleSetYear(input);
            } else if (input.equals("list years")) {
                listYears();
            } else if (input.equals("exit") || input.equals("quit")) {
                System.out.println("Αντίο!");
                return;
            } else if (input.isEmpty()) {
                // αγνόησε κενές γραμμές
            } else {
                System.out.println("Άγνωστη εντολή. Γράψε 'help'.");
            }
        }
    }

    private static void handleSetYear(String input) {
        try {
            String[] parts = input.split("\\s+");
            if (parts.length != 3) {
                System.out.println("Χρήση: set year <έτος>");
                return;
            }

            int year = Integer.parseInt(parts[2]);
            currentYear = year;

            yearManager.getOrLoad(year);

            System.out.println("Επιλέχθηκε έτος: " + year);
        } catch (Exception e) {
            System.out.println("Μη έγκυρο έτος.");
        }
    }

        // Υπολογισμός ισοζυγίου (έσοδα - έξοδα) σε long
    public static long computeBalance(long revenues, long expenses) {
        return revenues - expenses;
    }

    // Υπολογισμός ισοζυγίου (έσοδα - έξοδα) σε int (για τα tests)
    public static int computeBalance(int revenues, int expenses) {
        return revenues - expenses;
    }

    private static void showSummary() {
        Budget b = yearManager.getOrLoad(currentYear);

        long revenues = b.getTotalRevenue();
        long expenses = b.getTotalExpenses();
        long balance = computeBalance(revenues, expenses);

        System.out.println("\n— Σύνοψη προϋπολογισμού (" + currentYear + ") —");
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
        System.out.println();
    }

    private static void showCategories() {
        Budget b = yearManager.getOrLoad(currentYear);

        var categories = b.getCategories();

        System.out.println("\nΚατηγορίες προϋπολογισμού (" + currentYear + "):");

        if (categories == null || categories.isEmpty()) {
            System.out.println("  (Δεν υπάρχουν διαθέσιμα δεδομένα)");
            return;
        }

        for (BudgetCategory c : categories) {
            System.out.printf("  - %s : %,d €%n", c.getName(), c.getAmount());
        }

        System.out.println();
    }

    private static void listYears() {
        var years = yearManager.getLoadedYears();

        System.out.println("\nΦορτωμένα έτη:");

        if (years == null || years.isEmpty()) {
            System.out.println("  (Δεν έχει φορτωθεί ακόμη κανένα έτος)");
            System.out.println();
            return;
        }

        for (Integer y : years) {
            String marker = (y == currentYear) ? "  * " : "    ";
            System.out.println(marker + y);
        }
        System.out.println();
    }
}
