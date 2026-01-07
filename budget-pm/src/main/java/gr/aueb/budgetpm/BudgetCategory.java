package gr.aueb.budgetpm;

public class BudgetCategory {

    private String code;
    private String name;
    private long amount;

    public BudgetCategory(String code, String name, long amount) {
        this.code = code;
        this.name = name;
        this.amount = amount;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public long getAmount() {
        return amount;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    //Προσθέτει στο τρέχον ποσό ένα επιπλέον ποσό.
    public void addAmount(long delta) {
        this.amount += delta;
    }

    @Override
    public String toString() {
        return "BudgetCategory{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                '}';
    }
}
