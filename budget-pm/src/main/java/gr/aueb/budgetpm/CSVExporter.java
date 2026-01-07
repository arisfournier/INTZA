package gr.aueb.budgetpm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Εξαγωγή δεδομένων Budget σε CSV για χρήση σε Excel/Google Sheets (γραφήματα).
 */
public final class CSVExporter {

    private CSVExporter() {}

    /**
     * Εξάγει τις κατηγορίες ενός budget σε CSV:
     * Category,Value
     */
    public static Path exportCategories(Budget budget, Path outputFile) throws IOException {
        if (budget == null) throw new IllegalArgumentException("budget is null");
        if (outputFile == null) throw new IllegalArgumentException("outputFile is null");

        Path parent = outputFile.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Category,Value\n");

        for (BudgetCategory c : budget.getCategories()) {
            String cat = escapeCsv(c.getName());
            long value = c.getAmount();
            sb.append(cat).append(",").append(value).append("\n");
        }

        Files.writeString(outputFile, sb.toString(), StandardCharsets.UTF_8);
        return outputFile;
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String out = s.replace("\"", "\"\"");
        return needsQuotes ? "\"" + out + "\"" : out;
    }
}
