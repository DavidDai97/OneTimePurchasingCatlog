public class Item {
    private double price;
    private String buyer;
    private int averageLeadTime;
    private String currency;
    private int purchaseTime;

    public Item(String newBuyer, int leadTime, String newCurrency, double newPrice){
        this.buyer = newBuyer;
        this.averageLeadTime = leadTime;
        this.currency = newCurrency;
        this.purchaseTime = 1;
        this.price = newPrice;
    }

    public double getPrice() {
        return price;
    }
    public int getAverageLeadTime() {
        return averageLeadTime;
    }
    public int getPurchaseTime() {
        return purchaseTime;
    }
    public String getBuyer() {
        return buyer;
    }
    public String getCurrency() {
        return currency;
    }

    public void setAverageLeadTime(int newDifference){
        this.averageLeadTime = (int) Math.ceil(((double)(this.averageLeadTime + newDifference)) / (double)purchaseTime);
    }
    public void setPrice(double newPrice){
        this.price = newPrice;
    }
    public void setBuyer(String newBuyer){
        this.buyer = newBuyer;
    }

    public void addPurchaseTime(){
        this.purchaseTime++;
    }

}
