package json;

public class JsonTrade {
    String sym;
    double rate;
    double amount;
    public JsonTrade(){}
    public JsonTrade(String sym, double rate, double amount) {
        this.sym = sym;
        this.rate = rate;
        this.amount = amount;
    }
}
