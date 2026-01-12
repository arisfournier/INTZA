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

Εκτέλεση με την εντολή: mvn exec:java
[D[A[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C μέσω του maven

Οδηγίες χρήσης (CLI):
set year 2020
show summary
show categories

set value HOSPITALS 300
increase all 5
reduce STAFF 10

scenario demo 10
scenario show demo
compare scenario demo

compare years 2019 2020

export csv 2020
save all
exit
[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C
[D[A[A[B[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[C[D[D[ - Ενδεικτική ροή χρήσης :[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D
Βασικές εντολές:
Βασικές εντολές

help : εμφάνιση διαθέσιμων εντολών

set year <YEAR> : επιλογή έτους

list years : εμφανίζει ποια έτη είναι φορτωμένα

show summary : σύνοψη προϋπολογισμού έτους

show categories : λίστα κατηγοριών & τιμών

set value <CAT> <VALUE> : ορισμός τιμής κατηγορίας

increase all <X> / reduce all <X> : οριζόντια μεταβολή (%) σε όλες τις κατηγορίες

increase <CAT> <X> / reduce <CAT> <X> : μεταβολή (%) σε συγκεκριμένη κατηγορία

show changes : εμφανίζει τις αλλαγές χρήστη σε σχέση με αρχικές τιμές

scenario <NAME> <X> : δημιουργία σεναρίου μεταβολής X% (όλες οι κατηγορίες)

scenario show <NAME> : προβολή τιμών σεναρίου

compare scenario <NAME> : σύγκριση σεναρίου με baseline έτους

compare years <Y1> <Y2> : σύγκριση δύο ετών

export csv <YEAR> : εξαγωγή κατηγοριών σε CSV

save all / load year <YEAR> : αποθήκευση/ανάκτηση

exit : έξοδος[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[A[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[D[A[A[A[A[A[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[B[C[C[C[C[C[C[C[C[C[C[C[C[C


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
│  │        ├─ BudgetApiReader.java
│  │        ├─ BudgetYearManager.java
│  │        ├─ BudgetScenario.java
│  │        ├─ BudgetComparator.java
│  │        ├─ CSVExporter.java
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
