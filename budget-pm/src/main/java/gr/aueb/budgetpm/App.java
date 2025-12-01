package gr.aueb.budgetpm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
              save year <έτος>   - αποθήκευση προϋπολογισμού έτους σε αρχείο
              load year <έτος>   - φόρτωση προϋπολογισμού έτους από αρχείο
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
            } else if (input.startsWith("save year")) {
                handleSaveYear(input);
            } else if (input.startsWith("load year")) {
                handleLoadYear(input);
            } else if (input.startsWith("compare countries")) {
                handleCompareCountries(input);
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

    private static void handleSaveYear(String input) {
        try {
            String[] parts = input.split("\\s+");
            if (parts.length != 3) {
                System.out.println("Χρήση: save year <έτος>");
                return;
            }

            int year = Integer.parseInt(parts[2]);

            // Πάρε το budget για αυτό το έτος (θα το φορτώσει από API αν δεν υπάρχει)
            Budget b = yearManager.getOrLoad(year);

            // Φάκελος αποθήκευσης
            Path dir = Paths.get("data");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            // Όνομα αρχείου π.χ. data/budget-2020.json
            Path file = dir.resolve("budget-" + year + ".json");

            BudgetStorage.saveBudget(b, file);

            System.out.println("Αποθηκεύτηκε ο προϋπολογισμός του " + year + " στο αρχείο: " + file);

        } catch (NumberFormatException e) {
            System.out.println("Μη έγκυρο έτος. Χρήση: save year <έτος>");
        } catch (Exception e) {
            System.out.println("Σφάλμα κατά την αποθήκευση: " + e.getMessage());
        }
    }

    private static void handleLoadYear(String input) {
        try {
            String[] parts = input.split("\\s+");
            if (parts.length != 3) {
                System.out.println("Χρήση: load year <έτος>");
                return;
            }

            int year = Integer.parseInt(parts[2]);

            Path dir = Paths.get("data");
            Path file = dir.resolve("budget-" + year + ".json");

            if (!Files.exists(file)) {
                System.out.println("Δεν βρέθηκε αποθηκευμένος προϋπολογισμός για το έτος " + year +
                        " (αρχείο: " + file + ")");
                return;
            }

            Budget loaded = BudgetStorage.loadBudget(file);

            // Βάζουμε το φορτωμένο budget στον yearManager
            yearManager.putBudget(year, loaded);
            currentYear = year;

            System.out.println("Φορτώθηκε ο προϋπολογισμός του " + year + " από το αρχείο: " + file);

        } catch (NumberFormatException e) {
            System.out.println("Μη έγκυρο έτος. Χρήση: load year <έτος>");
        } catch (Exception e) {
            System.out.println("Σφάλμα κατά τη φόρτωση: " + e.getMessage());
        }
    }

    private static void handleCompareCountries(String input) {
        try {
            String[] parts = input.split("\\s+");
            // Αναμένουμε: compare countries GR IT 2020
            if (parts.length != 5) {
                System.out.println("Χρήση: compare countries <C1> <C2> <year>");
                return;
            }

            String country1 = parts[2].toUpperCase();
            String country2 = parts[3].toUpperCase();
            int year = Integer.parseInt(parts[4]);

            System.out.println("\nΦόρτωση δεδομένων...");

            // Φόρτωση budget για κάθε χώρα/έτος
            BudgetYearManager mgr1 = new BudgetYearManager(country1);
            BudgetYearManager mgr2 = new BudgetYearManager(country2);

            Budget b1 = mgr1.getOrLoad(year);
            Budget b2 = mgr2.getOrLoad(year);

            long rev1 = b1.getTotalRevenue();
            long exp1 = b1.getTotalExpenses();
            long bal1 = computeBalance(rev1, exp1);

            long rev2 = b2.getTotalRevenue();
            long exp2 = b2.getTotalExpenses();
            long bal2 = computeBalance(rev2, exp2);

            System.out.println("\n===== Σύγκριση Χωρών (" + year + ") =====");
            System.out.printf("%-12s %15s %15s %15s%n", "Χώρα", "Έσοδα", "Έξοδα", "Ισοζύγιο");
            System.out.println("---------------------------------------------------------------");
            System.out.printf("%-12s %,15d €, %,15d €, %,15d €%n", country1, rev1, exp1, bal1);
            System.out.printf("%-12s %,15d €, %,15d €, %,15d €%n", country2, rev2, exp2, bal2);

            System.out.println("\nΔιαφορές ( " + country1 + " - " + country2 + " ):");
            System.out.printf("Έσοδα : %,d €%n", (rev1 - rev2));
            System.out.printf("Έξοδα : %,d €%n", (exp1 - exp2));
            System.out.printf("Ισοζύγιο: %,d €%n\n", (bal1 - bal2));

        } catch (NumberFormatException e) {
            System.out.println("Μη έγκυρο έτος. Χρήση: compare countries <C1> <C2> <year>");
        } catch (Exception e) {
            System.out.println("Σφάλμα στο compare: " + e.getMessage());
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
