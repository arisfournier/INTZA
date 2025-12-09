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
              show changes        - εμφάνιση αλλαγών χρήστη για το τρέχον έτος
              show categories    - εμφάνιση κατηγοριών
              increase all <X>    - αύξηση όλων των κατηγοριών κατά X%%
              increase <CAT> <X>  - αύξηση συγκεκριμένης κατηγορίας κατά X%%
              reduce all <X>      - μείωση όλων των κατηγοριών κατά X%%
              reduce <CAT> <X>    - μείωση συγκεκριμένης κατηγορίας κατά X%%
              set year <έτος>    - επιλογή έτους (π.χ. set year 2020)
              compare <year1> <year2>  - σύγκριση δύο ετών
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
            } else if (input.equals("show changes")) {
                showChanges();
            } else if (input.equals("show categories")) {
                showCategories();
            } else if (input.startsWith("increase all")) {
                handleIncreaseAll(input);
            } else if (input.startsWith("increase ")) {
                handleIncreaseCategory(input);
            } else if (input.startsWith("reduce all")) {
                handleReduceAll(input);
            } else if (input.startsWith("reduce ")) {
                handleReduceCategory(input);
            } else if (input.startsWith("set year")) {
                handleSetYear(input);
            } else if (input.equals("list years")) {
                listYears();
            } else if (input.startsWith("save year")) {
                handleSaveYear(input);
            } else if (input.startsWith("load year")) {
                handleLoadYear(input);
            } else if (input.startsWith("compare ")) {
                handleCompare(input);
            } else if (input.startsWith("compare countries")) {
                handleCompareCountries(input);
            } else if (input.equals("save all")) {
                handleSaveAll();
            } else if (input.equals("load all")) {
                handleLoadAll();
            } else if (input.startsWith("set value")) {
                handleSetValue(input);
            } else if (input.equals("exit") || input.equals("quit")) {
                System.out.println("Αντίο!");
                return;
            } else if (input.isEmpty()) {
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

    private static void handleSaveAll() {
        try {
            Path dir = Path.of("data");
            if (!Files.exists(dir)) Files.createDirectories(dir);

            Path file = dir.resolve("all-budgets.json");

            yearManager.saveAll(file);

            System.out.println("Αποθηκεύτηκαν όλα τα budgets στο " + file);
        } catch (Exception e) {
            System.out.println("Σφάλμα στο save all: " + e.getMessage());
        }
    }

    private static void handleLoadAll() {
        try {
            Path file = Path.of("data/all-budgets.json");

            if (!Files.exists(file)) {
                System.out.println("Δεν υπάρχει αποθηκευμένο αρχείο all-budgets.json");
                return;
            }

            yearManager.loadAll(file);

            System.out.println("Φορτώθηκαν όλα τα budgets από " + file);

        } catch (Exception e) {
            System.out.println("Σφάλμα στο load all: " + e.getMessage());
        }
    }

    /**
 * Εντολή: set value <CATEGORY> <AMOUNT>
 * Αλλάζει την τιμή μιας κατηγορίας για το τρέχον έτος.
 */
    private static void handleSetValue(String input) {
        try {
            String[] parts = input.split("\\s+");

            if (parts.length != 4) {
                System.out.println("Χρήση: set value <CATEGORY> <AMOUNT>");
                return;
            }

            if (currentYear < 0) {
                System.out.println("Πρέπει να ορίσετε έτος με την εντολή: set year <YEAR>");
                return;
            }

            String category = parts[2].toUpperCase();
            long amount = Long.parseLong(parts[3]);

            // Έλεγχος περιορισμών
            if (amount < 0) {
                System.out.println("Το ποσό δεν μπορεί να είναι αρνητικό!");
                return;
            }

            Budget budget = yearManager.getOrLoad(currentYear);

            // Έλεγχος αν υπάρχει η κατηγορία
            if (!budget.getApiValues().containsKey(category) &&
                !budget.getUserChanges().containsKey(category)) {
                System.out.println("Η κατηγορία '" + category + "' δεν υπάρχει στο budget.");
                return;
            }

            // Καταχώρηση αλλαγής
            budget.setUserValue(category, amount);

            System.out.println("Η κατηγορία " + category + " ενημερώθηκε σε: " + amount + " €");

        } catch (NumberFormatException e) {
            System.out.println("Μη έγκυρο ποσό. Πρέπει να δώσετε αριθμό.");
        } catch (Exception e) {
            System.out.println("Σφάλμα στο set value: " + e.getMessage());
        }
    }
    
    /**
 * Εντολή: compare <YEAR1> <YEAR2>
 * Συγκρίνει τις τελικές τιμές ανά κατηγορία μεταξύ δύο ετών.
 */
    private static void handleCompare(String input) {
        try {
            String[] parts = input.split("\\s+");
            if (parts.length != 3) {
                System.out.println("Χρήση: compare <YEAR1> <YEAR2>");
                return;
            }

            int y1 = Integer.parseInt(parts[1]);
            int y2 = Integer.parseInt(parts[2]);

            Budget b1 = yearManager.getOrLoad(y1);
            Budget b2 = yearManager.getOrLoad(y2);

            System.out.printf("\nΣύγκριση προϋπολογισμών (%d → %d)\n\n", y1, y2);
            System.out.printf("%-15s %15s %15s %15s\n", 
                "Κατηγορία", y1, y2, "Διαφορά");
            System.out.println("--------------------------------------------------------------");

            // Βρίσκουμε ΟΛΕΣ τις κατηγορίες που υπάρχουν σε κάποιο από τα δύο έτη
            var categories = new java.util.TreeSet<String>();
            for (var cat : b1.getCategories()) categories.add(cat.getName());
            for (var cat : b2.getCategories()) categories.add(cat.getName());

            // Για κάθε κατηγορία, παίρνουμε τις ποσότητες
            for (String category : categories) {
                long v1 = b1.getFinalValueFromCategoryName(category);
                long v2 = b2.getFinalValueFromCategoryName(category);
                long diff = v2 - v1;

                System.out.printf("%-15s %,15d %,15d %,15d\n",
                    category, v1, v2, diff);
            }

            System.out.println();

        } catch (Exception e) {
            System.out.println("Σφάλμα στη σύγκριση: " + e.getMessage());
        }
    }

    private static void handleIncreaseAll(String input) {
        try {
            String[] parts = input.split("\\s+");
            if (parts.length != 3) {
                System.out.println("Χρήση: increase all <percent>");
                return;
            }

            if (currentYear < 0) {
                System.out.println("Πρώτα ορίστε έτος με: set year <YEAR>");
                return;
            }

            double percent = Double.parseDouble(parts[2]);
            Budget b = yearManager.getOrLoad(currentYear);

            var apiValues = b.getApiValues();
            var changes = b.getUserChanges();

            // Εφαρμόζουμε σε όλα τα κλειδιά του API
            for (String key : apiValues.keySet()) {
                long current = b.getFinalValue(key);
                long newValue = Math.round(current * (1 + percent / 100.0));
                if (newValue < 0) newValue = 0;
                b.setUserValue(key, newValue);
            }

            // Και σε κλειδιά που υπάρχουν μόνο στα userChanges
            for (String key : changes.keySet()) {
                if (apiValues.containsKey(key)) continue;
                long current = b.getFinalValue(key);
                long newValue = Math.round(current * (1 + percent / 100.0));
                if (newValue < 0) newValue = 0;
                b.setUserValue(key, newValue);
            }

            System.out.printf("Αυξήθηκαν όλες οι κατηγορίες κατά %.2f%%%n", percent);

        } catch (NumberFormatException e) {
            System.out.println("Μη έγκυρο ποσοστό. Χρήση: increase all <percent>");
        } catch (Exception e) {
            System.out.println("Σφάλμα στο increase all: " + e.getMessage());
        }
    }

    private static void handleIncreaseCategory(String input) {
        try {
            String[] parts = input.split("\\s+");
            // increase <CAT> <percent>
            if (parts.length != 3) {
                System.out.println("Χρήση: increase <CATEGORY> <percent>");
                return;
            }

            if ("all".equalsIgnoreCase(parts[1])) {
                // Αυτό το case το πιάνει ήδη η handleIncreaseAll
                handleIncreaseAll(input);
                return;
            }

            if (currentYear < 0) {
                System.out.println("Πρώτα ορίστε έτος με: set year <YEAR>");
                return;
            }

            String category = parts[1].toUpperCase();
            double percent = Double.parseDouble(parts[2]);

            Budget b = yearManager.getOrLoad(currentYear);

            // Έλεγχος αν υπάρχει η κατηγορία ως "κλειδί" (όπως στο set value)
            if (!b.getApiValues().containsKey(category) &&
                !b.getUserChanges().containsKey(category)) {
                System.out.println("Η κατηγορία '" + category + "' δεν υπάρχει στο budget.");
                System.out.println("Δες 'show categories' και χρησιμοποίησε τα ονόματα (π.χ. HOSPITALS, PHARMA, STAFF).");
                return;
            }

            long current = b.getFinalValue(category);
            long newValue = Math.round(current * (1 + percent / 100.0));
            if (newValue < 0) newValue = 0;

            b.setUserValue(category, newValue);

            System.out.printf("Η κατηγορία %s αυξήθηκε κατά %.2f%% (%,d → %,d)%n",
                    category, percent, current, newValue);

        } catch (NumberFormatException e) {
            System.out.println("Μη έγκυρο ποσοστό. Χρήση: increase <CATEGORY> <percent>");
        } catch (Exception e) {
            System.out.println("Σφάλμα στο increase <CATEGORY>: " + e.getMessage());
        }
    }

    private static void handleReduceAll(String input) {
        try {
            String[] parts = input.split("\\s+");
            if (parts.length != 3) {
                System.out.println("Χρήση: reduce all <percent>");
                return;
            }
 
            if (currentYear < 0) {
                System.out.println("Πρώτα ορίστε έτος με: set year <YEAR>");
                return;
            }

            double percent = Double.parseDouble(parts[2]);
            if (percent < 0) {
                System.out.println("Το ποσοστό πρέπει να είναι μη αρνητικό.");
                return;
            }

            Budget b = yearManager.getOrLoad(currentYear);

            var apiValues = b.getApiValues();
            var changes = b.getUserChanges();

            double factor = 1 - percent / 100.0;
            if (factor < 0) factor = 0;

            for (String key : apiValues.keySet()) {
                long current = b.getFinalValue(key);
                long newValue = Math.round(current * factor);
                if (newValue < 0) newValue = 0;
                b.setUserValue(key, newValue);
            }

            for (String key : changes.keySet()) {
                if (apiValues.containsKey(key)) continue;
                long current = b.getFinalValue(key);
                long newValue = Math.round(current * factor);
                if (newValue < 0) newValue = 0;
                b.setUserValue(key, newValue);
            }

            System.out.printf("Μειώθηκαν όλες οι κατηγορίες κατά %.2f%%%n", percent);

        } catch (NumberFormatException e) {
            System.out.println("Μη έγκυρο ποσοστό. Χρήση: reduce all <percent>");
        } catch (Exception e) {
            System.out.println("Σφάλμα στο reduce all: " + e.getMessage());
        }
    }

    private static void handleReduceCategory(String input) {
        try {
            String[] parts = input.split("\\s+");
            // reduce <CAT> <percent>
            if (parts.length != 3) {
                System.out.println("Χρήση: reduce <CATEGORY> <percent>");
                return;
            }

            if ("all".equalsIgnoreCase(parts[1])) {
                handleReduceAll(input);
               return;
            }

            if (currentYear < 0) {
                System.out.println("Πρώτα ορίστε έτος με: set year <YEAR>");
                return;
            }

            String category = parts[1].toUpperCase();
            double percent = Double.parseDouble(parts[2]);
            if (percent < 0) {
                System.out.println("Το ποσοστό πρέπει να είναι μη αρνητικό.");
                return;
            }

            Budget b = yearManager.getOrLoad(currentYear);

            if (!b.getApiValues().containsKey(category) &&
                !b.getUserChanges().containsKey(category)) {
                System.out.println("Η κατηγορία '" + category + "' δεν υπάρχει στο budget.");
                System.out.println("Δες 'show categories' και χρησιμοποίησε τα ονόματα (π.χ. HOSPITALS, PHARMA, STAFF).");
                return;
            }

            long current = b.getFinalValue(category);
            double factor = 1 - percent / 100.0;
            if (factor < 0) factor = 0;

            long newValue = Math.round(current * factor);
            if (newValue < 0) newValue = 0;

            b.setUserValue(category, newValue);

            System.out.printf("Η κατηγορία %s μειώθηκε κατά %.2f%% (%,d → %,d)%n",
                    category, percent, current, newValue);

        } catch (NumberFormatException e) {
            System.out.println("Μη έγκυρο ποσοστό. Χρήση: reduce <CATEGORY> <percent>");
        } catch (Exception e) {
            System.out.println("Σφάλμα στο reduce <CATEGORY>: " + e.getMessage());
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
        System.out.println("  Έσοδα : " + formatMoney(revenues));
        System.out.println("  Έξοδα : " + formatMoney(expenses));
        System.out.println("  Ισοζύγιο : " + formatMoney(balance));

        if (balance < 0) {
            System.out.println("  (Έλλειμμα)");
        } else if (balance > 0) {
            System.out.println("  (Πλεόνασμα)");
        } else {
            System.out.println("  (Ισοσκελισμένο)");
        }
        System.out.println();
    }

    private static void showChanges() {
        if (currentYear < 0) {
            System.out.println("Πρώτα πρέπει να ορίσετε έτος με: set year <YEAR>");
            return;
        }

        Budget b = yearManager.getOrLoad(currentYear);

        var changes = b.getUserChanges();
        if (changes == null || changes.isEmpty()) {
            System.out.println("\nΔεν υπάρχουν αλλαγές χρήστη για το έτος " + currentYear + ".\n");
            return;
        }

        System.out.println("\nΑλλαγές χρήστη για έτος " + currentYear + ":");

        for (var entry : changes.entrySet()) {
            String category = entry.getKey();
            long newValue = entry.getValue();
            long original = b.getApiValues().getOrDefault(category, 0L);
            long diff = newValue - original;

            System.out.printf(" - %-12s αρχικό=%,d  νέο=%,d  διαφορά=%,d%n",
                    category, original, newValue, diff);
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
            System.out.printf("  - %-15s : %s%n", c.getName(), formatMoney(c.getAmount()));
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

    // Μορφοποιηση ποσων - αποτελεσματων (π.χ. 1.5 δις €)
    private static String formatMoney(long amount) {
        if (amount >= 1_000_000_000L) {
            // Αν είναι πάνω από 1 δις, το διαιρούμε και βάζουμε "δις"
            return String.format("%.2f δις €", amount / 1_000_000_000.0);
        } else if (amount >= 1_000_000L) {
            // Αν είναι πάνω από 1 εκατομμύριο, βάζουμε "εκ."
            return String.format("%.2f εκ. €", amount / 1_000_000.0);
        } else {
            // Αν είναι μικρό ποσό, το δείχνουμε ολόκληρο
            return String.format("%,d €", amount);
        }
    }
}
