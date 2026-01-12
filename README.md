# INTZA – Budget PM (Πρωθυπουργός για μία μέρα)

CLI εφαρμογή σε Java για επισκόπηση/επεξεργασία κρατικού προϋπολογισμού, σενάρια αλλαγών, σύγκριση ετών και εξαγωγή δεδομένων.

---

## 1) Οδηγίες μεταγλώττισης (Maven)

Απαιτήσεις:
- Java 17+ (ή η έκδοση που χρησιμοποιείτε)
- Maven 3.8+

Μεταγλώττιση + tests:
```bash
mvn clean test
mvn package

Εκτέλεση του γραφικού περιβάλλοντος με την εντολή: mvn exec:java

Δομή αποθετηρίοϋ:

budget-pm/
├─ pom.xml
├─ README.md
├─ src/
│  ├─ main/
│  │  └─ java/
│  │     └─ gr/aueb/budgetpm/
│  │        ├─ App.java
│  │        ├─ Budget.java
│  │        ├─ BudgetCategory.java
│  │        ├─ BudgetComperator.java
│  │        ├─ BudgetApiReader.java
│  │        ├─ BudgetYearManager.java
│  │        ├─ BudgetScenario.java
│  │        ├─ BudgetComparator.java
│  │        ├─ CSVExporter.java
│  │        ├─ GuiApp.java
│  │        ├─ GuiCharts.java
│  │        └─ CountryComparator.java
│  └─ test/
│     └─ java/
│        └─ gr/aueb/budgetpm/
│           ├─ ...Test.java
│           └─ ...
├─ data/
│  ├─ export-<YEAR>.csv
│  └─ saved-<YEAR>.json (ή άλλο format αποθήκευσης)
└─ target/


UML Σχεδιασμός

classDiagram
  class App {
    +main(String[] args)
  }

  class Budget {
    -Map~String,Long~ categories
    -Map~String,Long~ originalCategories
    +getCategories()
    +setCategoryValue(name,value)
    +getTotalExpenses()
    +getTotalRevenue()
  }

  class BudgetCategory {
    +String name
    +long amount
  }

  class BudgetYearManager {
    -Map~Integer,Budget~ budgetsByYear
    +getOrLoad(year) Budget
    +saveAll()
    +loadYear(year)
  }

  class BudgetScenario {
    +String name
    +Budget baseline
    +double percentChange
    +getAllCategoryValues() Map~String,Long~
  }

  class BudgetComparator {
    +compareYears(b1,b2) ...
  }

  class CSVExporter {
    +exportCategories(budget, path)
  }

  class CountryComparator {
    +compare(a,b) Map
  }

  App --> BudgetYearManager
  BudgetYearManager --> Budget
  Budget --> BudgetCategory
  App --> BudgetScenario
  App --> CSVExporter
  App --> BudgetComparator
  App --> CountryComparator

Δομές δεδομένων & αλγόριθμοι (επιγραμματικά):

Map (HashMap/LinkedHashMap) για αποθήκευση κατηγοριών → O(1) αναζήτηση/ενημέρωση τιμών.

List / συλλογές για προβολή κατηγοριών και παραγωγή αναφορών.

Σενάρια (BudgetScenario): εφαρμογή ποσοστιαίας μεταβολής στις κατηγορίες και δημιουργία “προβαλλόμενων” τιμών χωρίς να αλλάζει το baseline.

Σύγκριση ετών/σεναρίων: επανάληψη στις κατηγορίες και υπολογισμός διαφορών (diff = νέο - παλιό).

CSV export: δημιουργία αρχείου Category,Value για χρήση σε Excel/Sheets (και γραφήματα).


Πρόσθετη τεχνική τεκμηρίωση
Tests (JUnit)

Τα tests εκτελούνται μέσω:

mvn test

JavaDoc

Στις βασικές κλάσεις έχουν προστεθεί σχόλια JavaDoc για την περιγραφή ρόλων/μεθόδων (όπου απαιτείται από την εργασία).
